package com.bot.processor.admin.subActions;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.model.Operations;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.ICarStorage;
import com.bot.processor.SubAction;
import com.bot.processor.admin.AdminHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class EnterNewDescription implements SubAction {
    @Autowired
    private IAccommodationStorage accommodationStorage;
    @Autowired
    private ICarStorage carStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {
        UserAccommodation accommodation = tempObject.getAdministrationData().getUserAccommodation();
        accommodation.setDescription(update.getMessage().getText());
        //TODO подпорка, убрать. Тут нет даты почему то
        accommodation.setCreatedDate(new Date());
        accommodationStorage.saveAccommodation(accommodation);
        user.setWaitingMessages(false);
        user.setLastCallback(null);
    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        UserAccommodation userAccommodation = accommodationStorage.getFirstNotAgreed();
        MessageWrapper wrapper = new MessageWrapper();
        int count = accommodationStorage.countNotAgreed();
        if (count == 0) {
            return MessageWrapper.builder()
                    .sendMessage(Collections.singletonList(new SendMessage(Util.getUserId(update),
                            "Не обработанных заявок нет")))
                    .build();
        }
        List<Car> cars = new ArrayList<>();
        userAccommodation.getCarsId().forEach(id -> cars.add(carStorage.getCarById(Integer.parseInt(id))));

        String text = AdminHelper.generateAccommodationMsgText(userAccommodation, user, count, cars);
        TempObject approved = AdminHelper.getTempForButton(tempObject, Operations.APPROVED_REQUEST,
                ActionOnRequest.APPROVED,
                userAccommodation);

        TempObject edit = AdminHelper.getTempForButton(tempObject, Operations.EDIT_REQUEST,
                ActionOnRequest.EDIT, userAccommodation);

        TempObject rejected = AdminHelper.getTempForButton(tempObject, Operations.REJECTED_REQUEST,
                ActionOnRequest.REJECTED, userAccommodation);

        List<ButtonWrapper> data = new ArrayList<>();

        data.add(new ButtonWrapper("Одобрить", Util.generateToken(approved), approved));
        data.add(new ButtonWrapper("Изменить", Util.generateToken(edit), edit));
        data.add(new ButtonWrapper("Отклонить", Util.generateToken(rejected), rejected));

        wrapper.setButtons(data);
        ReplyKeyboard keyboard = Util.createKeyboardOneBtnLine(data);
        if (AdminHelper.isWithPhotos(userAccommodation)) {
            AdminHelper.addPhotos(wrapper, userAccommodation, update, text, keyboard);
        } else {
            SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
            sendMessage.setReplyMarkup(keyboard);
            wrapper.setSendMessage(Collections.singletonList(sendMessage));
        }
        return wrapper;
    }
}

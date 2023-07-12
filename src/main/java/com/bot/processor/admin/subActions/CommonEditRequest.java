package com.bot.processor.admin.subActions;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.oprations.Operations;
import com.bot.processor.SubAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class CommonEditRequest implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        TempObject editDescription = tempObject.clone();
        editDescription.setOperation(Operations.EDIT_DESCRIPTION);
        ButtonWrapper buttonEditDescription = new ButtonWrapper("Изменить описание", Util.generateToken(editDescription), editDescription);

        TempObject editEngine = tempObject.clone();
        editEngine.setOperation(Operations.EDIT_DESCRIPTION);
        ButtonWrapper buttonEditEngine = new ButtonWrapper("Удалить двигатели", Util.generateToken(editEngine), editEngine);


        TempObject deleteCar = tempObject.clone();
        editEngine.setOperation(Operations.DELETE_CAR);
        ButtonWrapper buttonDeleteCar = new ButtonWrapper("Удалить машину", Util.generateToken(deleteCar), deleteCar);


        List<ButtonWrapper> buttons = List.of(buttonEditDescription, buttonEditEngine, buttonDeleteCar);
        ReplyKeyboard keyboard = Util.createKeyboardOneBtnLine(buttons);
        SendMessage sendMessage = new SendMessage(Util.getUserId(update), "Выбери, что именно изменить:");
        sendMessage.setReplyMarkup(keyboard);
        return MessageWrapper.builder()
                .sendMessage(Collections.singletonList(sendMessage)).leaveOldMessages(true)
                .buttons(buttons).build();
    }
}

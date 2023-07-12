package com.bot.processor.admin.subActions;

import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.model.UserAccommodation;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.SubAction;
import com.bot.processor.admin.AdminHelper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SendRejected implements SubAction {
    @Autowired
    private IAccommodationStorage accommodationStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        String rejectedText = update.getMessage().getText();
        UserAccommodation accommodation = tempObject.getAdministrationData().getUserAccommodation();
        String userNotification = "Привет! твой запрос \"" + accommodation.getDescription() + "\"" + " отклонен. \n" +
                "Комментарий администратора: \n" + rejectedText;
        String adminNotification = "Запрос помечен как отклоненный. Пользователю отправлено уведомление с текекстом: \n" + userNotification;
        accommodationStorage.saveAccommodation(AdminHelper.rejectedAccommodation(accommodation));
        log.info("user accommodation " + accommodation.getId() + " will be rejected and updated");
        List<SendMessage> msgs = new ArrayList<>();
        msgs.add(new SendMessage(accommodation.getClientLogin(), userNotification));
        msgs.add(new SendMessage(Util.getUserId(update), adminNotification));
        user.setWaitingMessages(false);
        user.setLastCallback(null);
        return MessageWrapper.builder().sendMessage(msgs).build();
    }
}

package com.bot.processor.admin.subActions;

import com.bot.bot.BotConfig;
import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.model.UserAccommodation;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.INotificationCenter;
import com.bot.processor.IUserStorage;
import com.bot.processor.SubAction;
import com.bot.processor.admin.AdminHelper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;

@Component
@Slf4j
public class Approved implements SubAction {
    @Autowired
    private IAccommodationStorage accommodationStorage;
    @Autowired
    private IUserStorage userStorage;
    @Autowired
    private INotificationCenter notificationCenter;
    @Autowired
    private BotConfig config;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {
        UserAccommodation userAccommodation = tempObject.getAdministrationData().getUserAccommodation();
        accommodationStorage.saveAccommodation(AdminHelper.approveAccommodation(userAccommodation));
        log.info("user accommodation " + userAccommodation.getId() + " will be approved and updated");
    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        UserAccommodation userAccommodation = tempObject.getAdministrationData().getUserAccommodation();
        //TODO по сути тут пользователь нужен только для имени. Нужно хранить имя пользователья в объявлении и убрать лишнюю зависимость
        User client = userStorage.getUser(userAccommodation.getClientLogin());
        MessageWrapper messageWrapper = notificationCenter.
                createMsgsForChannel(userAccommodation, client, config.getValidateData().getChannelID());

        if (messageWrapper.getSendMessage() == null) {
            messageWrapper.setSendMessage(new ArrayList<>());
        }
        messageWrapper.getSendMessage().add(new SendMessage(Util.getUserId(update), "Запрос согласован!"));
        messageWrapper.getSendMessage().add(new SendMessage(client.getLogin(),
                "Ваш запрос №" + userAccommodation.getId() + " согласован и размещен на канале!"));
        return messageWrapper;
    }
}

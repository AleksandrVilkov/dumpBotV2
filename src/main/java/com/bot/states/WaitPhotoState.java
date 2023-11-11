package com.bot.states;

import com.bot.enums.BotCommand;
import com.bot.enums.State;
import com.bot.model.domain.BotUser;
import com.bot.model.domain.IncomingData;
import com.bot.model.domain.Message;
import com.bot.model.domain.Photo;
import com.bot.service.photo.PhotoService;
import com.bot.service.user.UserService;
import com.bot.service.userrequest.UserRequestService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class WaitPhotoState implements BaseState {
    @Autowired
    UserRequestService userRequestService;
    @Autowired
    PhotoService photoService;
    @Autowired
    UserService userService;

    //TODO отрефачить
    @Override
    public List<Message> execute(BotUser botUser, IncomingData incomingData) {
        var userRequestId = userRequestService.findActiveRequestId(botUser.getId());
        if (Strings.isBlank(incomingData.getPhotoId())) {
           return List.of(new Message(botUser.getId(), "Ты прислал не фото"));
        }
        photoService.savePhoto(Photo.builder().telegramUserId(botUser.getId()).
                photoId(incomingData.getPhotoId())
                .requestId(userRequestId).
                build());

        return List.of(new Message(botUser.getId(), "Фото успешно загружено\n Если это все - нажми " +
                BotCommand.FINISH.getCommand()));
    }

    @Override
    public List<Message> handleCommands(BotUser botUser, List<BotCommand> commands) {

        List<Message> result = new ArrayList<>();
        commands.forEach(command -> {
            if (command.equals(BotCommand.FINISH)) {
                var userRequestId = userRequestService.findActiveRequestId(botUser.getId());
                if (photoService.existsPhotosByRequestId(userRequestId)) {
                    userService.saveUserState(botUser.getId(), State.WAIT_USER_APPROVE);
                    result.add(userRequestService.generatePreview(botUser.getId()));
                    result.add(new Message(botUser.getId(), "Посмотри как будет выглядеть объявление. Если все хорошо - нажми " + BotCommand.APPROVE.getCommand() + " \n Что бы начать все заново - " + BotCommand.CANCEL.getCommand()));
                } else {
                    result.add(new Message(botUser.getId(), "Ты не добавил ни одного фото!"));
                }
            }

        });
        return result;
    }
}

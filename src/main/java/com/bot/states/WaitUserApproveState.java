package com.bot.states;

import com.bot.enums.BotCommand;
import com.bot.enums.State;
import com.bot.model.domain.*;
import com.bot.service.user.UserService;
import com.bot.service.userrequest.UserRequestService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaitUserApproveState implements BaseState {
    @Autowired
    UserRequestService userRequestService;
    @Autowired
    UserService userService;

    @Override
    public List<Message> execute(BotUser botUser, IncomingData incomingData) {
        return List.of(new Message(botUser.getId(), "Я нахожусь в ожидании или отклонении запроса. Сначала выполни это действие"));
    }

    @Override
    public List<Message> handleCommands(BotUser botUser, List<BotCommand> commands) {
        List<Message> result = new ArrayList<>();
        var request = userRequestService.findActiveRequest(botUser.getId());
        commands.forEach(command -> {
            switch (command) {
                case APPROVE:
                    request.setUserApprove(true);
                    userRequestService.markNonActive(request.getId());
                    result.add(userRequestService.messageToChanel(request));
                    result.add(new Message(botUser.getId(), "Твой запрос успешно отправлен в канал"));
                    break;
                case CANCEL:
                    userRequestService.markNonActive(request.getId());
                    userService.resetState(botUser.getId());
                    result.add(new Message(botUser.getId(), "Твой запрос успешно удален"));
                    break;
                case ADD_PHOTO:
                    userService.saveUserState(botUser.getId(), State.WAIT_PHOTO);
                    result.add(new Message(botUser.getId(), "Отлично, отправь мне фото"));
                    break;
                default:
                    result.add(new Message(botUser.getId(),
                           "Неизвестная команда. Я ожидаю "+BotCommand.APPROVE.getCommand()+" или " + BotCommand.CANCEL.getCommand()));
            }

        });
        return result;
    }
}

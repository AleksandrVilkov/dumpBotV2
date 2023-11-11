package com.bot.states;

import com.bot.enums.BotCommand;
import com.bot.enums.State;
import com.bot.model.domain.BotUser;
import com.bot.model.domain.IncomingData;
import com.bot.model.domain.Message;
import com.bot.service.user.UserService;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NoneState implements BaseState {
    @Autowired
    UserService userService;
    @Override
    public List<Message> execute(BotUser botUser, IncomingData incomingData) {
        return createWelcomeMessage(botUser);
    }

    @Override
    public List<Message> handleCommands(BotUser botUser, List<BotCommand> commands) {
        return createWelcomeMessage(botUser);
    }

    private List<Message> createWelcomeMessage(BotUser botUser) {
        var result = new Message(botUser.getId(), "Я ");
        result.setChatId(botUser.getId());
        result.setText("Привет! Выбери что ты хочешь сделать: \n" +
                "отправить объявление /send");

        userService.saveUserState(botUser.getId(), State.WAIT_COMMAND);
        return List.of(result);
    }


}

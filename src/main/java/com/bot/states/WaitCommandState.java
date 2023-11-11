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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.bot.enums.BotCommand.SEND;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WaitCommandState implements BaseState {

    @Autowired
    UserService userService;

    @Override
    public List<Message> execute(BotUser botUser, IncomingData incomingData) {
        return List.of(new Message(botUser.getId(), "Я жду команду"));
    }

    @Override
    public List<Message> handleCommands(BotUser botUser, List<BotCommand> commands) {
        List<Message> messages = new ArrayList<>();
        for (BotCommand command : commands) {
            if (Objects.requireNonNull(command) == SEND) {
                messages.add(handleSendCommand(botUser));
            } else {
                messages.add(errorCommand(botUser));
            }
        }
        return messages;
    }

    private Message errorCommand(BotUser botUser) {
        return new Message(botUser.getId(),
                "Я не знаю такой команды, пришли мне команду, с которой я знаком!");
    }

    private Message handleSendCommand(BotUser botUser) {
        userService.saveUserState(botUser.getId(), State.WAIT_TEXT);
        return new Message(botUser.getId(), "Пришли мне текст объявления или запроса на покупку детали.\n" +
                "Обрати внимание, текст должен включать в себя:\n" +
                "- Подробное описание запчасти;\n" +
                "- Применяемость к автомобилю(ям);\n" +
                "- Если это продажа - то обязательно цену и фото. Для поиска запчастей по желанию.\n" +
                "\bВ случае если пункты будут не выполнелны  - я об этом сообщю.\b"
        );

    }


}

package com.bot.bot;

import com.bot.config.BotConfig;
import com.bot.enums.ChatMemberStatus;
import com.bot.enums.State;
import com.bot.mappers.UserMapper;
import com.bot.model.domain.BotUser;
import com.bot.model.domain.IncomingData;
import com.bot.model.domain.Message;
import com.bot.model.domain.UserState;
import com.bot.states.BaseState;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

@Component
@Getter
@Setter
@NoArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Bot extends TelegramLongPollingBot {
    @Autowired
    BotConfig config;
    @Autowired
    UserMapper userMapper;
    @Autowired
    Map<State, BaseState> stateStrategy;

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        var botUser = BotUser.fromTgUser(findTgUserInUpdate(update));
        log.info("От пользователя " + botUser.getId() + "поступил апдейт");
        if (isSubscriptions(botUser)) {
            handle(botUser, update);
        } else {
            log.warn("Пользователь "+ update.getMessage().getChatId().toString() +"не подписан на канал");
            send(new Message(update.getMessage().getChatId().toString(), "Сначала подпишсь на канал!"));
        }
    }

    private void handle(BotUser botUser, Update update) {
        try {
            var userState = userMapper.getUserState(botUser.getId()).
                    orElse(UserState.builder()
                            .state(State.NONE)
                            .telegramId(botUser.getTelegramLogin()).build());

            var state = stateStrategy.get(userState.getState());
            log.info("Состояние пользователя " + botUser.getId() + "- " + state.toString());
            var incomingData = IncomingData.fromUpdate(update);

            List<Message> msgs;

            if (!incomingData.getCommands().isEmpty()) {
                msgs = state.handleCommands(botUser, incomingData.getCommands());
            } else {
                msgs = state.execute(botUser, IncomingData.fromUpdate(update));
            }

            msgs.forEach(this::send);

        } catch (Exception e) {
            send(new Message(update.getMessage().getChatId().toString(),
                    "Простите, произошло недоразумение:\n" + e.getMessage()));

        }

    }

    private void send(Message message) {
        try {
            if (message.withMedia())
                execute(message.getBotMediaMessage());

            if (message.withOnePhoto()) {
                execute(message.getBotTextMessageWithPhoto());
            } else {
                execute(message.getBotTextMessage());
            }

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }

    private User findTgUserInUpdate(Update update) {
        if (update.getMessage() != null) {
            return update.getMessage().getFrom();
        }
        throw new RuntimeException("Не удалось найти пользователя");
    }

    public boolean isSubscriptions(BotUser botUser) {
        GetChatMember chatMember = new GetChatMember();
        chatMember.setUserId(Long.valueOf(botUser.getId()));
        chatMember.setChatId(String.valueOf(config.getValidateData().getChannelID()));
        try {
            String status = execute(chatMember).getStatus();
            return !status.equalsIgnoreCase(ChatMemberStatus.KICKED.getName())
                    && !status.equalsIgnoreCase(ChatMemberStatus.LEFT.getName());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}


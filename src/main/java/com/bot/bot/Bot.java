package com.bot.bot;

import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Collections;

@Component
@Getter
@Setter
@NoArgsConstructor
@Slf4j
public class Bot extends TelegramLongPollingBot {
    @Autowired
    BotConfig config;
    @Autowired
    IProcessor processor;

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
        MessageWrapper msgs;

        if (Validator.validateUser(update, this, config)) {
            msgs = processor.startProcessing(update);
        } else {
            msgs = createValidationError(update);
            deleteOldMessage(update);
            log.warn("user validate error");
        }

        deleteOldMessage(update);
        sendMsgs(msgs, update);
    }

    private void deleteOldMessage(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage(Util.getUserId(update), Util.getMessageId(update));
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMsgs(MessageWrapper msgs, Update update) {
        try {
            if (msgs.getSendMediaGroup() != null) {
                execute(msgs.getSendMediaGroup());
            }
            if (msgs.getSendPhoto() != null) {
                execute(msgs.getSendPhoto());
            }
            if (msgs.getSendMessage() != null && !msgs.getSendMessage().isEmpty()) {
                for (SendMessage sendMessage : msgs.getSendMessage()) {
                    execute(sendMessage);
                }
            }

        } catch (TelegramApiException e) {
            log.error(e.getMessage());
            deleteOldMessage(update);
            throw new RuntimeException(e);
        }
    }


    private MessageWrapper createValidationError(Update update) {
        return MessageWrapper.builder().sendMessage(Collections.singletonList(
                new SendMessage(String.valueOf(update.getMessage().getFrom().getId()),
                        "Ты не подписан на канал!"))).build();
    }

}


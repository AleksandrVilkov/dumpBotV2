package com.bot.bot;

import com.bot.common.Util;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        List<SendMessage> msgs = new ArrayList<>();

        if (Validator.validateUser(update, this, config)) {
            List<SendMessage> result = processor.startProcessing(update);
            msgs.addAll(result);
        } else {
            msgs.addAll(createValidationError(update));
            log.warn("user validate error");
        }

        deleteOldMessage(update);
        sendMsgs(msgs);
    }

    private void deleteOldMessage(Update update) {
        DeleteMessage deleteMessage = new DeleteMessage(Util.getUserId(update), Util.getMessageId(update));
        try {
            execute(deleteMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendMsgs(List<SendMessage> msgs) {
        for (SendMessage sendMessage : msgs) {
            try {
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error(e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private List<SendMessage> createValidationError(Update update) {
        return Collections.singletonList(
                new SendMessage(String.valueOf(update.getMessage().getFrom().getId()),
                        "Ты не подписан на канал!"));
    }

}


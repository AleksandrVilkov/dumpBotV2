package com.bot.bot;

import com.bot.common.Util;
import com.bot.model.TempObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
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
    ITempStorage tempStorage;
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

    /**
     * Получаем апдейт, смотрим, подписан ли пользователь на канал. Если да, пытаемся найти кеш по нему.
     * Если кеш есть - обрабатываем апдейт с кешем, если нет - то без кеша.
     * Если пользователь не подписан на канал - шлем ему уведомление о необходимости подписаться
     * */
    @Override
    public void onUpdateReceived(Update update) {
        List<SendMessage> msgs = new ArrayList<>();

        if (Validator.validateUser(update, this, config)) {
            String tempString = tempStorage.get(String.valueOf(update.getMessage().getFrom().getId()));
            if (tempString != null && !tempString.isEmpty()) {
                try {
                    TempObject tempObject = Util.readTempObject(tempString);
                    List<SendMessage> result = processor.startProcessing(update, tempObject);
                    msgs.addAll(result);
                } catch (JsonProcessingException e) {
                    msgs.addAll(createCommonError(update));
                    log.error("error reading tempObject");
                }
            } else {
                List<SendMessage> result = processor.startProcessing(update);
                msgs.addAll(result);
            }
        } else {
            msgs.addAll(createValidationError(update));
            log.warn("user validate error");
        }

     sendMsgs(msgs);
    }

    private void sendMsgs(List<SendMessage> msgs) {
        for (SendMessage sendMessage: msgs) {
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
    private List<SendMessage> createCommonError(Update update) {
        return Collections.singletonList(
                new SendMessage(String.valueOf(update.getMessage().getFrom().getId()),
                        "Упс, что то пошло не так..."));
    }
}


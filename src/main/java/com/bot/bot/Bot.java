package com.bot.bot;

import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.processor.ITempStorage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
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
    @Autowired
    ITempStorage tempStorage;

    @Override
    public String getBotUsername() {
        return config.getName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    /**
     * Получаем update, показываем что бот что то печатает.
     * Проверяем, подписал ли человек на канал. Если не подписан - даем ошибку что надо бы подписаться.
     * Запускаем процесс, на выходе получаем MessageWrapper.
     * Сохраняем временные файлы. (Кнопки, и все что есть во временных данных.
     * Отправляем результат процесса, удаляем старые сообщения
     * */

    @Override
    public void onUpdateReceived(Update update) {
        //Отправим анимацию "Печатает..."
        sendTyping(update);

        MessageWrapper msgs;
        if (Validator.validateUser(update, this, config)) {
            msgs = processor.startProcessing(update);
        } else {
            msgs = createValidationError(update);
            log.warn("user validate error");
        }
        saveTemp(msgs);
        sendMsgs(msgs, update);
    }

    private void sendTyping(Update update) {
        try {
            SendChatAction chatAction = new SendChatAction();
            chatAction.setAction(ActionType.TYPING);
            chatAction.setChatId(Util.getUserId(update));
            execute(chatAction);
        } catch (TelegramApiException e) {
            log.warn(e.getMessage());
        }
    }

    private void saveTemp(MessageWrapper msgs) {
        if (msgs.getTemp() != null) {
            msgs.getTemp().forEach((key, value) -> tempStorage.set(key, value.toString()));
        }
        if (msgs.getButtons() != null) {
            msgs.getButtons().forEach(b -> tempStorage.set(b.getKey(), b.getTempObject().toString()));
        }
    }

    private void deleteOldMessage(Update update, MessageWrapper messageWrapper) {
        if (messageWrapper.isLeaveOldMessages()) {
            return;
        }
        String userId = Util.getUserId(update);
        //Счтитаем, может быть не более 15 сообщений в чате
        for (int i = 0; i < 15; i++) {
            int messageId = Util.getMessageId(update) - i;
            DeleteMessage deleteMessage = new DeleteMessage(userId, messageId);
            log.info(" Message id " + messageId + " was deleted for user " + userId);
            try {
                execute(deleteMessage);
            } catch (TelegramApiException e) {
                log.warn("Unable to delete message for user " + userId + ". Message id " + messageId + " does not exist");
            }
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
            deleteOldMessage(update, msgs);
        } catch (TelegramApiException e) {
            log.error(e.getMessage());

            throw new RuntimeException(e);
        }
    }


    private MessageWrapper createValidationError(Update update) {
        return MessageWrapper.builder().sendMessage(Collections.singletonList(
                new SendMessage(String.valueOf(update.getMessage().getFrom().getId()),
                        "Ты не подписан на канал!"))).build();
    }

}


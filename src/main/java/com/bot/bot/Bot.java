package com.bot.bot;

import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.processor.ITempStorage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ActionType;
import org.telegram.telegrambots.meta.api.methods.send.SendChatAction;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
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
     */

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
        List<Message> newSendingMessages = sendMsgs(msgs);
        processHistory(newSendingMessages, msgs, update);
    }

    private void processHistory(List<Message> newSendingMessages, MessageWrapper msgs, Update update) {
        List<String> msgsId = new ArrayList<>();
        newSendingMessages.forEach(message -> msgsId.add(String.valueOf(message.getMessageId())));
        String key = "deleteMessageFor" + Util.getUserId(update);
        List<String> deleting = tempStorage.getList(key);
        if (msgs.isLeaveOldMessages()) {
            msgsId.addAll(deleting);
        } else {
            deleteOldMessage(deleting, update);
        }
        tempStorage.setList(key, msgsId);
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

    private void deleteOldMessage(List<String> deleting, Update update) {
        String userId = Util.getUserId(update);
        deleting.forEach(msgId -> {
            try {
                DeleteMessage deleteMessage = new DeleteMessage(userId, Integer.parseInt(msgId));
                log.info(" Message id " + msgId + " was deleted for user " + userId);
                execute(deleteMessage);
            } catch (Exception e) {
                log.warn("Unable to delete message for user " + userId + ". Message id " + msgId + " does not exist");
            }
        });

        int currentId = Util.getMessageId(update);
        try {
            DeleteMessage deleteMessage = new DeleteMessage(userId, currentId);
            log.info("Current message with ID " + currentId + " was deleted for user " + userId);
            execute(deleteMessage);
        } catch (Exception e) {
            log.warn("Unable to delete message for user " + userId + ". Message id " + currentId + " does not exist");
        }
    }

    private List<Message> sendMsgs(MessageWrapper msgs) {
        List<Message> newMessages = new ArrayList<>();

        try {
            if (msgs.getSendMediaGroup() != null) {
                List<Message> execute = execute(msgs.getSendMediaGroup());
                newMessages.addAll(execute);
            }
            if (msgs.getSendPhoto() != null) {
                Message message = execute(msgs.getSendPhoto());
                newMessages.add(message);
            }
            if (msgs.getSendMessage() != null && !msgs.getSendMessage().isEmpty()) {
                for (SendMessage sendMessage : msgs.getSendMessage()) {
                    Message execute = execute(sendMessage);
                    newMessages.add(execute);
                }
            }
        } catch (TelegramApiException e) {
            log.error(e.getMessage());
        }
        return newMessages;
    }


    private MessageWrapper createValidationError(Update update) {
        return MessageWrapper.builder().sendMessage(Collections.singletonList(
                new SendMessage(String.valueOf(update.getMessage().getFrom().getId()),
                        "Ты не подписан на канал!"))).build();
    }

}


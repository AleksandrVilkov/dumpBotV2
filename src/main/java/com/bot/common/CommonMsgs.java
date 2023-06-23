package com.bot.common;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;

public class CommonMsgs {
    public static List<SendMessage> createCommonError(Update update) {
        if (update.hasCallbackQuery()) {
            return Collections.singletonList(
                    new SendMessage(String.valueOf(update.getCallbackQuery().getFrom().getId()),
                            "Упс, что то пошло не так..."));
        }

        return Collections.singletonList(
                new SendMessage(String.valueOf(update.getMessage().getFrom().getId()),
                        "Упс, что то пошло не так..."));
    }
}

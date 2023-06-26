package com.bot.common;

import com.bot.model.MessageWrapper;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
public class CommonMsgs {
    public static MessageWrapper createCommonError(Update update) {
            return MessageWrapper.builder().sendMessage(Collections.singletonList(
                    new SendMessage(Util.getUserId(update),
                            "Упс, что то пошло не так..."))).build();
    }
}

package com.bot.processor.common;

import com.bot.common.Util;
import com.bot.model.ButtonWrapper;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.List;

public class ProcessorUtil {
    public static MessageWrapper createMessages(String text, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(Util.getUserId(update));
        return MessageWrapper.builder().sendMessage(Collections.singletonList(sendMessage)).build();
    }

    public static MessageWrapper createMessages(String text, Update update, List<ButtonWrapper> buttonWrappers) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(Util.createKeyboardOneBtnLine(buttonWrappers));
        sendMessage.setChatId(Util.getUserId(update));
        return MessageWrapper.builder().sendMessage(Collections.singletonList(sendMessage)).buttons(buttonWrappers).build();
    }

    public static void confirmCarSelection(TempObject tempObject) {
        if (tempObject.getSelectedData().getCars() == null) {
            tempObject.getSelectedData().setCars(tempObject.getOption().getCarList());
        } else {
            tempObject.getSelectedData().getCars().addAll(tempObject.getOption().getCarList());
        }

    }
}

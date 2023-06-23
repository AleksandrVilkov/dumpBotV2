package com.bot.processor.common;

import com.bot.common.Util;
import com.bot.model.TempObject;
import com.bot.processor.ITempStorage;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.Collections;
import java.util.List;

public class ProcessorUtil {
    public static String getKeyAndSaveTemp(TempObject newTemp, ITempStorage tempStorage) {
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        return key;
    }
    public static List<SendMessage> createMessages(String text, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(Util.getUserId(update));
        return Collections.singletonList(sendMessage);
    }
    public static List<SendMessage> createMessages(String text, Update update, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setChatId(Util.getUserId(update));
        return Collections.singletonList(sendMessage);
    }
    public static void confirmCarSelection(TempObject tempObject) {
        if ( tempObject.getSelectedData().getCars() == null) {
            tempObject.getSelectedData().setCars(tempObject.getOption().getCarList());
        } else {
            tempObject.getSelectedData().getCars().addAll(tempObject.getOption().getCarList());
        }

    }
}

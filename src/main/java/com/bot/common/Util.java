package com.bot.common;

import com.bot.model.ButtonWrapper;
import com.bot.model.TempObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Util {

    public static <T extends Enum<T>> T findEnumConstant(final Class<T> cls, final String name) {
        if (name == null) {
            return null;
        }
        for (T enumConstant : cls.getEnumConstants()) {
            if (enumConstant.name().equals(name)) {
                return enumConstant;
            }
        }
        return null;
    }

    public static String getUserId(Update update) {
        if (update.hasMessage()) {
            return String.valueOf(update.getMessage().getFrom().getId());
        }
        if (update.hasCallbackQuery()) {
            return String.valueOf(update.getCallbackQuery().getFrom().getId());
        } else {
            return "";
        }
    }

    public static String getUserName(Update update) {
        if (update.hasMessage()) {
            return String.valueOf(update.getMessage().getFrom().getUserName());
        }
        if (update.hasCallbackQuery()) {
            return String.valueOf(update.getCallbackQuery().getFrom().getUserName());
        } else {
            return "";
        }
    }

    public static int getMessageId(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getMessageId();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage().getMessageId();
        } else {
            return 0;
        }
    }

    public static TempObject readTempObject(String tempStr) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(tempStr, TempObject.class);
    }

    public static String generateToken(TempObject tempObject) {
        return String.valueOf(tempObject.hashCode());
    }

    public static InlineKeyboardMarkup createKeyboardOneBtnLine(List<ButtonWrapper> buttons) {
        //Создаем обьект разметки клавиатуры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        buttons.forEach(
                buttonsWrapper -> {
                    ArrayList<InlineKeyboardButton> rowButtons = new ArrayList<>();
                    InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(buttonsWrapper.getName());
                    inlineKeyboardButton.setCallbackData(buttonsWrapper.getKey());
                    rowButtons.add(inlineKeyboardButton);
                    keyboard.add(new ArrayList<>(rowButtons));
                }
        );
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
    public static boolean isOnlyNumber(String string) {
        String regexp = "\\d+";
        Pattern pattern = Pattern.compile(regexp);
        return pattern.matcher(string).find();
    }
}
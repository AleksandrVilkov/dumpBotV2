package com.bot.common;

import com.bot.model.TempObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
//        MessageDigest md = null;
//        try {
//            md = MessageDigest.getInstance("MD5");
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException(e);
//        }
//        byte[] digest = md.digest(tempObject.toString().getBytes(StandardCharsets.UTF_8));
//        return DatatypeConverter.printHexBinary(digest);
    }

    public static InlineKeyboardMarkup createKeyboardThreeBtn(Map<String, String> data) {
        //Создаем обьект разметки клавиатуры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();

        int countBtnInRow = 3;
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        ArrayList<InlineKeyboardButton> rowButtons = new ArrayList<>();

        int rowIter = 0;
        int commonIter = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(entry.getKey());
            inlineKeyboardButton.setCallbackData(entry.getValue());
            rowButtons.add(inlineKeyboardButton);
            rowIter++;
            commonIter++;
            if (rowIter >= countBtnInRow || rowIter == data.size() || commonIter == data.size()) {
                keyboard.add(new ArrayList<>(rowButtons));
                rowButtons.clear();
                rowIter = 0;
            }
        }

        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup createKeyboardOneBtnLine(Map<String, String> data) {
        //Создаем обьект разметки клавиатуры
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            ArrayList<InlineKeyboardButton> rowButtons = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(entry.getKey());
            inlineKeyboardButton.setCallbackData(entry.getValue());
            rowButtons.add(inlineKeyboardButton);
            keyboard.add(new ArrayList<>(rowButtons));
        }
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }

    public static InlineKeyboardMarkup createKeyboardWithNavi(Map<String, String> data, Map<String, String> dataNavigation) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        for (Map.Entry<String, String> entry : data.entrySet()) {
            ArrayList<InlineKeyboardButton> rowButtons = new ArrayList<>();
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton(entry.getKey());
            inlineKeyboardButton.setCallbackData(entry.getValue());
            rowButtons.add(inlineKeyboardButton);
            keyboard.add(new ArrayList<>(rowButtons));
        }

        List<InlineKeyboardButton> navi = new ArrayList<>();
        for (Map.Entry<String, String> entry : dataNavigation.entrySet()) {
            InlineKeyboardButton inlineKeyboardButton = new InlineKeyboardButton();
            inlineKeyboardButton.setText(entry.getKey());
            inlineKeyboardButton.setCallbackData(entry.getValue());
            navi.add(inlineKeyboardButton);
        }

        keyboard.add(navi);
        inlineKeyboardMarkup.setKeyboard(keyboard);
        return inlineKeyboardMarkup;
    }
}
package com.bot.processor.admin;

import com.bot.common.Util;
import com.bot.model.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class AdminHelper {
    public static UserAccommodation approveAccommodation(UserAccommodation userAccommodation) {
        userAccommodation.setApproved(true);
        userAccommodation.setTopical(false);
        userAccommodation.setCreatedDate(new Date());
        return userAccommodation;
    }

    public static UserAccommodation rejectedAccommodation(UserAccommodation userAccommodation) {
        userAccommodation.setRejected(true);
        userAccommodation.setTopical(false);
        userAccommodation.setCreatedDate(new Date());
        return userAccommodation;
    }

    public static void addPhotos(MessageWrapper wrapper, UserAccommodation userAccommodation, Update update, String text, ReplyKeyboard keyboard) {
        if (userAccommodation.getPhotos().size() == 1) {
            SendPhoto sendPhoto = new SendPhoto();
            sendPhoto.setPhoto(new InputFile(userAccommodation.getPhotos().get(0)));
            sendPhoto.setChatId(Util.getUserId(update));
            sendPhoto.setCaption(text);
            sendPhoto.setReplyMarkup(keyboard);
            wrapper.setSendPhoto(sendPhoto);
        } else {
            SendMediaGroup sendMediaGroup = new SendMediaGroup();
            sendMediaGroup.setMedias(AdminHelper.getMedias(userAccommodation));
            sendMediaGroup.setChatId(Util.getUserId(update));
            SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
            sendMessage.setReplyMarkup(keyboard);
            wrapper.setSendMediaGroup(sendMediaGroup);
            wrapper.setSendMessage(Collections.singletonList(sendMessage));
        }
    }

    public static List<InputMedia> getMedias(UserAccommodation userAccommodation) {
        List<InputMedia> inputMedia = new ArrayList<>();
        boolean isFirst = true;
        for (String photo : userAccommodation.getPhotos()) {
            InputMediaPhoto inputMediaPhoto = new InputMediaPhoto(photo);
            if (isFirst) {
                isFirst = false;
            }
            inputMedia.add(inputMediaPhoto);
        }
        return inputMedia;
    }

    public static boolean isWithPhotos(UserAccommodation userAccommodation) {
        return !(userAccommodation.getPhotos() != null && userAccommodation.getPhotos().isEmpty());
    }

    public static TempObject getTempForButton(TempObject oldObject, Operation operation, ActionOnRequest action, UserAccommodation userAccommodation) {
        TempObject tempObject = oldObject.clone();
        tempObject.setOperation(operation);
        AdministrationData approvedData = new AdministrationData();
        approvedData.setAction(action);
        approvedData.setUserAccommodation(userAccommodation);
        tempObject.setAdministrationData(approvedData);
        return tempObject;
    }

    public static String generateAccommodationMsgText(UserAccommodation userAccommodation, User user, int count, List<Car> cars) {
        final String SEPARATOR = "\n\n";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Всего не рассмотрено заявок: ").append(count).append(SEPARATOR);
        stringBuilder.append("Рассмотите заявку № ").append(userAccommodation.getId()).append(SEPARATOR)
                .append("Описание: ").append(userAccommodation.getDescription()).append(SEPARATOR)
                .append("Автор: @").append(user.getUserName()).append(SEPARATOR)
                .append("Тип запроса: ").append(userAccommodation.getType().name()).append(SEPARATOR);
        if (userAccommodation.getCarsId() != null && !userAccommodation.getCarsId().isEmpty()) {
            stringBuilder.append("Указаны автомобили: ");
            cars.forEach(car -> {
                stringBuilder.append(car.getBrand().getName()).append(" ")
                        .append(car.getModel().getName())
                        .append(" (").append(car.getEngine().getName()).append(")").append(SEPARATOR);
            });

        } else {
            stringBuilder.append("Автомобили не указаны.").append(SEPARATOR);
        }

        return stringBuilder.toString();
    }
}

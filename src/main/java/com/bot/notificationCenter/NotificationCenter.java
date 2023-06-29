package com.bot.notificationCenter;

import com.bot.model.*;
import com.bot.processor.ICarStorage;
import com.bot.processor.INotificationCenter;
import com.bot.processor.IUserStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.util.ArrayList;
import java.util.List;

@Component
public class NotificationCenter implements INotificationCenter {
    @Autowired
    IUserStorage userStorage;
    @Autowired
    ICarStorage carStorage;

    @Override
    public List<SendMessage> getMsgsForAllAdmins(String text) {
        ArrayList<SendMessage> result = new ArrayList<>();
        List<User> admins = userStorage.findAdmins();
        for (User admin : admins) {
            result.add(new SendMessage(admin.getLogin(), text));
        }
        return result;
    }

    @Override
    public MessageWrapper createMsgsForChannel(UserAccommodation userAccommodation, User user, long channelID) {
        final String SEPARATOR = "\n\n";
        StringBuilder stringBuilder = new StringBuilder();
        if (userAccommodation.getType().equals(AccommodationType.SALE)) {
            stringBuilder.append("Продам: ");
        }
        if (userAccommodation.getType().equals(AccommodationType.SEARCH)) {
            stringBuilder.append("Куплю: ");
        }
        stringBuilder.append(userAccommodation.getDescription()).append(SEPARATOR);
        if (userAccommodation.getCarsId() != null && !userAccommodation.getCarsId().isEmpty()) {
            stringBuilder.append("Подходит к автомобилям: ");
            for (String id : userAccommodation.getCarsId()) {
                Car car = carStorage.getCarById(Integer.parseInt(id));
                stringBuilder.append(car.getBrand().getName()).append(" ")
                        .append(car.getModel().getName())
                        .append(" (").append(car.getEngine().getName()).append(")").append(SEPARATOR);
            }
        }


        stringBuilder.append("Обращаться: @").append(user.getUserName());

        MessageWrapper messageWrapper = new MessageWrapper();
        List<SendMessage> sendMessageList = new ArrayList<>();
        if (isWithPhotos(userAccommodation)) {
            if (userAccommodation.getPhotos().size() == 1) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setPhoto(new InputFile(userAccommodation.getPhotos().get(0)));
                sendPhoto.setChatId(channelID);
                sendPhoto.setCaption(stringBuilder.toString());
                messageWrapper.setSendPhoto(sendPhoto);
                return messageWrapper;
            } else {
                SendMediaGroup sendMediaGroup = new SendMediaGroup();
                sendMediaGroup.setMedias(getMedias(userAccommodation));
                sendMediaGroup.setChatId(channelID);
                messageWrapper.setSendMediaGroup(sendMediaGroup);
            }
        }
        sendMessageList.add(new SendMessage(String.valueOf(channelID),
                stringBuilder.toString()));
        messageWrapper.setSendMessage(sendMessageList);
        return messageWrapper;
    }

    private boolean isWithPhotos(UserAccommodation userAccommodation) {
        return !(userAccommodation.getPhotos() != null && userAccommodation.getPhotos().isEmpty());
    }

    private List<InputMedia> getMedias(UserAccommodation userAccommodation) {
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
}

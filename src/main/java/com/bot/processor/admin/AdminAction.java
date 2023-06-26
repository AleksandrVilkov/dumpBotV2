package com.bot.processor.admin;

import com.bot.bot.BotConfig;
import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.*;
import com.bot.processor.common.CommonCar;
import com.bot.processor.common.ProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.*;

@Component
@Slf4j
public class AdminAction implements Action {
    final String ACTION_NAME = "ADMIN_QUERY_PROCESSING";
    final String SEPARATOR = "\n\n";
    @Autowired
    IAccommodationStorage accommodationStorage;
    @Autowired
    ICarStorage carStorage;
    @Autowired
    BotConfig config;
    @Autowired
    IUserStorage userStorage;

    @Autowired
    ITempStorage tempStorage;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject) {

        //TODO нужно отрефакторить
        switch (tempObject.getOperation()) {
            case START -> {
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return starting(update, tempObject);
            }

            case APPROVED_REQUEST -> {
                return approved(update, tempObject);
            }
            case REJECTED_REQUEST -> {
                return rejected(update, tempObject);
            }
            case SEND_REJECTED_REQUEST -> {
                return sendRejected(update, tempObject);
            }
            case EDIT_REQUEST -> {
                return edit(update, tempObject);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private MessageWrapper approved(Update update, TempObject tempObject) {
        UserAccommodation userAccommodation = tempObject.getAdministrationData().getUserAccommodation();
        approvedAccommodation(userAccommodation);
        User user = userStorage.getUser(userAccommodation.getClientLogin());
        MessageWrapper messageWrapper = createMsgsForChannel(userAccommodation, user);

        if (messageWrapper.getSendMessage() == null) {
            messageWrapper.setSendMessage(new ArrayList<>());
        }

        messageWrapper.getSendMessage().add(new SendMessage(Util.getUserId(update), "Запрос согласован!"));
        messageWrapper.getSendMessage().add(new SendMessage(user.getLogin(), "Ваш запрос №" + userAccommodation.getId() + " согласован и размещен на канале!"));
        return messageWrapper;
    }

    private void approvedAccommodation(UserAccommodation userAccommodation) {
        userAccommodation.setApproved(true);
        userAccommodation.setTopical(false);
        userAccommodation.setCreatedDate(new Date());
        accommodationStorage.saveAccommodation(userAccommodation);
        log.info("user accommodation " + userAccommodation.getId() + " will be approved and updated");

    }

    private MessageWrapper edit(Update update, TempObject tempObject) {
        return null;
    }

    private MessageWrapper rejected(Update update, TempObject tempObject) {
        String text = "Укажи причину отклонения запроса. Обрати внимание, это сообщение получит автор.";
        SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
        User user = userStorage.getUser(Util.getUserId(update));
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operation.SEND_REJECTED_REQUEST);
        user.setLastCallback(ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));
        user.setWaitingMessages(true);
        userStorage.saveUser(user);
        return MessageWrapper.builder().sendMessage(Collections.singletonList(sendMessage)).build();
    }

    private MessageWrapper sendRejected(Update update, TempObject tempObject) {
        return null;
    }

    private MessageWrapper starting(Update update, TempObject tempObject) {
        MessageWrapper wrapper = new MessageWrapper();
        int count = accommodationStorage.countNotAgreed();
        if (count == 0) {
            return MessageWrapper.builder()
                    .sendMessage(Collections.singletonList(new SendMessage(Util.getUserId(update), "Не обработанных заявок нет")))
                    .build();
        }

        UserAccommodation userAccommodation = accommodationStorage.getFirstNotAgreed();
        User user = userStorage.getUser(userAccommodation.getClientLogin());
        String text = generateAccommodationMsgText(userAccommodation, user, count);
        TempObject approved = getTempForButton(tempObject, Operation.APPROVED_REQUEST,
                ActionOnRequest.APPROVED,
                userAccommodation);

        TempObject edit = getTempForButton(tempObject, Operation.EDIT_REQUEST,
                ActionOnRequest.EDIT, userAccommodation);

        TempObject rejected = getTempForButton(tempObject, Operation.REJECTED_REQUEST,
                ActionOnRequest.REJECTED, userAccommodation);

        Map<String, String> data = new HashMap<>();
        data.put("Одобрить", ProcessorUtil.getKeyAndSaveTemp(approved, tempStorage));
        data.put("Изменить", ProcessorUtil.getKeyAndSaveTemp(edit, tempStorage));
        data.put("Отклонить", ProcessorUtil.getKeyAndSaveTemp(rejected, tempStorage));

        ReplyKeyboard keyboard = Util.createKeyboardOneBtnLine(data);
        if (isWithPhotos(userAccommodation)) {
            if (userAccommodation.getPhotos().size() == 1) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setPhoto(new InputFile(userAccommodation.getPhotos().get(0)));
                sendPhoto.setChatId(Util.getUserId(update));
                sendPhoto.setCaption(text);
                sendPhoto.setReplyMarkup(keyboard);
                wrapper.setSendPhoto(sendPhoto);
            } else {
                //TODO куча фото
                SendMediaGroup sendMediaGroup = new SendMediaGroup();
                sendMediaGroup.setMedias(getMedias(userAccommodation));
                sendMediaGroup.setChatId(Util.getUserId(update));
                SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
                sendMessage.setReplyMarkup(keyboard);
                wrapper.setSendMediaGroup(sendMediaGroup);
                wrapper.setSendMessage(Collections.singletonList(sendMessage));
            }
        } else {
            SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
            sendMessage.setReplyMarkup(keyboard);
            wrapper.setSendMessage(Collections.singletonList(sendMessage));
        }
        return wrapper;
    }

    private MessageWrapper createMsgsForChannel(UserAccommodation userAccommodation, User user) {

        StringBuilder stringBuilder = new StringBuilder();
        if (userAccommodation.getType().equals(AccommodationType.SALE)) {
            stringBuilder.append("Продам: ");
        }
        if (userAccommodation.getType().equals(AccommodationType.SEARCH)) {
            stringBuilder.append("Куплю: ");
        }
        if (userAccommodation.getCarsId() != null && !userAccommodation.getCarsId().isEmpty()) {
            stringBuilder.append(userAccommodation.getDescription()).append(SEPARATOR).append("Подходит к автомобилям: ");
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
        long channelID = config.getValidateData().getChannelID();
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
        stringBuilder.append(userAccommodation.getDescription());
        sendMessageList.add(new SendMessage(String.valueOf(channelID),
                stringBuilder.toString()));
        messageWrapper.setSendMessage(sendMessageList);
        return messageWrapper;

    }

    private boolean isWithPhotos(UserAccommodation userAccommodation) {
        return !(userAccommodation.getPhotos() != null && userAccommodation.getPhotos().isEmpty());
    }

    private String generateAccommodationMsgText(UserAccommodation userAccommodation, User user, int count) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Всего не рассмотрено заявок: ").append(count).append(SEPARATOR);
        stringBuilder.append("Рассмотите заявку № ").append(userAccommodation.getId()).append(SEPARATOR)
                .append("Описание: ").append(userAccommodation.getDescription()).append(SEPARATOR)
                .append("Автор: @").append(user.getUserName()).append(SEPARATOR)
                .append("Тип запроса: ").append(userAccommodation.getType().name()).append(SEPARATOR);
        if (userAccommodation.getCarsId() != null && !userAccommodation.getCarsId().isEmpty()) {
            stringBuilder.append("Указаны автомобили: ");
            for (String id : userAccommodation.getCarsId()) {
                Car car = carStorage.getCarById(Integer.parseInt(id));
                stringBuilder.append(car.getBrand().getName()).append(" ")
                        .append(car.getModel().getName())
                        .append(" (").append(car.getEngine().getName()).append(")").append(SEPARATOR);
            }
        } else {
            stringBuilder.append("Автомобили не указаны.").append(SEPARATOR);
        }

        return stringBuilder.toString();
    }

    private TempObject getTempForButton(TempObject oldObject, Operation operation, ActionOnRequest action, UserAccommodation userAccommodation) {
        TempObject tempObject = oldObject.clone();
        tempObject.setOperation(operation);
        AdministrationData approvedData = new AdministrationData();
        approvedData.setAction(action);
        approvedData.setUserAccommodation(userAccommodation);
        tempObject.setAdministrationData(approvedData);
        return tempObject;
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

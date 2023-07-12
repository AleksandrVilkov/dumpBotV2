package com.bot.processor.admin;

import com.bot.bot.BotConfig;
import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AdminAction implements Action {
    final String ACTION_NAME = "ADMIN_QUERY_PROCESSING";
    @Autowired
    IAccommodationStorage accommodationStorage;
    @Autowired
    ICarStorage carStorage;
    @Autowired
    BotConfig config;
    @Autowired
    IUserStorage userStorage;
    @Autowired
    INotificationCenter notificationCenter;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        switch (tempObject.getOperation()) {
            case START -> {
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return starting(update, tempObject, user);
            }
            case APPROVED_REQUEST -> {
                return approved(update, tempObject);
            }
            case REJECTED_REQUEST -> {
                return rejected(update, tempObject, user);
            }
            case SEND_REJECTED_REQUEST -> {
                return sendRejected(update, tempObject, user);
            }
            case EDIT_REQUEST -> {
                return commonEdit(update, tempObject);
            }
            case EDIT_DESCRIPTION -> {
                return editDescription(update, tempObject, user);
            }
//            case EDIT_CAR -> {
//                return commonEditCars(update, tempObject, user);
//            }
            case ENTER_NEW_DESCRIPTION -> {
                return saveNewDescription(update, tempObject, user);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private MessageWrapper saveNewDescription(Update update, TempObject tempObject, User user) {
        UserAccommodation accommodation = tempObject.getAdministrationData().getUserAccommodation();
        accommodation.setDescription(update.getMessage().getText());
        //TODO подпорка, убрать. Тут нет даты почему то
        accommodation.setCreatedDate(new Date());
        accommodationStorage.saveAccommodation(accommodation);
        user.setWaitingMessages(false);
        user.setLastCallback(null);

        return starting(update, tempObject, accommodation, user);
    }

    private MessageWrapper editDescription(Update update, TempObject tempObject, User user) {
        tempObject.setOperation(Operation.ENTER_NEW_DESCRIPTION);
        String key = Util.generateToken(tempObject);
        user.setWaitingMessages(true);
        user.setLastCallback(key);

        SendMessage sendMessage = new SendMessage(Util.getUserId(update), "Измените текст:");
        return MessageWrapper.builder()
                .sendMessage(Collections.singletonList(sendMessage))
                .leaveOldMessages(true).build().addTemp(key, tempObject);
    }


    private MessageWrapper approved(Update update, TempObject tempObject) {
        UserAccommodation userAccommodation = tempObject.getAdministrationData().getUserAccommodation();
        accommodationStorage.saveAccommodation(AdminHelper.approveAccommodation(userAccommodation));
        log.info("user accommodation " + userAccommodation.getId() + " will be approved and updated");
        //TODO по сути тут пользователь нужен только для имени. Нужно хранить имя пользователья в объявлении и убрать лишнюю зависимость
        User client = userStorage.getUser(userAccommodation.getClientLogin());
        MessageWrapper messageWrapper = notificationCenter.
                createMsgsForChannel(userAccommodation, client, config.getValidateData().getChannelID());

        if (messageWrapper.getSendMessage() == null) {
            messageWrapper.setSendMessage(new ArrayList<>());
        }

        messageWrapper.getSendMessage().add(new SendMessage(Util.getUserId(update), "Запрос согласован!"));
        messageWrapper.getSendMessage().add(new SendMessage(client.getLogin(),
                "Ваш запрос №" + userAccommodation.getId() + " согласован и размещен на канале!"));
        return messageWrapper;
    }


    private MessageWrapper commonEdit(Update update, TempObject tempObject) {
        TempObject editDescription = tempObject.clone();
        editDescription.setOperation(Operation.EDIT_DESCRIPTION);
        ButtonWrapper buttonEditDescription = new ButtonWrapper("Изменить описание", Util.generateToken(editDescription), editDescription);

        TempObject editEngine = tempObject.clone();
        editEngine.setOperation(Operation.EDIT_DESCRIPTION);
        ButtonWrapper buttonEditEngine = new ButtonWrapper("Удалить двигатели", Util.generateToken(editEngine), editEngine);


        TempObject deleteCar = tempObject.clone();
        editEngine.setOperation(Operation.DELETE_CAR);
        ButtonWrapper buttonDeleteCar = new ButtonWrapper("Удалить машину", Util.generateToken(deleteCar), deleteCar);


        List<ButtonWrapper> buttons = List.of(buttonEditDescription, buttonEditEngine, buttonDeleteCar);
        ReplyKeyboard keyboard = Util.createKeyboardOneBtnLine(buttons);
        SendMessage sendMessage = new SendMessage(Util.getUserId(update), "Выбери, что именно изменить:");
        sendMessage.setReplyMarkup(keyboard);
        return MessageWrapper.builder()
                .sendMessage(Collections.singletonList(sendMessage)).leaveOldMessages(true)
                .buttons(buttons).build();
    }

    private MessageWrapper rejected(Update update, TempObject tempObject, User user) {
        String text = "Укажи причину отклонения запроса. Обрати внимание, это сообщение получит автор.";
        SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operation.SEND_REJECTED_REQUEST);
        String key = Util.generateToken(newTemp);
        user.setLastCallback(key);
        user.setWaitingMessages(true);
        MessageWrapper messageWrapper = MessageWrapper.builder().sendMessage(Collections.singletonList(sendMessage)).build();
        messageWrapper.addTemp(key, newTemp);
        return messageWrapper;
    }

    private MessageWrapper sendRejected(Update update, TempObject tempObject, User user) {
        String rejectedText = update.getMessage().getText();
        UserAccommodation accommodation = tempObject.getAdministrationData().getUserAccommodation();
        String userNotification = "Привет! твой запрос \"" + accommodation.getDescription() + "\"" + " отклонен. \n" +
                "Комментарий администратора: \n" + rejectedText;
        String adminNotification = "Запрос помечен как отклоненный. Пользователю отправлено уведомление с текекстом: \n" + userNotification;
        accommodationStorage.saveAccommodation(AdminHelper.rejectedAccommodation(accommodation));
        log.info("user accommodation " + accommodation.getId() + " will be rejected and updated");
        List<SendMessage> msgs = new ArrayList<>();
        msgs.add(new SendMessage(accommodation.getClientLogin(), userNotification));
        msgs.add(new SendMessage(Util.getUserId(update), adminNotification));
        user.setWaitingMessages(false);
        user.setLastCallback(null);
        return MessageWrapper.builder().sendMessage(msgs).build();
    }


    private MessageWrapper starting(Update update, TempObject tempObject, User user) {
        UserAccommodation userAccommodation = accommodationStorage.getFirstNotAgreed();
        return starting(update, tempObject, userAccommodation, user);
    }

    private MessageWrapper starting(Update update, TempObject tempObject, UserAccommodation userAccommodation, User user) {
        MessageWrapper wrapper = new MessageWrapper();
        int count = accommodationStorage.countNotAgreed();
        if (count == 0) {
            return MessageWrapper.builder()
                    .sendMessage(Collections.singletonList(new SendMessage(Util.getUserId(update),
                            "Не обработанных заявок нет")))
                    .build();
        }
        List<Car> cars = new ArrayList<>();
        userAccommodation.getCarsId().forEach(id -> cars.add(carStorage.getCarById(Integer.parseInt(id))));

        String text = AdminHelper.generateAccommodationMsgText(userAccommodation, user, count, cars);
        TempObject approved = AdminHelper.getTempForButton(tempObject, Operation.APPROVED_REQUEST,
                ActionOnRequest.APPROVED,
                userAccommodation);

        TempObject edit = AdminHelper.getTempForButton(tempObject, Operation.EDIT_REQUEST,
                ActionOnRequest.EDIT, userAccommodation);

        TempObject rejected = AdminHelper.getTempForButton(tempObject, Operation.REJECTED_REQUEST,
                ActionOnRequest.REJECTED, userAccommodation);

        List<ButtonWrapper> data = new ArrayList<>();

        data.add(new ButtonWrapper("Одобрить", Util.generateToken(approved), approved));
        data.add(new ButtonWrapper("Изменить", Util.generateToken(edit), edit));
        data.add(new ButtonWrapper("Отклонить", Util.generateToken(rejected), rejected));

        wrapper.setButtons(data);
        ReplyKeyboard keyboard = Util.createKeyboardOneBtnLine(data);
        if (AdminHelper.isWithPhotos(userAccommodation)) {
            AdminHelper.addPhotos(wrapper, userAccommodation, update, text, keyboard);
        } else {
            SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
            sendMessage.setReplyMarkup(keyboard);
            wrapper.setSendMessage(Collections.singletonList(sendMessage));
        }
        return wrapper;
    }
}

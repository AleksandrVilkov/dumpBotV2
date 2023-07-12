package com.bot.processor.search;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.oprations.Operations;
import com.bot.processor.Action;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.ICarStorage;
import com.bot.processor.INotificationCenter;
import com.bot.processor.common.CarOperation;
import com.bot.processor.common.PhotoOperation;
import com.bot.processor.common.ProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
@Slf4j
public class SearchAction implements Action {
    @Autowired
    ICarStorage carStorage;
    @Autowired
    IAccommodationStorage accommodationStorage;
    @Autowired
    INotificationCenter notificationCenter;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        final String ACTION_NAME = "SEARCH";
        switch (tempObject.getOperation()) {
            case START -> {
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }
            case NEED_CAR -> {
                log.info("Start NEED_CAR " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return secondStep(update, tempObject);
            }
            case CONCERN_SELECTION -> {
                log.info("Start CONCERN_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return thirdStep(update, tempObject);
            }
            case BRAND_SELECTION -> {
                log.info("Start BRAND_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fourthStep(update, tempObject);
            }
            case MODEL_SELECTION -> {
                log.info("Start MODEL_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fifthStep(update, tempObject);
            }
            case ENGINE_SELECTION -> {
                log.info("Start ENGINE_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return sixthStep(update, tempObject);
            }
            case PRE_PHOTO -> {
                log.info("Start PRE_PHOTO " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return seventhStep(update, tempObject);
            }
            case PHOTO -> {
                log.info("Start PHOTO " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return eighthStep(update, tempObject, user);
            }
            case DESCRIPTION -> {
                log.info("Start DESCRIPTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return ninthStep(update, tempObject, user);
            }
            case END -> {
                log.info("Start 9 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return tenthStep(update, tempObject, user);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private MessageWrapper firstStep(Update update, TempObject tempObject) {
        String text = "Давай разместим запрос на поиск. Нажми кнопку \"Начать\"";
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operations.NEED_CAR);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Util.getUserId(update));
        List<ButtonWrapper> data = new ArrayList<>();
        data.add(new ButtonWrapper("Начать", Util.generateToken(newTemp), newTemp));
        return ProcessorUtil.createMessages(text, update, data);
    }

    private MessageWrapper secondStep(Update update, TempObject tempObject) {
        String text = "Хочешь указать пренадлежность детали к автомобилю? Если указать - то большее количество пользователей получат уведомление, и шанс найти то что ты ищешь - возрастает.";

        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operations.CONCERN_SELECTION);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Util.getUserId(update));
        List<ButtonWrapper> data = new ArrayList<>();
        data.add(new ButtonWrapper("Указать авто", Util.generateToken(newTemp), newTemp));

        TempObject newTempNoCar = tempObject.clone();
        newTempNoCar.setOperation(Operations.PRE_PHOTO);
        data.add(new ButtonWrapper("Не указывать", Util.generateToken(newTempNoCar), newTempNoCar));
        return ProcessorUtil.createMessages(text, update, data);
    }

    private MessageWrapper thirdStep(Update update, TempObject tempObject) {
        List<Car> cars = carStorage.getCars();
        if (tempObject.getSelectedData() == null) {
            SelectedData selectedData = new SelectedData();
            tempObject.setSelectedData(selectedData);
        }
        Map<String, List<Car>> carByConcern = new HashMap<>();
        cars.forEach(
                car -> {
                    String concern = car.getConcern().getName();
                    if (carByConcern.containsKey(concern)) {
                        carByConcern.get(concern).add(car);
                    } else {
                        List<Car> carBrands = new ArrayList<>();
                        carBrands.add(car);
                        carByConcern.put(concern, carBrands);
                    }
                }
        );
        return CarOperation.chooseConcern(update, tempObject, cars, Operations.BRAND_SELECTION);
    }

    private MessageWrapper fourthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseBrand(update, tempObject, Operations.MODEL_SELECTION);
    }

    private MessageWrapper fifthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseModel(update, tempObject, Operations.ENGINE_SELECTION);
    }

    private MessageWrapper sixthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseEngine(update, tempObject, Operations.PRE_PHOTO);
    }

    private MessageWrapper seventhStep(Update update, TempObject tempObject) {
        List<ButtonWrapper> data = new ArrayList<>();
        String text;
        if (tempObject.getOption() != null && tempObject.getOption().getCarList() != null && !tempObject.getOption().getCarList().isEmpty()) {
            Car car = tempObject.getOption().getCarList().get(0);
            ProcessorUtil.confirmCarSelection(tempObject);
            text = "Отлично. Автомобиль " + car.getBrand() + " " + car.getModel() + "(" + car.getEngine() + ")" + "успешно выбран. \n Что дальше?";
            TempObject tempCarElse = tempObject.clone();
            tempCarElse.setOperation(Operations.CONCERN_SELECTION);
            data.add(new ButtonWrapper("Добавить авто", Util.generateToken(tempCarElse), tempCarElse));

        } else {
            text = "Хорошо, продолжим без указания авто. Давай решим, будем мы прикладывать фото искомой запчасти? Имей ввиду, это так же повысит возможность найти то, что ты ищешь";
        }
        TempObject tempPhoto = tempObject.clone();
        tempPhoto.setOperation(Operations.PHOTO);
        data.add(new ButtonWrapper("Добавить фото", Util.generateToken(tempPhoto), tempPhoto));

        TempObject tempNoPhoto = tempObject.clone();
        tempNoPhoto.setOperation(Operations.DESCRIPTION);
        data.add(new ButtonWrapper("Без фото", Util.generateToken(tempNoPhoto), tempNoPhoto));
        return ProcessorUtil.createMessages(text, update, data);
    }

    private MessageWrapper eighthStep(Update update, TempObject tempObject, User user) {
        TempObject newTemp = tempObject.clone();
        return PhotoOperation.addPhoto(user, update, newTemp, Operations.DESCRIPTION);
    }

    private MessageWrapper ninthStep(Update update, TempObject tempObject, User user) {
        String text = "Опиши то, что ты ищешь:";
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operations.END);
        user.setWaitingMessages(true);
        String key = Util.generateToken(newTemp);
        user.setLastCallback(key);
        MessageWrapper messageWrapper = ProcessorUtil.createMessages(text, update);
        messageWrapper.addTemp(key, newTemp);
        return messageWrapper;
    }

    private MessageWrapper tenthStep(Update update, TempObject tempObject, User user) {
        TempObject newTemp = tempObject.clone();
        String description = update.getMessage().getText();
        UserAccommodation.UserAccommodationBuilder builder = UserAccommodation.builder()
                .type(AccommodationType.SEARCH)
                .createdDate(new Date())
                .clientLogin(user.getLogin())
                .clientId(user.getId())
                .approved(false)
                .rejected(false)
                .topical(true)
                .description(description);

        if (newTemp.getSelectedData() != null) {
            builder.photos(newTemp.getSelectedData().getPhotos())
                    .carsId(CarOperation.getCarsId(tempObject.getSelectedData().getCars()));
        }
        UserAccommodation userAccommodation = builder.build();
        if (accommodationStorage.saveAccommodation(userAccommodation)) {
            String userText = "Отлично, твой запрос отправлен на модерацию. После одобрения он будет размещен на канале. Я сообщую об этом.";
            String adminText = "Поступил новый запрос на поиски детали: " + userAccommodation.getDescription() + "\n Открой кабинет администратора для обработки. /start";
            List<SendMessage> result = new ArrayList<>();
            result.add(new SendMessage(Util.getUserId(update), userText));
            result.addAll(notificationCenter.getMsgsForAllAdmins(adminText));
            user.setWaitingMessages(false);
            user.setLastCallback(null);
            return MessageWrapper.builder().sendMessage(result).build();
        } else {
            log.error("error saving user accommodation for user " + Util.getUserId(update));
            return CommonMsgs.createCommonError(update);
        }
    }
}

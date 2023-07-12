package com.bot.processor.sale;

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
public class SaleAction implements Action {
    @Autowired
    ICarStorage carStorage;
    @Autowired
    IAccommodationStorage accommodationStorage;
    @Autowired
    INotificationCenter notificationCenter;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        final String ACTION_NAME = "SALE";
        switch (tempObject.getOperation()) {
            case START -> {
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }
            case CONCERN_SELECTION -> {
                log.info("Start CONCERN_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return secondStep(update, tempObject);
            }
            case BRAND_SELECTION -> {
                log.info("Start BRAND_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return thirdStep(update, tempObject);
            }
            case MODEL_SELECTION -> {
                log.info("Start MODEL_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fourthStep(update, tempObject);
            }
            case ENGINE_SELECTION -> {
                log.info("Start ENGINE_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fifthStep(update, tempObject);
            }
            case PRE_PHOTO -> {
                log.info("Start PRE_PHOTO " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return sixthStep(update, tempObject);
            }
            case PHOTO -> {
                log.info("Start PHOTO " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return seventhStep(update, tempObject, user);
            }
            case ENTER_PRICE -> {
                log.info("Start ENTER_PRICE " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return enterPrice(update,tempObject,user);
            }
            case DESCRIPTION -> {
                log.info("Start DESCRIPTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return eighthStep(update, tempObject, user);
            }
            case END -> {
                log.info("Start END " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return ninthStep(update, tempObject, user);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private MessageWrapper enterPrice(Update update, TempObject tempObject, User user) {
        String text = "Укажи цену:";
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operations.DESCRIPTION);
        user.setWaitingMessages(true);
        String key = Util.generateToken(newTemp);
        user.setLastCallback(key);
        MessageWrapper messageWrapper = ProcessorUtil.createMessages(text, update);
        messageWrapper.addTemp(key, newTemp);
        return messageWrapper;
    }

    private MessageWrapper firstStep(Update update, TempObject tempObject) {
        String text = "Давай разместим с тобой объявление. Нужно будет указать машины, на которые подходит деталь, приложить фото, указать описание и цену. Нажми на кнопку начать:";
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operations.CONCERN_SELECTION);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Util.getUserId(update));
        List<ButtonWrapper> data = new ArrayList<>();
        data.add(new ButtonWrapper("Начать", Util.generateToken(newTemp), newTemp));
        return ProcessorUtil.createMessages(text, update, data);
    }

    private MessageWrapper secondStep(Update update, TempObject tempObject) {
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

    private MessageWrapper thirdStep(Update update, TempObject tempObject) {
        return CarOperation.chooseBrand(update, tempObject, Operations.MODEL_SELECTION);
    }

    private MessageWrapper fourthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseModel(update, tempObject, Operations.ENGINE_SELECTION);
    }

    private MessageWrapper fifthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseEngine(update, tempObject, Operations.PRE_PHOTO);
    }

    private MessageWrapper sixthStep(Update update, TempObject tempObject) {
        if (tempObject.getOption().getCarList().size() != 1) {
            log.error("CarList size > 1!");
            return CommonMsgs.createCommonError(update);
        }
        Car car = tempObject.getOption().getCarList().get(0);
        ProcessorUtil.confirmCarSelection(tempObject);
        String text = "Отлично. Автомобиль " + car.getBrand() + " " + car.getModel() + "(" + car.getEngine() + ")" + "успешно выбран. \n Что дальлше?";

        List<ButtonWrapper> data = new ArrayList<>();
        TempObject tempCarElse = tempObject.clone();
        tempCarElse.setOperation(Operations.CONCERN_SELECTION);

        TempObject tempPhoto = tempObject.clone();
        tempPhoto.setOperation(Operations.PHOTO);
        data.add(new ButtonWrapper("Добавить фото", Util.generateToken(tempPhoto), tempPhoto));
        data.add(new ButtonWrapper("Добавить авто", Util.generateToken(tempCarElse), tempCarElse));
        return ProcessorUtil.createMessages(text, update, data);
    }

    private MessageWrapper seventhStep(Update update, TempObject tempObject, User user) {
        TempObject newTemp = tempObject.clone();
        return PhotoOperation.addPhoto(user, update, newTemp, Operations.ENTER_PRICE);
    }

    private MessageWrapper eighthStep(Update update, TempObject tempObject, User user) {
        String price = update.getMessage().getText();
        if (!Util.isOnlyNumber(price)) {
            return ProcessorUtil.createMessages("Допустимо вводить только цифры. Укажи цену еще раз:", update);
        }
        TempObject newTemp = tempObject.clone();
        newTemp.setPrice(Integer.parseInt(price));
        newTemp.setOperation(Operations.END);
        user.setWaitingMessages(true);
        String key = Util.generateToken(newTemp);
        user.setLastCallback(key);
        String text = "Укажи описание к объявлению. Опиши товар, не забудь обязательно указать цену! Пиши так, что бы твой товар захотели купить!";
        MessageWrapper messageWrapper = ProcessorUtil.createMessages(text, update);
        messageWrapper.addTemp(key, newTemp);
        return messageWrapper;
    }

    private MessageWrapper ninthStep(Update update, TempObject tempObject, User user) {
        TempObject newTemp = tempObject.clone();
        String description = update.getMessage().getText();
        UserAccommodation userAccommodation = UserAccommodation.builder()
                .type(AccommodationType.SALE)
                .createdDate(new Date())
                .clientLogin(user.getLogin())
                .clientId(user.getId())
                .approved(false)
                .rejected(false)
                .topical(true)
                .price(tempObject.getPrice())
                .description(description)
                .photos(newTemp.getSelectedData().getPhotos())
                .carsId(CarOperation.getCarsId(tempObject.getSelectedData().getCars()))
                .build();

        if (accommodationStorage.saveAccommodation(userAccommodation)) {
            String userText = "Отлично, твое обьявление отправлено на модерацию. После одобрения оно будет размещено на канале. Я сообщую об этом.";
            String adminText = "Поступило новое объявление : " + userAccommodation.getDescription() + "\n Открой кабинет администратора для обработки. /start";
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

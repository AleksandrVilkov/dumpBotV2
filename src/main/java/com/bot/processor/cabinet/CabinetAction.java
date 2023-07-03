package com.bot.processor.cabinet;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.ICarStorage;
import com.bot.processor.IUserStorage;
import com.bot.processor.common.CarOperation;
import com.bot.processor.common.ProcessorUtil;
import com.bot.processor.registration.RegistrationHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
@Slf4j
public class CabinetAction implements Action {
    @Autowired
    ICarStorage carStorage;

    @Autowired
    IUserStorage userStorage;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        switch (tempObject.getOperation()) {
            case START -> {
                return start(user, tempObject, update);
            }
            case EDIT_CAR -> {
                return editCar(user, tempObject, update);
            }
            case BRAND_SELECTION -> {
                return brandSelection(user, tempObject, update);
            }
            case MODEL_SELECTION -> {
                return modelSelection(update, tempObject);
            }
            case ENGINE_SELECTION -> {
                return engineSelection(update, tempObject);
            }

            case MY_ACCOMMODATION -> {
                return getMyAccommodation(user, tempObject, update);
            }
            case END -> {
                return end(user, update, tempObject);
            }
        }
        return null;
    }

    private MessageWrapper end(User user, Update update, TempObject tempObject) {
        if (tempObject.getOption().getCarList().size() != 1) {
            return CommonMsgs.createCommonError(update);
        }
        user.setCarId(tempObject.getOption().getCarList().get(0).getId());
        if (userStorage.saveUser(user)) {
            String text = "Отлично, данные обновлены!";
            return ProcessorUtil.createMessages(text, update);
        } else {
            log.error("error saving user " + Util.getUserId(update));
            return CommonMsgs.createCommonError(update);
        }
    }

    private MessageWrapper engineSelection(Update update, TempObject tempObject) {
        return CarOperation.chooseEngine(update, tempObject, Operation.END);
    }

    private MessageWrapper modelSelection(Update update, TempObject tempObject) {
        return CarOperation.chooseModel(update, tempObject, Operation.ENGINE_SELECTION);
    }

    private MessageWrapper brandSelection(User user, TempObject tempObject, Update update) {
        return CarOperation.chooseBrand(update, tempObject, Operation.MODEL_SELECTION);
    }

    private MessageWrapper getMyAccommodation(User user, TempObject tempObject, Update update) {
        return null;
    }

    private MessageWrapper editCar(User user, TempObject tempObject, Update update) {
        Car car = carStorage.getCarById(user.getCarId());
        String text = "На текущий момент у тебя выбран: " +
                car.getBrand() + " " + car.getModel() + " (" + car.getEngine() + ")"
                + "Что бы изменить - начни выбирать авто по кнопке ниже";

        List<Car> cars = carStorage.getCars();
        Map<String, List<Car>> carByConcern = new HashMap<>();
        cars.forEach(
                c -> {
                    String concern = c.getConcern().getName();
                    if (carByConcern.containsKey(concern)) {
                        carByConcern.get(concern).add(c);
                    } else {
                        List<Car> carBrands = new ArrayList<>();
                        carBrands.add(c);
                        carByConcern.put(concern, carBrands);
                    }
                }
        );
        List<SendMessage> sendMessageList = new ArrayList<>();
        sendMessageList.add(new SendMessage(Util.getUserId(update), text));
        MessageWrapper messageWrapper = CarOperation.chooseConcern(update, tempObject, cars, Operation.BRAND_SELECTION);
        sendMessageList.addAll(messageWrapper.getSendMessage());
        messageWrapper.setSendMessage(sendMessageList);
        return messageWrapper;
    }

    private MessageWrapper start(User user, TempObject tempObject, Update update) {
        TempObject editCar = tempObject.clone();
        editCar.setOperation(Operation.EDIT_CAR);
        TempObject userAcc = tempObject.clone();
        userAcc.setOperation(Operation.MY_ACCOMMODATION);

        List<ButtonWrapper> buttons = new ArrayList<>();
        buttons.add(new ButtonWrapper("Изменить авто", Util.generateToken(editCar), editCar));
        buttons.add(new ButtonWrapper("Мои запросы", Util.generateToken(userAcc), userAcc));
        return ProcessorUtil.createMessages("Выбери действие:", update, buttons);
    }
}

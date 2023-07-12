package com.bot.processor.cabinet.subActions;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.model.Operations;
import com.bot.processor.ICarStorage;
import com.bot.processor.SubAction;
import com.bot.processor.common.CarOperation;
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
public class CabinetEditCar implements SubAction {
    @Autowired
    private ICarStorage carStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
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
        MessageWrapper messageWrapper = CarOperation.chooseConcern(update, tempObject, cars, Operations.BRAND_SELECTION);
        sendMessageList.addAll(messageWrapper.getSendMessage());
        messageWrapper.setSendMessage(sendMessageList);
        return messageWrapper;
    }
}

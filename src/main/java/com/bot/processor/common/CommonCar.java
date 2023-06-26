package com.bot.processor.common;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.ITempStorage;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;

@Component
public class CommonCar {
    public static MessageWrapper chooseEngine(Update update, TempObject tempObject, ITempStorage tempStorage, Operation step) {
        String text = "Укажи двигатель:";

        Map<String, List<Car>> result = new HashMap<>();
        tempObject.getOption().getCarList().forEach(
                car -> {
                    String engine = car.getEngine().getName();
                    if (result.containsKey(engine)) {
                        result.get(engine).add(car);
                    } else {
                        List<Car> carEngine = new ArrayList<>();
                        carEngine.add(car);
                        result.put(engine, carEngine);
                    }
                }
        );

        Map<String, String> data = getCarData(tempObject, result, step,tempStorage);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }
    public static MessageWrapper chooseModel(Update update, TempObject tempObject, ITempStorage tempStorage, Operation step) {
        String text = "Теперь выбери модель:";

        Map<String, List<Car>> result = new HashMap<>();
        tempObject.getOption().getCarList().forEach(
                car -> {
                    String model = car.getModel().getName();
                    if (result.containsKey(model)) {
                        result.get(model).add(car);
                    } else {
                        List<Car> carModel = new ArrayList<>();
                        carModel.add(car);
                        result.put(model, carModel);
                    }
                }
        );

        Map<String, String> data = getCarData(tempObject, result, step, tempStorage);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    public static MessageWrapper chooseBrand(Update update, TempObject tempObject, ITempStorage tempStorage, Operation step) {
        String text = "Теперь выбери бренд:";

        Map<String, List<Car>> result = new HashMap<>();
        tempObject.getOption().getCarList().forEach(
                car -> {
                    String brand = car.getBrand().getName();
                    if (result.containsKey(brand)) {
                        result.get(brand).add(car);
                    } else {
                        List<Car> carBrands = new ArrayList<>();
                        carBrands.add(car);
                        result.put(brand, carBrands);
                    }
                }
        );

        Map<String, String> data = getCarData(tempObject, result, step, tempStorage);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    public static MessageWrapper chooseConcern(Update update, TempObject tempObject, List<Car> cars, ITempStorage tempStorage, Operation step) {
        String text = "Выбери концерн, к которому относится твой автомобиль:";
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

        Map<String, String> data = getCarData(tempObject, carByConcern, step, tempStorage);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    public static List<String> getCarsId(List<Car> cars) {
        List<String> ids = new ArrayList<>();
        cars.forEach(car -> ids.add(String.valueOf(car.getId())));
        return ids;
    }


    private static MessageWrapper createMessages(String text, Update update, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setChatId(Util.getUserId(update));
        return MessageWrapper.builder().sendMessage(Collections.singletonList(sendMessage)).build();
    }

    private static Map<String, String> getCarData(TempObject tempObject,
                                                  Map<String, List<Car>> result,
                                                  Operation step, ITempStorage tempStorage) {
        Map<String, String> data = new HashMap<>();

        for (Map.Entry<String, List<Car>> entry : result.entrySet()) {
            TempObject newTemp = tempObject.clone();
            if (newTemp.getOption() != null) {
                newTemp.getOption().getCarList().clear();
                newTemp.getOption().getCarList().addAll(entry.getValue());
                newTemp.getOption().setCarValue(entry.getKey());
            } else {
                OptionData optionData = new OptionData();
                optionData.setCarList(entry.getValue());
                optionData.setCarValue(entry.getKey());
                newTemp.setOption(optionData);
            }
            newTemp.setOperation(step);
            String key = getKeyAndSaveTemp(newTemp, tempStorage);
            data.put(entry.getKey(), key);
        }
        return data;
    }

    private static String getKeyAndSaveTemp(TempObject newTemp, ITempStorage tempStorage) {
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        return key;
    }
}

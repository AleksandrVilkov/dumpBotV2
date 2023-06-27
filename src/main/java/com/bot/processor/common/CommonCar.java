package com.bot.processor.common;

import com.bot.common.Util;
import com.bot.model.*;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
public class CommonCar {
    public static MessageWrapper chooseEngine(Update update, TempObject tempObject, Operation step) {
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

        List<ButtonWrapper> data = getCarData(tempObject, result, step);
        return ProcessorUtil.createMessages(text, update, data);
    }

    public static MessageWrapper chooseModel(Update update, TempObject tempObject, Operation step) {
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

        List<ButtonWrapper> data = getCarData(tempObject, result, step);
        return ProcessorUtil.createMessages(text, update, data);
    }

    public static MessageWrapper chooseBrand(Update update, TempObject tempObject, Operation step) {
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

        List<ButtonWrapper> data = getCarData(tempObject, result, step);
        return ProcessorUtil.createMessages(text, update, data);
    }

    public static MessageWrapper chooseConcern(Update update, TempObject tempObject, List<Car> cars, Operation step) {
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

        List<ButtonWrapper> buttonWrappers = getCarData(tempObject, carByConcern, step);
        return ProcessorUtil.createMessages(text, update, buttonWrappers);
    }

    public static List<String> getCarsId(List<Car> cars) {
        List<String> ids = new ArrayList<>();
        cars.forEach(car -> ids.add(String.valueOf(car.getId())));
        return ids;
    }


    private static List<ButtonWrapper> getCarData(TempObject tempObject,
                                                  Map<String, List<Car>> data,
                                                  Operation step) {
        List<ButtonWrapper> result = new ArrayList<>();

        data.forEach((key, value) -> {
            TempObject newTemp = tempObject.clone();
            if (newTemp.getOption() != null) {
                newTemp.getOption().getCarList().clear();
                newTemp.getOption().getCarList().addAll(value);
                newTemp.getOption().setCarValue(key);
            } else {
                OptionData optionData = new OptionData();
                optionData.setCarList(value);
                optionData.setCarValue(key);
                newTemp.setOption(optionData);
            }
            newTemp.setOperation(step);
            String tempKey = Util.generateToken(newTemp);
            result.add(new ButtonWrapper(key, tempKey, newTemp));
        });
        return result;
    }
}

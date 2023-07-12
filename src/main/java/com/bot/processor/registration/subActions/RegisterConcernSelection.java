package com.bot.processor.registration.subActions;

import com.bot.model.Car;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.ICarStorage;
import com.bot.processor.SubAction;
import com.bot.processor.common.CarOperation;
import com.bot.model.Operations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class RegisterConcernSelection implements SubAction {
    @Autowired
    private ICarStorage carStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {
        tempObject.getSelectedData().setRegion(tempObject.getOption().getRegion());
    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        List<Car> cars = carStorage.getCars();
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
}

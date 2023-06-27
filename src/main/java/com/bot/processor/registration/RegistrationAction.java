package com.bot.processor.registration;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.ICarStorage;
import com.bot.processor.IRegionStorage;
import com.bot.processor.IUserStorage;
import com.bot.processor.common.CarOperation;
import com.bot.processor.common.ProcessorUtil;
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
public class RegistrationAction implements Action {
    @Autowired
    IRegionStorage regionStorage;
    @Autowired
    ICarStorage carStorage;
    @Autowired
    IUserStorage userStorage;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject) {
        final String ACTION_NAME = "REGISTRATION";
        //Определяем шаг
        //TODO переделаем на энам
        switch (tempObject.getOperation()) {
            case START -> {
                //Выбор страны
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }
            case CITY_SELECTION -> {
                //Выбор города
                log.info("Start CITY_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return secondStep(update, tempObject);
            }
            case CONCERN_SELECTION -> {
                //Выбор концерна
                log.info("Start CONCERN_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return thirdStep(update, tempObject);
            }
            case BRAND_SELECTION -> {
                //выбор бренда
                log.info("Start BRAND_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fourthStep(update, tempObject);
            }
            case MODEL_SELECTION -> {
                //выбор модели
                log.info("Start MODEL_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fifthStep(update, tempObject);
            }
            case ENGINE_SELECTION -> {
                //выбор двигателя
                log.info("Start ENGINE_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return sixthStep(update, tempObject);
            }
            case END -> {
                //завершение
                log.info("Start END " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return seventhStep(update, tempObject);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private MessageWrapper firstStep(Update update, TempObject tempObject) {
        String text = "Выбери страну:";
        List<String> countries = regionStorage.getCountries();
        List<ButtonWrapper> buttons = new ArrayList<>();
        for (String c : countries) {
            TempObject newTemp = tempObject.clone();
            OptionData optionData = new OptionData();
            optionData.setCountryCode(c);
            newTemp.setOption(optionData);
            newTemp.setOperation(Operation.CITY_SELECTION);
            buttons.add(new ButtonWrapper(c, Util.generateToken(newTemp), newTemp));
        }
        MessageWrapper messageWrapper = ProcessorUtil.createMessages(text, update, buttons);
        messageWrapper.setButtons(buttons);
        return messageWrapper;
    }

    private MessageWrapper secondStep(Update update, TempObject tempObject) {
        String text = "Выбери город:";
        confirmCountrySelection(tempObject); //Запоминаем выбо страны
        String countryCode = tempObject.getSelectedData().getCountryCode();

        int commonRegionCount = regionStorage.countAllByCountryCode(countryCode);
        int countRegionsOnPage = 8;

        Navigation navigation = tempObject.getNavigation();

        int currentPage = RegistrationHelper.defineCurrentPage(navigation);
        int nextPage = RegistrationHelper.defineNextPage(navigation, currentPage);

        List<Region> regions = regionStorage.getRegionPageByCountryCode(countryCode,
                nextPage == 0 ? 0 : nextPage - 1, countRegionsOnPage, true);

        List<ButtonWrapper> buttons = getRegionButtons(regions, tempObject);
        if (currentPage != 0) {
            buttons.add(RegistrationHelper.createBackBntData(tempObject, nextPage));
        }
        if ((currentPage + 1) * countRegionsOnPage < commonRegionCount || (navigation != null && navigation.isBack())) {
            buttons.add(RegistrationHelper.createNextBntData(tempObject, nextPage));
        }


        return ProcessorUtil.createMessages(text, update, buttons);
    }

    private MessageWrapper thirdStep(Update update, TempObject tempObject) {
        confirmRegionSelection(tempObject);
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
        return CarOperation.chooseConcern(update, tempObject, cars, Operation.BRAND_SELECTION);
    }

    private MessageWrapper fourthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseBrand(update, tempObject, Operation.MODEL_SELECTION);
    }

    private MessageWrapper fifthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseModel(update, tempObject, Operation.ENGINE_SELECTION);
    }

    private MessageWrapper sixthStep(Update update, TempObject tempObject) {
        return CarOperation.chooseEngine(update, tempObject, Operation.END);
    }

    private MessageWrapper seventhStep(Update update, TempObject tempObject) {
        if (tempObject.getOption().getCarList().size() != 1) {
            return CommonMsgs.createCommonError(update);
        }
        ProcessorUtil.confirmCarSelection(tempObject);
        User user = RegistrationHelper.createUser(tempObject, update);
        if (userStorage.saveUser(user)) {
            String text = "Отлично, ты зарегистрирован!";
            return ProcessorUtil.createMessages(text, update);
        } else {
            log.error("error saving user " + Util.getUserId(update));
            return CommonMsgs.createCommonError(update);
        }
    }


    private List<ButtonWrapper> getRegionButtons(List<Region> regions, TempObject tempObject) {
        List<ButtonWrapper> res = new ArrayList<>();
        for (Region region : regions) {
            TempObject newTemp = tempObject.clone();
            newTemp.getOption().setRegion(region);
            newTemp.setOperation(Operation.CONCERN_SELECTION);
            String key = Util.generateToken(newTemp);
            res.add(new ButtonWrapper(region.getName(), key, newTemp));
        }
        return res;
    }


    private void confirmCountrySelection(TempObject tempObject) {
        if (tempObject.getSelectedData() == null) {
            SelectedData selectedData = new SelectedData();
            tempObject.setSelectedData(selectedData);
        }
        tempObject.getSelectedData().setCountryCode(tempObject.getOption().getCountryCode());
    }


    private void confirmRegionSelection(TempObject tempObject) {
        tempObject.getSelectedData().setRegion(tempObject.getOption().getRegion());
    }
}

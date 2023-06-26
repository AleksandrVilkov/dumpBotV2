package com.bot.processor.registration;

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
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
@Slf4j
public class RegistrationAction implements Action {
    @Autowired
    IRegionStorage regionStorage;
    @Autowired
    ICarStorage carStorage;
    @Autowired
    ITempStorage tempStorage;
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
        Map<String, String> datas = new HashMap<>();
        for (String c : countries) {
            TempObject newTemp = tempObject.clone();
            OptionData optionData = new OptionData();
            optionData.setCountryCode(c);
            newTemp.setOption(optionData);
            newTemp.setOperation(Operation.CITY_SELECTION);
            String key = ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage);
            datas.put(c, key);
        }
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(datas));
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

        List<Region> regions = regionStorage.getRegionPageByCountryCode(countryCode, nextPage == 0 ? 0 : nextPage - 1, countRegionsOnPage, true);

        Map<String, String> navi = new LinkedHashMap<>();
        Map<String, String> datas = getRegionData(regions, tempObject);
        if (currentPage != 0) {
            navi.putAll(RegistrationHelper.createBackBntData(tempObject, nextPage, tempStorage));
        }
        if ((currentPage + 1) * countRegionsOnPage < commonRegionCount || (navigation != null && navigation.isBack())) {
            navi.putAll(RegistrationHelper.createNextBntData(tempObject, nextPage, tempStorage));
        }
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardWithNavi(datas, navi));
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
        return CommonCar.chooseConcern(update, tempObject, cars, tempStorage, Operation.BRAND_SELECTION);
    }

    private MessageWrapper fourthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseBrand(update, tempObject, tempStorage, Operation.MODEL_SELECTION);
    }

    private MessageWrapper fifthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseModel(update, tempObject, tempStorage, Operation.ENGINE_SELECTION);
    }

    private MessageWrapper sixthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseEngine(update, tempObject, tempStorage, Operation.END);
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


    private Map<String, String> getRegionData(List<Region> regions, TempObject tempObject) {
        Map<String, String> datas = new HashMap<>();
        for (Region region : regions) {
            TempObject newTemp = tempObject.clone();
            newTemp.getOption().setRegion(region);
            newTemp.setOperation(Operation.CONCERN_SELECTION);
            String key = ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage);
            datas.put(region.getName(), key);
        }
        return datas;
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

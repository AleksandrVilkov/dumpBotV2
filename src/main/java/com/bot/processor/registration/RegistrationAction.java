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
    public List<SendMessage> execute(Update update, TempObject tempObject) {
        final String ACTION_NAME = "REGISTRATION";
        //Определяем шаг
        //TODO переделаем на энам
        switch (tempObject.getStep()) {
            case 0 -> {
                //Выбор страны
                log.info("Start 0 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }
            case 1 -> {
                //Выбор города
                log.info("Start 1 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return secondStep(update, tempObject);
            }
            case 2 -> {
                //Выбор концерна
                log.info("Start 2 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return thirdStep(update, tempObject);
            }
            case 3 -> {
                //выбор бренда
                log.info("Start 3 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fourthStep(update, tempObject);
            }
            case 4 -> {
                //выбор модели
                log.info("Start 4 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fifthStep(update, tempObject);
            }
            case 5 -> {
                //выбор двигателя
                log.info("Start 5 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return sixthStep(update, tempObject);
            }
            case 6 -> {
                //завершение
                log.info("Start 6 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return seventhStep(update, tempObject);
            }
            //выбор модели
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private List<SendMessage> firstStep(Update update, TempObject tempObject) {
        String text = "Выбери страну:";
        List<String> countries = regionStorage.getCountries();
        Map<String, String> datas = new HashMap<>();
        for (String c : countries) {
            TempObject newTemp = tempObject.clone();
            OptionData optionData = new OptionData();
            optionData.setCountryCode(c);
            newTemp.setOption(optionData);
            newTemp.setStep(1);
            String key = ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage);
            datas.put(c, key);
        }
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(datas));
    }

    private List<SendMessage> secondStep(Update update, TempObject tempObject) {
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

    private List<SendMessage> thirdStep(Update update, TempObject tempObject) {
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
        return CommonCar.chooseConcern(update, tempObject, cars, tempStorage, 3);
    }

    private List<SendMessage> fourthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseBrand(update, tempObject, tempStorage, 4);
    }

    private List<SendMessage> fifthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseModel(update, tempObject, tempStorage, 5);
    }

    private List<SendMessage> sixthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseEngine(update, tempObject, tempStorage, 6);
    }

    private List<SendMessage> seventhStep(Update update, TempObject tempObject) {
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
            newTemp.setStep(2);
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

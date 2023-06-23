package com.bot.processor.registration;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.*;
import com.bot.processor.Action;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import java.util.*;

@Component
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
        //Определяем шаг
        //TODO переделаем на энам
        switch (tempObject.getStep()) {
            case 0 -> {
                //Выбор страны
                return firstStep(update, tempObject);
            }
            case 1 -> {
                //Выбор города
                return secondStep(update, tempObject);
            }
            case 2 -> {
                //Выбор концерна
                return thirdStep(update, tempObject);
            }
            case 3 -> {
                //выбор бренда
                return fourthStep(update, tempObject);
            }
            case 4 -> {
                //выбор модели
                return fifthStep(update, tempObject);
            }
            case 5 -> {
                //выбор двигателя
                return sixthStep(update, tempObject);
            }
            case 6 -> {
                //завершение
                return seventhStep(update, tempObject);
            }
            //выбор модели
            default -> {
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
            String key = getKeyAndSaveTemp(newTemp);
            datas.put(c, key);
        }
        return createMessages(text, update, Util.createKeyboardOneBtnLine(datas));
    }

    private List<SendMessage> secondStep(Update update, TempObject tempObject) {
        String text = "Выбери город:";
        confirmCountrySelection(tempObject); //Запоминаем выбо страны
        String countryCode = tempObject.getSelectedData().getSelectedRegistrationData().getCountryCode();

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
        return createMessages(text, update, Util.createKeyboardWithNavi(datas, navi));
    }

    private List<SendMessage> thirdStep(Update update, TempObject tempObject) {
        confirmRegionSelection(tempObject);
        String text = "Выбери концерн, к которому относится твой автомобиль:";
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

        Map<String, String> data = getCarData(tempObject, carByConcern, 3);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> fourthStep(Update update, TempObject tempObject) {
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

        Map<String, String> data = getCarData(tempObject, result, 4);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> fifthStep(Update update, TempObject tempObject) {
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

        Map<String, String> data = getCarData(tempObject, result, 5);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> sixthStep(Update update, TempObject tempObject) {
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

        Map<String, String> data = getCarData(tempObject, result, 6);
        return createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> seventhStep(Update update, TempObject tempObject) {
        if (tempObject.getOption().getCarList().size() != 1) {
            return CommonMsgs.createCommonError(update);
        }
        confirmCarSelection(tempObject);
        User user = RegistrationHelper.createUser(tempObject,update);
       if (userStorage.saveUser(user)) {
           String text = "Отлично, ты зарегистрирован!";
           return createMessages(text, update);
       } else {
           return CommonMsgs.createCommonError(update);
       }
    }



    private Map<String, String> getRegionData(List<Region> regions, TempObject tempObject) {
        Map<String, String> datas = new HashMap<>();
        for (Region region : regions) {
            TempObject newTemp = tempObject.clone();
            newTemp.getOption().setRegion(region);
            newTemp.setStep(2);
            String key = getKeyAndSaveTemp(newTemp);
            datas.put(region.getName(), key);
        }
        return datas;
    }

    private Map<String, String> getCarData(TempObject tempObject, Map<String, List<Car>> result, int step) {
        Map<String, String> data = new HashMap<>();

        for (Map.Entry<String, List<Car>> entry : result.entrySet()) {
            TempObject newTemp = tempObject.clone();
            newTemp.getOption().getCarList().clear();
            newTemp.getOption().getCarList().addAll(entry.getValue());
            newTemp.getOption().setCarValue(entry.getKey());
            newTemp.setStep(step);
            String key = getKeyAndSaveTemp(newTemp);
            data.put(entry.getKey(), key);
        }
        return data;
    }

    private String getKeyAndSaveTemp(TempObject newTemp) {
        String key = Util.generateToken(newTemp);
        tempStorage.set(key, newTemp.toString());
        return key;
    }

    private List<SendMessage> createMessages(String text, Update update, InlineKeyboardMarkup inlineKeyboardMarkup) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setReplyMarkup(inlineKeyboardMarkup);
        sendMessage.setChatId(Util.getUserId(update));
        return Collections.singletonList(sendMessage);
    }
    private List<SendMessage> createMessages(String text, Update update) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(text);
        sendMessage.setChatId(Util.getUserId(update));
        return Collections.singletonList(sendMessage);
    }

    private void confirmCountrySelection(TempObject tempObject) {
        if (tempObject.getSelectedData() == null) {
            SelectedData selectedData = new SelectedData();
            SelectedRegistrationData srd = new SelectedRegistrationData();
            selectedData.setSelectedRegistrationData(srd);
            tempObject.setSelectedData(selectedData);
        }
        tempObject.getSelectedData().getSelectedRegistrationData().setCountryCode(tempObject.getOption().getCountryCode());
    }

    private void confirmCarSelection(TempObject tempObject) {
        tempObject.getSelectedData().getSelectedRegistrationData().setCars(tempObject.getOption().getCarList());
    }

    private void confirmRegionSelection(TempObject tempObject) {
        tempObject.getSelectedData().getSelectedRegistrationData().setRegion(tempObject.getOption().getRegion());
    }
}

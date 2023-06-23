package com.bot.processor.sale;

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
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

@Component
@Slf4j
public class SaleAction implements Action {
    @Autowired
    ITempStorage tempStorage;
    @Autowired
    ICarStorage carStorage;

    @Autowired
    IUserStorage userStorage;

    @Autowired
    IAccommodationStorage accommodationStorage;

    @Override
    public List<SendMessage> execute(Update update, TempObject tempObject) {
        final String ACTION_NAME = "SALE";
        switch (tempObject.getStep()) {
            case 0 -> {
                log.info("Start 0 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }
            case 1 -> {
                log.info("Start 1 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return secondStep(update, tempObject);
            }
            case 2 -> {
                log.info("Start 2 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return thirdStep(update, tempObject);
            }
            case 3 -> {
                log.info("Start 3 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return fourthStep(update, tempObject);
            }
            case 4 -> {
                log.info("Start 4 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return fifthStep(update, tempObject);
            }
            case 5 -> {
                log.info("Start 5 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return sixthStep(update, tempObject);
            }
            case 6 -> {
                log.info("Start 6 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return seventhStep(update, tempObject);
            }
            case 7 -> {
                log.info("Start 7 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return eighthStep(update, tempObject);
            }
            case 8 -> {
                log.info("Start 7 "+ACTION_NAME+" step for user " + Util.getUserId(update));
                return ninthStep(update, tempObject);
            }
            default -> {
                log.error("Cannot find step number in "+ACTION_NAME+" for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private List<SendMessage> firstStep(Update update, TempObject tempObject) {
        int step = 1;
        String text = "Давай разместим с тобой объявление. Нужно будет указать машины, на которые подходит деталь, приложить фото, указать описание и цену. Нажми на кнопку начать:";
        TempObject newTemp = tempObject.clone();
        newTemp.setStep(step);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Util.getUserId(update));
        Map<String, String> data = new HashMap<>();
        data.put("Начать", ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> secondStep(Update update, TempObject tempObject) {
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
        return CommonCar.chooseConcern(update, tempObject, cars, tempStorage, 2);
    }

    private List<SendMessage> thirdStep(Update update, TempObject tempObject) {
        return CommonCar.chooseBrand(update, tempObject, tempStorage, 3);
    }

    private List<SendMessage> fourthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseModel(update, tempObject, tempStorage, 4);
    }

    private List<SendMessage> fifthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseEngine(update, tempObject, tempStorage, 5);
    }

    private List<SendMessage> sixthStep(Update update, TempObject tempObject) {
        if (tempObject.getOption().getCarList().size() != 1) {
            log.error("CarList size > 1!");
            return CommonMsgs.createCommonError(update);
        }
        Car car = tempObject.getOption().getCarList().get(0);
        ProcessorUtil.confirmCarSelection(tempObject);
        String text = "Отлично. Автомобиль " + car.getBrand() + " " + car.getModel() + "(" + car.getEngine() + ")" + "успешно выбран. \n Что дальлше?";

        Map<String, String> data = new HashMap<>();
        TempObject tempCarElse = tempObject.clone();
        tempCarElse.setStep(1);

        TempObject tempPhoto = tempObject.clone();
        tempPhoto.setStep(6);
        data.put("Добавить фото", ProcessorUtil.getKeyAndSaveTemp(tempPhoto, tempStorage));
        data.put("Добавить авто", ProcessorUtil.getKeyAndSaveTemp(tempCarElse, tempStorage));
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> seventhStep(Update update, TempObject tempObject) {
        User user = userStorage.getUser(Util.getUserId(update));
        String text;
        TempObject newTemp = tempObject.clone();
        if (user.isWaitingMessages()) {
            List<PhotoSize> photoSizes = update.getMessage().getPhoto();
            Set<String> photoIds = new HashSet<>();

            for (PhotoSize photoSize : photoSizes) {
                //TODO проверять уникальность
                photoIds.add(photoSize.getFileId());
            }
            if (newTemp.getSelectedData().getPhotos() == null) {
                newTemp.getSelectedData().setPhotos(new ArrayList<>());
            }
            newTemp.getSelectedData().getPhotos().addAll(photoIds);
            user.setLastCallback(ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));

            String buttonName = "Готово";
            text = "Отлично. Если есть еще фото - дай их мне. Если нет - нажми кнопку " + buttonName;
            Map<String, String> data = new HashMap<>();
            newTemp.setStep(7);
            data.put(buttonName, ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));
            return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(data));
        } else {
            text = "Пришли мне фотографии. Фото можно прислать как по одному, так и группой.";
            user.setWaitingMessages(true);
            user.setLastCallback(ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));
        }
        userStorage.saveUser(user);
        return ProcessorUtil.createMessages(text, update);
    }

    private List<SendMessage> eighthStep(Update update, TempObject tempObject) {
        String text = "Укажи описание к объявлению. Опиши товар, не забудь обязательно указать цену! Пиши так, что бы твой товар захотели купить!";
        User user = userStorage.getUser(Util.getUserId(update));
        TempObject newTemp = tempObject.clone();
        newTemp.setStep(8);
        user.setWaitingMessages(true);
        String key = ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage);
        user.setLastCallback(key);
        userStorage.saveUser(user);
        return ProcessorUtil.createMessages(text, update);
    }

    private List<SendMessage> ninthStep(Update update, TempObject tempObject) {
        TempObject newTemp = tempObject.clone();
        String description = update.getMessage().getText();
        User user = userStorage.getUser(Util.getUserId(update));
        UserAccommodation userAccommodation = UserAccommodation.builder()
                .type(AccommodationType.SALE)
                .createdDate(new Date())
                .clientLogin(user.getLogin())
                .clientId(user.getId())
                .approved(false)
                .rejected(false)
                .topical(true)
                .description(description)
                .photos(newTemp.getSelectedData().getPhotos())
                .carsId(getCarsId(tempObject.getSelectedData().getCars()))
                .build();

        if (accommodationStorage.saveAccommodation(userAccommodation)) {
            List<User> admins = userStorage.findAdmins();
            String userText = "Отлично, твое обьявление отправлено на модерацию. После одобрения оно будет размещено на канале. Я сообщую об этом.";
            String adminText = "Поступило новое объявление : " + userAccommodation.getDescription() + "\n Открой кабинет администратора для обработки. /start";
            List<SendMessage> result = new ArrayList<>();
            result.add(new SendMessage(Util.getUserId(update), userText));
            for (User admin : admins) {
                result.add(new SendMessage(admin.getLogin(), adminText));
            }

            user.setWaitingMessages(false);
            user.setLastCallback(null);
            userStorage.saveUser(user);
            return result;
        } else {
            log.error("error saving user accommodation for user " + Util.getUserId(update));
            return CommonMsgs.createCommonError(update);
        }
    }

    private List<String> getCarsId(List<Car> cars) {
        List<String> ids = new ArrayList<>();
        cars.forEach(car -> ids.add(String.valueOf(car.getId())));
        return ids;
    }
}

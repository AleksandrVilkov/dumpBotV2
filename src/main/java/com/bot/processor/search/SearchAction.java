package com.bot.processor.search;

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
public class SearchAction implements Action {
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
        final String ACTION_NAME = "SEARCH";
        switch (tempObject.getOperation()) {
            case START -> {
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }
            case NEED_CAR -> {
                log.info("Start NEED_CAR " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return secondStep(update, tempObject);
            }
            case CONCERN_SELECTION -> {
                log.info("Start CONCERN_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return thirdStep(update, tempObject);
            }
            case BRAND_SELECTION -> {
                log.info("Start BRAND_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fourthStep(update, tempObject);
            }
            case MODEL_SELECTION -> {
                log.info("Start MODEL_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return fifthStep(update, tempObject);
            }
            case ENGINE_SELECTION -> {
                log.info("Start ENGINE_SELECTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return sixthStep(update, tempObject);
            }
            case PRE_PHOTO -> {
                log.info("Start PRE_PHOTO " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return seventhStep(update, tempObject);
            }
            case PHOTO -> {
                log.info("Start PHOTO " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return eighthStep(update, tempObject);
            }
            case DESCRIPTION -> {
                log.info("Start DESCRIPTION " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return ninthStep(update, tempObject);
            }
            case END -> {
                log.info("Start 9 " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return tenthStep(update, tempObject);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }

    private List<SendMessage> firstStep(Update update, TempObject tempObject) {
        int step = 1;
        String text = "Давай разместим запрос на поиск. Нажми кнопку \"Начать\"";
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operation.NEED_CAR);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Util.getUserId(update));
        Map<String, String> data = new HashMap<>();
        data.put("Начать", ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> secondStep(Update update, TempObject tempObject) {
        String text = "Хочешь указать пренадлежность детали к автомобилю? Если указать - то большее количество пользователей получат уведомление, и шанс найти то что ты ищешь - возрастает.";

        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operation.CONCERN_SELECTION);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(Util.getUserId(update));
        Map<String, String> data = new HashMap<>();
        data.put("Указать авто", ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage));

        TempObject newTemp1 = tempObject.clone();
        newTemp1.setOperation(Operation.PRE_PHOTO);
        data.put("Не указывать", ProcessorUtil.getKeyAndSaveTemp(newTemp1, tempStorage));
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> thirdStep(Update update, TempObject tempObject) {
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
        return CommonCar.chooseConcern(update, tempObject, cars, tempStorage, Operation.BRAND_SELECTION);
    }

    private List<SendMessage> fourthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseBrand(update, tempObject, tempStorage, Operation.MODEL_SELECTION);
    }

    private List<SendMessage> fifthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseModel(update, tempObject, tempStorage, Operation.ENGINE_SELECTION);
    }

    private List<SendMessage> sixthStep(Update update, TempObject tempObject) {
        return CommonCar.chooseEngine(update, tempObject, tempStorage, Operation.PRE_PHOTO);
    }

    private List<SendMessage> seventhStep(Update update, TempObject tempObject) {
        //TODO тут может быть не выбраны автомобили. Нужно предусмотреть.

        Map<String, String> data = new HashMap<>();
        String text;
        if (tempObject.getOption() != null && tempObject.getOption().getCarList() != null && !tempObject.getOption().getCarList().isEmpty()) {
            Car car = tempObject.getOption().getCarList().get(0);
            ProcessorUtil.confirmCarSelection(tempObject);
            text = "Отлично. Автомобиль " + car.getBrand() + " " + car.getModel() + "(" + car.getEngine() + ")" + "успешно выбран. \n Что дальше?";
            TempObject tempCarElse = tempObject.clone();
            tempCarElse.setOperation(Operation.CONCERN_SELECTION);
            data.put("Добавить авто", ProcessorUtil.getKeyAndSaveTemp(tempCarElse, tempStorage));

        } else {
            text = "Хорошо, продолжим без указания авто. Давай решим, будем мы прикладывать фото искомой запчасти? Имей ввиду, это так же повысит возможность найти то, что ты ищешь";
        }
        TempObject tempPhoto = tempObject.clone();
        tempPhoto.setOperation(Operation.PHOTO);
        data.put("Добавить фото", ProcessorUtil.getKeyAndSaveTemp(tempPhoto, tempStorage));

        TempObject tempNoPhoto = tempObject.clone();
        tempNoPhoto.setOperation(Operation.DESCRIPTION);
        data.put("Без фото", ProcessorUtil.getKeyAndSaveTemp(tempNoPhoto, tempStorage));
        return ProcessorUtil.createMessages(text, update, Util.createKeyboardOneBtnLine(data));
    }

    private List<SendMessage> eighthStep(Update update, TempObject tempObject) {
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
            newTemp.setOperation(Operation.DESCRIPTION);
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

    private List<SendMessage> ninthStep(Update update, TempObject tempObject) {
        String text = "Укажи описание к объявлению. Опиши то, что ты ищешь:";
        User user = userStorage.getUser(Util.getUserId(update));
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operation.END);
        user.setWaitingMessages(true);
        String key = ProcessorUtil.getKeyAndSaveTemp(newTemp, tempStorage);
        user.setLastCallback(key);
        userStorage.saveUser(user);
        return ProcessorUtil.createMessages(text, update);
    }

    private List<SendMessage> tenthStep(Update update, TempObject tempObject) {
        TempObject newTemp = tempObject.clone();
        String description = update.getMessage().getText();
        User user = userStorage.getUser(Util.getUserId(update));
        UserAccommodation.UserAccommodationBuilder builder = UserAccommodation.builder()
                .type(AccommodationType.SALE)
                .createdDate(new Date())
                .clientLogin(user.getLogin())
                .clientId(user.getId())
                .approved(false)
                .rejected(false)
                .topical(true)
                .description(description);

        if (newTemp.getSelectedData() != null) {
            builder.photos(newTemp.getSelectedData().getPhotos())
                    .carsId(CommonCar.getCarsId(tempObject.getSelectedData().getCars()));
        }
        UserAccommodation userAccommodation = builder.build();
        if (accommodationStorage.saveAccommodation(userAccommodation)) {
            List<User> admins = userStorage.findAdmins();
            String userText = "Отлично, твой запрос отправлен на модерацию. После одобрения он будет размещен на канале. Я сообщую об этом.";
            String adminText = "Поступил новый запрос на поиски детали: " + userAccommodation.getDescription() + "\n Открой кабинет администратора для обработки. /start";
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
}

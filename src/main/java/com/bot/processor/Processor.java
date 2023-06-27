package com.bot.processor;

import com.bot.bot.IProcessor;
import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.Action;
import com.bot.model.*;
import com.bot.processor.admin.AdminAction;
import com.bot.processor.cabinet.CabinetAction;
import com.bot.processor.registration.RegistrationAction;
import com.bot.processor.sale.SaleAction;
import com.bot.processor.search.SearchAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class Processor implements IProcessor {
    @Autowired
    IUserStorage userStorage;
    @Autowired
    ITempStorage tempStorage;
    @Autowired
    RegistrationAction registrationAction;
    @Autowired
    SaleAction saleAction;
    @Autowired
    SearchAction searchAction;
    @Autowired
    CabinetAction cabinetAction;
    @Autowired
    AdminAction adminAction;

    @Override
    public MessageWrapper startProcessing(Update update) {
        String userId = Util.getUserId(update);
        boolean userCreated = userStorage.checkUser(userId);

        MessageWrapper result;

        if (update.hasCallbackQuery() || updateHasPhoto(update)) {
            return startProcessingCallback(update);
        }
        SendMessage msg = new SendMessage();
        msg.setChatId(userId);
        if (userCreated) {
            User user = userStorage.getUser(userId);
            if (user.isWaitingMessages()) {
                result = startProcessingCallback(update);
            } else {
                msg.setText("Выбери дейтсвие:");
                List<ButtonWrapper> data = createMenuData(update, userStorage.getUser(userId));
                msg.setReplyMarkup(Util.createKeyboardOneBtnLine(data));
                result = MessageWrapper.builder().sendMessage(Collections.singletonList(msg)).
                        buttons(data).build();
            }
            return result;

        } else {
            //Если пользователя нет - предлогаем регистрацию
            msg.setChatId(userId);
            msg.setText("К сожалению, ты не зарегистрирован. Нажми на кнопку регистрации");
            List<ButtonWrapper> data = new ArrayList<>();
            TempObject regTemp = getTemp(update, Action.REGISTRATION);
            String key = Util.generateToken(regTemp);
            data.add(new ButtonWrapper("Регистрация", key, regTemp));
            msg.setReplyMarkup(Util.createKeyboardOneBtnLine(data));
            result = MessageWrapper.builder().sendMessage(Collections.singletonList(msg)).buttons(data).build();
            return result;
        }
    }


    private MessageWrapper startProcessingCallback(Update update) {
        //Мы ожидаем ключ. По этому ключу из редиса дергаем темп.
        String key;
        if (!update.hasCallbackQuery() || hasPhoto(update)) {
            User user = userStorage.getUser(Util.getUserId(update));
            key = user.getLastCallback();
        } else {
            key = update.getCallbackQuery().getData();
        }
        if (key == null) {
            return CommonMsgs.createCommonError(update);
        }
        String tempString = tempStorage.get(key);

        if (tempString != null && !tempString.isEmpty()) {
            TempObject tempObject;
            try {
                tempObject = Util.readTempObject(tempString);
            } catch (JsonProcessingException e) {
                log.error("error reading tempObject");
                log.error(e.getMessage());
                return CommonMsgs.createCommonError(update);
            }
            checkTemp(tempObject);
            switch (tempObject.getAction()) {
                case REGISTRATION -> {
                    return registrationAction.execute(update, tempObject);
                }
                case SALE -> {
                    return saleAction.execute(update, tempObject);
                }
                case SEARCH -> {
                    return searchAction.execute(update, tempObject);
                }
                case ADMIN -> {
                    return adminAction.execute(update, tempObject);
                }
                case CABINET -> {
                    return cabinetAction.execute(update, tempObject);
                }
                default -> {
                    return CommonMsgs.createCommonError(update);
                }
            }

        } else {
            log.error("tempObject is empty!");
            return CommonMsgs.createCommonError(update);
        }
    }

    private void checkTemp(TempObject tempObject) {
        initSelectedData(tempObject);
    }

    private boolean hasPhoto(Update update) {
        if (update.hasMessage()) {
            return update.getMessage().getPhoto() != null && !update.getMessage().getPhoto().isEmpty();
        }
        return false;
    }

    private void initSelectedData(TempObject tempObject) {
        if (tempObject.getSelectedData() == null) {
            tempObject.setSelectedData(new SelectedData());
        }
        if (tempObject.getSelectedData().getPhotos() == null) {
            tempObject.getSelectedData().setPhotos(new ArrayList<>());
        }
    }

    private List<ButtonWrapper> createMenuData(Update update, User user) {
        List<ButtonWrapper> menu = new ArrayList<>();
        TempObject cabinetTemp = getTemp(update, Action.CABINET);
        menu.add(new ButtonWrapper("Личный кабинет", Util.generateToken(cabinetTemp), cabinetTemp));

        TempObject saleTemp = getTemp(update, Action.SALE);
        menu.add(new ButtonWrapper("Продать", Util.generateToken(saleTemp), saleTemp));

        TempObject searchTemp = getTemp(update, Action.SEARCH);
        menu.add(new ButtonWrapper("Запрос на поиск", Util.generateToken(searchTemp), searchTemp));
        if (user.getRole().equals(Role.ADMIN_ROLE)) {

            TempObject statisticTemp = getTemp(update, Action.STATISTICS);
            menu.add(new ButtonWrapper("Cтатистика", Util.generateToken(statisticTemp), statisticTemp));

            TempObject adminTemp = getTemp(update, Action.ADMIN);
            menu.add(new ButtonWrapper("Запросы", Util.generateToken(adminTemp), adminTemp));
        }
        return menu;
    }

    private TempObject getTemp(Update update, Action action) {
        return TempObject.builder()
                .userId(Util.getUserId(update))
                .operation(Operation.START)
                .action(action).build();
    }


    private boolean updateHasPhoto(Update update) {
        return update.hasMessage() &&
                update.getMessage().getPhoto() != null &&
                !update.getMessage().getPhoto().isEmpty();
    }
}


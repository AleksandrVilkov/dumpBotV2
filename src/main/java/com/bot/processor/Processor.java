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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
                Map<String, String> data = createMenuData(update, userStorage.getUser(userId));
                msg.setReplyMarkup(Util.createKeyboardOneBtnLine(data));
                result = MessageWrapper.builder().sendMessage(Collections.singletonList(msg)).build();
            }
            return result;

        } else {
            //Если пользователя нет - предлогаем регистрацию
            msg.setChatId(userId);
            msg.setText("К сожалению, ты не зарегистрирован. Нажми на кнопку регистрации");
            Map<String, String> data = new HashMap<>();
            String key = getKeyAndSaveStartTemp(update, Action.REGISTRATION);
            data.put("Регистрация", key);
            msg.setReplyMarkup(Util.createKeyboardThreeBtn(data));
            result = MessageWrapper.builder().sendMessage(Collections.singletonList(msg)).build();
            return result;
        }
    }







    private MessageWrapper startProcessingCallback(Update update) {
        //Мы ожидаем ключ. По этому ключу из редиса дергаем темп.
        String key;
        if (!update.hasCallbackQuery()) {
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

    private Map<String, String> createMenuData(Update update, User user) {
        Map<String, String> menu = new HashMap<>();
        menu.put("Личный кабинет", getKeyAndSaveStartTemp(update, Action.CABINET));
        menu.put("Продать", getKeyAndSaveStartTemp(update, Action.SALE));
        menu.put("Запрос на поиск", getKeyAndSaveStartTemp(update, Action.SEARCH));
        if (user.getRole().equals(Role.ADMIN_ROLE)) {
            menu.put("Cтатистика", getKeyAndSaveStartTemp(update, Action.STATISTICS));
            menu.put("Запросы", getKeyAndSaveStartTemp(update, Action.ADMIN));
        }
        return menu;
    }

    private String getKeyAndSaveStartTemp(Update update, Action action) {
        TempObject tempObject = TempObject.builder()
                .userId(Util.getUserId(update))
                .operation(Operation.START)
                .action(action).build();
        String key = Util.generateToken(tempObject);
        tempStorage.set(key, tempObject.toString());
        return key;
    }


    private boolean updateHasPhoto(Update update) {
        return update.hasMessage() &&
                update.getMessage().getPhoto() != null &&
                !update.getMessage().getPhoto().isEmpty();
    }
}


package com.bot.processor;

import com.bot.bot.IProcessor;
import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.Action;
import com.bot.model.TempObject;
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
import java.util.HashMap;
import java.util.List;
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
    public List<SendMessage> startProcessing(Update update) {

        if (update.hasCallbackQuery()) {
            return startProcessingCallback(update);
        }
        String userId = Util.getUserId(update);
        boolean userCreated = userStorage.checkUser(userId);

        List<SendMessage> result = new ArrayList<>();
        if (userCreated) {
            //TODO показываем меню
        } else {
            //Если пользователя нет - предлогаем регистрацию
            SendMessage msg = new SendMessage();
            msg.setChatId(Util.getUserId(update));
            msg.setText("К сожалению, ты не зарегистрирован. Нажми на кнопку регистрации");
            Map<String, String> data = new HashMap<>();

            TempObject tempObject = TempObject.builder()
                    .userId(Util.getUserId(update))
                    .action(Action.REGISTRATION).build();
            String key = Util.generateToken(tempObject);
            tempStorage.set(key, tempObject.toString());
            data.put("Регистрация", key);
            msg.setReplyMarkup(Util.createKeyboardThreeBtn(data));
            result.add(msg);
        }
        return result;
    }

    private List<SendMessage> startProcessingCallback(Update update) {
        //Мы ожидаем ключ. По этому ключу из редиса дергаем темп.
        String key = update.getCallbackQuery().getData();
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
}


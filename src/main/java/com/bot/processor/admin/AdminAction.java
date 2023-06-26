package com.bot.processor.admin;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.IAccommodationStorage;
import com.bot.processor.ICarStorage;
import com.bot.processor.ITempStorage;
import com.bot.processor.common.ProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class AdminAction implements Action {
    final String ACTION_NAME = "ADMIN_QUERY_PROCESSING";
    final String SEPARATOR = "\n\n";
    @Autowired
    IAccommodationStorage accommodationStorage;
    @Autowired
    ICarStorage carStorage;

    @Autowired
    ITempStorage tempStorage;

    @Override
    public List<SendMessage> execute(Update update, TempObject tempObject) {
        switch (tempObject.getOperation()) {
            case START -> {
                log.info("Start " + ACTION_NAME + " step for user " + Util.getUserId(update));
                return firstStep(update, tempObject);
            }

            case APPROVED_REQUEST -> {
                return CommonMsgs.createCommonError(update);
            }
            case REJECTED_REQUEST -> {
                return CommonMsgs.createCommonError(update);
            }
            case EDIT_REQUEST -> {
                return CommonMsgs.createCommonError(update);
            }
            default -> {
                log.error("Cannot find step number in " + ACTION_NAME + " for user " + Util.getUserId(update));
                return CommonMsgs.createCommonError(update);
            }
        }
    }


    private List<SendMessage> firstStep(Update update, TempObject tempObject) {
        int count = accommodationStorage.countNotAgreed();
        //TODO отрефакторить. Добавить фото к заявке
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Всего не рассмотрено заявок: ").append(count).append(SEPARATOR);
        UserAccommodation userAccommodation = accommodationStorage.getFirstNotAgreed();
        stringBuilder.append("Рассмотите заявку № ").append(userAccommodation.getId()).append(SEPARATOR)
                .append("Описание: ").append(userAccommodation.getDescription()).append(SEPARATOR)
                .append("Автор: ").append(userAccommodation.getClientLogin()).append(SEPARATOR)
                .append("Тип запроса: ").append(userAccommodation.getType().name()).append(SEPARATOR);
        if (userAccommodation.getCarsId() != null && !userAccommodation.getCarsId().isEmpty()) {
            stringBuilder.append("Указаны автомобили: ");
            for (String id : userAccommodation.getCarsId()) {
                Car car = carStorage.getCarById(Integer.parseInt(id));
                stringBuilder.append(car.getBrand().getName()).append(" ")
                        .append(car.getModel().getName())
                        .append(" (").append(car.getEngine().getName()).append(")").append(SEPARATOR);
            }
        } else {
            stringBuilder.append("Автомобили не указаны.").append(SEPARATOR);
        }
        TempObject approved = getTempForButton(tempObject, Operation.APPROVED_REQUEST, ActionOnRequest.APPROVED, userAccommodation);
        TempObject edit = getTempForButton(tempObject, Operation.EDIT_REQUEST, ActionOnRequest.EDIT, userAccommodation);
        TempObject rejected = getTempForButton(tempObject, Operation.REJECTED_REQUEST, ActionOnRequest.REJECTED, userAccommodation);

        Map<String, String> data = new HashMap<>();
        data.put("Одобрить", ProcessorUtil.getKeyAndSaveTemp(approved, tempStorage));
        data.put("Изменить", ProcessorUtil.getKeyAndSaveTemp(edit, tempStorage));
        data.put("Отклонить", ProcessorUtil.getKeyAndSaveTemp(rejected, tempStorage));

        SendMessage sendMessage = new SendMessage(Util.getUserId(update), stringBuilder.toString());
        sendMessage.setReplyMarkup(Util.createKeyboardOneBtnLine(data));
        return Collections.singletonList(sendMessage);
    }

    private TempObject getTempForButton(TempObject oldObject, Operation operation, ActionOnRequest action, UserAccommodation userAccommodation) {
        TempObject tempObject = oldObject.clone();
        tempObject.setOperation(operation);
        AdministrationData approvedData = new AdministrationData();
        approvedData.setAction(action);
        approvedData.setUserAccommodation(userAccommodation);
        tempObject.setAdministrationData(approvedData);
        return tempObject;
    }
}

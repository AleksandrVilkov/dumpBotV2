package com.bot.processor.cabinet.subActions;

import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.oprations.Operations;
import com.bot.processor.SubAction;
import com.bot.processor.common.ProcessorUtil;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Component
public class StartCabinet implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        TempObject editCar = tempObject.clone();
        editCar.setOperation(Operations.EDIT_CAR);
        TempObject userAcc = tempObject.clone();
        userAcc.setOperation(Operations.MY_ACCOMMODATION);

        List<ButtonWrapper> buttons = new ArrayList<>();
        buttons.add(new ButtonWrapper("Изменить авто", Util.generateToken(editCar), editCar));
        buttons.add(new ButtonWrapper("Мои запросы", Util.generateToken(userAcc), userAcc));
        return ProcessorUtil.createMessages("Выбери действие:", update, buttons);
    }
}

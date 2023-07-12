package com.bot.processor.cabinet.subActions;

import com.bot.model.MessageWrapper;
import com.bot.model.Operations;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.SubAction;
import com.bot.processor.common.CarOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class CabinetEngineSelection implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        return CarOperation.chooseEngine(update, tempObject, Operations.END);
    }
}

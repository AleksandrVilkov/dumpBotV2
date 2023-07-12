package com.bot.processor.registration.subActions;

import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.SubAction;
import com.bot.processor.common.CarOperation;
import com.bot.model.Operations;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class RegisterBrandSelection implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        return CarOperation.chooseBrand(update, tempObject, Operations.MODEL_SELECTION);
    }
}

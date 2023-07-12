package com.bot.processor.registration;

import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.Action;
import com.bot.processor.SubAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class RegistrationAction implements Action {
    @Autowired
    private RegSubActionFactory regSubActionFactory;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        SubAction subAction = regSubActionFactory.get(tempObject.getOperation());
        subAction.processPreviousStep(update, tempObject, user);
        return subAction.createResponse(update, tempObject, user);
    }
}

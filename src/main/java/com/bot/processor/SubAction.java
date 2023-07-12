package com.bot.processor;

import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface SubAction {
    void processPreviousStep(Update update, TempObject tempObject, User user);
    MessageWrapper createResponse(Update update, TempObject tempObject, User user);
}

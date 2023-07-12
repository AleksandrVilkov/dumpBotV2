package com.bot.processor.admin.subActions;

import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.SubAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;

import static com.bot.processor.oprations.Operations.ENTER_NEW_DESCRIPTION;

@Component
@Slf4j
public class EditDescription implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        tempObject.setOperation(ENTER_NEW_DESCRIPTION);
        String key = Util.generateToken(tempObject);
        user.setWaitingMessages(true);
        user.setLastCallback(key);

        SendMessage sendMessage = new SendMessage(Util.getUserId(update), "Измените текст:");
        return MessageWrapper.builder()
                .sendMessage(Collections.singletonList(sendMessage))
                .leaveOldMessages(true).build().addTemp(key, tempObject);
    }
}

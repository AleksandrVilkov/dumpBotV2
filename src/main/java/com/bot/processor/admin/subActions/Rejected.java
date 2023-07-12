package com.bot.processor.admin.subActions;

import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.processor.oprations.Operations;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.SubAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Collections;

@Component
@Slf4j
public class Rejected implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        String text = "Укажи причину отклонения запроса. Обрати внимание, это сообщение получит автор.";
        SendMessage sendMessage = new SendMessage(Util.getUserId(update), text);
        TempObject newTemp = tempObject.clone();
        newTemp.setOperation(Operations.SEND_REJECTED_REQUEST);
        String key = Util.generateToken(newTemp);
        user.setLastCallback(key);
        user.setWaitingMessages(true);
        MessageWrapper messageWrapper = MessageWrapper.builder().sendMessage(Collections.singletonList(sendMessage)).build();
        messageWrapper.addTemp(key, newTemp);
        return messageWrapper;
    }
}

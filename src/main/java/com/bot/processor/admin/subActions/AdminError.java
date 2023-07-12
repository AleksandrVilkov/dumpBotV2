package com.bot.processor.admin.subActions;

import com.bot.common.CommonMsgs;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.SubAction;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
public class AdminError implements SubAction {
    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        return CommonMsgs.createCommonError(update);
    }
}

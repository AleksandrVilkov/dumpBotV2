package com.bot.processor.admin;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.*;
import com.bot.processor.Action;
import com.bot.processor.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class AdminAction implements Action {
    private final String ACTION_NAME = "ADMIN_QUERY_PROCESSING";

    @Autowired
    AdminSubActionFactory adminSubActionFactory;

    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        SubAction subAction = adminSubActionFactory.get(tempObject.getOperation());
        subAction.processPreviousStep(update,tempObject,user);
        return subAction.createResponse(update,tempObject,user);
    }
}

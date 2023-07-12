package com.bot.processor.cabinet.subActions;

import com.bot.common.CommonMsgs;
import com.bot.common.Util;
import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.IUserStorage;
import com.bot.processor.SubAction;
import com.bot.processor.common.ProcessorUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class End implements SubAction {
    @Autowired
    IUserStorage userStorage;

    @Override
    public void processPreviousStep(Update update, TempObject tempObject, User user) {

    }

    @Override
    public MessageWrapper createResponse(Update update, TempObject tempObject, User user) {
        if (tempObject.getOption().getCarList().size() != 1) {
            return CommonMsgs.createCommonError(update);
        }
        user.setCarId(tempObject.getOption().getCarList().get(0).getId());
        if (userStorage.saveUser(user)) {
            String text = "Отлично, данные обновлены!";
            return ProcessorUtil.createMessages(text, update);
        } else {
            log.error("error saving user " + Util.getUserId(update));
            return CommonMsgs.createCommonError(update);
        }
    }
}

package com.bot.processor.cabinet;

import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import com.bot.model.User;
import com.bot.processor.Action;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.Update;


@Component
public class CabinetAction implements Action {
    @Override
    public MessageWrapper execute(Update update, TempObject tempObject, User user) {
        return null;
    }
}

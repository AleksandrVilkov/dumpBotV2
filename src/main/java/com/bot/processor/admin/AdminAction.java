package com.bot.processor.admin;

import com.bot.model.TempObject;
import com.bot.processor.Action;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class AdminAction implements Action {
    @Override
    public List<SendMessage> execute(Update update, TempObject tempObject) {
        return null;
    }
}

package com.bot.processor;

import com.bot.model.MessageWrapper;
import com.bot.model.TempObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface Action {
    MessageWrapper execute(Update update, TempObject tempObject);
}

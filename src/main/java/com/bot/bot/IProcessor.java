package com.bot.bot;

import com.bot.model.TempObject;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

public interface IProcessor {
    List<SendMessage> startProcessing(Update update, TempObject tempObject);
    List<SendMessage> startProcessing(Update update);
}

package com.bot.processor;

import com.bot.bot.IProcessor;
import com.bot.model.TempObject;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
public class Processor implements IProcessor {
    @Override
    public List<SendMessage> startProcessing(Update update, TempObject tempObject) {
        return null;
    }

    @Override
    public List<SendMessage> startProcessing(Update update) {
        return null;
    }
}

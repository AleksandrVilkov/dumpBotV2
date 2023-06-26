package com.bot.bot;

import com.bot.model.MessageWrapper;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface IProcessor {
    MessageWrapper startProcessing(Update update);
}

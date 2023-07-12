package com.bot.processor;

import com.bot.model.Operations;

public interface SubActionFactory {
    SubAction get(Operations operation);
}

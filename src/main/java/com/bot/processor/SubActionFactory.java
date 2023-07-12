package com.bot.processor;

import com.bot.processor.oprations.Operations;

public interface SubActionFactory {
    SubAction get(Operations operation);
}

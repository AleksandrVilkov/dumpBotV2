package com.bot.service.ai;

import com.bot.model.ai.AIResult;

public interface AIService {
    AIResult parseText(String text);
}

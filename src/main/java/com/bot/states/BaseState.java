package com.bot.states;

import com.bot.enums.BotCommand;
import com.bot.model.domain.BotUser;
import com.bot.model.domain.IncomingData;
import com.bot.model.domain.Message;

import java.util.List;

public interface BaseState {
    List<Message> execute(BotUser botUser, IncomingData incomingData);

    List<Message> handleCommands(BotUser botUser, List<BotCommand> commands);
}

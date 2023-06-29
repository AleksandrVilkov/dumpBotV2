package com.bot.processor;

import com.bot.model.MessageWrapper;
import com.bot.model.User;
import com.bot.model.UserAccommodation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

public interface INotificationCenter {
    List<SendMessage> getMsgsForAllAdmins(String msgText);

    MessageWrapper createMsgsForChannel(UserAccommodation userAccommodation, User user, long channelID);

}

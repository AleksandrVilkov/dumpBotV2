package com.bot.service.user;


import com.bot.enums.State;

public interface UserService {
    void resetState(String telegramUserId);
    void saveUserState(String tgUserId, State state);
}

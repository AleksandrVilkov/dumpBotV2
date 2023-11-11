package com.bot.service.user;


import com.bot.enums.State;
import com.bot.mappers.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserMapper userMapper;

    @Override
    public void resetState(String telegramUserId) {
        userMapper.saveUserState(telegramUserId, State.NONE);
    }

    @Override
    public void saveUserState(String tgUserId, State state) {
        userMapper.saveUserState(tgUserId, state);
    }
}

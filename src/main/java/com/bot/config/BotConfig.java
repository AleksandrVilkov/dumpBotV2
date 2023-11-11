package com.bot.config;

import com.bot.enums.State;
import com.bot.states.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Map;

@Getter
@Setter
@Component
public class BotConfig {
    @Value("${bot.token}")
    private String token;
    @Value("${bot.name}")
    private String name;
    @Autowired
    private ValidatorConfig validateData;

    @Autowired
    NoneState noneState;
    @Autowired
    WaitCommandState waitCommandState;
    @Autowired
    WaitTextState waitTextState;
    @Autowired
    WaitPhotoState waitPhotoState;
    @Autowired
    WaitUserApproveState approveState;

    @Bean("state")
    Map<State, BaseState> getState() {
        return Map.of(State.NONE, noneState,
                State.WAIT_COMMAND, waitCommandState,
                State.WAIT_TEXT, waitTextState,
                State.WAIT_PHOTO, waitPhotoState,
                State.WAIT_USER_APPROVE, approveState);
    }
}


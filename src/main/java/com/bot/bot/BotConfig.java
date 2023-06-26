package com.bot.bot;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class BotConfig {
    @Value("${bot.token}")
    private String token;
    @Value("${bot.name}")
    private String name;
    @Autowired
    private ValidateData validateData;
}


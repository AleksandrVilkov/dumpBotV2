package com.bot.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@NoArgsConstructor
@Component
public class ValidatorConfig {
    @Value("${bot.validateData.channelID}")
    private long channelID;
    @Value("${bot.validateData.channelURL}")
    private String channelURL;
}

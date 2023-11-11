package com.bot.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
public class AIConfig {
    @Value("${ai.uri}")
    String uri;
    @Value("${ai.key}")
    String RapidAPIKey;
    @Value("${ai.host}")
    String RapidAPIHost;
    @Value("${ai.content-type}")
    String contentType;

}

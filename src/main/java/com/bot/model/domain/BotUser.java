package com.bot.model.domain;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BotUser {
    private String id;
    private String telegramLogin;
    private String firstName;
    private String secondName;
    private String languageCode;

    public static BotUser fromTgUser(org.telegram.telegrambots.meta.api.objects.User u) {
        return BotUser.builder()
                .id(String.valueOf(u.getId()))
                .telegramLogin(u.getUserName())
                .firstName(u.getFirstName())
                .secondName(u.getLastName())
                .languageCode(u.getLanguageCode())
                .build();
    }
}

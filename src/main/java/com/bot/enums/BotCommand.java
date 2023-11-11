package com.bot.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BotCommand {
    SEND("/send"), APPROVE("/approve"), CANCEL("/cancel"), FINISH("/finish"), ADD_PHOTO("/addPhoto");
    private final String command;

    public static BotCommand findByCommand(String command) {
        for (BotCommand e : BotCommand.values()) {
            if (e.getCommand().equals(command)) {
                return e;
            }
        }
        throw new RuntimeException("Неизвестная команда");
    }
}

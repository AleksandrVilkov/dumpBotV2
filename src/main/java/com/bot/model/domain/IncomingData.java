package com.bot.model.domain;

import com.bot.enums.BotCommand;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IncomingData {

    static final String BOT_COMMAND = "bot_command";
    String text;
    String userName;
    List<BotCommand> commands;
    String photoId;

    public static IncomingData fromUpdate(Update u) {
        var builder = IncomingData.builder()
                .commands(defineCommands(u.getMessage()));
        if (u.getMessage() != null) {
            builder.text(u.getMessage().getText()).userName("@" + u.getMessage().getFrom().getUserName());
            if (u.getMessage().hasPhoto()) {
                builder.photoId(u.getMessage().getPhoto().get(0).getFileId());
            }
        }
        return builder.build();
    }

    private static List<BotCommand> defineCommands(Message m) {
        List<BotCommand> result = new ArrayList<>();
        if (m != null && m.hasEntities()) {
            m.getEntities().forEach(e -> {
                if (BOT_COMMAND.equalsIgnoreCase(e.getType())) {
                    result.add(BotCommand.findByCommand(e.getText()));
                }
            });
        }
        return result;
    }
}

package com.bot.model;

import lombok.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageWrapper {
    private List<SendMessage> sendMessage;
    private SendPhoto sendPhoto;
    private SendMediaGroup sendMediaGroup;
    private int needDeleting;

    public int getCountMsgs() {
        int result = 0;
        if (this.sendMessage != null && !this.sendMessage.isEmpty()) {
            result = result + sendMessage.size();
        }
        if (sendPhoto != null) {
            result = result + 1;
        }
        if (sendMediaGroup != null) {
            result = result + sendMediaGroup.getMedias().size();
        }
        return result;
    }
}

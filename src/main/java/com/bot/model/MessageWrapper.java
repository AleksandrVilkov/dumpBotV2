package com.bot.model;

import lombok.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MessageWrapper {
    private List<SendMessage> sendMessage;
    @Getter
    private List<ButtonWrapper> buttons;
    private Map<String, TempObject> temp;
    @Getter
    private SendPhoto sendPhoto;
    @Getter
    private SendMediaGroup sendMediaGroup;
    @Getter
    @Setter
    private boolean leaveOldMessages;

    public void setSendMessage(List<SendMessage> sendMessage) {
        this.sendMessage = sendMessage;
    }

    public void setSendPhoto(SendPhoto sendPhoto) {
        this.sendPhoto = sendPhoto;
    }

    public void setSendMediaGroup(SendMediaGroup sendMediaGroup) {
        this.sendMediaGroup = sendMediaGroup;
    }

    public MessageWrapper addTemp(String key, TempObject value) {
        if (this.temp == null) {
            this.temp = new HashMap<>();
        }
        this.temp.put(key,value);
        return this;
    }

    public void setButtons(List<ButtonWrapper> buttons) {
        this.buttons = buttons;
    }

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

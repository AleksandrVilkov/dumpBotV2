package com.bot.bot;

import com.bot.model.ChatMemberStatus;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class Validator {
    public static boolean validateUser(Update update, Bot bot, BotConfig config) {
        long id;
        if (update.hasCallbackQuery()) {
            id = update.getCallbackQuery().getFrom().getId();
        } else {
            id = update.getMessage().getFrom().getId();
        }
        GetChatMember chatMember = new GetChatMember();
        chatMember.setUserId(id);
        chatMember.setChatId(String.valueOf(config.getValidateData().getChannelID()));
        try {
            String status = bot.execute(chatMember).getStatus();
            return !status.equalsIgnoreCase(ChatMemberStatus.KICKED.getName())
                    && !status.equalsIgnoreCase(ChatMemberStatus.LEFT.getName());
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}

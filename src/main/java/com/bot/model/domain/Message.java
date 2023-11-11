package com.bot.model.domain;

import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.util.CollectionUtils;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
    String chatId;
    String text;
    List<Photo> photos;

    public Message(String chatId, String text) {
        this.chatId = chatId;
        this.text = text;
    }

    public boolean withMedia() {
        return !CollectionUtils.isEmpty(photos) && photos.size() != 1;
    }
    public boolean withOnePhoto() {
        return !CollectionUtils.isEmpty(photos) && photos.size() == 1;
    }


    public SendMessage getBotTextMessage() {
        return SendMessage.builder().chatId(chatId).text(text).build();
    }

    public SendPhoto getBotTextMessageWithPhoto() {
        return SendPhoto.builder().chatId(chatId).caption(text).photo(new InputFile(photos.get(0).getPhotoId())).build();
    }

    public SendMediaGroup getBotMediaMessage() {
        return SendMediaGroup.builder()
                .medias(photos.stream().map(photo -> new InputMediaPhoto(photo.getPhotoId())).collect(Collectors.toList()))
                .chatId(chatId)
                .build();
    }
}
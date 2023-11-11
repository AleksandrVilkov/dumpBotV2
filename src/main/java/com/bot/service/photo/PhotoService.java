package com.bot.service.photo;

import com.bot.model.domain.Photo;

import java.util.UUID;

public interface PhotoService {
    void savePhoto(Photo photo);
    boolean existsPhotosByRequestId(UUID requestId);
}

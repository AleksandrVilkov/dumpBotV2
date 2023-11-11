package com.bot.service.photo;

import com.bot.mappers.PhotoMapper;
import com.bot.model.domain.Photo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PhotoServiceImpl implements PhotoService {
    @Autowired
    PhotoMapper photoMapper;
    @Override
    public void savePhoto(Photo photo) {
        photoMapper.saveUserPhoto(photo);
    }

    @Override
    public boolean existsPhotosByRequestId(UUID requestId) {
       return photoMapper.existsPhotosByRequestId(requestId);
    }
}

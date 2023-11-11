package com.bot.mappers;

import com.bot.model.domain.Photo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@Mapper
public interface PhotoMapper {
    void saveUserPhoto(@Param("photo") Photo photo);
    List<Photo> findByRequestId(@Param("requestId") UUID requestId);
    boolean existsPhotosByRequestId(@Param("requestId")UUID requestId);
}

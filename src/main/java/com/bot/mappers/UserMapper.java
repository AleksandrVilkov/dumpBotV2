package com.bot.mappers;

import com.bot.enums.State;
import com.bot.model.domain.UserState;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@Mapper
public interface UserMapper {
    Optional<UserState> getUserState(@Param("telegramId") String tgUserId);

    Boolean userExist(@Param("telegramId") String tgUserId);

    void saveUserState(@Param("telegramId") String tgUserId, @Param("state") State state);
}
package com.bot.mappers;

import com.bot.model.domain.UserRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@Mapper
public interface UserRequestMapper {
    void saveUserRequest(@Param("userRequest") UserRequest userRequest);

    void markNonActive(@Param("userRequestId") UUID id);
    Optional<UUID> findActiveRequestId(@Param("telegramUserId") String telegramUserId);
    Optional<UserRequest> findActiveRequest(@Param("telegramUserId") String telegramUserId);
}

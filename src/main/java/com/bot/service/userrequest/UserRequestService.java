package com.bot.service.userrequest;

import com.bot.model.domain.Message;
import com.bot.model.domain.UserRequest;

import java.util.UUID;

public interface UserRequestService {
    Message generatePreview(String telegramUserId);
    Message messageToChanel(UserRequest userRequest);

    UserRequest findActiveRequest(String telegramUserId);

    UUID findActiveRequestId(String telegramUserId);

    void markNonActive(UUID userRequestId);

    void saveUserRequest(UserRequest userRequest);
}

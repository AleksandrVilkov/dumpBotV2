package com.bot.model.domain;

import com.bot.enums.UserRequestType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserRequest {
    UUID id;
    String telegramLogin;
    String telegramUserId;
    String text;
    UserRequestType type;
    Object userApprove;
    Object adminApprove;
    Object deleted;
    Object posted;
    Object hasPhoto;
    Object createdAt;
    Object tgAccountCreatedAt;
    Object inProgress;
    //TODO какая то фигня с кастингом
    //Error attempting to get column 'created_at' from result set.  Cause: org.postgresql.util.PSQLException: Cannot cast to boolean: "1700593804570"
    //; bad SQL grammar []; nested exception is org.postgresql.util.PSQLException: Cannot cast to boolean: "1700593804570"
}

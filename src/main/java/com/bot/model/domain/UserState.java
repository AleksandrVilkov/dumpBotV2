package com.bot.model.domain;

import com.bot.enums.State;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserState {
    String telegramId;
    State state;


}

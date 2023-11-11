package com.bot.model.ai;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class AIResult {
    String price;
    String description;
    ArrayList<String> cars;
    String type;
}

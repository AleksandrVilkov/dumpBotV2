package com.bot.model.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
public class AIResponse {

    AIResult results;
    Stats stats;


    public static AIResponse fromJson(String string) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(string, AIResponse.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}

@FieldDefaults(level = AccessLevel.PRIVATE)
@Data
class Stats {
    public int n_text_characters;
    public int n_entities;
    public int n_tokens_used;
}

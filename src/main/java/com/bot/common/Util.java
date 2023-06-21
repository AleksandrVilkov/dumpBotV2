package com.bot.common;

import com.bot.model.TempObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Util {

    public static <T extends Enum<T>> T findEnumConstant(final Class<T> cls, final String name) {
        if (name == null) {
            return null;
        }
        for (T enumConstant : cls.getEnumConstants()) {
            if (enumConstant.name().equals(name)) {
                return enumConstant;
            }
        }
        return null;
    }

    public static TempObject readTempObject (String tempStr) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(tempStr, TempObject.class);
    }
}

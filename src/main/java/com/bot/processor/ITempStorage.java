package com.bot.processor;

public interface ITempStorage {
    String get(String key);

    String set(String key, String data);
}

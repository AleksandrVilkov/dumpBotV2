package com.bot.processor;

import java.util.List;

public interface ITempStorage {
    String get(String key);

    String set(String key, String data);

    List<String> getList(String key);
    List<String> setList(String key,List<String> data);
}

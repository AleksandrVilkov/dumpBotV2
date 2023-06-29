package com.bot.processor;

import java.util.List;

public interface ITempStorage {
    String get(String key);

    void set(String key, String data);

    List<String> getList(String key);
    void setList(String key,List<String> data);
}

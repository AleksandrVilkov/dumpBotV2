package com.bot.tempStorage;

import com.bot.processor.ITempStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@Component
@Slf4j
public class RedisConnector implements ITempStorage {
    Jedis jedis;
    private final String SEP = ";";

    public RedisConnector(@Value("${tempStorage.host}") String host, @Value("${tempStorage.port}") String port) {
        this.jedis = new Jedis(host, Integer.parseInt(port));
        log.info("Redis connection was established.");
    }

    @Override
    public String get(String key) {
        return jedis.get(key);
    }

    @Override
    public String set(String key, String data) {
        return jedis.set(key, data);
    }

    @Override
    public List<String> getList(String key) {
        //TODO посмотреть как списки сохранять
        String value = jedis.get(key);
        if (value == null) {
            return new ArrayList<>();
        }
        return Arrays.stream(value.split(SEP)).collect(Collectors.toList());
    }

    @Override
    public List<String> setList(String key, List<String> data) {
        StringBuilder builder = new StringBuilder();
        data.forEach(string -> {
            builder.append(string).append(SEP);
        });
        jedis.set(key, builder.toString());
        return data;
    }
}

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
    public void set(String key, String data) {
        jedis.set(key, data);
    }

    @Override
    public List<String> getList(String key) {
        //TODO, не всегда норм отрабатывает
        List<String> res = new ArrayList<>();
        long size = jedis.llen(key.getBytes());
        for (long i = 0; i<= size; i++) {
            res.add(Arrays.toString(jedis.rpop(key.getBytes())));
        }
        return res;
    }

    @Override
    public void setList(String key, List<String> data) {
        String[] d = data.toArray(new String[0]);
        jedis.lpush(key, d);
    }
}

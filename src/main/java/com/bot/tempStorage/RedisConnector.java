package com.bot.tempStorage;

import com.bot.processor.ITempStorage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Getter
@Setter
@Component
@Slf4j
public class RedisConnector implements ITempStorage {
    Jedis jedis;
    public RedisConnector( @Value("${tempStorage.host}") String host, @Value("${tempStorage.port}") String port) {
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
}

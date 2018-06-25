package com.jamesmcnee.redissontransactional.configuration;

import com.jamesmcnee.redissontransactional.RedissonLockingTransactionManager;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.redisson.spring.transaction.RedissonTransactionManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan
@EnableCaching
public class RedisConfiguration {
    @Bean(destroyMethod="shutdown")
    RedissonClient redisson() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://localhost:6379");
        return Redisson.create(config);
    }

    @Bean
    CacheManager cacheManager(RedissonClient redissonClient) {
        Map<String, CacheConfig> config = new HashMap<>();

        config.put("testMap", new CacheConfig(Duration.ofSeconds(20).toMillis(), Duration.ofMinutes(10).toMillis()));
        return new RedissonSpringCacheManager(redissonClient, config);
    }

    @Bean
    public RedissonLockingTransactionManager transactionManager(RedissonClient redisson) {
        return new RedissonLockingTransactionManager(redisson);
    }
}

package com.jamesmcnee.redissontransactional.repository;

import com.jamesmcnee.redissontransactional.RedissonLockingTransactionManager;
import org.redisson.api.RMap;
import org.redisson.api.RTransaction;
import org.redisson.spring.transaction.RedissonTransactionManager;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class RedissonTransactionFactory {
    private RedissonLockingTransactionManager redissonTransactionManager;

    private RTransaction transaction;
    private Map<String, RMap> activeMaps = new HashMap<>();

    public RedissonTransactionFactory(RedissonLockingTransactionManager redissonLockingTransactionManager) {
        this.redissonTransactionManager = redissonLockingTransactionManager;
    }

    public <K, V> RMap<K, V> getMap(String map) {
        return this.returnOrCreateMapInstance(map);
    }

    private <K, V> RMap<K, V> returnOrCreateMapInstance(String map) {
        activeMaps.putIfAbsent(map, returnOrCreateTransaction().getMap(map));

        return activeMaps.get(map);
    }

    private RTransaction returnOrCreateTransaction() {
        if(Objects.isNull(transaction)) {
            replaceStoredTransaction();
        }

        try {
            transaction.getMap("ATTEMPT_TO_GET_MAP");
        } catch (IllegalStateException ise) {
            replaceStoredTransaction();
        }

        return transaction;
    }

    private void replaceStoredTransaction() {
        activeMaps.clear();
        transaction = redissonTransactionManager.getCurrentTransaction();
    }
}

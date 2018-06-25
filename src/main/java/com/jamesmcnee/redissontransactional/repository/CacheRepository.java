package com.jamesmcnee.redissontransactional.repository;

import com.jamesmcnee.redissontransactional.entity.EmployeeEntity;
import org.redisson.api.RMap;
import org.springframework.stereotype.Repository;

@Repository
public class CacheRepository {

    private final RedissonTransactionFactory transactionFactory;

    public CacheRepository(RedissonTransactionFactory transactionFactory) {
        this.transactionFactory = transactionFactory;
    }

    public void save(EmployeeEntity entity) {
        transactionFactory.getMap("testMap").put(entity.getId(), entity);
    }

    public EmployeeEntity get(String id) {
        RMap<String, EmployeeEntity> map = transactionFactory.getMap("testMap");

        return map.get(id);
    }
}

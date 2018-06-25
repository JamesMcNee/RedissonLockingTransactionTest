package com.jamesmcnee.redissontransactional.serivce;

import com.jamesmcnee.redissontransactional.entity.EmployeeEntity;
import com.jamesmcnee.redissontransactional.repository.CacheRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CacheService {

    private CacheRepository cacheRepository;

    @Autowired
    public CacheService(CacheRepository cacheRepository) {
        this.cacheRepository = cacheRepository;
    }

    @Transactional
    public EmployeeEntity save(EmployeeEntity employeeEntity) {
        this.cacheRepository.save(employeeEntity);

        System.out.println("Saving employee with ID: " + employeeEntity.getId());

        return get(employeeEntity.getId());
    }

    @Transactional
    public EmployeeEntity get(String id) {
        return this.cacheRepository.get(id);
    }
}

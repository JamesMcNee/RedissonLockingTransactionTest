package com.jamesmcnee.redissontransactional.controller;

import com.jamesmcnee.redissontransactional.entity.EmployeeEntity;
import com.jamesmcnee.redissontransactional.serivce.CacheService;
import org.ajbrown.namemachine.Name;
import org.ajbrown.namemachine.NameGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@RestController
public class EmployeeController {

    private CacheService cacheService;

    @Autowired
    public EmployeeController(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @RequestMapping(method = RequestMethod.POST, path = "/api/employee")
    public List<EmployeeEntity> saveRandomEntity(@RequestParam(name = "repeat", required = false, defaultValue = "1") int repeat) {
        List<EmployeeEntity> employees = new ArrayList<>();

        for(int i = 0; i < repeat; i++) {
            Name randomName = new NameGenerator().generateName();
            EmployeeEntity entity = new EmployeeEntity();

            entity.setId(UUID.randomUUID().toString());
            entity.setForename(randomName.getFirstName());
            entity.setSurname(randomName.getLastName());
            entity.setGender(randomName.getGender().toString());
            entity.setAge(new Random().nextInt(70) + 1);
            entity.setDepartment("SALES");
            entity.setActive((entity.getAge() % 2) == 0);

            EmployeeEntity returnedEntity = this.cacheService.save(entity);
            System.out.println("SAVED: " + returnedEntity.getId());

            employees.add(returnedEntity);
        }

        return employees;
    }

    @RequestMapping(method = RequestMethod.GET, path = "/api/employee/{id}")
    public EmployeeEntity getEmployee(@PathVariable(name = "id") String id) {
        return cacheService.get(id);
    }
}

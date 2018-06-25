package com.jamesmcnee.redissontransactional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class RedissonTransactionalApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedissonTransactionalApplication.class, args);

		System.out.println("----- APPLICATION STARTED SUCCESSFULLY -----");
	}
}

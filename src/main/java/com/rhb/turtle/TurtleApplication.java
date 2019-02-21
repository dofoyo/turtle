package com.rhb.turtle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class TurtleApplication {
	public static void main(String[] args) {
		SpringApplication.run(TurtleApplication.class, args);
	}

}


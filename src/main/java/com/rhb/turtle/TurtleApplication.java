package com.rhb.turtle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class TurtleApplication {
	public static void main(String[] args) {
		SpringApplication.run(TurtleApplication.class, args);
	}
	
	
	@Bean
	public TaskScheduler taskScheduler(){
		ThreadPoolTaskScheduler taskScheduer = new ThreadPoolTaskScheduler();
		taskScheduer.setPoolSize(10);
		taskScheduer.setThreadNamePrefix("turtle task");
		return taskScheduer;
	}

}


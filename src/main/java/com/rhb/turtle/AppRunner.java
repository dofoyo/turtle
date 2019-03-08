package com.rhb.turtle;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.turtle.operation.PreyRepository;


@Component
public class AppRunner implements CommandLineRunner {
	
	@Autowired
	@Qualifier("turtlePreyRepositoryImp")
	PreyRepository trr ;
	
	
    @Override
    public void run(String... args) throws Exception {
		trr.generatePreys();
    }

}
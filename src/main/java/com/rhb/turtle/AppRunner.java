package com.rhb.turtle;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.turtle.simulation.repository.TurtleSimulationCagrRepository;

@Component
public class AppRunner implements CommandLineRunner {
	
	@Autowired
	@Qualifier("turtleSimulationCagrRepositoryImp")
	TurtleSimulationCagrRepository cagrRepository;

    @Override
    public void run(String... args) throws Exception {
    	cagrRepository.generateDailyCAGR();
    }

}
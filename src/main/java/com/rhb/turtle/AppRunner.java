package com.rhb.turtle;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.rhb.turtle.simulation.repository.TurtleSimulationRepository;
import com.rhb.turtle.simulation.service.TurtleSimulationService;
import com.rhb.turtle.util.FileUtil;

@Component
public class AppRunner implements CommandLineRunner {
/*	@Value("${reportPath}")
	private String reportPath;
	
    private static final Logger logger = LoggerFactory.getLogger(AppRunner.class);

    private final TurtleSimulationService turtleSimulationServiceImp;

    public AppRunner(TurtleSimulationService turtleSimulationServiceImp) {
        this.turtleSimulationServiceImp = turtleSimulationServiceImp;
    }*/

    @Override
    public void run(String... args) throws Exception {
/*		Map<String,String> result = turtleSimulationServiceImp.simulate();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("winRatio: " + result.get("winRatio"));
		System.out.println("CAGR: " + result.get("cagr"));
		FileUtil.writeTextFile(reportPath + "/record" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);
*/
    }

}
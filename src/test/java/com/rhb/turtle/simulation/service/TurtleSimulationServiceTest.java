package com.rhb.turtle.simulation.service;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.simulation.service.TurtleSimulationService;
import com.rhb.turtle.util.FileUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationServiceTest {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("turtleSimulationServiceImp")
	TurtleSimulationService ts;
	
	//@Test
	public void simulate() {
		Map<String,String> result = ts.simulate();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("winRatio: " + result.get("winRatio"));
		System.out.println("CAGR: " + result.get("cagr"));
		FileUtil.writeTextFile(reportPath + "/record" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);
	}
	
	@Test
	public void simulate2() {
		Map<String,String> result = ts.simulate2();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("winRatio: " + result.get("winRatio"));
		System.out.println("CAGR: " + result.get("cagr"));
		FileUtil.writeTextFile(reportPath + "/record" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);
	}
}

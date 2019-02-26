package com.rhb.turtle.simulation;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.simulation.spider.TurtleSimulationSpider;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationSpiderTest {
	@Autowired
	@Qualifier("turtleSimulationSpiderImp")
	TurtleSimulationSpider turtleSimulationSpider;
	
	
	//@Test
	public void getLatestDate() {
		LocalDate date = turtleSimulationSpider.getLatestMarketDate();
		System.out.println(date);
	}
	
	//@Test
	public void getLatestMarketData() {
		String id = "sh600519";
		Map<String, String> data = turtleSimulationSpider.getLatestMarketData(id);
		System.out.println(data);
	}
	
	//@Test
	public void downLatestTop100() {
		turtleSimulationSpider.downLatestDailyTop100();
		System.out.println("done! file have been saved!");
	}
	
	@Test
	public void downKdata() {
/*		String[] ids = {"sh601318","sh600519","sz000725","sz000063","sz000651","sh600030","sz000858","sz300059","sz000333","sh600887"};
		for(String id : ids) {
			turtleSimulationSpider.downKdata(id);
		}*/
		turtleSimulationSpider.downKdata("sz300059");
	}
	
}

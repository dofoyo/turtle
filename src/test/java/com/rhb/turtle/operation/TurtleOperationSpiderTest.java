package com.rhb.turtle.operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.operation.TurtleOperationSpider;
import com.rhb.turtle.simulation.spider.TurtleSimulationSpider;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev2")
public class TurtleOperationSpiderTest {
	@Autowired
	@Qualifier("turtleOperationSpiderImp")
	TurtleOperationSpider turtleOperationSpider;
	
	@Autowired
	@Qualifier("turtleOperationRepositoryImp")
	TurtleOperationRepository tr ;
	
	
	//@Test
	public void getLatestDate() {
		turtleOperationSpider.downLatestDailyTop100();
		System.out.println("done");
	}
	
	@Test
	public void downKdatas() {
		String year="2017";
		String jidu;
		List<String> ids = tr.getDailyTop100Ids();
		for(String id : ids) {
			for(int i=1; i<5; i++) {
				turtleOperationSpider.downKdatas(id, year, Integer.toString(i));
				try {Thread.sleep(5000);} catch (InterruptedException e) {e.printStackTrace();}
			}
		}
		System.out.println("done");
	}
	
}

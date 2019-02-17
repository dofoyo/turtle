package com.rhb.turtle.operation;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.operation.TurtleOperationSpider;
import com.rhb.turtle.simulation.TurtleSimulationSpider;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev2")
public class MarketInfoOperationSpiderTest {
	@Autowired
	@Qualifier("marketInfoOperationSpiderImp")
	TurtleOperationSpider marketInfoOperationSpider;
	
	
	@Test
	public void getLatestDate() {
		marketInfoOperationSpider.downLatestDailyTop100();
		System.out.println("done");
	}
	
}

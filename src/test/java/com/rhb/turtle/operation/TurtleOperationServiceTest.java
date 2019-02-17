package com.rhb.turtle.operation;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.operation.TurtleOperationService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev2")
public class TurtleOperationServiceTest {
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;
	
	@Test
	public void operate() {
		ts.operate();
	}
	
	//@Test
	public void downKdatas() {
		ts.doClosingWork(10);
		System.out.println("done");
	}
	
}

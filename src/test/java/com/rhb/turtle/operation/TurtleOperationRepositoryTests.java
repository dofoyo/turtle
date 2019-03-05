package com.rhb.turtle.operation;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.operation.TurtleOperationRepository;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev2")
public class TurtleOperationRepositoryTests {
	@Autowired
	@Qualifier("turtleOperationRepositoryImp")
	TurtleOperationRepository tor ;

	@Autowired
	@Qualifier("turtlePreyRepositoryImp")
	PreyRepository trr ;
	
	//@Test
	public void dd() {
		trr.generatePreys();
		System.out.println("done!");
	}
	
	@Test
	public void getOnhands() {
		List<Map<String,String>> holds = tor.getHolds();
		for(Map<String,String> hold : holds) {
			System.out.println(hold);
		}
	}
	
	//@Test
	public void getKDatas() {
		String id = "sz000735";
		List<Map<String,String>> kDatas = tor.getKDatas(id);
		int i=1;
		for(Map<String,String> kdata : kDatas) {
			System.out.println(i++ + " -- " + kdata);
		}
	}
	
	//@Test
	public void getDailyTop100Ids() {
		List<String> ids = tor.getDailyTop100Ids();
		System.out.println(ids);
	}
	
	//@Test
	public void generateAvaTop50() {
		tor.generateAvaTop50(tor.getDailyTop100Ids());
	}
	
	//@Test
	public void getArticles() {
		Map<String,String> a = tor.getArticles();
		System.out.println(a);
	}
	
}

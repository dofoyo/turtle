package com.rhb.turtle.operation.repository;

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@ActiveProfiles("dev2")
public class TurtleOperationRepositoryTests {
	@Autowired
	@Qualifier("turtleOperationRepositoryImp")
	TurtleOperationRepository tr ;

	
	//@Test
	public void getArticleIDs() {
		List<String> ids = tr.getArticleIDs();
		int i=1;
		for(String id : ids) {
			System.out.println(i++ + ". " + id);			
		}
	}
	
	@Test
	public void getDailyTopCodes() {
		List<String> ids = tr.getArticleIDs();
		int i=1;
		for(String id : ids) {
			System.out.println(i++ + ". " + id);
			
			List<Map<String,String>> kdatas = tr.getKDatas(id);
			
			for(Map<String,String> kdata : kdatas) {
				System.out.println(kdata);
			}
		}
	}
	
}

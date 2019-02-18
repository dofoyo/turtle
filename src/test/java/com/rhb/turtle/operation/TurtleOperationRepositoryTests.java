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
	TurtleOperationRepository tr ;

	
	//@Test
	public void getArticleIDs() {
		List<String> ids = tr.getArticleIDs();
		int i=1;
		for(String id : ids) {
			System.out.println(i++ + ". " + id);			
		}
	}
	
	//@Test
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
	
	
	//@Test
	public void getKDatas() {
		String id = "sz000735";
		List<Map<String,String>> kDatas = tr.getKDatas(id);
		int i=1;
		for(Map<String,String> kdata : kDatas) {
			System.out.println(i++ + " -- " + kdata);
		}
	}
	
	//@Test
	public void getDailyTop100Ids() {
		List<String> ids = tr.getDailyTop100Ids();
		System.out.println(ids);
	}
	
	@Test
	public void dd() {
		tr.generateAvaTop50(tr.getDailyTop100Ids());
	}
	
}

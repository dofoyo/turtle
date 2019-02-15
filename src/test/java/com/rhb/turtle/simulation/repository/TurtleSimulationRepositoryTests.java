package com.rhb.turtle.simulation.repository;

import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.simulation.repository.TurtleSimulationRepository;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationRepositoryTests {
	@Autowired
	@Qualifier("turtleRepositorySimulation")
	TurtleSimulationRepository tr ;

	//@Test
	public void testGetCodes() {
		Set<String> codes = tr.getCodes();
		for(String code : codes) {
			System.out.println(code);
		}
	}
	
	//@Test
	public void testGetKDatas() {
		String code = "000001";
		LocalDate beginDate = LocalDate.parse("2008-01-01");
		LocalDate endDate = LocalDate.parse("2018-09-12");
		
		List<Map<String,String>> kDatas = tr.getKDatas(code,beginDate,endDate);
		for(Map<String,String> data : kDatas) {
			System.out.println(data.get("date") + "," + data.get("open")+ "," + data.get("high")+ "," + data.get("low")+ "," + data.get("close")+ "," + data.get("amount"));
		}
	}
	
	//@Test
	public void testGetTops() {
		LocalDate beginDate = LocalDate.parse("2018-01-01");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		LocalDate date;
		String code;
		Map<LocalDate,List<String>> tops = tr.getAvaTops(40, beginDate, endDate);
		for(Map.Entry<LocalDate,List<String>> entry : tops.entrySet()) {
			date = entry.getKey();
			System.out.print(date + ":");
			for(Iterator<String> i= entry.getValue().iterator(); i.hasNext();) {
				code = i.next();
				System.out.print(code + ",");
			}
			System.out.println("\n");
		}
	}
	
	//@Test
	public void testGenerateTops() {
		LocalDate beginDate = LocalDate.parse("2000-01-01");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 100;
		tr.generateTops(top, beginDate, endDate);
	}
	
	//@Test
	public void testGenerateDailyTop100() {
		tr.generateDailyTop100();
	}
	
	@Test
	public void testGenerateAvaTop50() {
		tr.generateAvaTop50();
	}
	
	//@Test
	public void testGetDailyTops() {
		LocalDate beginDate = LocalDate.parse("2019-02-10");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 5;
		Map<LocalDate, List<String>> tops = tr.getDailyTops(top, beginDate, endDate);
		System.out.println(tops);
	}
	
	//@Test
	public void testGetAvaTops() {
		LocalDate beginDate = LocalDate.parse("2019-02-10");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 5;
		Map<LocalDate, List<String>> tops = tr.getAvaTops(top, beginDate, endDate);
		System.out.println(tops);
	}
	
	//@Test
	public void getDailyTopCodes() {
		LocalDate beginDate = LocalDate.parse("2000-01-01");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 50;
		Set<String> codes = tr.getDailyTopIds(top, beginDate, endDate);
		int i=1;
		for(String code : codes) {
			System.out.println(i++ + ". " + code);			
		}
	}
	
}

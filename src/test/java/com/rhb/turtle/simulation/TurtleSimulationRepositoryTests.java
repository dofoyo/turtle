package com.rhb.turtle.simulation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
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

import com.rhb.turtle.simulation.TurtleSimulationRepository;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationRepositoryTests {
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;

	//@Test
	public void getIds() {
		Set<String> ids = turtleSimulationRepository.getIds();
		for(String id : ids) {
			System.out.println(id);
		}
	}
	
	//@Test
	public void getKDatas() {
		String code = "000001";
		LocalDate beginDate = LocalDate.parse("2008-01-01");
		LocalDate endDate = LocalDate.parse("2018-09-12");
		
		List<Map<String,String>> kDatas = turtleSimulationRepository.getKDatas(code,beginDate,endDate);
		for(Map<String,String> data : kDatas) {
			System.out.println(data.get("date") + "," + data.get("open")+ "," + data.get("high")+ "," + data.get("low")+ "," + data.get("close")+ "," + data.get("amount"));
		}
	}
	
	//@Test
	public void getAvaTops() {
		LocalDate beginDate = LocalDate.parse("2018-01-01");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 5;
		LocalDate date;
		String code;
		Map<LocalDate,List<String>> tops = turtleSimulationRepository.getAvaTops(top, beginDate, endDate);
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
	public void generateTops() {
		LocalDate beginDate = LocalDate.parse("2000-01-01");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 100;
		turtleSimulationRepository.generateTops(top, beginDate, endDate);
	}
	
	//@Test
	public void generateDailyTop100() {
		turtleSimulationRepository.generateDailyTop100();
	}
	
	@Test
	public void generateAvaTop50() {
		turtleSimulationRepository.generateAvaTop50();
	}
	
	//@Test
	public void getDailyTops() {
		LocalDate beginDate = LocalDate.parse("2019-02-10");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 5;
		Map<LocalDate, List<String>> tops = turtleSimulationRepository.getDailyTops(top, beginDate, endDate);
		System.out.println(tops);
	}
	
	
	//@Test
	public void getDailyTopIds() {
		LocalDate beginDate = LocalDate.parse("2000-01-01");
		LocalDate endDate = LocalDate.parse("2019-02-12");
		Integer top = 50;
		Set<String> codes = turtleSimulationRepository.getDailyTopIds(top, beginDate, endDate);
		int i=1;
		for(String code : codes) {
			System.out.println(i++ + ". " + code);			
		}
	}
	
	//@Test
	public void getBluechipIds() {
		LocalDate beginDate = LocalDate.parse("2010-02-01");
		LocalDate endDate = LocalDate.parse("2019-01-02");
		Set<String> bluechipIds = new HashSet<String>();
		List<String> tmp; 
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			tmp = turtleSimulationRepository.getBluechipIds(date);
			if(tmp!=null) bluechipIds.addAll(tmp);
		}
		
		Set<String> kdataIds = turtleSimulationRepository.getIds();

		
		for(String id : bluechipIds) {
			if(!kdataIds.contains(id)) {
				System.out.println(id);
			}
		}
	}
	
	//@Test
	public void getFiveKData() {
		String id = "sh600519";
		LocalDateTime datetime = LocalDateTime.parse("2018-01-31 09:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		datetime = datetime.plusMinutes(5);
		Map<String,String> data = turtleSimulationRepository.getFiveKData(id, datetime);
		System.out.println(data);
	}
	
	
	
}

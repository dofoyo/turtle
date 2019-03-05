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

import com.rhb.turtle.simulation.repository.TurtleSimulationCagrRepository;
import com.rhb.turtle.simulation.repository.TurtleSimulationRepository;
import com.rhb.turtle.simulation.repository.entity.BarEntity;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationRepositoryTests {
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;

	@Autowired
	@Qualifier("turtleSimulationCagrRepositoryImp")
	TurtleSimulationCagrRepository cagrRepository ;

	//@Test
	public void generateCAGRTop50() {
		cagrRepository.generateCAGRTop50();
		System.out.println("generateDailyCAGR done!");
	}
	
	//@Test
	public void testGenerateDailyCAGR() {
		cagrRepository.generateDailyCAGR();
		System.out.println("generateDailyCAGR done!");
	}
	
	//@Test
	public void getKDatas() {
/*		String itemID = "sh600900";
		LocalDate endDate = LocalDate.parse("2009-05-18");
		Integer duration = 89;
		System.out.println(endDate);
		List<Map<String,String>> kDatas = turtleSimulationRepository.getKdatas(itemID,duration,endDate);
		for(Map<String,String> data : kDatas) {
			System.out.println(data.get("date") + "," + data.get("open")+ "," + data.get("high")+ "," + data.get("low")+ "," + data.get("close")+ "," + data.get("amount"));
		}*/
	}
	
	//@Test
	public void generateDailyTop100() {
		turtleSimulationRepository.generateDailyTop100();
	}
	
	//@Test
	public void generateAvaTop50() {
		turtleSimulationRepository.generateAvaTop50();
	}
	
	//@Test
	public void getFiveKData() {
		String id = "sh600519";
		LocalDateTime datetime = LocalDateTime.parse("2018-01-31 09:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		datetime = datetime.plusMinutes(5);
		//Map<String,String> data = turtleSimulationRepository.getFiveKData(id, datetime);
		//System.out.println(data);
	}
	
	//@Test
	public void testItemEntityCache() {
		String[] ids = {"sh600030","sz000063"};
		
		LocalDate beginDate = LocalDate.parse("2019-01-27");
		LocalDate endDate = LocalDate.parse("2019-02-13");		
		
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			for(String id : ids) {
				//BarEntity entity = turtleSimulationRepository.getItemEntity(id).getBar(date);
				
				//System.out.println(entity);
			}
		}
	}
	
	
	
}

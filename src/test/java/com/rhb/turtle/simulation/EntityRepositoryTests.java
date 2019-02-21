package com.rhb.turtle.simulation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.simulation.repository.entity.BarEntity;
import com.rhb.turtle.simulation.repository.entity.EntityRepository;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class EntityRepositoryTests {
	@Autowired
	@Qualifier("entityRepositoryImp")
	EntityRepository itemEntityRepository ;

	//@Test
	public void testGet5MinKData() {
		String itemID = "sh600519";
		BarEntity<LocalDateTime> bar;
		LocalDateTime datetime = LocalDateTime.parse("2018-01-31 09:30", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		for(int i=0; i<10; i++) {
			bar = itemEntityRepository.get5MinKData(itemID).getBar(datetime);
			System.out.println(bar);
			datetime = datetime.plusMinutes(5);
			
		}
	}
	
	//@Test
	public void testGetDailyKData() {
		String[] ids = {"sh600030","sz000063"};
		
		LocalDate beginDate = LocalDate.parse("2019-01-27");
		LocalDate endDate = LocalDate.parse("2019-02-13");		
		BarEntity<LocalDate> entity;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			for(String id : ids) {
				entity = itemEntityRepository.getDailyKData(id).getBar(date);
				
				System.out.println(entity);
			}
		}
	}
	
	@Test
	public void testGetDailyTopIds() {
		LocalDate beginDate = LocalDate.parse("2019-01-27");
		LocalDate endDate = LocalDate.parse("2019-02-13");
		
		Map<LocalDate,List<String>> ids;
		List<String> tmp;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			ids = itemEntityRepository.getBluechipIds();
			System.out.println(date);
			tmp = ids.get(date);
			if(tmp!=null) {
				for(String str : tmp) {
					System.out.print(str + ",");
				}
				System.out.println("");
			}
		}
		
	}
	
	
	
}

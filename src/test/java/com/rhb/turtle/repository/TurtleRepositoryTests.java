package com.rhb.turtle.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleRepositoryTests {
	@Autowired
	@Qualifier("TurtleRepositoryImpDzh")
	TurtleRepository tr ;

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
	
	@Test
	public void testGetAmountTops() {
		LocalDate dd = LocalDate.parse("2018-08-31");
		LocalDate beginDate = LocalDate.parse("2018-01-01");
		LocalDate endDate = LocalDate.parse("2018-09-12");
		LocalDate date;
		String code;
		Map<LocalDate,Set<String>> tops = tr.getAmountTops(5, beginDate, endDate);
		for(Map.Entry<LocalDate,Set<String>> entry : tops.entrySet()) {
			date = entry.getKey();
			System.out.print(date + ":");
			for(Iterator<String> i= entry.getValue().iterator(); i.hasNext();) {
				code = i.next();
				System.out.print(code + ",");
			}
			System.out.println("\n");
		}
		
		System.out.println(tops.get(dd));
		System.out.println(tops.get(dd).contains("600887"));
	}
}

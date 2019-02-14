package com.rhb.turtle.spider;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class MarketInfoSpiderTest {
	@Autowired
	@Qualifier("MarketInfoSpiderImp")
	MarketInfoSpider marketInfoSpider;
	
	
	//@Test
	public void getLatestDate() {
		LocalDate date = marketInfoSpider.getLatestMarketDate();
		System.out.println(date);
	}
	
	//@Test
	public void getLatestMarketData() {
		String id = "sh600519";
		Map<String, String> data = marketInfoSpider.getLatestMarketData(id);
		System.out.println(data);
	}
	
	@Test
	public void downLatestTop100() {
		marketInfoSpider.downLatestTop100();
		System.out.println("done! file have been saved!");
	}
	
	//@Test
	public void downKdata() {
		String id = "sz300384";
		marketInfoSpider.downKdata(id);
	}
	
}

package com.rhb.turtle.service;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.turtle.util.FileUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleServiceTest {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("TurtleServiceImp")
	TurtleService ts;
	
	@Test
	public void testGetKDatas() {
		Map<String,String> result = ts.doTrade();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("winRatio: " + result.get("winRatio"));
		System.out.println("CAGR: " + result.get("cagr"));
		FileUtil.writeTextFile(reportPath + "/record" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);

	}
}

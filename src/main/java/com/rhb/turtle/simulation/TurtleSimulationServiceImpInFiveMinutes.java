package com.rhb.turtle.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Kbar;
import com.rhb.turtle.domain.Turtle;
import com.rhb.turtle.util.Line;

@Service("TurtleSimulationServiceImpInFiveMinutes")
public class TurtleSimulationServiceImpInFiveMinutes implements TurtleSimulationService {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;

	@Autowired
	@Qualifier("turtleSimulationSpiderImp")
	TurtleSimulationSpider turtleSimulationSpider ;
	
	private LocalDate beginDate = LocalDate.parse("2018-01-31");
	private LocalDate endDate = LocalDate.parse("2019-02-15");
	
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌一个atr，总资金下跌1%
	 */
	private BigDecimal deficitFactor = new BigDecimal(0.005); 
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot = new BigDecimal(100); 
	
	private Integer maxOfLot = 4;  //当maxOfLot为10时，相当于可以全仓买入一只股票
	
	private Integer top = 10;  //成交量排名前10名，不能低于5
	
	private Integer openDuration = 90;
	private Integer closeDuration = 30;
	
	private BigDecimal initCash = new BigDecimal(1000000);
	
	private boolean isStop = false; 

	/*
	 * 指定某一只牛股按5分钟线进行模拟
	 * 测试结果
	 * 
	 */
	
	@Override
	public Map<String, String> simulate() {
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash);
		String id = "sz000651";
		
		//准备2018-01-31前至少90天K线数据
		//获得上一个交易日收盘后下载的K线数据
		List<Map<String,String>> kDatas;
		LocalDate preBeginDate = LocalDate.parse("2017-01-31");
		LocalDate preEndDate = LocalDate.parse("2018-01-31");
		kDatas = turtleSimulationRepository.getKDatas(id, preBeginDate, preEndDate);
		turtle.addBar(kDatas);
		
		
		Map<String, BigDecimal> prices;

		Map<String,String> fiveKData;
		Map<String,String> dailyKData;
		LocalDateTime datetime = null;
		String[] times = {"09:35","09:40","09:45","09:50","09:55","10:00","10:05","10:10","10:15","10:20","10:25","10:30","10:35","10:40","10:45","10:50","10:55","11:00","11:05","11:10","11:15","11:20","11:25","11:30","13:05","13:10","13:15","13:20","13:25","13:30","13:35","13:40","13:45","13:50","13:55","14:00","14:05","14:10","14:15","14:20","14:25","14:30","14:35","14:40","14:45","14:50","14:55","15:00"};
		long days = endDate.toEpochDay()-beginDate.toEpochDay();
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			System.out.println(++i + "/" + days + "," + date);
			for(String time : times) {
				datetime = LocalDateTime.parse(date.toString() + " " + time, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
				fiveKData = turtleSimulationRepository.getFiveKData(id, datetime);
				if(fiveKData!=null) {
					//System.out.println(fiveKData);
					prices = turtle.getArticlePrices(id);
					prices.put("now", new BigDecimal(fiveKData.get("close")));
					System.out.println(Line.draw(prices));

					turtle.doit(fiveKData, isStop);
				}
			}
			dailyKData = turtleSimulationRepository.getDailyKData(id,date);
			if(dailyKData!=null) turtle.addBar(dailyKData);
		}
		
		return turtle.result();
	}

}

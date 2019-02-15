package com.rhb.turtle.simulation.service;

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

import com.rhb.turtle.domain.Trade;
import com.rhb.turtle.domain2.Kbar;
import com.rhb.turtle.domain2.Turtle;
import com.rhb.turtle.simulation.repository.TurtleSimulationRepository;
import com.rhb.turtle.simulation.spider.MarketInfoSimulationSpider;

@Service("turtleSimulationServiceImp")
public class TurtleSimulationServiceImp implements TurtleSimulationService {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;

	@Autowired
	@Qualifier("marketInfoSimulationSpiderImp")
	MarketInfoSimulationSpider marketInfoSimulationSpider ;
	
	private LocalDate beginDate = LocalDate.parse("2000-01-01");
	private LocalDate endDate = LocalDate.parse("2019-01-01");
	
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌一个atr，总资金下跌1%
	 */
	private BigDecimal deficitFactor = new BigDecimal(0.005); 
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot = new BigDecimal(100); 
	
	private Integer maxOfLot = 4;  //当maxOfLot为10时，相当于可以全仓买入一只股票
	
	private Integer top = 10;  //成交量排名前40名，不能低于5
	
	private Integer openDuration = 90;
	private Integer closeDuration = 30;
	
	private BigDecimal initCash = new BigDecimal(100000);


	@Override
	public void operate() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		//判断是否是交易日
		LocalDate today = LocalDate.now();
		LocalDate theDay = marketInfoSimulationSpider.getLatestMarketDate();
		if(today.equals(theDay)) {
			System.out.println("today is the trade day! Good Luck!");
		}else {
			System.out.println("today is NOT the trade day! take it easy!");
			return;
		}
		
		LocalDateTime start = LocalDateTime.parse(today.toString()+" 09:30:00",df);
		LocalDateTime end = LocalDateTime.parse(today.toString()+" 15:00:00",df);
		LocalDateTime now;
		
		List<String> articleIDs = turtleSimulationRepository.getArticleIDs();
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash);
		
		
		for(int i=0; ; i++) {
			System.out.println(articleIDs.get(i));

			now=LocalDateTime.now(); 
			if(now.isAfter(start) && now.isBefore(end)){
				System.out.println(now.toString());
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			}
			
			if(i==articleIDs.size()-1) i=0;
		}
	}

	@Override
	public void doClosingWork(Integer top) {
		List<String> ids = turtleSimulationRepository.getLatestDailyTopIds();
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Map<String, String> getOperationDetails() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, String> simulate2() {
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash);

		List<Kbar> bars;
		List<Map<String,String>> kDatas;
		Map<String,String> kData;
		List<String> ids;
		long days = endDate.toEpochDay()-beginDate.toEpochDay();
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			System.out.println(++i + "/" + days + "," + date);
			
			ids = turtleSimulationRepository.getAvaTopIds(top, date);
			if(ids!=null) {
				ids.addAll(turtle.getArticleIDsOfOnHand());
				
				for(String id : ids) {
					kData = turtleSimulationRepository.getKData(id,date);
					if(kData!=null) {
						turtle.doit(kData);
						turtle.addBar(kData);
					}
				}
				
				//System.out.println(ids);
				
			}
		}
		
		return turtle.result();
	}
	
	
	@Override
	public Map<String,String> simulate() {
		Trade trade = new Trade(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash, this.beginDate, this.endDate);
		
		Map<LocalDate,List<String>> tops = turtleSimulationRepository.getAvaTops(this.top, this.beginDate, this.endDate);
		
		System.out.println(tops.size());
		
		Set<String> ids = this.getIds(tops);
		int k = 0;
		for(String id : ids) {
			System.out.println(++k + "/" + ids.size());
			trade.addItem(id, "", this.lot, turtleSimulationRepository.getKDatas(id, this.beginDate, this.endDate), tops);
		}
		return trade.getResult();
	}
	
	private Set<String> getIds(Map<LocalDate,List<String>> tops){
		Set<String> ids = new HashSet<String>();
		for(Map.Entry<LocalDate, List<String>> entry : tops.entrySet()) {
			for(String id : entry.getValue()) {
				ids.add(id);
			}
		}
		return ids;
	}
	


}

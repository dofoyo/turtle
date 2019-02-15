package com.rhb.turtle.operation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain2.Turtle;
import com.rhb.turtle.operation.repository.TurtleOperationRepository;
import com.rhb.turtle.operation.spider.MarketInfoOperationSpider;

@Service("turtleOperationServiceImp")
public class TurtleOperationServiceImp implements TurtleOperationService {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("turtleOperationRepositoryImp")
	TurtleOperationRepository turtleOperationRepository ;

	@Autowired
	@Qualifier("marketInfoOperationSpiderImp")
	MarketInfoOperationSpider marketInfoOperationSpider ;
	
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
		LocalDate theDay = marketInfoOperationSpider.getLatestMarketDate();
		if(today.equals(theDay)) {
			System.out.println("today is the trade day! Good Luck!");
		}else {
			System.out.println("today is NOT the trade day! take it easy!");
			return;
		}
		
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash);

		//获得上一个交易日收盘后生成的article.txt
		List<String> articleIDs = turtleOperationRepository.getArticleIDs();
		
		//获得近几天的K线数据
		List<Map<String,String>> kDatas;
		for(String id : articleIDs) {
			kDatas = turtleOperationRepository.getKDatas(id);
			turtle.addBar(kDatas);
		}
		
		LocalDateTime start = LocalDateTime.parse(today.toString()+" 09:30:00",df);
		LocalDateTime end = LocalDateTime.parse(today.toString()+" 15:00:00",df);
		LocalDateTime now;
		
		Map<String, String> latestKdata;
		Map<String, BigDecimal> prices;
		Line line;
		for(int i=0; ; ) {
			now=LocalDateTime.now(); 

			//if(now.isAfter(start) && now.isBefore(end)){
				System.out.print(now.toString() + "      ");
				System.out.print(articleIDs.get(i) + "     ");

				latestKdata = marketInfoOperationSpider.getLatestMarketData(articleIDs.get(i));
				
				prices = turtle.getArticlePrices(articleIDs.get(i));
				line = new Line();
				line.addPot("high", prices.get("high"));
				line.addPot("low", prices.get("low"));
				line.addPot("now", new BigDecimal(latestKdata.get("close")));
				
				System.out.println(line.draw());
				
				turtle.doit(latestKdata);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}			
			//}else {
			//	System.out.println("\ntoday' trade period is over! bye bye! have a good time!");
			//	return;
			//}
			
			i++;
			if(i==articleIDs.size()-1) i=0;
		}
	}
	


	@Override
	public void doClosingWork(Integer top) {
		//List<String> ids = turtleOperationRepository.getLatestDailyTopIds();
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public Map<String, String> getOperationDetails() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	//内部类-------------
	class Line {
		List<Pot> pots = new ArrayList<Pot>();
		public void addPot(String name, BigDecimal price) {
			pots.add(new Pot(name,price));
		}
		
		private String getDots(Pot low, Pot high) {
			StringBuffer sb = new StringBuffer();
			Integer ratio = high.getPrice().subtract(low.getPrice()).divide(low.getPrice(),BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
			for(int i=0; i<ratio; i++) {
				sb.append("-");
			}
			sb.append(high);
			return sb.toString();
		}
		
		public String draw() {
			Collections.sort(pots,new Comparator<Pot>() {
				@Override
				public int compare(Pot o1, Pot o2) {
					return o1.getPrice().compareTo(o2.getPrice());
				}
			});
			
			StringBuffer sb = new StringBuffer();
			for(int i=0; i<pots.size(); i++) {
				if(i==0) {
					sb.append(pots.get(i));
				}else {
					sb.append(getDots(pots.get(i-1),pots.get(i)));
				}
			}
			
			return sb.toString();
		}
		
		class Pot{
			private String name;
			private BigDecimal price;
			public Pot(String name, BigDecimal price) {
				this.name = name;
				this.price = price;
			}
			
			public String getName() {
				return name;
			}
			public void setName(String name) {
				this.name = name;
			}
			public BigDecimal getPrice() {
				return price;
			}
			public void setPrice(BigDecimal price) {
				this.price = price;
			}
			
			public String toString() {
				return name + "(" + price + ")";
			}
			
		}
	};

}

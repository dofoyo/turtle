package com.rhb.turtle.operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Bar;
import com.rhb.turtle.domain.Order;
import com.rhb.turtle.domain.Turtle;
import com.rhb.turtle.util.Line;

@Service("turtleOperationServiceImp")
public class TurtleOperationServiceImp implements TurtleOperationService {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleOperationServiceImp.class);

	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("turtleOperationRepositoryImp")
	TurtleOperationRepository turtleOperationRepository ;

	@Autowired
	@Qualifier("turtlePreyRepositoryImp")
	PreyRepository turtlePreyRepository ;
	
	@Autowired
	@Qualifier("turtleOperationSpiderImp")
	KdataSpider turtleOperationSpider ;

	@Override
	public void tendOnhands() {
        //DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

		//取得最后的交易日
		//LocalDate today = LocalDate.now();
		LocalDate theDay = turtleOperationSpider.getLatestMarketDate();
/*		if(today.equals(theDay)) {
			System.out.println("today is the trade day! Good Luck!");
		}else {
			System.out.println("today is NOT the trade day! take it easy!");
			return;
		}*/
		
		Turtle turtle = new Turtle();

		//获得上一个交易日收盘后生成的article.txt
		Map<String,String> articles = turtleOperationRepository.getArticles();
		
		//获得上一个交易日收盘后下载的K线数据
		List<Map<String,String>> kDatas;
/*		for(Map.Entry<String, String> article : articles.entrySet()) {
			//System.out.println(id);
			kDatas = turtleOperationRepository.getKDatas(article.getKey());
			//System.out.println(kDatas);
			turtle.addBar(kDatas);
			List<Kbar> kbars = turtle.getKbars(id);
			for(Kbar bar : kbars) {
				System.out.println(bar);
			}
		}*/
		
		//导入onhands.json
		//String orderID,String itemID, LocalDate date,Integer direction, BigDecimal price, BigDecimal stopPrice, BigDecimal reopenPrice
		//TODO 只录入时间、代码、价格，系统自动生成stopPrice、reopenPrice
		List<OrderEntity> orders = turtleOperationRepository.getOnhands();
		for(OrderEntity o : orders) {
			kDatas = turtleOperationRepository.getKDatas(o.getItemID());
			turtle.addBar(kDatas);
			turtle.putOrder(new Order(o.getOrderID(),o.getItemID(),LocalDate.parse(o.getDate()),Integer.parseInt(o.getDirection()),new BigDecimal(o.getPrice()),new BigDecimal(o.getStopPrice()),new BigDecimal(o.getReopenPrice())));
		}
		
		//LocalDateTime start = LocalDateTime.parse(today.toString()+" 09:30:00",df);
		//LocalDateTime end = LocalDateTime.parse(today.toString()+" 15:00:00",df);
		LocalDateTime now;
		
		Map<String, String> latestKdata;
		Map<String, BigDecimal> prices;
		while(true) {
			for(Map.Entry<String, String> article : articles.entrySet()) {
				now=LocalDateTime.now(); 

				//if(now.isAfter(start) && now.isBefore(end)){
					System.out.print(now.format(DateTimeFormatter.ofPattern("HH:mm:ss")) + " ");
					System.out.print(article.getKey() + article.getValue() + " ");

					latestKdata = turtleOperationSpider.getLatestMarketData(article.getKey());
					
					prices = turtle.getItemFeatures(article.getKey());
					prices.put("now", new BigDecimal(latestKdata.get("close")));

					System.out.println(Line.draw(prices));
					System.out.println("\n\n");
					
					//turtle.doit(latestKdata);
					
				//}else {
					//System.out.println("\ntoday' trade period is over! bye bye! have a good time!");
					//return;
				//}
			}
			
			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}			

		}
	}
	


	@Override
	public void doClosingWork() {
		LocalDate today = LocalDate.now();
		LocalDate theDay = turtleOperationSpider.getLatestMarketDate();
		if(!today.equals(theDay)) {
			System.out.println("NOT trade date, bye!");
			return;
		}
		LocalDateTime end = LocalDateTime.parse(today.toString() + " 15:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		LocalDateTime now = LocalDateTime.now();
		if(now.isBefore(end)) {
			System.out.println("NOT end of the trade, bye!");
			return;
		}
		
		//下载dailyTop100
		System.out.println("downLatestDailyTop100..............");
		List<String> dailyTop100IDs = turtleOperationSpider.downLatestDailyTop100();
		Set<String>  ids = new HashSet<String>(dailyTop100IDs);

		
		//加入articles.txt
		Map<String,String> articles = turtleOperationRepository.getArticles();
		for(Map.Entry<String, String> article : articles.entrySet()) {
			ids.add(article.getKey());
		}
		
		//下载dailyTop100和article.txt的最新K线数据
		System.out.println("downKdatas..............");
		int i=1;
		int total = ids.size();
		for(String id : ids) {
			System.out.println(i++ + "/" + total);
			turtleOperationSpider.downKdatas(id);
		}
		
		//生成avatop50
		System.out.println("generateAvaTop50..........");
		turtleOperationRepository.generateAvaTop50(dailyTop100IDs);
		
		
		//生成preys.txt
		System.out.println("generate preys");
		turtlePreyRepository.generatePreys();
		
	}
	
	@Override
	public void huntPreys() {
		turtlePreyRepository.generatePreys();
	}



	@Override
	public List<Map<String,String>> getPreys() {
		turtlePreyRepository.generatePreys();
		return turtlePreyRepository.getPreys();
	}

	@Override
	public List<Map<String,String>> getOnhands() {
		List<Map<String,String>> onhands = new ArrayList<Map<String,String>>();
		Map<String,String> onhand;
		
		Turtle turtle = new Turtle();

		List<Map<String,String>> kDatas;
		Map<String, String> latestKdata;
		Map<String, BigDecimal> prices;
		
		//导入onhands.json
		//String orderID,String itemID, LocalDate date,Integer direction, BigDecimal price, BigDecimal stopPrice, BigDecimal reopenPrice
		//TODO 只录入时间、代码、价格，系统自动生成stopPrice、reopenPrice
		List<OrderEntity> orders = turtleOperationRepository.getOnhands();
		for(OrderEntity o : orders) {
			kDatas = turtleOperationRepository.getKDatas(o.getItemID());
			turtle.addBar(kDatas);
			
			turtle.putOrder(new Order(o.getOrderID(),o.getItemID(),LocalDate.parse(o.getDate()),Integer.parseInt(o.getDirection()),new BigDecimal(o.getPrice()),new BigDecimal(o.getStopPrice()),new BigDecimal(o.getReopenPrice())));
			
			latestKdata = turtleOperationSpider.getLatestMarketData(o.getItemID());
			
			prices = turtle.getItemFeatures(o.getItemID());
			
			onhand = new HashMap<String,String>();
			onhand.put("itemID", o.getItemID());
			onhand.put("code", latestKdata.get("code"));
			onhand.put("name", latestKdata.get("name"));
			onhand.put("now", latestKdata.get("close"));
			
			onhand.put("high", prices.get("high").toString());
			onhand.put("low", prices.get("low").toString());
			onhand.put("buy", prices.get("buy").toString());
			onhand.put("stop", prices.get("stop").toString());
			onhand.put("drop", prices.get("drop").toString());
			
			onhands.add(onhand);
			
		}
		return onhands;
	}
}

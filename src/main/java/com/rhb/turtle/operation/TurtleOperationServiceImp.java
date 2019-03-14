package com.rhb.turtle.operation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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

import com.rhb.turtle.api.KdatasView;
import com.rhb.turtle.domain.Bar;
import com.rhb.turtle.domain.Turtle;

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
	public void doClosingWork() {
		LocalDate today = LocalDate.now();
		LocalDate theDay = turtleOperationSpider.getLatestMarketDate();
		if(!today.equals(theDay)) {
			System.out.println("NOT trade date, bye!");
			//return;
		}
		LocalDateTime end = LocalDateTime.parse(today.toString() + " 15:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		LocalDateTime now = LocalDateTime.now();
		if(now.isBefore(end)) {
			System.out.println("NOT end of the trade, bye!");
			//return;
		}
		
		//下载dailyTop100
		System.out.println("downLatestDailyTop100..............");
		//List<String> dailyTop100IDs = turtleOperationSpider.downLatestDailyTop(100);
		List<String> dailyTop100IDs = turtleOperationRepository.getDailyTop100Ids();
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
		List<Map<String,String>> kDatas;
		Map<String,String> kData;
		LocalDate date;
		for(String id : ids) {
			System.out.println(i++ + "/" + total);
			kDatas = turtleOperationRepository.getKDatas(id);
			if(kDatas.size()==0) {
				turtleOperationSpider.downKdatas(id);
			}else {
				kData = kDatas.get(kDatas.size()-1);
				date = LocalDate.parse(kData.get("dateTime"));
				if(date.isBefore(theDay)) {
					turtleOperationSpider.downKdatas(id);
				}
			}
		}
		
		//生成avatop50
		System.out.println("generateAvaTop50..........");
		//turtleOperationRepository.generateAvaTop50(dailyTop100IDs);
		
		
		//生成preys.txt
		System.out.println("generate preys");
		turtlePreyRepository.generatePreys();
		
	}
	
	@Override
	public void huntPreys() {
		LocalDate today, theDay;
		LocalDateTime begin,end, now;
		long times,startTime;
		
		startTime=System.currentTimeMillis(); 
		System.out.println("生成 preys.............");

		today = LocalDate.now();
		theDay = turtleOperationSpider.getLatestMarketDate();
		if(!today.equals(theDay)) {
			System.out.println("NOT the trade day, bye!");
			return;
		}
		begin = LocalDateTime.parse(today.toString() + " 09:30:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		end = LocalDateTime.parse(today.toString() + " 15:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		now = LocalDateTime.now();
		if(now.isAfter(end) || now.isBefore(begin)) {
			System.out.println("NOT the trade time, bye!");
			return;
		}
		
		turtlePreyRepository.generatePreys();
		
		System.out.format(".............生成preys结束, 用时：%d秒\n",(System.currentTimeMillis() - startTime)/1000);
			
/*			try {
				times = 5 * 60 * 1000;
				System.out.println("wait " + times + " minutes, then begin next.");
				Thread.sleep(times);
			} catch (Exception e) {e.printStackTrace();}
*/			
	}

	@Override
	public List<Map<String,String>> getPreys(String status) {
		//generatePreys();
		return turtlePreyRepository.getPreys(status);
	}

	@Override
	public List<Map<String,String>> getHolds() {
		List<Map<String,String>> onhands = new ArrayList<Map<String,String>>();
		Map<String,String> onhand;
		Map<String,String> features;
		
		Turtle turtle = new Turtle();

		List<Map<String,String>> kDatas;
		Map<String, String> latestKdata;
		
		List<Map<String,String>> holds = turtleOperationRepository.getHolds();
		
		String itemID;
		BigDecimal price, stopPrice, reopenPrice,atr;
		for(Map<String,String> hold : holds) {
			
			itemID = hold.get("itemID");
			price = new BigDecimal(hold.get("price"));
			
			kDatas = turtleOperationRepository.getKDatas(itemID);
			if(kDatas.size()==0) {
				turtleOperationSpider.downKdatas(itemID);
				kDatas = turtleOperationRepository.getKDatas(itemID);
			}
			
			turtle.addBars(kDatas);

			latestKdata = turtleOperationSpider.getLatestMarketData(itemID);
			turtle.setLatestBar(latestKdata);
			
			features = turtle.getFeatures(itemID);

			atr = new BigDecimal(features.get("atr"));
			stopPrice = price.subtract(atr);
			reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
			//turtle.putOrder(new Order(order.getOrderID(),order.getItemID(),LocalDate.parse(order.getDate()),Integer.parseInt(order.getDirection()),new BigDecimal(order.getPrice()),new BigDecimal(order.getStopPrice()),new BigDecimal(order.getReopenPrice())));
			
			onhand = new HashMap<String,String>();
			onhand.put("now", features.get("now"));
			onhand.put("high", features.get("openHigh"));
			onhand.put("low", features.get("openLow"));
			
			onhand.put("buy", hold.get("price"));
			onhand.put("stop", stopPrice.toString());
			onhand.put("drop", features.get("dropLow"));
			onhand.put("reopen", reopenPrice.toString());

			
			onhand.put("itemID", itemID);
			onhand.put("code", latestKdata.get("code"));
			onhand.put("name", latestKdata.get("name"));
			
			onhands.add(onhand);
			
		}
		return onhands;
	}

	@Override
	public KdatasView getKdatas(String itemID) {
		KdatasView view = new KdatasView();
		
		Turtle turtle = new Turtle();

		List<Map<String,String>> kDatas = turtleOperationRepository.getKDatas(itemID);
		turtle.addBars(kDatas);
		
		List<Bar> bars = turtle.getBars(itemID);
		LocalDate latestDate = null;
		for(Bar bar : bars) {
			view.addKdata(bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose());
			latestDate = bar.getDate();
		}
		
		Map<String, String> latestKdata = turtleOperationSpider.getLatestMarketData(itemID);
		if(!latestDate.equals(LocalDate.parse(latestKdata.get("dateTime")))){
			view.addKdata(latestKdata.get("dateTime"), latestKdata.get("open"), latestKdata.get("high"), latestKdata.get("low"), latestKdata.get("close"));
		}
		
		view.setItemID(itemID);
		view.setCode(latestKdata.get("code"));
		view.setName(latestKdata.get("name"));

		return view;
	}

	
	private void generatePreys() {
		boolean isTradeDay = false;
		boolean isTradeTime = false;
		LocalDate today = LocalDate.now();
		LocalDate theDay = turtleOperationSpider.getLatestMarketDate();
		if(today.equals(theDay)) {
			isTradeDay = true;
		}
		LocalDateTime end = LocalDateTime.parse(today.toString() + " 15:00:00", DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
		LocalDateTime now = LocalDateTime.now();
		if(now.isBefore(end)) {
			isTradeTime = true;
		}
		if(isTradeDay && isTradeTime) turtlePreyRepository.generatePreys();		
	}
	
	
}

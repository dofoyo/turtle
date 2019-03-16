package com.rhb.turtle.simulation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Bar;
import com.rhb.turtle.domain.Turtle;
import com.rhb.turtle.simulation.repository.TurtleSimulationRepository;
import com.rhb.turtle.simulation.repository.entity.BarEntity;
import com.rhb.turtle.simulation.repository.entity.EntityRepository;
import com.rhb.turtle.simulation.spider.TurtleSimulationSpider;

@Service("turtleSimulationServiceImp")
public class TurtleSimulationServiceImp implements TurtleSimulationService {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;

	
	@Autowired
	@Qualifier("entityRepositoryImp")
	EntityRepository itemEntityRepository ;
	
	@Autowired
	@Qualifier("turtleSimulationSpiderImp")
	TurtleSimulationSpider turtleSimulationSpider ;
	
	private LocalDate beginDate = LocalDate.parse("2010-01-01");
	private LocalDate endDate = LocalDate.parse("2018-09-13");
	
	/*
	 * 亏损因子，即买了一个品种后 ，该品种价格下跌一个atr，总资金下跌百分之几
	 */
	private BigDecimal deficitFactor = new BigDecimal(0.005); 
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot = new BigDecimal(100); 
	
	private Integer maxOfLot = 3;  
	
	private Integer top = 21;  //成交量排名前13名，不能低于5
	
	private Integer openDuration = 89;
	private Integer closeDuration = 34;
	//private BigDecimal tunnle
	
	private BigDecimal initCash = new BigDecimal(1000000);
	
	private boolean isStop  = true;  //是否止损
	
	private Integer gap = 30; 
	
	/*
	 * avtop21 只用gap限制开仓，不限制加仓
	 * top   gap cagr profit
	 * av21  30   16   4027968
	 * da21  30
	 */

	@Override
	public Map<String, String> simulate() {
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash,this.isStop,this.gap);
		//Turtle turtle = new Turtle();
		
		boolean cache = true;
		
		List<Map<String,String>> features;
		Map<String,String> feature;
		List<Bar> bars;
		List<Map<String,String>> kDatas;
		Map<String,String> kData;
		BarEntity barEntity;
		List<String> itemIDs;
		Set<String> tmp;
		long days = endDate.toEpochDay()-beginDate.toEpochDay();
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			features = new ArrayList<Map<String,String>>();
			System.out.println(++i + "/" + days + "," + date);

			//直接从dailyTop100中选前top个进行模拟 - 不止损(年复合收益率4%,盈率42%), 止损(年复合收益率13%,盈率24%)
			itemIDs = turtleSimulationRepository.getDailyTopIds(top, date); cache = false;

			//根据dailyTop100生成avaTop50，从中选前top个进行模拟 , 不止损(年复合收益率13%,盈率38%), 止损(年复合收益率19%,盈率21%)
			//itemIDs = turtleSimulationRepository.getAvaTopIds(top, date);  
			
			//根据dailyTop100生成avaTop50，从中选通道最窄的前top个进行模拟, 不止损(年复合收益率8%,盈率41%), 止损(年复合收益率11%,盈率15%)
			//itemIDs = turtleSimulationRepository.getNvaTopIds(top, date, openDuration); 

			//从gulex生成的bluechips中选取全部进行模拟, 不止损(年复合收益率0%,盈率38%), 止损(年复合收益率0%,盈率20%)
			//itemIDs = turtleSimulationRepository.getBluechipIds(date);

			//指定某一只牛股进行模拟，
			//格力电器（000651, 不止损年复合收益率5%）
			//贵州茅台（600519,不止损(年复合收益率14%,盈率65%), 止损(年复合收益率0%,盈率27%)）
			//中国平安（601318,不止损年复合收益率8%）
			//itemIDs = new ArrayList<String>();  itemIDs.add("sh600705");			
			
			if(itemIDs!=null) {
				
				//加入目前还持有的id，通过set去重
				tmp = new HashSet<String>(itemIDs);
				for(String id: turtle.getArticleIDsOfOnHand()) {
					tmp.add(id);
				}

				//重新补充历史记录
				Iterator<String> ids = tmp.iterator();
				while(ids.hasNext()){
				    String itemID = ids.next();
					
				    turtle.clearBars(itemID);
					kDatas = turtleSimulationRepository.getKdatas(itemID, openDuration, date);
					if(kDatas==null || kDatas.size()==0) {
						ids.remove();
					}else {
						turtle.addBars(kDatas);
						if(!cache) itemEntityRepository.EvictDailyKDataCache();
					}
				}
				
				for(String itemID : tmp) {
					barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
					if(barEntity!=null) {  //停牌的排除 
						kData = barEntity.getMap();
						turtle.setLatestBar(kData);  
						if(!barEntity.getHigh().equals(barEntity.getLow())) { //排除一字板，因为无法成交，
							feature = turtle.getFeatures(itemID);
							if(feature!=null) {
								features.add(feature);
							}
						}
						if(!cache) itemEntityRepository.EvictDailyKDataCache();
					}
				}

				Collections.sort(features, new Comparator<Map<String,String>>(){
					@Override
					public int compare(Map<String, String> o1, Map<String, String> o2) {
						BigDecimal hl1 = new BigDecimal(o1.get("hlgap"));
						BigDecimal hl2 = new BigDecimal(o2.get("hlgap"));
						return hl2.compareTo(hl1);
					}
				});
				
				//System.out.println(features);
				
				for(Map<String,String> f : features) {
					barEntity = itemEntityRepository.getDailyKData(f.get("itemID")).getBar(date);
					if(barEntity!=null && !barEntity.getHigh().equals(barEntity.getLow())) { //排除停牌的和一字板，因为无法成交，
						kData = barEntity.getMap();
						turtle.doit(kData);
					}
				}
				
				//System.out.println(ids);
				
			}
		}
		
		return turtle.result();
	}

}

/**
 * 	beginDate = LocalDate.parse("2010-01-01");
	endDate = LocalDate.parse("2018-09-13");
	deficitFactor = new BigDecimal(0.005); 
	maxOfLot = 5;  
	top = 13; 
	openDuration = 89;
	closeDuration = 34;
	isStop = true;
	
	//直接从dailyTop100中选前top个进行模拟 - 不止损(年复合收益率4%,盈率42%), 止损(年复合收益率13%,盈率24%)
	itemIDs = turtleSimulationRepository.getDailyTopIds(top, date);

	//根据dailyTop100生成avaTop50，从中选前top个进行模拟 , 不止损(年复合收益率13%,盈率38%), 止损(年复合收益率19%,盈率21%)
	//itemIDs = turtleSimulationRepository.getAvaTopIds(top, date);  
	
	//根据dailyTop100生成avaTop50，从中选通道最窄的前top个进行模拟, 不止损(年复合收益率8%,盈率41%), 止损(年复合收益率11%,盈率15%)
	//itemIDs = turtleSimulationRepository.getNvaTopIds(top, date, openDuration); 

	//从gulex生成的bluechips中选取全部进行模拟, 不止损(年复合收益率0%,盈率38%), 止损(年复合收益率0%,盈率20%)
	//itemIDs = turtleSimulationRepository.getBluechipIds(date);

	//指定某一只牛股进行模拟，
	//贵州茅台（600519,不止损(年复合收益率14%,盈率65%), 止损(年复合收益率0%,盈率27%)）
	//itemIDs = new ArrayList<String>();
	//itemIDs.add("sh600519");
 * 
 * 
 */

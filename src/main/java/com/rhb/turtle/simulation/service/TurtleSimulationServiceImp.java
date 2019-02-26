package com.rhb.turtle.simulation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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
	
	private LocalDate beginDate = LocalDate.parse("2017-01-01");
	private LocalDate endDate = LocalDate.parse("2019-02-23");
	
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌一个atr，总资金下跌1%
	 */
	private BigDecimal deficitFactor = new BigDecimal(0.005); 
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot = new BigDecimal(100); 
	
	private Integer maxOfLot = 5;  
	
	private Integer top = 13;  //成交量排名前13名，不能低于5
	
	private Integer openDuration = 55;
	private Integer closeDuration = 21;
	
	private BigDecimal initCash = new BigDecimal(100000);
	
	private boolean isStop  = true;  //是否止损


	@Override
	public Map<String, String> simulate() {
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash);

		List<Bar> bars;
		List<Map<String,String>> kDatas;
		Map<String,String> kData;
		BarEntity barEntity;
		List<String> itemIDs;
		Set<String> tmp;
		long days = endDate.toEpochDay()-beginDate.toEpochDay();
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			System.out.println(++i + "/" + days + "," + date);

			//直接从dailyTop100中选前top个进行模拟 - 不止损(年复合收益率4%,盈率42%), 止损(年复合收益率13%,盈率24%)
			//itemIDs = turtleSimulationRepository.getDailyTopIds(top, date);

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
			itemIDs = new ArrayList<String>();
		
			itemIDs.add("sz000735");			
			
			if(itemIDs!=null) {
				
				//加入目前还持有的id，通过set去重
				tmp = new HashSet<String>(itemIDs);
				for(String id: turtle.getArticleIDsOfOnHand()) {
					if(!tmp.contains(id)) itemIDs.add(id);
				}
				
				for(String itemID : itemIDs) {
					barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
					if(barEntity!=null && !barEntity.getHigh().equals(barEntity.getLow())) { //排除一字板，因为无法成交，
						kData = barEntity.getMap();
						turtle.doit(kData,isStop);
						turtle.addBar(kData);
					}
					//itemEntityRepository.EvictDailyKDataCache();
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

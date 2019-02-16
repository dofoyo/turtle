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
	
	private LocalDate beginDate = LocalDate.parse("2010-01-01");
	private LocalDate endDate = LocalDate.parse("2019-02-12");
	
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
	public Map<String, String> simulate2() {
		Turtle turtle = new Turtle(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash);

		List<Kbar> bars;
		List<Map<String,String>> kDatas;
		Map<String,String> kData;
		List<String> ids;
		Set<String> tmp;
		long days = endDate.toEpochDay()-beginDate.toEpochDay();
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			System.out.println(++i + "/" + days + "," + date);
			
			ids = turtleSimulationRepository.getAvaTopIds(top, date);
			//ids = turtleSimulationRepository.getNvaTopIds(top, date, openDuration);
			//ids = new ArrayList<String>();
			//ids.add("sz000651");
			if(ids!=null) {
				
				//加入目前还持有的id，通过set去重
				tmp = new HashSet<String>(ids);
				for(String id: turtle.getArticleIDsOfOnHand()) {
					if(!tmp.contains(id)) ids.add(id);
				}
				
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

}

package com.rhb.turtle.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
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
import com.rhb.turtle.repository.TurtleRepository;

@Service("TurtleServiceImp")
public class TurtleServiceImp implements TurtleService {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("TurtleRepositoryImpDzh")
	TurtleRepository turtleRepository ;
	
	private LocalDate beginDate = LocalDate.parse("2016-01-01");
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
	public Map<String,String> simulate() {
		Trade trade = new Trade(this.deficitFactor,this.openDuration,this.closeDuration,this.maxOfLot,this.initCash, this.beginDate, this.endDate);
		
		Map<LocalDate,List<String>> tops = turtleRepository.getAvaTops(this.top, this.beginDate, this.endDate);
		
		Set<String> ids = this.getIds(tops);
		int k = 0;
		for(String id : ids) {
			System.out.println(++k + "/" + ids.size());
			trade.addItem(id, "", this.lot, turtleRepository.getKDatas(id, this.beginDate, this.endDate),tops);
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
	

	@Override
	public void doClosingWork(Integer top) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void operate() {
		
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
		Set<String> ids;
		long days = endDate.toEpochDay()-beginDate.toEpochDay();
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			
			System.out.println(++i + "/" + days);
			kDatas = turtleRepository.getKDatas(date,top);
			if(kDatas!=null && kDatas.size()>0) {
				ids = turtle.getArticleIDsOfOnHand();
				if(ids!=null && ids.size()>0) {
					for(String id : ids) {
						kData = turtleRepository.getKData(id,date);
						if(kData!=null) {
							kDatas.add(kData);
						}
					}
				}
				
				turtle.doit(kDatas);
			}
		}
		
		return turtle.result();
	}
	
	


}

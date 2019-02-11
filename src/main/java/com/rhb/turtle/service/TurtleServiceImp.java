package com.rhb.turtle.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Trade;
import com.rhb.turtle.repository.TurtleRepository;

@Service("TurtleServiceImp")
public class TurtleServiceImp implements TurtleService {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("TurtleRepositoryImpDzh")
	TurtleRepository turtleRepository ;
	
	private LocalDate beginDate = LocalDate.parse("2018-01-01");
	private LocalDate endDate = LocalDate.parse("2019-01-01");
	
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌1%，总资金也下跌1%
	 */
	private BigDecimal deficitFactor = new BigDecimal(0.01); 
	
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot = new BigDecimal(100); 
	
	private Integer maxOfLot = 10;  //当maxOfLot为10时，相当于可以全仓买入一只股票
	
	
	private Integer top = 40;  //成交量排名前40名，不能低于5
	
	/*
	 * 通道区间
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,5,0.01,55,20,10万): 34万, 盈率 = 38%, 年复合增长率14%，有三年亏损
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,5,0.01,90,30,10万): 40万, 盈率 = 45%, 年复合增长率16%，有三年亏损
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,5,0.01,30,10,10万): 40万, 盈率 = 53%，年复合增长率16%，有一年亏损
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,5,0.01,20,10,10万): 50万, 盈率 = 52%，年复合增长率19%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,10,0.01,20,10,10万): 100万, 盈率 = 55%，年复合增长率29%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,20,0.01,20,10,10万): 316万, 盈率 = 54%，年复合增长率46%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,30,0.01,20,10,10万): 214万, 盈率 = 51%，年复合增长率40%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,35,0.01,20,10,10万): 180万, 盈率 = 51%，年复合增长率37%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,40,0.01,20,10,10万): 377万, 盈率 = 51%，年复合增长率49%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,45,0.01,20,10,10万): 370万, 盈率 = 51%，年复合增长率49%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,50,0.01,20,10,10万): 343万, 盈率 = 51%，年复合增长率48%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,60,0.01,20,10,10万): 272万, 盈率 = 50%，年复合增长率44%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,80,0.01,20,10,10万): 219万, 盈率 = 49%，年复合增长率40%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,100,0.01,20,10,10万): 258万, 盈率 = 50%，年复合增长率43%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(3,110,0.01,20,10,10万): 245万, 盈率 = 50%，年复合增长率42%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(5,40,0.01,20,10,10万): 739万, 盈率 = 55%，年复合增长率61%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(8,40,0.01,20,10,10万): 985万, 盈率 = 58%，年复合增长率66%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(10,40,0.01,20,10,10万): 996万, 盈率 = 58%，年复合增长率66%，每年盈利  YYY
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(20,40,0.01,20,10,10万): 902万, 盈率 = 58%，年复合增长率64%，每年盈利
	 * (maxLot,top,deficitFactor,openDuration,closeDuration,cash)=(10,40,0.01,20,10,100万): 1亿2738万, 盈率 = 58%，年复合增长率71%，每年盈利  YYY
	 *  
	 */
	private Integer openDuration = 20;
	private Integer closeDuration = 10;
	
	private BigDecimal initCash = new BigDecimal(1000000);
	
	@Override
	public Map<String,String> doTrade() {
		Trade trade = new Trade(this.deficitFactor, this.openDuration, this.closeDuration,this.maxOfLot,this.initCash, this.beginDate, this.endDate);
		
		Map<LocalDate,Set<String>> tops = turtleRepository.getAmountTops(this.top, this.beginDate, this.endDate);
		
		Set<String> codes = turtleRepository.getCodes();
		int k = 0;
		for(String code : codes) {
			System.out.println(++k + "/" + codes.size());
			trade.addItem(code, "", this.lot, turtleRepository.getKDatas(code, this.beginDate, this.endDate),tops);
		}
		
		return trade.getResult();
	}
	
	


}

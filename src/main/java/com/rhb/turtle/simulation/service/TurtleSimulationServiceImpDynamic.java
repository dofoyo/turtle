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
import com.rhb.turtle.util.FileUtil;
import com.rhb.turtle.util.JsonUtil;

@Service("turtleSimulationServiceImpDynamic")
public class TurtleSimulationServiceImpDynamic implements TurtleSimulationService {
	@Value("${reportPath}")
	private String reportPath;	
	
	@Value("${cagrsPath}")
	private String cagrsPath;
	
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;
	
	@Autowired
	@Qualifier("entityRepositoryImp")
	EntityRepository itemEntityRepository ;
	
	private LocalDate beginDate = LocalDate.parse("2010-01-01");
	private LocalDate endDate = LocalDate.parse("2018-09-13");
	
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
	private BigDecimal initCash = new BigDecimal(100000);
	
	private Integer total = 17808;
	private Integer i=1;
	
	@Override
	public Map<String, String> simulate() {
		long startTime=System.currentTimeMillis(); 
		
		Set<String> ids = turtleSimulationRepository.getIdsFromDir();
		
		this.total = this.total * ids.size();
/*		for(String id : ids) {
			System.out.println(id);
			go(id);
			itemEntityRepository.EvictDailyKDataCache();
		}*/
		
		go("sh603298");
		long used = (System.currentTimeMillis() - startTime)/1000;                //获得当前时间
		System.out.println("用时：" + used + "秒");          
		return null;
	}
	
	private void go(String itemID) {
		boolean[] isStops = {true, false};
		Integer[] oDurations = {21,34,55,89};
		Integer[] cDurations = {8,13,21,34,55};
		List<ItemValue> ivs = new ArrayList<ItemValue>();
		ItemValue iv;
		BigDecimal cagr;
		Map<String, String> runResult;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(5)) {
			iv = new ItemValue(date);
			for(Integer oDuration : oDurations) {
				for(Integer cDuration : cDurations) {
					for(boolean iStop : isStops) {
						if(oDuration > cDuration) {
							System.out.println(i++ + "/" + total + "," + itemID);
							//System.out.print("=");
							runResult = run(itemID,date,oDuration,cDuration,iStop);
							if(runResult!=null) {
								cagr = new BigDecimal(runResult.get("cagr"));
								if(cagr.compareTo(iv.getCAGR())==1) {
									iv.setCAGR(cagr);
									iv.setWinRatio(new BigDecimal(runResult.get("winRatio")));
									iv.setOpenDuration(oDuration);
									iv.setCloseDuration(cDuration);
									iv.setStop(iStop);
									//System.out.println(itemID + ": " + iv);
								}							
							}
							//System.out.println(oDuration + "," + cDuration + "," + iStop);
						}
					}
				}
			}
			//System.out.println("");
			if(iv.getCAGR().compareTo(new BigDecimal(0))==1) {
				ivs.add(iv);
				FileUtil.writeTextFile(cagrsPath+"/"+itemID+".json", JsonUtil.objectToJson(iv) + "\n", true);
				//System.out.println("write " + itemID + " to file");
			}
		}
	}
	
	
	private Map<String, String> run(String itemID, LocalDate endDate,Integer oDuration, Integer cDuration,boolean iStop){
		Turtle turtle = new Turtle(this.deficitFactor,oDuration,cDuration,this.maxOfLot,this.initCash);

		//准备bDate前至少233天K线数据
		//获得上一个交易日收盘后下载的K线数据
		BarEntity<LocalDate> barEntity = null;
		LocalDate preBeginDate = null;
		int k=1;
		for(LocalDate date=endDate.minusDays(1); date.isAfter(this.beginDate) ; date = date.minusDays(1)) {
			barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
			if(barEntity!=null) k++;
			if(k==233) {
				preBeginDate = date;
				break;
			}
		}
		if(preBeginDate==null) return null;
		
		//System.out.println("prepare k datas...");
		Map<String,String> kData = null;
		for(LocalDate date=preBeginDate; date.isBefore(endDate) ; date = date.plusDays(1)) {
			//System.out.println(date);
			barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
			if(barEntity!=null) {
				kData = barEntity.getMap();
				turtle.addBar(kData);
			}
		}
		//System.out.println("prepare k datas  done!");
		
		
		//long days = bDate.toEpochDay()- preBeginDate.toEpochDay();
		int i = 0;
		//System.out.println(days);
		//System.out.println("from " + preBeginDate + " to " + endDate);
		for(LocalDate date=preBeginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
			if(barEntity!=null) {
				kData = barEntity.getMap();
				turtle.doit(kData,iStop);
				turtle.addBar(kData);
			}
		}
		
		return turtle.result();
	}
	
	class ItemValue{
		private String date;
		private Integer openDuration;
		private Integer closeDuration;
		private boolean isStop;
		private BigDecimal CAGR;
		private BigDecimal winRatio;
		
		public ItemValue(LocalDate date) {
			this.date = date.toString();
			this.CAGR = new BigDecimal(0);
		}
		
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public Integer getOpenDuration() {
			return openDuration;
		}
		public void setOpenDuration(Integer openDuration) {
			this.openDuration = openDuration;
		}
		public Integer getCloseDuration() {
			return closeDuration;
		}
		public void setCloseDuration(Integer closeDuration) {
			this.closeDuration = closeDuration;
		}
		public boolean isStop() {
			return isStop;
		}
		public void setStop(boolean isStop) {
			this.isStop = isStop;
		}
		public BigDecimal getCAGR() {
			return CAGR;
		}
		public void setCAGR(BigDecimal cAGR) {
			CAGR = cAGR;
		}
		public BigDecimal getWinRatio() {
			return winRatio;
		}
		public void setWinRatio(BigDecimal winRatio) {
			this.winRatio = winRatio;
		}

		@Override
		public String toString() {
			return "ItemValue [date=" + date + ", openDuration=" + openDuration + ", closeDuration=" + closeDuration
					+ ", isStop=" + isStop + ", CAGR=" + CAGR + ", winRatio=" + winRatio + "]";
		}
		
	}
}

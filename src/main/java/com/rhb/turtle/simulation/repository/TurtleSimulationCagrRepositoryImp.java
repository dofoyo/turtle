package com.rhb.turtle.simulation.repository;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Turtle;
import com.rhb.turtle.simulation.repository.entity.BarEntity;
import com.rhb.turtle.simulation.repository.entity.CagrEntity;
import com.rhb.turtle.simulation.repository.entity.EntityRepository;
import com.rhb.turtle.simulation.repository.entity.ItemEntity;
import com.rhb.turtle.util.FileUtil;
import com.rhb.turtle.util.JsonUtil;
import com.rhb.turtle.util.Progress;

@Service("turtleSimulationCagrRepositoryImp")
public class TurtleSimulationCagrRepositoryImp implements TurtleSimulationCagrRepository {
	@Value("${reportPath}")
	private String reportPath;	
	
	@Value("${cagrsPath}")
	private String cagrsPath;
	
	@Value("${cagrTop50File}")
	private String cagrTop50File;

	@Value("${cagrTopFile}")
	private String cagrTopFile;
	
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
	private boolean isStop = false;
	private Integer gap = 30;
	
	@Override
	public List<String> getCAGRTops(Integer top, LocalDate date) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String[] getCAGRTopIDs() {
		return FileUtil.readTextFile(cagrTopFile).split(",");
	}
	
	class Board{
		private Map<String,Integer> b = new HashMap<String,Integer>();
		Integer i;
		public void put(String str) {
			i = b.get(str);
			if(i==null) {
				i = 1;
			}else {
				i++;
			}
			b.put(str, i);
		}
		
		public String show() {
			StringBuffer sb = new StringBuffer();
			for(Map.Entry<String,Integer> entry : b.entrySet()) {
				sb.append(entry.getKey() + "       " + entry.getValue() + " \n");
			}
			return sb.toString();
		}
	}
	
	@Override
	public void generateDailyCAGR() {
		long beginTime=System.currentTimeMillis(); 
		long startTime; 
		long used;
		
		String[] ids = this.getCAGRTopIDs();
		Board board = new Board();
		//this.total = this.total * ids.size();
		int total = ids.length;
		int i=1;
		String winner;

		for(String id : ids) {
			startTime=System.currentTimeMillis(); 
			winner = go(id);
			if(winner!=null) board.put(winner);
			itemEntityRepository.EvictDailyKDataCache();
			used = (System.currentTimeMillis() - startTime)/1000;                //获得当前时间
			System.out.format("  %d/%d, %s, %d秒\n",i++,total,id,used);
			System.out.println(board.show());
		}
		
		//go("sh601299");
		used = (System.currentTimeMillis() - beginTime)/1000;                //获得当前时间
		System.out.println("用时：" + used + "秒");          

	}

	private String go(String itemID) {
		String winner = null;
		boolean[] isStops = {true, false};
		Integer[] oDurations = {89,55,34,21};
		Integer[] cDurations = {55,34,21,13,8};
		Integer[] gaps = {20,25,30,35,40,45,50,55,60,65};
		//List<ItemValue> ivs = new ArrayList<ItemValue>();
		ItemValue iv;
		String csv;
		BigDecimal cagr;
		Map<String, String> runResult;
		
		//Integer total = 17808;
		Integer total = 280;
		Integer i=1;
		String writeToFile = " ";
		LocalDate date = endDate;
		//for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(5)) {
			iv = new ItemValue(date);
			csv = null;
			for(Integer gap : gaps) {
				for(Integer oDuration : oDurations) {
					for(Integer cDuration : cDurations) {
						for(boolean iStop : isStops) {
							if(oDuration > cDuration) {
								//System.out.print("=");
								runResult = run(itemID,beginDate,endDate,oDuration,cDuration,iStop,gap);
								if(runResult!=null) {
									cagr = new BigDecimal(runResult.get("cagr"));
									if(cagr.compareTo(iv.getCAGR())==1) {
										iv.setCAGR(cagr);
										iv.setWinRatio(new BigDecimal(runResult.get("winRatio")));
										iv.setOpenDuration(oDuration);
										iv.setCloseDuration(cDuration);
										iv.setStop(iStop);
										iv.setGap(gap);
										
										csv = runResult.get("CSV");
										//System.out.println(itemID + ": " + iv);
									}							
								}
								//System.out.println(oDuration + "," + cDuration + "," + iStop);
								Progress.show(total,i);
								i++;
							}
						}
					}
				}
			}
			
			//System.out.println("i = " + i);
			
			if(iv.getCAGR().compareTo(new BigDecimal(0))==1) {
				//ivs.add(iv);
				FileUtil.writeTextFile(cagrsPath  + "/" + itemID + ".json", JsonUtil.objectToJson(iv) + "\n", true);
				FileUtil.writeTextFile(reportPath + "/" + itemID + "_" + iv.getKey() + ".csv", csv , false);
				writeToFile = " y";
				
/*				if(iv.getCAGR().compareTo(new BigDecimal(100))==1) {
					FileUtil.writeTextFile(reportPath + "/" + itemID + "_" + iv.getOpenDuration() + "_" + iv.getCloseDuration() + "_" + System.currentTimeMillis() + ".csv",csv, false);
					writeToFile = "yy";
				}
*/				winner = iv.getKey();
			}
		//}
		System.out.format(" %s ",writeToFile);
		
		return winner;
	}
	
	
	private Map<String, String> run(String itemID, LocalDate beginDate,LocalDate endDate,Integer oDuration, Integer cDuration,boolean iStop,Integer gap){
		Turtle turtle = new Turtle(this.deficitFactor,oDuration,cDuration,this.maxOfLot,this.initCash,this.isStop,this.gap);
		
		
		//准备bDate前至少233天K线数据
		//获得上一个交易日收盘后下载的K线数据
		BarEntity<LocalDate> barEntity = itemEntityRepository.getDailyKData(itemID).getBar(endDate);
		if(barEntity==null) return null;  //非交易日或停牌，不计算
		
/*		LocalDate preBeginDate = null;
		int k=1;
		for(LocalDate date=endDate.minusDays(1); date.isAfter(this.beginDate); date = date.minusDays(1)) {
			barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
			if(barEntity!=null) k++;
			if(k==233) {
				preBeginDate = date;
				break;
			}
		}
		if(preBeginDate==null) return null;*/
		
		Map<String,String> kData = null;

		//System.out.println("prepare k datas...");
		/*
		for(LocalDate date=preBeginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			//System.out.println(date);
			barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
			if(barEntity!=null && !barEntity.getHigh().equals(barEntity.getLow())) { //排除一字板，因为无法成交，
				kData = barEntity.getMap();
				turtle.addBar(kData);
			}
		}
		*/
		//System.out.println("prepare k datas  done!");
		
		
		//long days = bDate.toEpochDay()- preBeginDate.toEpochDay();
		int i = 0;
		//System.out.println(days);
		//System.out.println("from " + preBeginDate + " to " + endDate);
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			barEntity = itemEntityRepository.getDailyKData(itemID).getBar(date);
			if(barEntity!=null && !barEntity.getHigh().equals(barEntity.getLow())) { //排除一字板，因为无法成交，
				kData = barEntity.getMap();
				turtle.doit(kData);
				turtle.addBar(kData);
			}
		}
		
		turtle.result().put("beginDate", beginDate.toString());
		turtle.result().put("endDate", endDate.toString());
		
		return turtle.result();
	}
	
	class ItemValue{
		private String date;
		private Integer openDuration;
		private Integer closeDuration;
		private boolean isStop;
		private BigDecimal CAGR;
		private BigDecimal winRatio;
		private Integer gap;
		
		public String getKey() {
			String key = "";
			if(openDuration!=null && closeDuration!=null) {
				//key = getOpenDuration().toString() + "_" + getCloseDuration() + "_" + (isStop() ? "1" : "0") + "_" + gap.toString();
				key = gap.toString();

			}
			return key;
		}
		
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

		public Integer getGap() {
			return gap;
		}

		public void setGap(Integer gap) {
			this.gap = gap;
		}

		@Override
		public String toString() {
			return "ItemValue [date=" + date + ", openDuration=" + openDuration + ", closeDuration=" + closeDuration
					+ ", isStop=" + isStop + ", CAGR=" + CAGR + ", winRatio=" + winRatio + ", gap=" + gap + "]";
		}

		
	}

	@Override
	public void generateCAGRTop50() {
		Integer top = 50;
		List<File> files = FileUtil.getFiles(cagrsPath, "json", true);
		String itemID;
		CagrEntity cagr;
		ItemEntity item;
		BarEntity bar;
		Set<LocalDate> dates;
		Map<LocalDate,TreeSet<CagrEntity>> tops = new HashMap<LocalDate,TreeSet<CagrEntity>>();
		TreeSet<CagrEntity> cagrs;
		
		int i=1;
		int total = files.size();
		for(File file : files) {
			System.out.println(i++ + "/" + total + ", generate tree set");
			
			itemID = file.getName().substring(0, 8);
			item = itemEntityRepository.getDailyCagr(itemID);
			dates = item.getDates();
			for(LocalDate date : dates) {
				cagr = item.getCagr(date);
				bar = itemEntityRepository.getDailyKData(itemID).getBar(date);
				if(cagr!=null && bar!=null) {
					if(tops.containsKey(cagr.getDate())) {
						cagrs = tops.get(cagr.getDate());
					}else {
						cagrs = new TreeSet<CagrEntity>();
						tops.put(cagr.getDate(), cagrs);
					}
					cagrs.add(cagr);
					if(cagrs.size()>top) {
						cagrs.pollLast();
					}
				}
			}
			itemEntityRepository.EvictDailyCagrsCache();
			itemEntityRepository.EvictDailyKDataCache();
		}
		
		StringBuffer sb = new StringBuffer();
		List<LocalDate> thedates = new ArrayList<LocalDate>(tops.keySet());
		Collections.sort(thedates);
		
		i=1;
		total = thedates.size();
		for(LocalDate theDate : thedates) {
			System.out.println(i++ + "/" + total + ", generate string");
			cagrs = tops.get(theDate);
			sb.append(theDate);
			sb.append(",");
			//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
			for(Iterator<CagrEntity> it = cagrs.iterator() ; it.hasNext();) {
				cagr = it.next();
				sb.append(cagr.getItemID());
				sb.append(",");
				//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
			
			//System.out.println("\n");
		}
		
		FileUtil.writeTextFile(cagrTop50File, sb.toString(), false);
		
	}


	
}

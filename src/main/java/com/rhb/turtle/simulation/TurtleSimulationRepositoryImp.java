package com.rhb.turtle.simulation;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.util.FileUtil;

@Service("turtleSimulationRepositoryImp")
public class TurtleSimulationRepositoryImp implements TurtleSimulationRepository{
	@Value("${kDataPath}")
	private String kDataPath;
	
	@Value("${dailyTop100File}")
	private String dailyTop100File;
	
	@Value("${avaTop50File}")
	private String avaTop50File;

	@Value("${bluechipsFile}")
	private String bluechipsFile;
	
	Map<LocalDate,List<String>> avaTopIds = null;
	Map<LocalDate,List<String>> dailyTopIds = null;
	Map<LocalDate,List<String>> bluechipIds = null;
	
	Map<String,Map<LocalDate,BarEntity>> kDatas = new HashMap<String,Map<LocalDate,BarEntity>>();
	Map<String,Integer> kDatasCounter = new HashMap<String,Integer>();
	
	Map<String,String> kDatasFile = new HashMap<String, String>();
	
	@Override
	public Map<String, String> getFiveKData(String id, LocalDateTime datetime) {
		String fileString = this.kDatasFile.get(id);
		if(fileString == null) {
			fileString = this.getKdatasFile(id);
		}
		Map<String,String> kData = null;
		String[] lines = fileString.split("\n");
		String[] columns = lines[0].split("\t");
		LocalDateTime dt;
		Integer length = lines.length;
		
		StringBuffer sb = new StringBuffer();
		for(int i=2; i<length; i++) {
			columns = lines[i].split("\t");
			dt = LocalDateTime.parse("20" + columns[0],DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			if(datetime.equals(dt)) {
				kData = new HashMap<String,String>();
				kData.put("id",id);
				kData.put("date", LocalDate.of(dt.getYear(), dt.getMonth(), dt.getDayOfMonth()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				kData.put("open", columns[1]);
				kData.put("high", columns[2]);
				kData.put("low", columns[3]);
				kData.put("close", columns[4]);
			}
		}
		return kData;
	}
	
	@Override
	public List<String> getBluechipIds(LocalDate date){
		if(bluechipIds == null) {
			this.initBluechipIds();
		}
		List<String> ids = bluechipIds.get(date);
		return ids!=null && ids.size()>0 ? ids : null;
	}

	@Override
	public List<String> getNvaTopIds(Integer top, LocalDate date, Integer duration) {
		List<String> nvas = new ArrayList<String>();

		List<Tunnel> tunnels = new ArrayList<Tunnel>();
		List<String> topIds = this.getAvaTopIds(50, date);
		
		//System.out.println("date: " + date);
		//System.out.println("top: " + top);
		//System.out.println("duration: " + duration);
		
		if(topIds==null) return null;
		
		for(String id : topIds) {
			tunnels.add(getTunnel(id,date,duration));
		}
		
		Collections.sort(tunnels, new Comparator<Tunnel>() {

			@Override
			public int compare(Tunnel o1, Tunnel o2) {
				return o1.getWidth().compareTo(o2.getWidth());
			}
			
		});
		
		for(int i=0; i<tunnels.size() && i<top; i++) {
			//System.out.println(tunnels.get(i).getId() + "," + tunnels.get(i).getWidth());
			nvas.add(tunnels.get(i).getId());
		}
		return nvas;
	}
	
	class Tunnel{
		private String id;
		private BigDecimal width;
		public Tunnel(String id, BigDecimal width) {
			this.id = id;
			this.width = width;
		}
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public BigDecimal getWidth() {
			return width;
		}
		public void setWidth(BigDecimal width) {
			this.width = width;
		}
	}
	
	private Tunnel getTunnel(String id, LocalDate date, Integer duration) {
		BigDecimal high = new BigDecimal(0);
		BigDecimal low = new BigDecimal(100000);
		LocalDate theDate = LocalDate.parse(date.toString());
		Map<String,String> kdata;
		for(int i=0; i<duration; ) {
			kdata = getDailyKData(id, theDate);
			
			if(kdata!=null) {
				if(high.compareTo(new BigDecimal(kdata.get("high")))==-1) high=new BigDecimal(kdata.get("high"));
				if(low.compareTo(new BigDecimal(kdata.get("low")))==1) low=new BigDecimal(kdata.get("low"));
				i++;
			}
			theDate = theDate.minusDays(1);
		}
		return new Tunnel(id,high.subtract(low).divide(low,BigDecimal.ROUND_HALF_UP));
	}
	
	
	
	@Override
	public List<String> getAvaTopIds(Integer top, LocalDate date) {
		if(avaTopIds == null) {
			this.initAvaTopIds();
		}
		List<String> ids = avaTopIds.get(date);
		return ids!=null && ids.size()>0 ? ids.subList(0, top) : null;
	}


	@Override
	public List<String> getDailyTopIds(Integer top, LocalDate date) {
		if(dailyTopIds == null) {
			this.initDailyTopIds();
		}
		List<String> ids = dailyTopIds.get(date);
		return ids!=null && ids.size()>0 ? ids.subList(0, top) : null;
	}

	
	
	@Override
	public Map<String,String> getDailyKData(String id, LocalDate date) {
		Map<LocalDate,BarEntity> datas = this.kDatas.get(id);
		if(datas == null) {
			datas = this.getKdatasFromTxt(id);
			
			this.kDatas.put(id, datas);
			this.kDatasCounter.put(id, 1);
			
			//System.out.println("kDatas.size() = " + this.kDatas.size());
		}else {
			this.kDatasCounter.put(id, this.kDatasCounter.get(id)+1);
			
		}
		Map<String,String> kData = null;

		BarEntity bar = datas.get(date);
		if(bar!=null) {
			kData = new HashMap<String,String>();
			kData.put("id", id);
			kData.put("date", date.toString());
			kData.put("open", bar.getOpen().toString());
			kData.put("high", bar.getHigh().toString());
			kData.put("low", bar.getLow().toString());
			kData.put("close", bar.getClose().toString());
		}
		//System.out.println("kDatas.size = " + this.kDatas.size());
		return kData;
	}
	
	private void initAvaTopIds() {
		avaTopIds = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(avaTop50File).split("\n");
		String[] columns;
		List<String> ids;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			ids = new ArrayList<String>();
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			for(int i=1; i<columns.length; i++) {
				ids.add(columns[i]);
			}
			avaTopIds.put(date, ids);				
		}
	}

	private void initDailyTopIds() {
		dailyTopIds = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(dailyTop100File).split("\n");
		String[] columns;
		List<String> ids;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			ids = new ArrayList<String>();
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			for(int i=1; i<columns.length; i++) {
				ids.add(columns[i]);
			}
			dailyTopIds.put(date, ids);				
		}
	}
	
	private void initBluechipIds() {
		bluechipIds = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(bluechipsFile).split("\n");
		String[] columns;
		List<String> ids;
		LocalDate date;
		String id;
		for(String line : lines) {
			columns = line.split(",");
			ids = new ArrayList<String>();
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			for(int i=1; i<columns.length; i++) {
				id = columns[i];
				ids.add(id.indexOf("60")==0 ? "sh"+id : "sz"+id);
			}
			bluechipIds.put(date, ids);				
		}
	}
	
	//--------------------------------
	
	@Override
	public List<Map<String, String>> getKDatas(LocalDate date, Integer top) {
		if(avaTopIds == null) {
			this.initAvaTopIds();
		}
		List<String> ids = avaTopIds.get(date);
		if(ids == null) {
			return null;
		}

		List<Map<String,String>> kDatas = new ArrayList<Map<String,String>>();
		BarEntity bar;
		Map<String,String> kData;
		for(int i=0; i<ids.size() && i<top; i++) {
			kData = getDailyKData(ids.get(i), date);
			if(kData!=null) {
				kDatas.add(kData);
			}
		}

		return kDatas;
	}

	
	@Override
	public List<Map<String, String>> getKDatas(String id, LocalDate beginDate, LocalDate endDate) {
		List<Map<String,String>> kDatas = new ArrayList<Map<String,String>>();
		
		String file = this.kDataPath + "/" + id + ".txt";
		//System.out.println(this.kDataPath);
		
		String[] lines = FileUtil.readTextFile(file).split("\n");
		String[] cells;
		LocalDate date;
		int length = lines.length;
		for(int i=2; i<length; i++) {
			cells = lines[i].split("\t");
			//LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,BigDecimal tr
			Map<String,String> kData = new HashMap<String,String>();
			kData.put("date", cells[0]);
			date = LocalDate.parse(cells[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
			if(date.isAfter(beginDate) && date.isBefore(endDate)) {
				kData.put("id", id);
				kData.put("date", date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				kData.put("open", cells[1]);
				kData.put("high", cells[2]);
				kData.put("low", cells[3]);
				kData.put("close", cells[4]);
				kData.put("amount", cells[6]);
				
				kDatas.add(kData);				
			}
		}
		return kDatas;
	}

	@Override
	public Set<String> getIds() {
		Set<String> codes = new HashSet<String>();
		List<File> files = FileUtil.getFiles(this.kDataPath, null, true);
		for(File file : files) {
			codes.add(file.getName().substring(0, 8));
		}
		
		return codes;
	}
	
	@Override
	public Map<LocalDate, List<String>> getDailyTops(Integer top,LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,List<String>> tops = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(dailyTop100File).split("\n");
		String[] columns;
		List<String> codes;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			codes = new ArrayList<String>();
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(date.isAfter(beginDate) && date.isBefore(endDate)) {
				for(int i=1; i<columns.length && i<=top; i++) {
					codes.add(columns[i]);
				}
				tops.put(date, codes);				
			}
		}
		return tops;
	}

	@Override
	public void generateTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,TreeSet<AvaBar>> dailyTops = new HashMap<LocalDate,TreeSet<AvaBar>>();
		LocalDate date;
		AvaBar bar;
		TreeSet<AvaBar> ts;
		String file;			
		String[] lines;
		String[] cells;
		int length;
		Avarage ava;
		Set<String> ids = this.getIds();
		int d = 0;
		for(String id : ids) {
			System.out.println(++d + "/" + ids.size());

			ava = new Avarage();
			file = this.kDataPath + "/" + id + ".txt";			
			lines = FileUtil.readTextFile(file).split("\n");
			length = lines.length;
			for(int i=2; i<length; i++) {
				cells = lines[i].split("\t");
				date = LocalDate.parse(cells[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				
				bar = new AvaBar(date,id,new BigDecimal(cells[6]));
				
				bar.setAva(ava.getAmountAvarage(bar));
				
				if(date.isAfter(beginDate) && date.isBefore(endDate) && ava.isOk()) {
					if(dailyTops.containsKey(date)) {
						ts = dailyTops.get(date);
					}else {
						ts = new TreeSet<AvaBar>();
						dailyTops.put(date, ts);
					}
					ts.add(bar);
					
					if(ts.size()>top) {
						ts.pollLast();
					}
				}
			}
		}
		
		StringBuffer sb = new StringBuffer();
		List<LocalDate> dates = new ArrayList<LocalDate>(dailyTops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			sb.append(theDate);
			sb.append(":");
			
			ts = dailyTops.get(theDate);
			if(ts.size()==top) {
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<AvaBar> i = ts.iterator() ; i.hasNext();) {
					bar = i.next();
					sb.append(bar.getId());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		//FileUtil.writeTextFile(topsFile, sb.toString(), false);
		
		
		
		
	}

	@Override
	public void generateDailyTop100() {
		LocalDate beginDate = LocalDate.parse("2000/01/01",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		LocalDate endDate = LocalDate.parse("2019/02/12",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Integer top = 100;
		
		Map<LocalDate,TreeSet<DailyBar>> dailyTops = new HashMap<LocalDate,TreeSet<DailyBar>>();
		LocalDate date;
		DailyBar dailyBar;
		TreeSet<DailyBar> dailyBars;
		String file;			
		String[] lines;
		String[] cells;
		int length;
		
		Set<String> ids = this.getIds();
		int d = 0;
		for(String id : ids) {
			System.out.println(++d + "/" + ids.size());

			file = this.kDataPath + "/" + id + ".txt";			
			lines = FileUtil.readTextFile(file).split("\n");
			length = lines.length;
			for(int i=2; i<length; i++) {
				cells = lines[i].split("\t");
				date = LocalDate.parse(cells[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				
				dailyBar = new DailyBar(date,id,new BigDecimal(cells[6]));
				
				if(date.isAfter(beginDate) && date.isBefore(endDate)) {
					if(dailyTops.containsKey(date)) {
						dailyBars = dailyTops.get(date);
					}else {
						dailyBars = new TreeSet<DailyBar>();
						dailyTops.put(date, dailyBars);
					}
					dailyBars.add(dailyBar);
					
					if(dailyBars.size()>top) {
						dailyBars.pollLast();
					}
				}
			}
		}
		
		StringBuffer sb = new StringBuffer();
		List<LocalDate> dates = new ArrayList<LocalDate>(dailyTops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			dailyBars = dailyTops.get(theDate);
			if(dailyBars.size()==top) {
				sb.append(theDate);
				sb.append(",");
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<DailyBar> i = dailyBars.iterator() ; i.hasNext();) {
					dailyBar = i.next();
					sb.append(dailyBar.getCode());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		FileUtil.writeTextFile(dailyTop100File, sb.toString(), false);
		
	}


	
	@Override
	public Map<LocalDate, List<String>> getAvaTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,List<String>> tops = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(avaTop50File).split("\n");
		String[] columns;
		List<String> codes;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			codes = new ArrayList<String>();
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(date.isAfter(beginDate) && date.isBefore(endDate)) {
				for(int i=1; i<columns.length && i<=top; i++) {
					codes.add(columns[i]);
				}
				tops.put(date, codes);				
			}
		}
		return tops;
	}

	private String getKdatasFile(String id) {
		String file = this.kDataPath + "/" + id + "_5.txt";			
		return FileUtil.readTextFile(file);
	}
	
	private Map<LocalDate,BarEntity> getKdatasFromTxt(String id){
		Map<LocalDate,BarEntity> datas = new HashMap<LocalDate,BarEntity>();
		
		String file = this.kDataPath + "/" + id + ".txt";			
		String[] lines = FileUtil.readTextFile(file).split("\n");
		
		Integer length = lines.length;
		
		String[] columns = lines[0].split("\t");
		String code = columns[0].substring(1, 7);
		String name = columns[1];
		
		LocalDate date;
		BarEntity bar;
		BigDecimal open;
		BigDecimal high;
		BigDecimal low;
		BigDecimal close;
		BigDecimal amount;
		
		for(int i=2; i<length; i++) {
			columns = lines[i].split("\t");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
			open = new BigDecimal(columns[1]);
			high = new BigDecimal(columns[2]);
			low = new BigDecimal(columns[3]);
			close = new BigDecimal(columns[4]);
			amount = new BigDecimal(columns[6]);
			
			bar = new BarEntity(code,name,date,open,high,low,close,amount);
			
			datas.put(date, bar);

		}

		return datas;
	}

	@Override
	public Set<String> getDailyTopIds(Integer top, LocalDate beginDate, LocalDate endDate) {
		Set<String> codes = new HashSet<String>();
		String[] lines = FileUtil.readTextFile(dailyTop100File).split("\n");
		String[] columns;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			if(date.isAfter(beginDate) && date.isBefore(endDate)) {
				for(int i=1; i<columns.length && i<=top; i++) {
					if(columns[1].indexOf("sh")==0 || columns[1].indexOf("sz")==0) {
						codes.add(columns[i]);
					}else {
						System.out.println("ERROR: NOT A CODE, " + columns[1]);
					}
				}
			}
		}
		return codes;
	}

	@Override
	public Map<LocalDate, Set<String>> getTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,TreeSet<AvaBar>> dailyTops = new HashMap<LocalDate,TreeSet<AvaBar>>();
		LocalDate date;
		AvaBar bar;
		TreeSet<AvaBar> ts;
		String file;			
		String[] lines;
		String[] cells;
		int length;
		Avarage ava;
		Set<String> codes = this.getIds();
		int d = 0;
		for(String code : codes) {
			System.out.println(++d + "/" + codes.size() + "," + code);

			ava = new Avarage();
			file = this.kDataPath + "/" + code + ".txt";			
			lines = FileUtil.readTextFile(file).split("\n");
			length = lines.length;
			for(int i=2; i<length; i++) {
				cells = lines[i].split("\t");
				date = LocalDate.parse(cells[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				
				bar = new AvaBar(date,code,new BigDecimal(cells[6]));
				
				bar.setAva(ava.getAmountAvarage(bar));
				
				if(date.isAfter(beginDate) && date.isBefore(endDate) && ava.isOk()) {
					if(dailyTops.containsKey(date)) {
						ts = dailyTops.get(date);
					}else {
						ts = new TreeSet<AvaBar>();
						dailyTops.put(date, ts);
					}
					ts.add(bar);
					
					if(ts.size()>top) {
						ts.pollLast();
					}
				}
			}
		}
		
		Map<LocalDate,Set<String>> tops = new HashMap<LocalDate,Set<String>>();
		Set<String> theCodes;
		List<LocalDate> dates = new ArrayList<LocalDate>(dailyTops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			ts = dailyTops.get(theDate);
			if(ts.size()==top) {
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<AvaBar> i = ts.iterator() ; i.hasNext();) {
					bar = i.next();
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
					if(tops.containsKey(theDate)) {
						theCodes = tops.get(theDate);
					}else {
						theCodes = new HashSet<String>();
						tops.put(theDate, theCodes);
					}
					theCodes.add(bar.getId());
				}
				//System.out.println("\n");
			}
		}
		
		/*
		for(Map.Entry<LocalDate,TreeSet<BarEntity>> entry : dailyTops.entrySet()) {
			date = entry.getKey();
			System.out.print(date + ":");
			for(Iterator<BarEntity> i= entry.getValue().iterator(); i.hasNext();) {
				bar = i.next();
				System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				if(tops.containsKey(date)) {
					theCodes = tops.get(date);
				}else {
					theCodes = new HashSet<String>();
					tops.put(date, theCodes);
				}
				theCodes.add(bar.getCode());
			}
			System.out.println("\n");
		}
		*/
		
		return tops;
	}


	@Override
	public void generateAvaTop50() {
		LocalDate beginDate = LocalDate.parse("2000/01/01",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		LocalDate endDate = LocalDate.parse("2019/02/12",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Integer top = 50;

		Map<LocalDate,TreeSet<AvaBar>> avaTops = new HashMap<LocalDate,TreeSet<AvaBar>>();
		Avarage avarage;
		AvaBar avaBar = null;
		TreeSet<AvaBar> avaBars;
		String file;			
		String[] lines;
		String[] cells;
		Integer length;
		LocalDate date;
		
		Set<String> ids = getDailyTopIds(top, beginDate, endDate);
		
		int d = 0;
		for(String id : ids) {
			System.out.println(++d + "/" + ids.size());

			avarage = new Avarage();
			file = this.kDataPath + "/" + id + ".txt";			
			lines = FileUtil.readTextFile(file).split("\n");
			length = lines.length;
			for(int i=2; i<length; i++) {
				cells = lines[i].split("\t");
				date = LocalDate.parse(cells[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				
				avaBar = new AvaBar(date,id,new BigDecimal(cells[6]));
				
				avaBar.setAva(avarage.getAmountAvarage(avaBar));
				
				if(date.isAfter(beginDate) && date.isBefore(endDate) && avarage.isOk()) {
					if(avaTops.containsKey(date)) {
						avaBars = avaTops.get(date);
					}else {
						avaBars = new TreeSet<AvaBar>();
						avaTops.put(date, avaBars);
					}
					avaBars.add(avaBar);
					
					if(avaBars.size()>top) {
						avaBars.pollLast();
					}
				}
			}
		}

		StringBuffer sb = new StringBuffer();
		List<LocalDate> dates = new ArrayList<LocalDate>(avaTops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			avaBars = avaTops.get(theDate);
			if(avaBars.size()==top) {
				sb.append(theDate);
				sb.append(",");
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<AvaBar> i = avaBars.iterator() ; i.hasNext();) {
					avaBar = i.next();
					sb.append(avaBar.getId());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		FileUtil.writeTextFile(avaTop50File, sb.toString(), false);
		
	}

}

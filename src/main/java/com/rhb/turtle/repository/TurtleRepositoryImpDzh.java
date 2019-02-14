package com.rhb.turtle.repository;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
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

@Service("TurtleRepositoryImpDzh")
public class TurtleRepositoryImpDzh implements TurtleRepository{
	@Value("${kDataPath}")
	private String kDataPath;
	
	@Value("${dailyTop100}")
	private String dailyTop100;
	
	@Value("${avaTop50}")
	private String avaTop50;
	
	Map<LocalDate,List<String>> avaTops = null;
	
	Map<String,Map<LocalDate,BarEntity>> kDatas = new HashMap<String,Map<LocalDate,BarEntity>>();

	@Override
	public List<Map<String, String>> getKDatas(LocalDate date, Integer top) {
		if(avaTops == null) {
			this.initAvaTops();
		}
		List<String> ids = avaTops.get(date);
		if(ids == null) {
			return null;
		}

		List<Map<String,String>> kDatas = new ArrayList<Map<String,String>>();
		BarEntity bar;
		Map<String,String> kData;
		for(int i=0; i<ids.size() && i<top; i++) {
			kData = getKData(ids.get(i), date);
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
				kData.put("date", cells[0]);
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
	public Set<String> getCodes() {
		Set<String> codes = new HashSet<String>();
		List<File> files = FileUtil.getFiles(this.kDataPath, null, true);
		for(File file : files) {
			codes.add(file.getName().substring(0, 8));
		}
		
		return codes;
	}

	private  Set<String> getIds() {
		Set<String> ids = new HashSet<String>();
		List<File> files = FileUtil.getFiles(this.kDataPath, null, true);
		for(File file : files) {
			ids.add(file.getName().substring(0, 8));
		}
		
		return ids;
	}
	
	@Override
	public Map<LocalDate, List<String>> getDailyTops(Integer top,LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,List<String>> tops = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(dailyTop100).split("\n");
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
		
		FileUtil.writeTextFile(dailyTop100, sb.toString(), false);
		
	}

	private void initAvaTops() {
		avaTops = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(avaTop50).split("\n");
		String[] columns;
		List<String> codes;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			codes = new ArrayList<String>();
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			for(int i=1; i<columns.length; i++) {
				codes.add(columns[i]);
			}
			avaTops.put(date, codes);				
		}
	}
	
	@Override
	public Map<LocalDate, List<String>> getAvaTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,List<String>> tops = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(avaTop50).split("\n");
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
	public Map<String,String> getKData(String id, LocalDate date) {
		Map<LocalDate,BarEntity> datas = this.kDatas.get(id);
		if(datas == null) {
			datas = this.getKdatasFromTxt(id);
			this.kDatas.put(id, datas);	
		}
		Map<String,String> kData = null;

		BarEntity bar = datas.get(date);
		if(bar!=null) {
			kData = new HashMap<String,String>();
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
		String[] lines = FileUtil.readTextFile(dailyTop100).split("\n");
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
		Set<String> codes = this.getCodes();
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
		
		FileUtil.writeTextFile(avaTop50, sb.toString(), false);
		
	}


	@Override
	public List<String> getAvaTops(Integer top, LocalDate date) {
		// TODO Auto-generated method stub
		return null;
	}

}

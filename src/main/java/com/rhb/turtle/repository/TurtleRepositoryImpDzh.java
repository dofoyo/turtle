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
	
	@Override
	public List<Map<String, String>> getKDatas(String code, LocalDate beginDate, LocalDate endDate) {
		List<Map<String,String>> kDatas = new ArrayList<Map<String,String>>();
		
		String file = this.kDataPath + "/" + code + ".txt";
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
			codes.add(file.getName().substring(0, 6));
		}
		
		return codes;
	}

	@Override
	public Map<LocalDate, Set<String>> getAmountTops(Integer top,LocalDate beginDate, LocalDate endDate) {
		Map<LocalDate,TreeSet<BarEntity>> dailyTops = new HashMap<LocalDate,TreeSet<BarEntity>>();
		LocalDate date;
		BarEntity bar;
		TreeSet<BarEntity> ts;
		String file;			
		String[] lines;
		String[] cells;
		int length;
		Avarage ava;
		Set<String> codes = this.getCodes();
		int d = 0;
		for(String code : codes) {
			System.out.println(++d + "/" + codes.size());

			ava = new Avarage();
			file = this.kDataPath + "/" + code + ".txt";			
			lines = FileUtil.readTextFile(file).split("\n");
			length = lines.length;
			for(int i=2; i<length; i++) {
				cells = lines[i].split("\t");
				date = LocalDate.parse(cells[0],DateTimeFormatter.ofPattern("yyyy/MM/dd"));
				
				bar = new BarEntity(date,code,new BigDecimal(cells[6]));
				
				bar.setAva(ava.getAva(bar));
				
				if(date.isAfter(beginDate) && date.isBefore(endDate) && ava.isOk()) {
					if(dailyTops.containsKey(date)) {
						ts = dailyTops.get(date);
					}else {
						ts = new TreeSet<BarEntity>();
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
				for(Iterator<BarEntity> i = ts.iterator() ; i.hasNext();) {
					bar = i.next();
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
					if(tops.containsKey(theDate)) {
						theCodes = tops.get(theDate);
					}else {
						theCodes = new HashSet<String>();
						tops.put(theDate, theCodes);
					}
					theCodes.add(bar.getCode());
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
	public Set<String> getTops() {
		// TODO Auto-generated method stub
		return null;
	}

}

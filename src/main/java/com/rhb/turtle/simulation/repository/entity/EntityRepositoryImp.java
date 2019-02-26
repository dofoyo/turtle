package com.rhb.turtle.simulation.repository.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.turtle.util.FileUtil;
import com.rhb.turtle.util.JsonUtil;

@Service("entityRepositoryImp")
public class EntityRepositoryImp implements EntityRepository{
	@Value("${kDataPath}")
	private String kDataPath;

	@Value("${cagrsPath}")
	private String cagrsPath;
	
	@Value("${dailyTop100File}")
	private String dailyTop100File;
	
	@Value("${avaTop50File}")
	private String avaTop50File;

	@Value("${bluechipsFile}")
	private String bluechipsFile;

	@Override
	@Cacheable("5MinKDatas")
	public ItemEntity<LocalDateTime> get5MinKData(String itemID) {
		ItemEntity<LocalDateTime> item = null;
		
		String file = this.kDataPath + "/" + itemID + "_5.txt";			
		//System.out.println("read from file " + file);
		String[] lines = FileUtil.readTextFile(file).split("\n");
		
		Integer length = lines.length;
		
		String[] columns = lines[0].split("\t");
		String code = columns[0].substring(1, 7);
		String name = columns[1];
		
		item = new ItemEntity<LocalDateTime>(itemID,code,name);
		
		LocalDateTime datetime;
		BarEntity<LocalDateTime> bar;
		BigDecimal open;
		BigDecimal high;
		BigDecimal low;
		BigDecimal close;
		BigDecimal amount;
		
		for(int i=2; i<length; i++) {
			columns = lines[i].split("\t");
			//System.out.println("20" + columns[0]);
			datetime = LocalDateTime.parse("20" + columns[0],DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm"));
			open = new BigDecimal(columns[1]);
			high = new BigDecimal(columns[2]);
			low = new BigDecimal(columns[3]);
			close = new BigDecimal(columns[4]);
			amount = new BigDecimal(columns[6]);
			
			bar = new BarEntity<LocalDateTime>(itemID,code,name,datetime,open,high,low,close,amount);
			
			item.setBar(datetime, bar);

		}
		return item;
	}
	

	@Override
	@CacheEvict(value="dailyKDatas",allEntries=true)
	public void EvictDailyKDataCache() {}
	
	@Override
	@Cacheable("dailyKDatas")
	public ItemEntity<LocalDate> getDailyKData(String itemID){
		ItemEntity<LocalDate> item = null;
		
		String file = this.kDataPath + "/" + itemID + ".txt";			
		//System.out.println("read from file " + file);
		String[] lines = FileUtil.readTextFile(file).split("\n");
		
		Integer length = lines.length;
		
		String[] columns = lines[0].split("\t");
		String code = columns[0].substring(1, 7);
		String name = columns[1];
		
		item = new ItemEntity<LocalDate>(itemID,code,name);
		
		LocalDate date;
		BarEntity<LocalDate> bar;
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
			
			bar = new BarEntity<LocalDate>(itemID,code,name,date,open,high,low,close,amount);
			
			item.setBar(date, bar);
		}
		return item;
	}
	
	

	@Override
	@Cacheable("dailyTop100Ids")
	public Map<LocalDate,List<String>> getDailyTopIds() {
		return getIDs(dailyTop100File);
	}

	@Override
	@Cacheable("avaTop50Ids")
	public Map<LocalDate,List<String>> getAvaTopIds() {
		return getIDs(avaTop50File);
	}

	@Override
	@Cacheable("bluechipIds")
	public Map<LocalDate,List<String>> getBluechipIds() {
		return getIDs(bluechipsFile);
	}
	
	private Map<LocalDate,List<String>> getIDs(String file){
		//System.out.println("get from file...");
		Map<LocalDate,List<String>> dailyIDSEntity = new HashMap<LocalDate,List<String>>();
		String[] lines = FileUtil.readTextFile(file).split("\n");
		String[] columns;
		LocalDate date;
		List<String> ids;
		for(String line : lines) {
			columns = line.split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			ids = new ArrayList<String>();
			for(int i=1; i<columns.length; i++) {
				ids.add(columns[i]);
			}
			dailyIDSEntity.put(date, ids);
		}
		return dailyIDSEntity;
	}


	@Override
	@Cacheable("dailyCagrs")
	public ItemEntity<LocalDate> getDailyCagr(String itemID) {
		ItemEntity<LocalDate> item = new ItemEntity<LocalDate>(itemID,"","");
		
		String file = this.cagrsPath + "/" + itemID + ".json";			
		//System.out.println("read from file " + file);
		String[] source = FileUtil.readTextFile(file).split("\n");
		for(String str : source) {
			CagrEntity cagr = JsonUtil.jsonToPojo(str, CagrEntity.class);
			if(cagr != null) {
				cagr.setItemID(itemID);
				item.setCagr(cagr.getDate(), cagr);
				//System.out.println(cagr);
			}
		}
		
		return item;
	}

	@Override
	@CacheEvict(value="dailyCagrs",allEntries=true)
	public void EvictDailyCagrsCache() {}

}

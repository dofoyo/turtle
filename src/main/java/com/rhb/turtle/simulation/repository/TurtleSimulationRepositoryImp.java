package com.rhb.turtle.simulation.repository;

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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.turtle.simulation.repository.entity.BarEntity;
import com.rhb.turtle.simulation.repository.entity.ItemEntity;
import com.rhb.turtle.simulation.repository.entity.EntityRepository;
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
	
	@Autowired
	@Qualifier("entityRepositoryImp")
	EntityRepository entityRepository;
	
	@Override
	public List<String> getBluechipIds(LocalDate date){
		return entityRepository.getBluechipIds().get(date);
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
		BarEntity<LocalDate> kdata;
		for(int i=0; i<duration; ) {
			kdata = entityRepository.getDailyKData(id).getBar(date);
			
			if(kdata!=null) {
				if(high.compareTo(kdata.getHigh())==-1) high=kdata.getHigh();
				if(low.compareTo(kdata.getLow())==1) low=kdata.getLow();
				i++;
			}
			theDate = theDate.minusDays(1);
		}
		return new Tunnel(id,high.subtract(low).divide(low,BigDecimal.ROUND_HALF_UP));
	}
	
	@Override
	public List<String> getAvaTopIds(Integer top, LocalDate date) {
		List<String> tmp = entityRepository.getAvaTopIds().get(date);
		if(tmp==null) {
			return null;
		}else {
			return tmp.size()>top ? tmp.subList(0, top) : tmp;
		}
	}


	@Override
	public List<String> getDailyTopIds(Integer top, LocalDate date) {
		List<String> tmp = entityRepository.getDailyTopIds().get(date);
		if(tmp==null) {
			return null;
		}else {
			return tmp.size()>top ? tmp.subList(0, top) : tmp;
		}
	}

	@Override
	public Set<String> getIdsFromDir() {
		Set<String> ids = new HashSet<String>();
		List<File> files = FileUtil.getFiles(this.kDataPath, null, true);
		for(File file : files) {
			ids.add(file.getName().substring(0, 8));
		}
		
		return ids;
	}
	
	@Override
	public void generateDailyTop100() {
		LocalDate beginDate = LocalDate.parse("2000/01/01",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Integer top = 100;
		
		Map<LocalDate,TreeSet<DailyBar>> dailyTops = new HashMap<LocalDate,TreeSet<DailyBar>>();
		DailyBar dailyBar = null;
		TreeSet<DailyBar> dailyBars;
		
		ItemEntity<LocalDate> item;
		List<LocalDate> dates;
		
		Set<String> ids = this.getIdsFromDir();
		int d = 0;
		for(String id : ids) {
			System.out.println(++d + "/" + ids.size());

			item = entityRepository.getDailyKData(id);
			dates = new ArrayList<LocalDate>(item.getDateTimes());
			Collections.sort(dates);
			for(LocalDate date : dates) {
				if(date.isAfter(beginDate)) {
					dailyBar = new DailyBar(date,id,item.getBar(date).getAmount());
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
		dates = new ArrayList<LocalDate>(dailyTops.keySet());
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


	private Set<String> getDailyTopIds() {
		Set<String> ids = new HashSet<String>();
		Map<LocalDate,List<String>> map = entityRepository.getDailyTopIds();
		for(Map.Entry<LocalDate, List<String>> entry : map.entrySet()) {
			for(String str : entry.getValue()) {
				ids.add(str);
			}
		}
		return ids;
	}


	@Override
	public void generateAvaTop50() {
		LocalDate beginDate = LocalDate.parse("2000-01-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		Integer top = 50;

		ItemEntity<LocalDate> item;
		List<LocalDate> dates;
		
		Map<LocalDate,TreeSet<AvaBar<LocalDate>>> avaTops = new HashMap<LocalDate,TreeSet<AvaBar<LocalDate>>>();
		Avarage avarage;
		AvaBar<LocalDate> avaBar = null;
		TreeSet<AvaBar<LocalDate>> avaBars;
		
		Set<String> ids = getDailyTopIds();
		int d = 0;
		for(String id : ids) {
			System.out.println(++d + "/" + ids.size());
			avarage = new Avarage();
			item = entityRepository.getDailyKData(id);
			dates = new ArrayList<LocalDate>(item.getDateTimes());
			Collections.sort(dates);
			for(LocalDate date : dates) {
				if(date.isAfter(beginDate)) {
					avaBar = new AvaBar<LocalDate>(date,id,item.getBar(date).getAmount());
					avaBar.setAva(avarage.getAmountAvarage(avaBar));
					if(avarage.isOk()) {
						if(avaTops.containsKey(date)) {
							avaBars = avaTops.get(date);
						}else {
							avaBars = new TreeSet<AvaBar<LocalDate>>();
							avaTops.put(date, avaBars);
						}
						avaBars.add(avaBar);
						if(avaBars.size()>top) {
							avaBars.pollLast();
						}
					}
				}
			}
		}

		StringBuffer sb = new StringBuffer();
		dates = new ArrayList<LocalDate>(avaTops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			avaBars = avaTops.get(theDate);
			if(avaBars.size()==top) {
				sb.append(theDate);
				sb.append(",");
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<AvaBar<LocalDate>> i = avaBars.iterator() ; i.hasNext();) {
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

	@Override
	public List<Map<String, String>> getKdatas(String itemID, Integer duration, LocalDate endDate) {
		List<Map<String, String>> kdatas = new ArrayList<Map<String,String>>();
		
		ItemEntity<LocalDate> item;
		item = entityRepository.getDailyKData(itemID);
		List<LocalDate> dates;
		if(item!=null) {
			int toIndex = 0;
			for(int i=0; i<item.getDateTimes().size(); i++) {
				toIndex = i;
				if(!item.getDateTimes().get(i).isBefore(endDate)) {
					break;
				}
			}
			
			int fromIndex = toIndex>duration ? toIndex-duration : 0;
			dates = item.getDateTimes().subList(fromIndex, toIndex);
			
			for(LocalDate date : dates) {
				kdatas.add(item.getBar(date).getMap());
			}
		}
		
		return kdatas;
	}

}

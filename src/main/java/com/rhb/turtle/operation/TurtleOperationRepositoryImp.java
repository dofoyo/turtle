package com.rhb.turtle.operation;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.simulation.AvaBar;
import com.rhb.turtle.simulation.Avarage;
import com.rhb.turtle.util.FileUtil;

@Service("turtleOperationRepositoryImp")
public class TurtleOperationRepositoryImp implements TurtleOperationRepository{
	@Value("${kDataPath}")
	private String kDataPath;
	
	@Value("${articleFile}")
	private String articleFile;

	@Value("${avaTop50File}")
	private String avaTop50File;

	@Value("${dailyTop100File}")
	private String dailyTop100File;
	
	@Override
	public List<String> getArticleIDs(){
		List<String> ids = new ArrayList<String>();
		String[] strs = FileUtil.readTextFile(articleFile).split(",");
		for(String str : strs) {
			ids.add(str.replaceAll("\r|\n", ""));
		}
		return ids;
	}
	
	@Override
	public List<Map<String,String>> getKDatas(String id) {
		List<Map<String,String>> kDatas = new ArrayList<Map<String,String>>();

		File dir = new File(this.kDataPath);
		FileFilter fileFilter = new WildcardFileFilter(id + "*.txt");
		File[] files = dir.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			kDatas.addAll(toKDatas(id,files[i]));
		}
		return kDatas;
	}
	
	private List<Map<String,String>> toKDatas(String id, File file){
		List<Map<String,String>> kDatas = new ArrayList<Map<String,String>>();
		List<String> lines;
		try {
			lines = FileUtils.readLines(file, "UTF-8");
			Integer length = lines.size();
			for(int i=length-1; i>0; i--) {
				kDatas.add(toKData(id,lines.get(i)));
			}			
		} catch (IOException e) {
			e.printStackTrace();
		}

		return kDatas;
	}
	
	private Map<String, String> toKData(String id, String line){
		String[] columns = line.split(",");
		
		Map<String,String> kData = new HashMap<String,String>();
		BigDecimal open = new BigDecimal(columns[1]);
		BigDecimal high = new BigDecimal(columns[2]);
		BigDecimal low = new BigDecimal(columns[4]);
		BigDecimal close = new BigDecimal(columns[3]);
		BigDecimal factor = new BigDecimal(columns[7]);
		String amount = columns[6];
		
		kData.put("id", id);
		kData.put("date", columns[0]);
		kData.put("open", open.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("high", high.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("low", low.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("close", close.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("amount", amount);
			
		return kData;
	}

	@Override
	public void generateAvaTop50(List<String> ids) {
		Integer top = 50;

		Map<LocalDate,TreeSet<AvaBar>> avaTops = new HashMap<LocalDate,TreeSet<AvaBar>>();
		Avarage avarage;
		AvaBar avaBar = null;
		TreeSet<AvaBar> avaBars;
		LocalDate date;
		
		List<Map<String,String>>  kDatas;
		int d = 0;
		for(String id : ids) {
			System.out.println(++d + "/" + ids.size());

			avarage = new Avarage();
			kDatas = getKDatas(id);
			//System.out.println(id + "," + kDatas.size());
			for(Map<String,String> kdata : kDatas) {
				date = LocalDate.parse(kdata.get("date"),DateTimeFormatter.ofPattern("yyyy-MM-dd"));
				avaBar = new AvaBar(date,id,new BigDecimal(kdata.get("amount")));
				avaBar.setAva(avarage.getAmountAvarage(avaBar));
				if(avarage.isOk()) {
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
		LocalDate theDate = dates.get(dates.size()-1);
		avaBars = avaTops.get(theDate);
		for(Iterator<AvaBar> i = avaBars.iterator() ; i.hasNext();) {
			avaBar = i.next();
			sb.append(avaBar.getId());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		FileUtil.writeTextFile(avaTop50File, sb.toString(), false);
		
	}

	@Override
	public List<String> getDailyTop100Ids() {
		List<String> ids = new ArrayList<String>();
		String[] columns = FileUtil.readTextFile(dailyTop100File).split(",");
		for(int i=1; i<columns.length; i++) {
			ids.add(columns[i].substring(0, 8));  //末尾有换行符
		}
		return ids;
	}

	
}
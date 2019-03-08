package com.rhb.turtle.operation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Turtle;
import com.rhb.turtle.util.FileUtil;

@Service("turtlePreyRepositoryImp")
public class PreyRepositoryImp implements PreyRepository {
	@Value("${preysFile}")
	private String preysFile;
	
	@Autowired
	@Qualifier("turtleOperationRepositoryImp")
	TurtleOperationRepository turtleOperationRepository ;
	
	@Autowired
	@Qualifier("turtleOperationSpiderImp")
	KdataSpider turtleOperationSpider ;
	
	@Override
	public void generatePreys() {
		LocalDate theDay = turtleOperationSpider.getLatestMarketDate();
		
		//Set<String> ids = new HashSet<String>(turtleOperationRepository.getDailyTop100Ids());
		Set<String> ids = new HashSet<String>(turtleOperationSpider.downLatestDailyTop(100));
		Map<String,String> articles = turtleOperationRepository.getArticles();
		for(String id : articles.keySet()) {
			ids.add(id);
		}
		
		//加载收盘后下载的K线数据
		//因为要根据之前的数据来判断当前数据，因此加载K线数据应为前一天的数据
		Turtle turtle = new Turtle();
		List<Map<String,String>> kDatas;
		LocalDate tmp;
		for(String id : ids) {
			kDatas = turtleOperationRepository.getKDatas(id);
			if(kDatas.size()==0) {
				System.out.println("NO kDatas, downloading...");
				turtleOperationSpider.downKdatas(id);
				kDatas = turtleOperationRepository.getKDatas(id);
			}
			if(kDatas.size()!=0) {
				Map<String,String> kdata = kDatas.get(kDatas.size()-1);
				tmp = LocalDate.parse(kdata.get("dateTime"));
				if(theDay.equals(tmp)){
					kDatas.remove(kDatas.size()-1);
				}
				turtle.addBars(kDatas);				
			}
		}

		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String, String> kData;
		Map<String,String> prey;
		for(String itemID : ids) {
			kData = turtleOperationSpider.getLatestMarketData(itemID);//获得最新的K线数据
			prey = turtle.hunt(kData);
			if(prey!=null) {
				list.add(prey);
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append("itemID");
		sb.append(",");
		sb.append("code");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("openLow");
		sb.append(",");
		sb.append("openHigh");
		sb.append(",");
		sb.append("now");
		sb.append(",");
		sb.append("dropLow");
		sb.append(",");
		sb.append("hlgap");
		sb.append(",");
		sb.append("nhgap");
		sb.append(",");
		sb.append("atr");
		sb.append(",");
		sb.append("status");
		sb.append("\n");
		for(Map<String,String> map : list) {
			sb.append(map.get("itemID"));
			sb.append(",");
			sb.append(map.get("code"));
			sb.append(",");
			sb.append(map.get("name"));
			sb.append(",");
			sb.append(map.get("openLow"));
			sb.append(",");
			sb.append(map.get("openHigh"));
			sb.append(",");
			sb.append(map.get("now"));
			sb.append(",");
			sb.append(map.get("dropLow"));
			sb.append(",");
			sb.append(map.get("hlgap"));
			sb.append(",");
			sb.append(map.get("nhgap"));
			sb.append(",");
			sb.append(map.get("atr"));
			sb.append(",");
			sb.append(map.get("status"));
			sb.append("\n");
		}
		FileUtil.writeTextFile(preysFile, sb.toString(), false);
		//FileUtil.write(sb.toString(), preysFile, "utf-8");
	}

	/*
	 * 	features.put("openHigh", openHigh.toString());
		features.put("openLow", openLow.toString());
		features.put("hlgap", hlgap.toString()); 
		features.put("nhgap", nhgap.toString()); 
		features.put("nlgap", nlgap.toString()); 
		features.put("dropHigh", dropHigh.toString());
		features.put("dropLow", dropLow.toString());
		features.put("now", now.toString());
		features.put("status", status.toString());
		features.put("atr", getATR().toString());
	 */
	
	@Override
	public List<Map<String,String>> getPreys(String status) {
		Map<String,String> articles = turtleOperationRepository.getArticles();

		List<Map<String,String>> preys = new ArrayList<Map<String,String>>();
		Map<String,String> prey;
		String[] lines = FileUtil.readTextFile(preysFile).split("\n");
		String[] columns;
		String name,id;
		for(int i=1; i<lines.length; i++) {
			columns = lines[i].split(",");
			if(columns[10].equals(status)) {
				id = columns[0];
				name = articles.get(id)==null ? columns[2] : articles.get(id);
				prey = new HashMap<String,String>();
				prey.put("itemID", id);
				prey.put("code", columns[1]);
				prey.put("name", name);
				prey.put("low", columns[3]);
				prey.put("high", columns[4]);
				prey.put("now", columns[5]);
				prey.put("drop", columns[6]);
				prey.put("hlgap", columns[7]);
				prey.put("nhgap", columns[8]);
				prey.put("atr", columns[9]);	
				
				preys.add(prey);
			}
		}
		//System.out.println(preys.size());
		return preys;
	}

}

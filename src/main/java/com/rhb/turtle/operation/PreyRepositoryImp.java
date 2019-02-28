package com.rhb.turtle.operation;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		List<String> ids = turtleOperationRepository.getDailyTop100Ids();
		
		//加载收盘后下载的K线数据
		//因为要根据之前的数据来判断当前数据，因此加载K线数据应为前一天的数据
		Turtle turtle = new Turtle();
		List<Map<String,String>> kDatas;
		LocalDate tmp;
		for(String id : ids) {
			kDatas = turtleOperationRepository.getKDatas(id);
			Map<String,String> kdata = kDatas.get(kDatas.size()-1);
			tmp = LocalDate.parse(kdata.get("dateTime"));
			if(theDay.equals(tmp)){
				//System.out.format("%s delete last kdata.\n", id);
				kDatas.remove(kDatas.size()-1);
			}
			//System.out.format("load %s's kdata.\n", id);
			turtle.addBar(kDatas);
		}

		List<Map<String,String>> list = new ArrayList<Map<String,String>>();
		Map<String, String> kData;
		Map<String,String> prey;
		for(String itemID : ids) {
			kData = turtleOperationSpider.getLatestMarketData(itemID);//获得最新的K线数据
			prey = turtle.hunt(kData);
			if(prey!=null && prey.get("operation").equals("open")) {
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
		sb.append("low");
		sb.append(",");
		sb.append("high");
		sb.append(",");
		sb.append("now");
		sb.append(",");
		sb.append("drop");
		sb.append(",");
		sb.append("hlgap");
		sb.append(",");
		sb.append("nhgap");
		sb.append("\n");
		for(Map<String,String> map : list) {
			sb.append(map.get("itemID"));
			sb.append(",");
			sb.append(map.get("code"));
			sb.append(",");
			sb.append(map.get("name"));
			sb.append(",");
			sb.append(map.get("low"));
			sb.append(",");
			sb.append(map.get("high"));
			sb.append(",");
			sb.append(map.get("now"));
			sb.append(",");
			sb.append(map.get("drop"));
			sb.append(",");
			sb.append(map.get("hlgap"));
			sb.append(",");
			sb.append(map.get("nhgap"));
			sb.append("\n");
		}
		FileUtil.writeTextFile(preysFile, sb.toString(), false);
	}

	@Override
	public List<Map<String,String>> getPreys() {
		List<Map<String,String>> preys = new ArrayList<Map<String,String>>();
		Map<String,String> prey;
		String[] lines = FileUtil.readTextFile(preysFile).split("\n");
		String[] columns;
		for(int i=1; i<lines.length; i++) {
			columns = lines[i].split(",");
			prey = new HashMap<String,String>();
			prey.put("itemID", columns[0]);
			prey.put("code", columns[1]);
			prey.put("name", columns[2]);
			prey.put("low", columns[3]);
			prey.put("high", columns[4]);
			prey.put("now", columns[5]);
			prey.put("drop", columns[6]);
			prey.put("hlgap", columns[7]);
			prey.put("nhgap", columns[8]);

			preys.add(prey);
		}
		//System.out.println(preys.size());
		return preys;
	}

}

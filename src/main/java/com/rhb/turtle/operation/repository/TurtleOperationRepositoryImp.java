package com.rhb.turtle.operation.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.util.FileUtil;

@Service("turtleOperationRepositoryImp")
public class TurtleOperationRepositoryImp implements TurtleOperationRepository{
	@Value("${kDataPath}")
	private String kDataPath;
	
	@Value("${articleFile}")
	private String articleFile;
	
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

		String file = this.kDataPath + "/" + id + ".txt";		
		//System.out.println(file);
		
		String[] lines = FileUtil.readTextFile(file).split("\n");
		
		Integer length = lines.length;
		String[] columns;
		Map<String,String> kData;
		BigDecimal open;
		BigDecimal high;
		BigDecimal low;
		BigDecimal close;
		BigDecimal factor;
		
		for(int i=length-1; i>0; i--) {
			columns = lines[i].split(",");
			
			open = new BigDecimal(columns[1]);
			high = new BigDecimal(columns[2]);
			low = new BigDecimal(columns[4]);
			close = new BigDecimal(columns[3]);
			factor = new BigDecimal(columns[7]);
			
			kData = new HashMap<String,String>();
			kData.put("id", id);
			kData.put("date", columns[0]);
			kData.put("open", open.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_DOWN).toString());
			kData.put("high", high.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_DOWN).toString());
			kData.put("low", low.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_DOWN).toString());
			kData.put("close", close.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_DOWN).toString());
			
			kDatas.add(kData);
		}

		return kDatas;
	}
	
}

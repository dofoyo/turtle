package com.rhb.turtle.operation;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
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
		
		kData.put("id", id);
		kData.put("date", columns[0]);
		kData.put("open", open.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("high", high.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("low", low.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
		kData.put("close", close.divide(factor,BigDecimal.ROUND_HALF_UP).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
			
		return kData;
	}
	
}

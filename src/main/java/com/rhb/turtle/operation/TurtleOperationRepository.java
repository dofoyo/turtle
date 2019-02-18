package com.rhb.turtle.operation;

import java.util.List;
import java.util.Map;

public interface TurtleOperationRepository {
	public List<Map<String,String>> getKDatas(String id);
	public List<String> getArticleIDs();
	public List<String> getDailyTop100Ids();
	
	/*
	 * 每天收盘后执行
	 * 只算出给定日期的即可
	 * 不需要循环把每天的算出来
	 */
	public void generateAvaTop50(List<String> ids);
}

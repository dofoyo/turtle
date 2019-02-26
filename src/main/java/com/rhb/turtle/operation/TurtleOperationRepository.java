package com.rhb.turtle.operation;

import java.util.List;
import java.util.Map;

import com.rhb.turtle.domain.Order;

public interface TurtleOperationRepository {
	public List<Map<String,String>> getKDatas(String id);
	public boolean getIsKDatasExist(String id, String year, String jidu);
	public Map<String,String> getArticles();
	public List<String> getDailyTop100Ids();
	
	/*
	 * 每天收盘后执行
	 * 只算出给定日期的即可
	 * 不需要循环把每天的算出来
	 */
	public void generateAvaTop50(List<String> ids);
	
	public List<OrderEntity> getOnhands();
}

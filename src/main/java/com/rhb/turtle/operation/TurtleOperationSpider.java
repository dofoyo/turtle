package com.rhb.turtle.operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface TurtleOperationSpider {
	public LocalDate getLatestMarketDate(); 
	
	/*
	 * 上交所的id为: sh + 代码
	 * 上交所的id为：sz + 代码
	 */
	public Map<String,String> getLatestMarketData(String id);
	
	public List<String> downLatestDailyTop100();
	
	public void downKdatas(String id, String year, String jidu);

}

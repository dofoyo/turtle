package com.rhb.turtle.operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KdataSpider {
	public LocalDate getLatestMarketDate(); 
	
	/*
	 * 上交所的id为: sh + 代码
	 * 上交所的id为：sz + 代码
	 */
	public Map<String,String> getLatestMarketData(String id);
	
	public List<String> downLatestDailyTop(Integer top);
	
	public void downKdatas(String id, String year, String jidu);
	
	
	/*
	 * 注意季度交接日
	 * 最好每天23点开始下载
	 */
	public Integer downKdatas(String id);

	
}

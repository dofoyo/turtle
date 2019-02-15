package com.rhb.turtle.simulation.spider;

import java.time.LocalDate;
import java.util.Map;

public interface MarketInfoSimulationSpider {
	public LocalDate getLatestMarketDate(); 
	
	/*
	 * 上交所的id为: sh + 代码
	 * 上交所的id为：sz + 代码
	 */
	public Map<String,String> getLatestMarketData(String id);
	
	public void downLatestDailyTop100();
	
	public void downKdata(String id);

}

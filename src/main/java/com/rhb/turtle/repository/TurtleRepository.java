package com.rhb.turtle.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TurtleRepository {
	public List<Map<String,String>> getKDatas(String code, LocalDate beginDate, LocalDate endDate);
	public List<Map<String,String>> getKDatas(LocalDate date, Integer top);

	
	/*
	 * sh或sz加上股票代码即为code
	 */
	public Map<String,String> getKData(String id, LocalDate date);
	
	/*
	 * 获得目录下所有代码
	 */
	public Set<String> getCodes();
	
	/*
	 * 根据top100和其K线数据，获得某时间段内300天平均成交金额排行最前的股票
	 */
	public Map<LocalDate, List<String>> getAvaTops(Integer top,LocalDate beginDate, LocalDate endDate);
	public List<String> getAvaTops(Integer top,LocalDate date);

	
	public Map<LocalDate, Set<String>> getTops(Integer top,LocalDate beginDate, LocalDate endDate);

	/*
	 * 生成近300天平均成交金额排行最前的股票
	 */
	public void generateTops(Integer top, LocalDate beginDate, LocalDate endDate);
	
	/*
	 * 从2000-01-01 年至2019-02-11的完整的K线图中生成300日平均成交量排名前50名的股票
	 */
	public void generateAvaTop50();
	
	/*
	 * 从2000-01-01 年至2019-02-11的完整的K线图中生成每日成交量排名前100名的股票
	 */
	public void generateDailyTop100();
	
	public Map<LocalDate, List<String>> getDailyTops(Integer top,LocalDate beginDate, LocalDate endDate);
	public Set<String> getDailyTopIds(Integer top,LocalDate beginDate, LocalDate endDate);
	
}

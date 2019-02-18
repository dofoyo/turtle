package com.rhb.turtle.simulation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TurtleSimulationRepository {
	public Map<String,String> getDailyKData(String id, LocalDate date);
	public Map<String,String> getFiveKData(String id, LocalDateTime datetime);
	public List<String> getAvaTopIds(Integer top,LocalDate date);
	public List<String> getDailyTopIds(Integer top,LocalDate date);
	public List<String> getBluechipIds(LocalDate date);
	/*
	 * 获得在一段时间内，最高价和最低价距离最小的股票id。
	 * 感觉这类股票一直在横盘整理，等待突破
	 * 
	 */
	public List<String> getNvaTopIds(Integer top,LocalDate date, Integer duration);

	
	/*
	 * 从2000-01-01 年至2019-02-11的完整的K线图中生成300日平均成交量排名前50名的股票
	 */
	public void generateAvaTop50();
	
	//---------------------------------
	
	public List<Map<String,String>> getKDatas(LocalDate date, Integer top);

	
	public List<Map<String,String>> getKDatas(String id, LocalDate beginDate, LocalDate endDate);

	
	/*
	 * sh或sz加上股票代码即为code
	 */
	
	/*
	 * 获得目录下所有代码
	 */
	public Set<String> getIds();
	
	/*
	 * 根据top100和其K线数据，获得某时间段内300天平均成交金额排行最前的股票
	 */
	public Map<LocalDate, List<String>> getAvaTops(Integer top,LocalDate beginDate, LocalDate endDate);

	
	public Map<LocalDate, Set<String>> getTops(Integer top,LocalDate beginDate, LocalDate endDate);

	/*
	 * 生成近300天平均成交金额排行最前的股票
	 */
	public void generateTops(Integer top, LocalDate beginDate, LocalDate endDate);
	

	
	/*
	 * 从2000-01-01 年至2019-02-11的完整的K线图中生成每日成交量排名前100名的股票
	 */
	public void generateDailyTop100();
	
	public Map<LocalDate, List<String>> getDailyTops(Integer top,LocalDate beginDate, LocalDate endDate);
	public Set<String> getDailyTopIds(Integer top,LocalDate beginDate, LocalDate endDate);
	
}
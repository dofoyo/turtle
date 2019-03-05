package com.rhb.turtle.simulation.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rhb.turtle.simulation.repository.entity.ItemEntity;


/*
 * sh或sz加上股票代码即为code
 */

public interface TurtleSimulationRepository {
	public List<Map<String,String>> getKdatas(String itemID, Integer duraion, LocalDate endDate);
	
	public List<String> getAvaTopIds(Integer top,LocalDate date);
	public List<String> getDailyTopIds(Integer top,LocalDate date);
	public List<String> getBluechipIds(LocalDate date);
	
	/*
	 * 获得在一段时间内，最高价和最低价距离最小的股票id。
	 * 感觉这类股票一直在横盘整理，等待突破
	 */
	public List<String> getNvaTopIds(Integer top,LocalDate date, Integer duration);

	
	/*
	 * 从2000-01-01 年至2019-02-11的完整的K线图中生成300日平均成交量排名前50名的股票
	 */
	public void generateAvaTop50();
	
	//---------------------------------
	

	/*
	 * 从2000-01-01 年完整的K线图中生成每日成交量排名前100名的股票
	 */
	public void generateDailyTop100();
	
	public Set<String> getIdsFromDir();
	
}

package com.rhb.turtle.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TurtleRepository {
	public List<Map<String,String>> getKDatas(String code, LocalDate beginDate, LocalDate endDate);
	
	/*
	 * 获得目录下所有代码
	 */
	public Set<String> getCodes();
	
	/*
	 * 成交金额排行最前的股票
	 */
	public Map<LocalDate, Set<String>> getAmountTops(Integer top,LocalDate beginDate, LocalDate endDate);
	
	
	/*
	 * 成交金额排行最前的股票代码
	 */
	public Set<String> getTops();
	
}

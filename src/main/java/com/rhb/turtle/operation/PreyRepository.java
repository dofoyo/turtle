package com.rhb.turtle.operation;

import java.util.List;
import java.util.Map;

public interface PreyRepository {
	
	/*
	 * 每天收盘后的收盘作业会调用一次，生成preys.csv
	 * 每次调用都会生成preys.csv
	 */
	public void generatePreys();
	
	/*
	 * 直接读取preys.csv
	 */
	public List<Map<String,String>> getPreys();
	
}

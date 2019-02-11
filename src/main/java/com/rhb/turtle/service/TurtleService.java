package com.rhb.turtle.service;

import java.util.Map;

public interface TurtleService {
	/*
	 * 以所有的沪深A股为标的进行历史数据测试
	 */
	public Map<String,String> doTrade();
}

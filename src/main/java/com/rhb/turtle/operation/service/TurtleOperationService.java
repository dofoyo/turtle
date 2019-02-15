package com.rhb.turtle.operation.service;

import java.util.Map;

public interface TurtleOperationService {
	
	/*
	 * 每天上午9:00启动
	 * 0、判断今天是否是交易日，不是交易日退出
	 * 1、初始数据准备
	 * 	1.1、获得上一个交易日收盘后生成的article.txt，
	 * 	1.2、获得article.txt所列的股票的近几天的K线数据，天数大于openduration
	 * 2、根据article.txt进入无限循环
	 * 		2.1、在交易时段（9:30 -- 11:30, 13:00 -- 15:00）
	 * 			2.1.1、获得当日items的当时最新市场数据，
	 * 			2.1.2、根据最新市场数据实施open、stop、close操作，保存操作数据account
	 *  		2.1.3、15:05前，继续循环，15:05后，结束循环，退出系统
	 * 
	 */
	public void operate();
	
	/*
	 * 每天下午16点启动
	 * 获得dailyTop100和操作记录account
	 * 下载完善dailyTop100所包含article的最近300天K线数据
	 * 根据300天k线数据生成avaTop50
	 * 根据avaTop50和account生成article.txt
	 */
	public void doClosingWork(Integer top);
	
	/*
	 * 提供实盘详细数据
	 */
	public Map<String,String> getOperationDetails();
}

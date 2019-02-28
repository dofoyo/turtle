package com.rhb.turtle.operation;

import java.util.List;
import java.util.Map;

public interface TurtleOperationService {
	/*
	 * 每天上午9:00启动
	 * 0、判断今天是否是交易日，不是交易日退出
	 * 1、初始数据准备
	 * 	1.1、导入onhands.json
	 * 	1.2、获得onhands.json所列的股票的近n天的K线数据，天数大于openduration
	 * 2、进入无限循环
	 * 		2.1、在交易时段（9:30 -- 11:30, 13:00 -- 15:00）
	 * 			2.1.1、获得当日onhands.json所列的股票当时最新市场数据，
	 * 			2.1.2、根据最新市场数据提示stop、close操作，自己买卖的成交价位要自己保存到onhands.json
	 *  		2.1.3、15:05前，继续循环，15:05后，结束循环，退出系统
	 * 
	 */
	public void tendOnhands();
	public List<Map<String,String>> getOnhands();

	
	/*
	 * 1 获得上一个交易日收盘后生成的article.txt，
	 * 2 获得article.txt所列的股票的近n天的K线数据，天数大于openduration
	 * 3 获得当日article.txt所列的股票的最新市场数据，
	 * 4 根据最新市场数据提示open操作
	 * 
	 */
	public void huntPreys();
	public List<Map<String,String>> getPreys();

	/*
	 * 每天下午16点启动
	 * 获得dailyTop100
	 * 下载完善dailyTop100所列股票的最近300天K线数据
	 * 根据300天k线数据生成avaTop50
	 * 生成preys.csv
	 */
	public void doClosingWork();
	
}

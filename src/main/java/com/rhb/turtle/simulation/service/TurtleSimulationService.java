package com.rhb.turtle.simulation.service;

import java.util.Map;

public interface TurtleSimulationService {
	/*
	 * 以所有的沪深A股为标的进行历史数据测试
	 * 返回测试详细数据
	 * 步骤：
	 * 1、准备好每天的top100和其完整K线数据（大智慧客户端复制出来的格式）
	 * 2、启动系统
	 * 3、系统根据top100和其k线数据，生成每日tops
	 * 4、按每日循环
	 * 	4.1、获得该日tops
	 * 	4.2、获得该日tops的该日市场数据
	 * 	4.3、根据该日市场数据执行open、stop、close操作
	 * 	4.4、回到循环头，开始下一日
	 * 
	 * 
	 * 总收益对买入卖出的价格非常敏感
	 * 1. 在测试时，买入卖出都是以当天的收盘价进行结算，因此误差非常大。
	 * 		用5分钟数据进行测试，效果才能体现。但5分钟的历史数据无处得到，
	 * 		因此解决办法是将日线周期拉长，即增加duration
	 * 2. 在实操时，买入卖出都是以当天的盘中价进行结算，收益应该会好很多。
	 * 3. dailyTop和avarageTop的差别也非常大。
	 * 		实盘时，可以分开进行操作，对比一下。
	 *
	 * 
	 * 
	 */
	public Map<String,String> simulate();
	public Map<String,String> simulate2();
	
	
	/*
	 * 每天上午9:00启动
	 * 0、判断今天是否是交易日，不是交易日退出
	 * 1、初始数据准备
	 * 	1.1、获得上一个交易日收盘后生成的article.txt，
	 * 	1.2、获得article.txt所列的股票的近几天的K线数据，天数与openduration一致
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

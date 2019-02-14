package com.rhb.turtle.service;

import java.util.Map;

public interface TurtleService {
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
	 * 步骤：
	 * 1、初始化：
	 * 	1.1、获得最新的top100和操作记录account，
	 * 	1.2、补充top100和account所包含item的最近300天的K线数据
	 * 	1.3、根据k线数据生成当日tops
	 * 	1.4、根据当日tops和account生成当日items
	 * 2、进入无限循环
	 * 	2.1、判断今天是否是交易日，是否在交易时段（9:30 -- 11:30, 13:00 -- 15:00）
	 * 		2.1.1、在交易时段
	 * 			2.1.2.1、获得当日items的当时最新市场数据，
	 * 			2.1.2.2、根据最新市场数据实施open、stop、close操作，保存操作数据account
	 *  		2.1.2.3、循环
	 * 
	 */
	public void operate();
	
	/*
	 * 每天下午16点开始
	 * 获得当天top100和操作记录account
	 * 完善top100和account所包含item的最近300天K线数据
	 * 根据k线数据生成tops
	 * 根据tops和account生成items
	 */
	public void doClosingWork(Integer top);
	
	/*
	 * 提供实盘详细数据
	 */
	public Map<String,String> getOperationDetails();
}

package com.rhb.turtle.api;

import java.util.List;

public interface TurtleApi {
	/*
	 * 查看在手股票的信息：当前区间的最高价、最低价、现价、买入价、止损价、退出价等
	 * 一个股票如果有几次买入，分别展示
	 */
	public ResponseContent<List<OnhandView>> getOnhands();
	
	/*
	 * 增加在手股票，只输入股票代码和价格
	 */
	public void buy();
	
	/*
	 * 减少在手股票
	 */
	public void sell();
	
	
	/*
	 * 查看即将突破或正在突破的股票信息，供参考
	 * 系统从每天成交量排名前100中找将突破或正在突破的股票
	 * 并将其放入preys.txt中
	 * 
	 */
	public ResponseContent<List<PreyView>> getPreys();

	/*
	 * 增加一个股票进入观察窗口
	 */
	public void addPrey();
	
	/*
	 * 删除一个股票
	 */
	public void deletePrey();

}

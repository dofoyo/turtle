package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Item {
	protected static final Logger logger = LoggerFactory.getLogger(Item.class);
	private String itemID;
	
	
	/*
	 * duration 的K线，只保持duration大小
	 */
	private List<Bar> bars;
	
	public Item(String itemID) {
		this.itemID = itemID;
		this.bars = new ArrayList<Bar>();
	}
	
	public List<Bar> getBars(){
		return bars;
	}
	
	public Bar addBar(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,Integer openDuration) {
		Bar bar=null;
		if(this.bars.size()==0) {
			bar = new Bar(itemID,date,open,high,low,close,this.getTR(high, low, close));
			this.bars.add(bar);
			return bar;
		}
		
		bar = this.isLatestBar(date);
		if(bar == null) {
			Bar preBar = this.bars.get(this.bars.size()-1);
			BigDecimal preClose = preBar.getClose();
			BigDecimal tr = getTR(high,low,preClose);
			bar = new Bar(itemID,date,open,high,low,close,tr);
			//System.out.println(bar);
			this.bars.add(bar);
			if(this.bars.size()>openDuration) {
				this.bars.remove(0);
			}
		}else{
			BigDecimal tr;
			if(this.bars.size()>1) {
				Bar preBar = this.bars.get(this.bars.size()-2);
				BigDecimal preClose = preBar.getClose();
				tr = getTR(high,low,preClose);
			}else {
				tr = getTR(high,low,close);
			}
			bar.setHigh(high);
			bar.setLow(low);
			bar.setOpen(open);
			bar.setClose(close);
			bar.setTr(tr);			
		}
		return bar;
	}
	
	/*
	 * 计算真实波动幅度
	 */
	private BigDecimal getTR(BigDecimal high, BigDecimal low, BigDecimal preClose) {
		BigDecimal h_l = high.subtract(low);
		BigDecimal h_p = high.subtract(preClose);
		BigDecimal p_l = preClose.subtract(low);
		
		BigDecimal max = h_l.compareTo(h_p)>0 ? h_l : h_p;
		max = max.compareTo(p_l)>0 ? max : p_l;
		
		return max;
	}

	private Bar isLatestBar(LocalDate date) {
		Bar bar = this.bars.get(this.bars.size()-1);
		if(bar.getDate().equals(date)) {
			return bar;
		}else {
			return null;
		}
	}

	/*
	 * 入市判断
	 * 返回1，开多仓
	 * 返回-1，开空仓
	 * 返回null,不开仓
	 */
	public Integer openPing(Bar bar, Integer openDuration) {
		//System.out.println("bars.size:" + bars.size());
		if(this.bars.size()>=openDuration) {
			BigDecimal[] highestAndLowest = getHighestAndLowest(openDuration);
			BigDecimal highest = highestAndLowest[0];
			BigDecimal lowest = highestAndLowest[1];
/*			
			if(itemID.equals("sh601318")) {
				System.out.println(this.bars);
				System.out.println("highest:" + highest + ",lowest:" + lowest);
			}*/
			
			//突破高点，上涨势头，买多
			if(bar.getHigh().compareTo(highest)>0) {
				//System.out.println(articleID + "," + openDuration +"天高点" + highest);
				//System.out.println(this.bars);
				String msg = itemID + "，" + bar.getDate() + "，盘中最高价格" + bar.getHigh() + "突破" + openDuration + "天高点" + highest + "，开多仓！！";
				//System.out.println(msg);
				logger.info(msg);
				return 1;
			}
			
			//突破低点，下跌势头，卖空
			if(bar.getLow().compareTo(lowest)<0) {
				String msg = itemID + "，" + bar.getDate() + "盘中最低价格" + bar.getLow() + "突破" + openDuration + "天低点" + lowest + "，开空仓！！";
				//System.out.println(msg);
				logger.info(msg);
				return -1;
			}
		}
		return null;
	}
	
	/*
	 * 计算一段时间内的平均波动幅度
	 */
	public BigDecimal getATR(Integer duration) {
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Bar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal sum_tr = new BigDecimal(0);
		for(Bar bar : subBars) {
			sum_tr = sum_tr.add(bar.getTr());
		}
		return sum_tr.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP); 
	}
	
	public LocalDate[] getBeginAndEndDateOfOpenDuration() {
		LocalDate[] dates = new LocalDate[2];
		dates[0] = this.bars.get(0).getDate();
		dates[1] = this.bars.get(this.bars.size()-1).getDate();
 		return dates;
	}
	
	/*
	 * 计算一段时间内的最高点、最低点
	 */
	public BigDecimal[] getHighestAndLowest(Integer duration){
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Bar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal highest = new BigDecimal(0);
		BigDecimal lowest = new BigDecimal(100000000);
		for(Bar bar : subBars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
			}
			
			if(bar.getLow().compareTo(lowest)<0) {
				lowest = bar.getLow();
			}
		}
		
		return new BigDecimal[]{highest,lowest}; 
	}
	
	
	/*
	 * 计算一段时间内的最高点比最低点高出的百分百
	 */
	public Integer getRateOfHighestAndLowest(Integer duration){
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Bar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal highest = new BigDecimal(0);
		BigDecimal lowest = new BigDecimal(100000000);
		for(Bar bar : subBars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
			}
			
			if(bar.getLow().compareTo(lowest)<0) {
				lowest = bar.getLow();
			}
		}
		
		Integer rate = highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		
		return rate; 
	}
	
	/*
	 * 退出判断
	 */
	public boolean closePing(Bar bar, Integer d, Integer closeDuration) {
		if(this.bars.size()>=closeDuration) {
			BigDecimal[] keyValues = getHighestAndLowest(closeDuration);
			BigDecimal highest = keyValues[0];
			BigDecimal lowest = keyValues[1];

			//持有空头头寸，突破高点，上涨势头，平仓
			if(d<0 && bar.getHigh().compareTo(highest)>0) {
				String msg = "持有" + itemID + "空头，但盘中最高价" + bar.getHigh() + "突破" + closeDuration + "天高点" + highest + "，立即平仓！！";
				logger.info(msg);
				return true;
			}
			
			//持有多头头寸，突破低点，下跌趋势，平仓
			if(d>0 && bar.getLow().compareTo(lowest)<0) {
				String msg = "持有" + itemID + "多头，但盘中最低价" + bar.getLow() + "突破" + closeDuration + "天低点" + lowest + "，立即平仓！！";
				logger.info(msg);
				return true;
			}			
		}
		return false;
	}
	

	public String getItemID() {
		return itemID;
	}
	
	
}

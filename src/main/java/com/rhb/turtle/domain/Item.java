package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Item {
	private String code;
	private String name;
	
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot; 
	
	/*
	 * 通道区间
	 * 该指标很重要
	 */
	private Integer openDuration; 
	private Integer closeDuration; 
	
	/*
	 * duration 的K线，只保持duration大小
	 */
	private List<Bar> bars;
	
	public Item(String code, String name, BigDecimal lot, Integer openDuration, Integer closeDuration) {
		this.code = code;
		this.name = name;
		this.lot = lot;
		this.openDuration = openDuration;
		this.closeDuration = closeDuration;
		this.bars = new ArrayList<Bar>();
	}
	
	/*
	 * 入市判断
	 */
	public Record openPing(LocalDate date, BigDecimal price) {
		Record record = null;
		
		if(this.bars.size()>=this.openDuration) {
			Bar preBar = this.bars.get(this.bars.size()-1);

			Map<String,BigDecimal> keyValues = getOpenKeyValues();
			BigDecimal highest = keyValues.get("highest");
			BigDecimal lowest = keyValues.get("lowest");
			BigDecimal atr = keyValues.get("atr");

			//突破高点，上涨势头，买多
			if(price.compareTo(highest)>0) {
				record = new Record(UUID.randomUUID().toString(), this.code, this.name, date, 1, price, atr, lot);
			}
			
			//突破低点，下跌势头，卖空
			if(price.compareTo(lowest)<0) {
				record = new Record(UUID.randomUUID().toString(), this.code, this.name, date, -1, price, atr, lot);
			}
		}
		return record;
	}
	
	/*
	 * 退出判断
	 */
	public BigDecimal closePing(LocalDate date,BigDecimal price, Integer d) {
	
		if(this.bars.size()>=this.openDuration) {
			Map<String,BigDecimal> keyValues = getCloseKeyValues();
			BigDecimal highest = keyValues.get("highest");
			BigDecimal lowest = keyValues.get("lowest");

			//持有空头头寸，突破高点，上涨势头，平仓
			if(d<0 && price.compareTo(highest)>0) {
				return highest;
			}
			
			//持有多头头寸，突破低点，下跌趋势，平仓
			if(d>0 && price.compareTo(lowest)<0) {
				return lowest;
			}			
		}
		return null;
	}
	
	public BigDecimal closePing(Integer direction) {
		if(this.bars.size()>=this.openDuration) {
			Map<String,BigDecimal> keyValues = getCloseKeyValues();
			BigDecimal highest = keyValues.get("highest");
			BigDecimal lowest = keyValues.get("lowest");

			//持有空头头寸，突破高点，上涨势头，平仓
			if(direction<0) {
				return highest;
			}
			
			//持有多头头寸，突破低点，下跌趋势，平仓
			if(direction>0) {
				return lowest;
			}			
		}
		return null;
	}
	
	/*
	 * 计算一段duration内的最高点、最低点和平均波动幅度
	 */
	private Map<String,BigDecimal> getOpenKeyValues(){
		Map<String,BigDecimal> hl = new HashMap<String,BigDecimal>();
		BigDecimal highest = new BigDecimal(0);
		BigDecimal lowest = new BigDecimal(100000000);
		BigDecimal sum_tr = new BigDecimal(0);
		for(Bar bar : this.bars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
			}
			
			if(bar.getLow().compareTo(lowest)<0) {
				lowest = bar.getLow();
			}
			
			sum_tr = sum_tr.add(bar.getTr());
		}
		
		//System.out.println("sum_tr = " + sum_tr);
		hl.put("highest", highest);
		hl.put("lowest", lowest);
		hl.put("atr", sum_tr.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP));
		return hl; 
	}

	/*
	 * 计算20天的最高点、最低点和平均波动幅度
	 */
	private Map<String,BigDecimal> getCloseKeyValues(){
		Integer fromIndex = this.bars.size()>closeDuration ? this.bars.size()-closeDuration : 0;
		Integer toIndex = this.bars.size();
		List<Bar> subBars = this.bars.subList(fromIndex, toIndex);
		
		Map<String,BigDecimal> hl = new HashMap<String,BigDecimal>();
		BigDecimal highest = new BigDecimal(0);
		BigDecimal lowest = new BigDecimal(100000000);
		//BigDecimal sum_tr = new BigDecimal(0);
		for(Bar bar : subBars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
			}
			
			if(bar.getLow().compareTo(lowest)<0) {
				lowest = bar.getLow();
			}
			
			//sum_tr.add(bar.getTr());
		}
		
		hl.put("highest", highest);
		hl.put("lowest", lowest);
		//hl.put("atr", sum_tr.divide(new BigDecimal(subBars.size())));
		return hl; 
	}

	
	public Bar addBar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Bar bar;
		if(this.bars.size()==0) {
			bar = new Bar(date,open,high,low,close,this.getTR(high, low, close));
			this.bars.add(bar);
			return bar;
		}
		
		bar = this.getBarByDate(date);
		if(bar == null) {
			Bar preBar = this.bars.get(this.bars.size()-1);
			BigDecimal preClose = preBar.getClose();
			BigDecimal tr = getTR(high,low,preClose);
			bar = new Bar(date,open,high,low,close,tr);
			//System.out.println(bar);
			this.bars.add(bar);
			if(this.bars.size()>this.openDuration) {
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
	
	private Bar getBarByDate(LocalDate date) {
		Bar bar = this.bars.get(this.bars.size()-1);
		if(bar.getDate().equals(date)) {
			return bar;
		}else {
			return null;
		}
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

	public BigDecimal getLot() {
		return lot;
	}

	public void setLot(BigDecimal lot) {
		this.lot = lot;
	}

	public BigDecimal getLatestClosePrice() {
		if(bars.size()>0) {
			return bars.get(bars.size()-1).getClose();
		}else {
			return null;
		}
	}
	
}

package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * openHigh和openLow是 openDuration期间的高点和低点，当前价now如果突破openHigh，则做多，如果跌破openLow，则做空。
 * dropHigh和dropLow时dropDuration期间的高点和低点，当前价now如果跌破dropLow时，则多头平仓，如果突破dropHigh，则空头平仓
 * 
 * 
 * addBar -- 新增历史数据
 * 	openHigh, openLow, hlgap, dropHigh, dropLow会变化
 * 
 * 
 * setLastestBar -- 设置实时数据
 *  now, nhgap, nlgap, status会变化
 * 
 * 
 * hlgap：高点比低点高出的百分百
 * nhgap: 当前价位比高点高出的百分百，为正表示当前价高于高点，向上突破
 * nlgap: 当前价位比低点低出的百分百，为正表示当前价低于低点，向下突破
 * status: 
 * 	2 -- 表示当前价位高于高点high，做多
 *  1 -- 表示当前价位低于高点high，高于dropLow,空头平仓
 * -1 -- 表示当前价位高于低点low，低于dropHigh，多头平仓
 * -2 -- 表示当前价位低于低点low，做空

 * 
 */
public class Item {
	protected static final Logger logger = LoggerFactory.getLogger(Item.class);
	private String itemID;
	private Integer openDuration;
	private Integer dropDuration;
	private List<Bar> bars;
	private Bar latestBar;

	private BigDecimal openHigh;
	private BigDecimal openLow;
	private BigDecimal hlgap;
	private BigDecimal dropHigh;
	private BigDecimal dropLow;

	private BigDecimal now;
	private BigDecimal nhgap;
	private BigDecimal nlgap;
	private Integer status;
	
	
	public Item(String itemID, Integer openDuration, Integer dropDuration) {
		this.itemID = itemID;
		this.openDuration = openDuration;
		this.dropDuration = dropDuration;
		this.bars = new ArrayList<Bar>();
	}
	
	public void clearBars() {
		this.bars = new ArrayList<Bar>();
	}
	
	public Bar getLatestBar() {
		return this.latestBar;
	}
	
	public void setLatestBar(Map<String,String> kData) {
		latestBar = generateBar(kData);
		
		now = latestBar.getClose();
		
		try {
			nhgap = latestBar.getClose().subtract(openHigh).divide(openHigh,BigDecimal.ROUND_HALF_UP);
			nlgap = latestBar.getClose().subtract(openLow).divide(openLow,BigDecimal.ROUND_HALF_UP);
		}catch(Exception e) {
			System.out.println(itemID);
			System.out.println(openHigh + "," + openLow);
			System.out.println(latestBar);
			System.out.println(bars);
			e.printStackTrace();
		}
		
		if(latestBar.getClose().compareTo(openHigh)>=0) {
			status = 2;
		}else if(latestBar.getClose().compareTo(openHigh)==-1 && latestBar.getClose().compareTo(dropLow)>=0) {
			status = 1;
		}else if(latestBar.getClose().compareTo(openLow)>=0 && latestBar.getClose().compareTo(dropHigh)==-1) {
			status = -1;
		}else {
			status = -2;
		}
	}
	
	public List<Bar> getBars(){
		return bars;
	}
	
	public void addBar(Map<String,String> kData) {
		Bar bar = null;
		if(this.bars.size()==0) {
			bar = generateBar(kData);
			bar.setTr(getTR(bar.getHigh(), bar.getLow(), bar.getClose()));
		}else {
			Bar preBar = this.bars.get(this.bars.size()-1);
			BigDecimal preClose = preBar.getClose();
			bar = generateBar(kData);
			bar.setTr(getTR(bar.getHigh(), bar.getLow(), preClose));
		}
		
		this.bars.add(bar);
		//System.out.println("addBar:" + bar);
		if(this.bars.size()>openDuration) {
			//System.out.println("removeBar:" + this.bars.get(0));
			this.bars.remove(0);
		}
		
		BigDecimal[] hl = getHighestAndLowest(openDuration);
		this.openHigh = hl[0];
		this.openLow = hl[1];
		this.hlgap = hl[2];
		
		hl = getHighestAndLowest(dropDuration);
		this.dropHigh = hl[0];
		this.dropLow = hl[1];
		
	}
	
	public String getItemID() {
		return itemID;
	}

	private Bar generateBar(Map<String,String> kData) {
		LocalDate date = LocalDate.parse(kData.get("dateTime"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		BigDecimal open = new BigDecimal(kData.get("open"));
		BigDecimal high = new BigDecimal(kData.get("high"));
		BigDecimal low = new BigDecimal(kData.get("low"));
		BigDecimal close = new BigDecimal(kData.get("close"));
		return new Bar(date,open,high,low,close);
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
	
	/*
	 * 计算平均波动幅度
	 */
	public BigDecimal getATR() {
		BigDecimal sum_tr = new BigDecimal(0);
		for(Bar bar : bars) {
			sum_tr = sum_tr.add(bar.getTr());
		}
		return sum_tr.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP); 
	}
	
	public Map<String,String> getFeatures(){
		if(this.bars.size() < openDuration) {
			return null;
		}
		Map<String,String> features = new HashMap<String,String>();
		
		features.put("itemID", itemID);
		features.put("openHigh", openHigh.toString());
		features.put("openLow", openLow.toString());
		features.put("hlgap", hlgap.toString()); 
		features.put("nhgap", nhgap.toString()); 
		features.put("nlgap", nlgap.toString()); 
		features.put("dropHigh", dropHigh.toString());
		features.put("dropLow", dropLow.toString());
		features.put("now", now.toString());
		features.put("status", status.toString());
		features.put("atr", getATR().toString());
		
		return features;
		
	}
	
	private BigDecimal[] getHighestAndLowest(Integer duration) {
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Bar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal highest = new BigDecimal(-1000000);
		BigDecimal lowest = new BigDecimal(1000000);
		for(Bar bar : subBars) {
			if(bar.getHigh().compareTo(highest)>0) {
				highest = bar.getHigh();
			}
			
			if(bar.getLow().compareTo(lowest)<0
					//&& bar.getLow().compareTo(new BigDecimal(0))==1
					) {
				lowest = bar.getLow();
			}
		}
		BigDecimal rate = highest.subtract(lowest).divide(lowest,BigDecimal.ROUND_HALF_UP);

		return new BigDecimal[]{highest,lowest,rate}; 
	}

	public Integer getOpenDuration() {
		return openDuration;
	}

	public Integer getDropDuration() {
		return dropDuration;
	}

	public BigDecimal getOpenHigh() {
		return openHigh;
	}

	public BigDecimal getOpenLow() {
		return openLow;
	}

	public BigDecimal getHlgap() {
		return hlgap;
	}

	public BigDecimal getDropHigh() {
		return dropHigh;
	}

	public BigDecimal getDropLow() {
		return dropLow;
	}

	public BigDecimal getNow() {
		return now;
	}

	public BigDecimal getNhgap() {
		return nhgap;
	}

	public BigDecimal getNlgap() {
		return nlgap;
	}

	public Integer getStatus() {
		return status;
	}

	
}

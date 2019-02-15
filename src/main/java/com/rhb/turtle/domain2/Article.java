package com.rhb.turtle.domain2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Article {
	private String articleID;
	
	
	/*
	 * duration 的K线，只保持duration大小
	 */
	private List<Kbar> bars;
	
	public Article(String articleID) {
		this.articleID = articleID;
		this.bars = new ArrayList<Kbar>();
	}
	
	public Kbar addBar(String articleID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,Integer openDuration) {
		Kbar bar=null;
		if(this.bars.size()==0) {
			bar = new Kbar(articleID,date,open,high,low,close,this.getTR(high, low, close));
			this.bars.add(bar);
			return bar;
		}
		
		bar = this.isLatestBar(date);
		if(bar == null) {
			Kbar preBar = this.bars.get(this.bars.size()-1);
			BigDecimal preClose = preBar.getClose();
			BigDecimal tr = getTR(high,low,preClose);
			bar = new Kbar(articleID,date,open,high,low,close,tr);
			//System.out.println(bar);
			this.bars.add(bar);
			if(this.bars.size()>openDuration) {
				this.bars.remove(0);
			}
		}else{
			BigDecimal tr;
			if(this.bars.size()>1) {
				Kbar preBar = this.bars.get(this.bars.size()-2);
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

	private Kbar isLatestBar(LocalDate date) {
		Kbar bar = this.bars.get(this.bars.size()-1);
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
	public Integer openPing(BigDecimal price, Integer openDuration) {
		if(this.bars.size()>=openDuration) {
			BigDecimal[] highestAndLowest = getHighestAndLowest(openDuration);
			BigDecimal highest = highestAndLowest[0];
			BigDecimal lowest = highestAndLowest[1];
			
			//突破高点，上涨势头，买多
			if(price.compareTo(highest)>0) {
				System.out.println("现价" + price + "突破" + openDuration + "天高点" + highest + ",开多仓！！");
				return 1;
			}
			
			//突破低点，下跌势头，卖空
			if(price.compareTo(lowest)<0) {
				System.out.println("现价" + price + "突破" + openDuration + "天低点" + highest + ",开空仓！！");
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
		List<Kbar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal sum_tr = new BigDecimal(0);
		for(Kbar bar : subBars) {
			sum_tr = sum_tr.add(bar.getTr());
		}
		return sum_tr.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP); 
	}
	
	/*
	 * 计算一段时间内的最高点、最低点
	 */
	public BigDecimal[] getHighestAndLowest(Integer duration){
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		Integer toIndex = this.bars.size();
		List<Kbar> subBars = this.bars.subList(fromIndex, toIndex);
		
		BigDecimal highest = new BigDecimal(0);
		BigDecimal lowest = new BigDecimal(100000000);
		for(Kbar bar : subBars) {
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
	 * 退出判断
	 */
	public boolean closePing(BigDecimal price, Integer d, Integer openDuration) {
		if(this.bars.size()>=openDuration) {
			BigDecimal[] keyValues = getHighestAndLowest(openDuration);
			BigDecimal highest = keyValues[0];
			BigDecimal lowest = keyValues[1];

			//持有空头头寸，突破高点，上涨势头，平仓
			if(d<0 && price.compareTo(highest)>0) {
				System.out.println("持有空头，但现价" + price + "突破" + openDuration + "天高点" + highest + ",立即平仓！！");
				return true;
			}
			
			//持有多头头寸，突破低点，下跌趋势，平仓
			if(d>0 && price.compareTo(lowest)<0) {
				System.out.println("持有多头，但现价" + price + "突破" + openDuration + "天低点" + lowest + ",立即平仓！！");
				return true;
			}			
		}
		return false;
	}
	
	//----------------------------------
	


	


/*	
	public Kbar addBar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,Integer openDuration) {
		Kbar bar=null;
		if(this.bars.size()==0) {
			//bar = new Bar2(date,open,high,low,close,this.getTR(high, low, close));
			//this.bars.add(bar);
			return bar;
		}
		
		bar = this.getBarByDate(date);
		if(bar == null) {
			Kbar preBar = this.bars.get(this.bars.size()-1);
			BigDecimal preClose = preBar.getClose();
			BigDecimal tr = getTR(high,low,preClose);
			//bar = new Bar2(date,open,high,low,close,tr);
			//System.out.println(bar);
			this.bars.add(bar);
			if(this.bars.size()>openDuration) {
				this.bars.remove(0);
			}
		}else{
			BigDecimal tr;
			if(this.bars.size()>1) {
				Kbar preBar = this.bars.get(this.bars.size()-2);
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
	*/	

	

	


	public BigDecimal getLatestClosePrice() {
		if(bars.size()>0) {
			return bars.get(bars.size()-1).getClose();
		}else {
			return null;
		}
	}

	public String getArticleID() {
		return articleID;
	}
	
	
}

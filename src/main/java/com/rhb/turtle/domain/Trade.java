package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Trade {
	List<Item> items = new ArrayList<Item>();
	List<Record> records = new ArrayList<Record>(); 
	
	private LocalDate beginDate;
	private LocalDate endDate;
	
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌1%，总资金也下跌1%
	 */
	private BigDecimal deficitFactor; 
	
	/*
	 * 通道区间
	 * 该指标很重要
	 */
	private Integer openDuration; 
	private Integer closeDuration; 
	
	/*
	 * 一个品种最多为多少单，默认值是4单
	 */
	private Integer maxOfLot; 
	
	/*
	 * 初始值现金，默认为一百万
	 */
	private BigDecimal initCash;
	
	public Trade(BigDecimal deficitFactor, 
				Integer openDuration, 
				Integer closeDuration, 
				Integer maxOfLot, 
				BigDecimal initCash,
				LocalDate beginDate, 
				LocalDate endDate) {
		this.deficitFactor = deficitFactor;
		this.openDuration = openDuration;
		this.closeDuration = closeDuration;
		this.maxOfLot = maxOfLot;
		this.initCash = initCash;
		this.beginDate = beginDate;
		this.endDate = endDate;
		
	}
	
	public Map<String,String> getResult() {
		Map<String,String> result = new HashMap<String,String>();
		Collections.sort(records, new Comparator<Record>() {
			public int compare(Record o1, Record o2) {
/*				if(o1.getDate().equals(o2.getDate())) {
					return o2.getAtr().compareTo(o1.getAtr());
				}else {
					return o1.getDate().compareTo(o2.getDate());
				}*/
				
/*				if(o1.getDate().equals(o2.getDate())) {
					return o1.getPrice().compareTo(o2.getPrice());
				}else {
					return o1.getDate().compareTo(o2.getDate());
				}*/
				
				return o1.getDate().compareTo(o2.getDate());
				
			}
		});
		
		Account account = new Account(initCash,deficitFactor,beginDate,endDate);
		for(Record record : records) {
			
			//if(record.getDirection()==1) System.out.println(record);
			
			if(record.getDirection()==1) {
				account.buy(record);
			}else if(record.getDirection()==0) {
				account.sell(record);
			}else if(record.getDirection()==-1) {
				
			}
		}
		
		result.put("CSV", account.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
		
		return result;
	}
	
	public void addItem(String code, String name, BigDecimal lot, List<Map<String,String>> kDatas,Map<LocalDate,Set<String>> tops) {
		Item item = new Item(code,name,lot,openDuration, closeDuration);
		
		List<Record> tmps = null;
		Record tmp = null;
		BigDecimal lastOpenPrice = null;  //最近一次开仓价
		BigDecimal stopPrice = null;
		BigDecimal closePrice = null;
		BigDecimal reOpenPrice = null;
		Record record = null;
		Integer d = 0;  //头寸，正数表示多头头寸，负数表示空头头寸
		LocalDate date = null;
		
		BigDecimal open = null;
		BigDecimal high = null;
		BigDecimal low = null;
		BigDecimal close = null;

		Bar bar;
		for(Map<String,String> kData : kDatas) {
			date = LocalDate.parse(kData.get("date"),DateTimeFormatter.ofPattern("yyyy/MM/dd"));
			open = new BigDecimal(kData.get("open"));
			high = new BigDecimal(kData.get("high")); 
			low = new BigDecimal(kData.get("low"));
			close = new BigDecimal(kData.get("close"));
			
			//止损
			if(d!=0) {
				for(Iterator<Record> i=tmps.iterator(); i.hasNext();) {
					tmp = i.next();
					stopPrice = tmp.stop(close);
					if(stopPrice != null){
						record = new Record(tmp.getId(),code,name,date,stopPrice);
						record.setNote("止损(" + tmp.getPrice() + "-" + tmp.getAtr() + "=" + stopPrice +")，当天收盘价：" + close);
						records.add(record);					
						i.remove();
					}
				}
			}
			
			//平仓
			if(d!=0) {
				closePrice = item.closePing(close, d);
				if(closePrice!=null) {
					for(Record r : tmps) {
						record = new Record(r.getId(),code,name,date,closePrice);
						record.setNote("平仓，当天收盘价：" + close);
						records.add(record);					
					}
					d = 0;
					tmps = null;
				}
			}
			
			//开仓
			if(tops.get(date)!=null && tops.get(date).contains(code)) {
				record = item.openPing(date, close);
				if(record != null) {
					if(record.getDirection()>0 && d==0) { //初次开多仓
						record.setNote("建多仓，当天收盘价：" + close);
						lastOpenPrice = record.getPrice();
						records.add(record);
						d++;
						if(tmps == null) {
							tmps = new ArrayList<Record>();
						}
						tmps.add(record);							
					}
					if(record.getDirection()>0 && d>0 && d<maxOfLot) {  //加开多仓
						//lastOpenPrice.add(this.getHalfATR()).compareTo(this.getPrice())==-1) {
						reOpenPrice = record.reOpen(lastOpenPrice);
						if(reOpenPrice != null) {
							record.setNote("加多仓("+ lastOpenPrice + "+" + record.getAtr() + "=" + reOpenPrice +")，当天收盘价：" + close);
							record.setPrice(reOpenPrice);
							lastOpenPrice = record.getPrice();
							records.add(record);
							d++;
							tmps.add(record);		
						}
					}
					if(record.getDirection()<0 && d==0) { //初次开空仓
						record.setNote("建空仓，当天收盘价：" + close);
						lastOpenPrice = record.getPrice();
						records.add(record);
						d--;
						if(tmps == null) {
							tmps = new ArrayList<Record>();
						}
						tmps.add(record);	
					}
					if(record.getDirection()<0 && d<0 && d>-1*maxOfLot) { //加开空仓
						reOpenPrice = record.reOpen(lastOpenPrice);
						if(reOpenPrice != null) {
							record.setNote("加空仓("+ lastOpenPrice + "-" + record.getAtr() + "=" + reOpenPrice +")，当天收盘价：" + close);
							record.setPrice(reOpenPrice);
							lastOpenPrice = record.getPrice();
							records.add(record);
							d--;
							tmps.add(record);
						}
					}
				}
			}
			
			bar = item.addBar(date, open, high, low, close);
/*			
			if(code.equals("600030")) {
				System.out.println(bar);
			}*/
		}
	}
	
	public List<Record> getRecords() {
		return records;
	}

	public BigDecimal getDeficitFactor() {
		return deficitFactor;
	}

	public void setDeficitFactor(BigDecimal deficitFactor) {
		this.deficitFactor = deficitFactor;
	}

	public Integer getOpenDuration() {
		return openDuration;
	}

	public void setOpenDuration(Integer openDuration) {
		this.openDuration = openDuration;
	}

	public Integer getCloseDuration() {
		return closeDuration;
	}

	public void setCloseDuration(Integer closeDuration) {
		this.closeDuration = closeDuration;
	}
}

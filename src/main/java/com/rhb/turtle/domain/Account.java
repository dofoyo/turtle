package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Account {
	private BigDecimal cash;
	private BigDecimal initCash;
	private BigDecimal value = new BigDecimal(0);

	private BigDecimal deficitFactor; 
	
	private Map<String,Record> onHands = new HashMap<String,Record>();
	
	private Map<String,Record> buy_his = new HashMap<String,Record>();
	private Map<String,Record> sell_his = new HashMap<String,Record>();
	
	private LocalDate beginDate;
	private LocalDate endDate;
	
	
	public Account(BigDecimal cash,
			BigDecimal deficitFactor,
			LocalDate beginDate, 
			LocalDate endDate) {
		this.initCash = cash;
		this.cash = cash;
		this.deficitFactor = deficitFactor;
		this.beginDate = beginDate;
		this.endDate = endDate;
	}
	
	public void buy(Record record) {
		
		Integer pu = this.getPositionUnit(record.getAtr(), record.getLot());
		Integer quantity = record.getLot().multiply(new BigDecimal(pu)).intValue();
		
/*		if(record.getCode().equals("600030")) {
			//return this.getTotal().multiply(deficitFactor).divide(atr,BigDecimal.ROUND_HALF_UP).divide(lot,BigDecimal.ROUND_HALF_UP).intValue();
			System.out.println(record.getDate());
			System.out.println("PositionUnit = (total * dificitFactor)/atr/lot = (" + this.getTotal() + "*" + deficitFactor + ")/" + record.getAtr() + "/" + record.getLot() + "=" + pu);
			System.out.println("quantity = lot*PositionUnit=" + record.getLot() + "*" + pu + "=" + quantity);
		}*/
		
		record.setQuantity(quantity);
		
		if(cash.subtract(record.getAmount()).compareTo(new BigDecimal(0))==-1) {
			//System.out.println(record.getDate() + " want to " + record.getNote() + " " + record.getCode()+", but has no cash!");
			record.setQuantity(0);
		}
		
		cash = cash.subtract(record.getAmount());  // 买入时，现金减少
		value = value.add(record.getAmount()); // 市值增加
		
		onHands.put(record.getId(), record);

		buy_his.put(record.getId(), record);
	}
	
	public void sell(Record record) {
		Record r = onHands.get(record.getId());
		if(r!=null) {
			record.setQuantity(r.getQuantity());
			
			cash = cash.add(record.getAmount()); //卖出时，现金增加
			value = value.subtract(record.getAmount());		//市值减少	
			
			onHands.remove(record.getId());

			sell_his.put(record.getId(), record);
		}
	}
	
	public Integer getWinRatio() {
		Integer wins = 0;
		Integer all = buy_his.size();
		Record buy;
		Record sell;
		for(Map.Entry<String,Record> entry : buy_his.entrySet()) {
			buy = entry.getValue();
			sell = sell_his.get(entry.getKey());
			if(sell!=null && sell.getPrice().compareTo(buy.getPrice())==1) {
				wins++;
			}
		}

		return (wins*100)/all;
	}
	
	/*
	 * 复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
	 * b = a(1+x)^n
	 * x = (b/a)^(1/n) - 1
	 * 
	 */
	public Integer getCAGR() {
		Integer n = endDate.getYear() - beginDate.getYear() + 1;
		Double ba = this.getTotal().divide(this.initCash,BigDecimal.ROUND_HALF_UP).doubleValue();
		Double x = (Math.pow(ba, 1.0/n) - 1) * 100;
		return x.intValue();
	}
	
	/*
	 * 根据账户总市值获得头寸规模单位，即一次可买入多少手
	 */
	private Integer getPositionUnit(BigDecimal atr, BigDecimal lot) {
		return this.getTotal().multiply(deficitFactor).divide(atr,BigDecimal.ROUND_HALF_UP).divide(lot,BigDecimal.ROUND_HALF_UP).intValue();
	}

	
	public BigDecimal getCash () {
		return this.cash;
	}
	
	public BigDecimal getValue() {
		BigDecimal total = new BigDecimal(0);
		for(Record r : onHands.values()) {
			total = total.add(r.getAmount());
		}

		return total;
	}
	
	public BigDecimal getTotal() {
		return this.cash.add(this.getValue());
	}

	public String getCSVTitle() {
		StringBuffer sb = new StringBuffer();
		sb.append("code");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("direction");
		sb.append(",");
		sb.append("openDate");
		sb.append(",");
		sb.append("openPrice");
		sb.append(",");
		sb.append("quantity");
		sb.append(",");
		sb.append("openAmount");
		sb.append(",");
		sb.append("buyNote");
		sb.append(",");
		sb.append("closeDate");
		sb.append(",");
		sb.append("closePrice");
		sb.append(",");
		sb.append("closeAmount");
		sb.append(",");
		sb.append("sellNote");
		sb.append(",");
		sb.append("profit");
		sb.append(",");
		sb.append("year");
		sb.append(",");
		sb.append("month");
		sb.append("\n");
		return sb.toString();
	}
	
	public String getCSV() {
		Record buy, sell;
		StringBuffer sb = new StringBuffer(this.getCSVTitle());
		for(Map.Entry<String,Record> entry : buy_his.entrySet()) {
			buy = entry.getValue();
			sell = sell_his.get(entry.getKey());
			sb.append("'" + buy.getCode());
			sb.append(",");
			sb.append(buy.getName());
			sb.append(",");
			sb.append(buy.getDirection());
			sb.append(",");
			sb.append(buy.getDate());
			sb.append(",");
			sb.append(buy.getPrice());
			sb.append(",");
			sb.append(buy.getQuantity());
			sb.append(",");
			sb.append(buy.getAmount());
			sb.append(",");
			sb.append(buy.getNote());
			sb.append(",");
			sb.append(sell==null ? "" : sell.getDate());
			sb.append(",");
			sb.append(sell==null ? "" : sell.getPrice());
			sb.append(",");
			sb.append(sell==null ? "" : sell.getAmount());
			sb.append(",");
			sb.append(sell==null ? "" : sell.getNote());
			sb.append(",");
			sb.append(sell==null ? 0 : sell.getAmount().subtract(buy.getAmount()));
			sb.append(",");
			sb.append(sell==null ? 0 : sell.getDate().getYear());
			sb.append(",");
			sb.append(sell==null ? 0 : sell.getDate().getMonth().getValue());
			sb.append("\n");

		}
		return sb.toString();
	}
}

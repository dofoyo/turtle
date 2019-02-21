package com.rhb.turtle.simulation.repository.entity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BarEntity<T> {
	private String itemID;
	private String code;
	private String name;
	private T dateTime;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal amount;
	
	public BarEntity(T dateTime, String itemID, BigDecimal amount) {
		this.itemID = itemID;
		this.dateTime = dateTime;
		this.amount = amount;
	}
	
	public BarEntity(String itemID,String code, String name, T dateTime, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, BigDecimal amount) {
		this.itemID = itemID;
		this.code = code;
		this.name = name;
		this.dateTime = dateTime;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.amount = amount;
	}
	
	public String getItemID() {
		return itemID;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public T getDateTime() {
		return this.dateTime;
	}

	public void setDateTime(T dateTime) {
		this.dateTime = dateTime;
	}

	public BigDecimal getOpen() {
		return open;
	}

	public void setOpen(BigDecimal open) {
		this.open = open;
	}

	public BigDecimal getHigh() {
		return high;
	}

	public void setHigh(BigDecimal high) {
		this.high = high;
	}

	public BigDecimal getLow() {
		return low;
	}

	public void setLow(BigDecimal low) {
		this.low = low;
	}

	public BigDecimal getClose() {
		return close;
	}

	public void setClose(BigDecimal close) {
		this.close = close;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public Map<String,String> getMap(){
		Map<String,String> m = new HashMap<String,String>();
		m.put("itemID",itemID);
		m.put("dateTime",dateTime.toString());
		m.put("open",open.toString());
		m.put("high",high.toString());
		m.put("low",low.toString());
		m.put("close",close.toString());
		m.put("amount",amount.toString());

		return m;
	}

	@Override
	public String toString() {
		return "BarEntity [itemID=" + itemID + ", code=" + code + ", name=" + name + ", dateTime=" + dateTime + ", open=" + open
				+ ", high=" + high + ", low=" + low + ", close=" + close + ", amount=" + amount + "]";
	}
	
	
}

package com.rhb.turtle.operation;

import java.math.BigDecimal;

public class PreyEntity {
	private String itemID;
	private String code;
	private String name;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal now;
	private Integer hlgap;
	private Integer nhgap;
	
	public PreyEntity(String[] values) {
		this.itemID = values[0];
		this.code = values[1];
		this.name = values[2];
		this.low = new BigDecimal(values[3]);
		this.high = new BigDecimal(values[4]);
		this.now = new BigDecimal(values[5]);
		this.hlgap = Integer.parseInt(values[6]);
		this.nhgap = Integer.parseInt(values[7]);
	}
	
	
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
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


	public BigDecimal getNow() {
		return now;
	}


	public void setNow(BigDecimal now) {
		this.now = now;
	}


	public Integer getHlgap() {
		return hlgap;
	}


	public void setHlgap(Integer hlgap) {
		this.hlgap = hlgap;
	}


	public Integer getNhgap() {
		return nhgap;
	}


	public void setNhgap(Integer nhgap) {
		this.nhgap = nhgap;
	}

	

}

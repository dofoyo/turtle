package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Bar {
	private LocalDate date;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal amount;
	private BigDecimal tr; // 波动幅度
	
	
	public Bar(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,BigDecimal tr) {
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
		this.tr = tr;
	}
	
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
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
	
	public BigDecimal getTr() {
		return tr;
	}

	public void setTr(BigDecimal tr) {
		this.tr = tr;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	@Override
	public String toString() {
		return "Bar [date=" + date + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close
				+ ", amount=" + amount + ", tr=" + tr + "]";
	}



}

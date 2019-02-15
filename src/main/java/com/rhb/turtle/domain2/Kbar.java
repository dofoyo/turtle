package com.rhb.turtle.domain2;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Kbar {
	private String articleID;
	private LocalDate date;
	private BigDecimal open;
	private BigDecimal high;
	private BigDecimal low;
	private BigDecimal close;
	private BigDecimal tr; // 波动幅度
	
	public Kbar(String articleID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		this.articleID = articleID;
		this.date = date;
		this.open = open;
		this.high = high;
		this.low = low;
		this.close = close;
	}
	
	public Kbar(String articleID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close,BigDecimal tr) {
		this.articleID = articleID;
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

	public String getArticleID() {
		return articleID;
	}

	public void setArticleID(String articleID) {
		this.articleID = articleID;
	}

}

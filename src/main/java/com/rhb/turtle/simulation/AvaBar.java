package com.rhb.turtle.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AvaBar implements Comparable<AvaBar>{
	@Override
	public String toString() {
		return "BarEntity [date=" + date + ", id=" + id + ", amount=" + amount + ", ava=" + ava + "]";
	}

	private LocalDate date;
	private String id;
	private BigDecimal amount = new BigDecimal(0);
	private BigDecimal ava = new BigDecimal(0);
	
	public AvaBar(String id) {
		this.id = id;
	}
	
	public AvaBar(LocalDate date,String id, BigDecimal amount) {
		this.date = date;
		this.id = id;
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	public BigDecimal getAva() {
		return ava;
	}

	public void setAva(BigDecimal ava) {
		this.ava = ava;
	}

	@Override
	public int compareTo(AvaBar o) {
		return o.getAva().compareTo(this.getAva());
	}
	
}

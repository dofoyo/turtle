package com.rhb.turtle.simulation.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class AvaBar<T> implements Comparable<AvaBar>{
	@Override
	public String toString() {
		return "BarEntity [datetime=" + datetime + ", id=" + id + ", amount=" + amount + ", ava=" + ava + "]";
	}

	private T datetime;
	private String id;
	private BigDecimal amount = new BigDecimal(0);
	private BigDecimal ava = new BigDecimal(0);
	
	public AvaBar(String id) {
		this.id = id;
	}
	
	public AvaBar(T datetime, String id, BigDecimal amount) {
		this.datetime = datetime;
		this.id = id;
		this.amount = amount;
	}

	public T getDatetime() {
		return datetime;
	}

	public void setDate(T date) {
		this.datetime = date;
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

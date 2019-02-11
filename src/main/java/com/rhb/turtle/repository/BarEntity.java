package com.rhb.turtle.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public class BarEntity  implements Comparable<BarEntity>{
	@Override
	public String toString() {
		return "BarEntity [date=" + date + ", code=" + code + ", amount=" + amount + ", ava=" + ava + "]";
	}

	private LocalDate date;
	private String code;
	private BigDecimal amount = new BigDecimal(0);
	private BigDecimal ava = new BigDecimal(0);
	
	public BarEntity(String code) {
		this.code = code;
	}
	
	public BarEntity(LocalDate date,String code, BigDecimal amount) {
		this.date = date;
		this.code = code;
		this.amount = amount;
	}

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
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
	public int compareTo(BarEntity o) {
		return o.getAva().compareTo(this.getAva());
	}
	
}

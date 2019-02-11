package com.rhb.turtle.repository;

import java.math.BigDecimal;
import java.util.LinkedList;

public class Avarage {
	private Integer top = 300;
	private LinkedList<BarEntity> bars = new LinkedList<BarEntity>();
	private BigDecimal total = new BigDecimal(0);
	
	public BigDecimal getAva(BarEntity bar) {
		bars.add(bar);
		this.total = this.total.add(bar.getAmount());
		if(bars.size() > 300) {
			BarEntity b = bars.removeFirst();
			this.total = this.total.subtract(b.getAmount());
		}
		
		BigDecimal ava = this.total.divide(new BigDecimal(bars.size()),BigDecimal.ROUND_HALF_UP);
		
		/*
		if(bar.getCode().equals("601318")) {
			System.out.println(bar.getCode() + "," + bar.getDate() + ",amount=" + bar.getAmount() + ", ava=" + total + "/" + bars.size() + "=" + ava);
		}
		*/
		
		return ava;
	}
	
	public boolean isOk() {
		return bars.size()==top ? true : false;
	}
}

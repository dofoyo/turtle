package com.rhb.turtle.simulation;

import java.math.BigDecimal;
import java.util.LinkedList;

public class Avarage {
	private Integer top = 300;
	private LinkedList<AvaBar> bars = new LinkedList<AvaBar>();
	private BigDecimal total = new BigDecimal(0);
	private String id = null;
	
	public BigDecimal getAmountAvarage(AvaBar bar){
		if(id == null) {
			id = bar.getId();
		}else{
			if(!bar.getId().equals(id)) {
				try {
					throw new Exception("id "+id + " 和 " + bar.getId() +" 不一致，无法求平均");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		bars.add(bar);
		this.total = this.total.add(bar.getAmount());
		if(bars.size() > 300) {
			AvaBar b = bars.removeFirst();
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

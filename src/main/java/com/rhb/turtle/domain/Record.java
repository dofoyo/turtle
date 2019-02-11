package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Record {
	private String id;
	private String code;
	private String name;
	
	/*
	 * 时间
	 */
	private LocalDate date;  
	/*
	 * 方向：“1”为买多，“-1”为卖空，“0”为平仓
	 */
	private Integer direction; 

	/*
	 * 价格
	 */
	private BigDecimal price;
	
	/*
	 * 平均波动幅度
	 */
	private BigDecimal atr = new BigDecimal(0);
	
	private BigDecimal lot;
	
	/*
	 * 仓单数量
	 */
	private Integer quantity;
	
	private String note;

	/*
	 * open时用
	 */
	public Record(String id, String code, String name, LocalDate date,Integer direction, BigDecimal price, BigDecimal atr,BigDecimal lot) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.date = date;
		this.direction = direction;
		this.price = price;
		this.atr = atr;
		this.lot = lot;
		this.quantity = null; // 开仓时的数量要根据执行时的市值计算得来
	}
	
	/*
	 * close时用（即平仓或止损）
	 */
	public Record(String id, String code, String name, LocalDate date, BigDecimal price) {
		this.id = id;   //与开仓的id一致
		this.code = code;
		this.name = name;
		this.date = date; 
		this.direction = 0;
		this.price = price;
		this.quantity = null; // 与开仓时的数量一致
	}
	
	/*
	 * 判断是否止损，如果止损，返回止损价；不止损，返回值为null
	 * 多仓时，当止损价大于现价，止损
	 * 空仓时，当止损价小于现价，止损
	 */
	public BigDecimal stop(BigDecimal price) {
		if(this.direction==1 && this.price.subtract(this.getAtr()).compareTo(price)==1) {
			return this.price.subtract(this.getAtr());
		}else if(this.direction==-1 && this.price.add(this.getAtr()).compareTo(price)==-1) {
			return this.price.add(this.getAtr());
		}
		return null;
	}
	
	/*
	 * 判断是否加开仓位，加仓价与最后一次建仓价格至少相差1/2个ATR
	 * 多仓时，加仓价小于现价，加仓
	 * 空仓时，加仓价大于现价，加仓
	 */
	public BigDecimal reOpen(BigDecimal lastOpenPrice) {
		//System.out.println(this.getDate() +"加仓价：" + lastOpenPrice.add(this.getHalfATR()) + ", 现价：" + this.getPrice());
		if(this.direction==1 && lastOpenPrice.add(this.getAtr()).compareTo(this.getPrice())==-1) {
			return lastOpenPrice.add(this.getAtr());
		}else if(this.direction==-1 && lastOpenPrice.subtract(this.getAtr()).compareTo(this.getPrice())==1) {
			return lastOpenPrice.subtract(this.getAtr());
		}
		return null;		
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public BigDecimal getHalfATR() {
		return this.atr.divide(new BigDecimal(2));
	}
	
	public BigDecimal getDoubleATR() {
		return this.atr.multiply(new BigDecimal(2));
	}
	
	public BigDecimal getAmount() {
		return price.multiply(new BigDecimal(quantity));
	}

	public BigDecimal getLot() {
		return lot;
	}

	public void setLot(BigDecimal lot) {
		this.lot = lot;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public BigDecimal getAtr() {
		return atr;
	}

	public void setAtr(BigDecimal atr) {
		this.atr = atr;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}

	@Override
	public String toString() {
		return "Record [id=" + id + ", code=" + code + ", name=" + name + ", date=" + date + ", direction=" + direction
				+ ", price=" + price + ", atr=" + atr + ", lot=" + lot + ", quantity=" + quantity + ", note=" + note
				+ "]";
	}
	
}

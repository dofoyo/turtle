package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Order {
	private String orderID;
	
	private String articleID;
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
	 * 仓单数量
	 */
	private Integer quantity;

	/*
	 * 止损价
	 */
	private BigDecimal stopPrice;

	/*
	 * 加仓价
	 */
	private BigDecimal reopenPrice;

	
	private String note;

	/*
	 * open时用
	 */
	public Order(String orderID,String articleID, LocalDate date,Integer direction, BigDecimal price, BigDecimal stopPrice, BigDecimal reopenPrice) {
		this.orderID = orderID;
		this.articleID = articleID;
		this.date = date;
		this.direction = direction;
		this.price = price;
		this.stopPrice = stopPrice;
		this.reopenPrice = reopenPrice;
	}
	
	/*
	 * close或stop时用（即平仓或止损）
	 */
	public Order(String orderID, LocalDate date, BigDecimal price,Integer quantity) {
		this.orderID = orderID;   //与开仓的id一致
		this.date = date; 
		this.direction = 0;
		this.price = price;
		this.quantity = quantity;
	}


	public BigDecimal getAmount() {
		return price.multiply(new BigDecimal(quantity));
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

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
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

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public String getItemID() {
		return articleID;
	}

	public void setArticleID(String articleID) {
		this.articleID = articleID;
	}

	public BigDecimal getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(BigDecimal stopPrice) {
		this.stopPrice = stopPrice;
	}

	public BigDecimal getReopenPrice() {
		return reopenPrice;
	}

	public void setReOpenPrice(BigDecimal reopenPrice) {
		this.reopenPrice = reopenPrice;
	}

	@Override
	public String toString() {
		return "Order [orderID=" + orderID + ", articleID=" + articleID + ", date=" + date + ", direction=" + direction
				+ ", price=" + price + ", quantity=" + quantity + ", stopPrice=" + stopPrice + ", reopenPrice="
				+ reopenPrice + ", note=" + note + "]";
	}
	
}

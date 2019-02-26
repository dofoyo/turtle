package com.rhb.turtle.operation;

public class OrderEntity {
	private String orderID;
	
	private String itemID;
	/*
	 * 时间
	 */
	private String date;  
	/*
	 * 方向：“1”为买多，“-1”为卖空，“0”为平仓
	 */
	private String direction; 

	/*
	 * 价格
	 */
	private String price;
	
	/*
	 * 仓单数量
	 */
	private String quantity;

	/*
	 * 止损价
	 */
	private String stopPrice;

	/*
	 * 加仓价
	 */
	private String reopenPrice;

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getQuantity() {
		return quantity;
	}

	public void setQuantity(String quantity) {
		this.quantity = quantity;
	}

	public String getStopPrice() {
		return stopPrice;
	}

	public void setStopPrice(String stopPrice) {
		this.stopPrice = stopPrice;
	}

	public String getReopenPrice() {
		return reopenPrice;
	}

	public void setReopenPrice(String reopenPrice) {
		this.reopenPrice = reopenPrice;
	}

	@Override
	public String toString() {
		return "OrderEntity [orderID=" + orderID + ", itemID=" + itemID + ", date=" + date + ", direction=" + direction
				+ ", price=" + price + ", quantity=" + quantity + ", stopPrice=" + stopPrice + ", reopenPrice="
				+ reopenPrice + "]";
	}


}

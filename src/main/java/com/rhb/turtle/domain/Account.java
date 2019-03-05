package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class Account {
	protected static final Logger logger = LoggerFactory.getLogger(Account.class);

	private BigDecimal cash;
	private BigDecimal initCash;
	private BigDecimal value = new BigDecimal(0);

	private Map<String,Order> onHands = new HashMap<String,Order>();
	
	private Map<String,Order> open_his = new HashMap<String,Order>();
	private Map<String,Order> close_his = new HashMap<String,Order>();
	private Map<String,Price> latestPrices = new HashMap<String,Price>(); //主要用于计算持仓利润
	
	private LocalDate beginDate = null;
	private LocalDate endDate = null;
	
	public Account(BigDecimal cash) {
		this.initCash = cash;
		this.cash = cash;
	}
	
	public void updatePrice(String itemID, String date, String price) {
		latestPrices.put(itemID, new Price(date,price));
	}
	
	public void putOrder(Order order) {
		onHands.put(order.getOrderID(), order);
	}

	public List<String> getItemIDsOfOnHand() {
		List<String> ids = new ArrayList<String>();
		for(Order order : onHands.values()) {
			ids.add(order.getItemID());
		}
		return ids;
	}

	
	public Integer getLots(String itemID) {
		Integer lot = 0;
		for(Order order : onHands.values()) {
			if(order.getItemID().equals(itemID)) {
				lot = lot + order.getDirection();
			}
		}
		return lot;
	}
	
	public List<Order> getOrders(String itemID){
		List<Order> orders = new ArrayList<Order>();
		for(Order order : onHands.values()) {
			if(order.getItemID().equals(itemID)) {
				orders.add(order);
			}
		}		
		
		Collections.sort(orders,new Comparator<Order>() {
			@Override
			public int compare(Order o1, Order o2) {
				return o1.getDate().compareTo(o2.getDate());
			}});
		return orders;
	}
	
	
	public BigDecimal getStopPrice(String itemID) {
		BigDecimal stopPrice = null;
		LocalDate date=null;
		for(Order order : onHands.values()) {
			if(order.getItemID().equals(itemID)) {
				if(date==null) {
					date = order.getDate();
					stopPrice = order.getStopPrice();
				}else {
					if(date.isBefore(order.getDate())) {
						date = order.getDate();
						stopPrice = order.getStopPrice();
					}
				}
			}
		}
		return stopPrice;
	}
	
	public BigDecimal getReopenPrice(String itemID) {
		BigDecimal reopenPrice = new BigDecimal(0);
		LocalDate date=null;
		for(Order order : onHands.values()) {
			if(order.getItemID().equals(itemID)) {
				if(date==null) {
					date = order.getDate();
					reopenPrice = order.getReopenPrice();
				}else {
					if(date.isBefore(order.getDate())) {
						date = order.getDate();
						reopenPrice = order.getReopenPrice();
					}
				}
			}
		}
		return reopenPrice;
	}
	
	public void open(Order openOrder, BigDecimal deficitFactor, BigDecimal atr, BigDecimal lot) {
		BigDecimal unit = new BigDecimal(this.getPositionUnit(atr, lot, deficitFactor));
	
		openOrder.setQuantity(lot.multiply(unit).intValue());
		
		if(cash.subtract(openOrder.getAmount()).compareTo(new BigDecimal(0))==-1) {
			openOrder.setQuantity(0);
		}
		
		cash = cash.subtract(openOrder.getAmount());  // 买入时，现金减少
		value = value.add(openOrder.getAmount()); // 市值增加
		onHands.put(openOrder.getOrderID(), openOrder);
		open_his.put(openOrder.getOrderID(), openOrder);
		
		if(beginDate == null) {
			beginDate = openOrder.getDate();
		}
		endDate = openOrder.getDate();
	}
	
	public void drop(Map<String,String> kData) {
		BigDecimal nowPrice = new BigDecimal(kData.get("close"));
		LocalDate date = LocalDate.parse(kData.get("dateTime"));
		
		Order openOrder;
		for(Iterator<Map.Entry<String, Order>> it=onHands.entrySet().iterator(); it.hasNext();) {
			openOrder = it.next().getValue();
			if(openOrder.getItemID().equals(kData.get("itemID"))) {
				Order closeOrder = new Order(openOrder.getOrderID(),date,nowPrice,openOrder.getQuantity());
				closeOrder.setNote("drop");
				//closeOrder.setCloseRateOfHL(rate);
				
				cash = cash.add(closeOrder.getAmount()); //卖出时，现金增加
				value = value.subtract(closeOrder.getAmount());		//市值减少
				it.remove();
				close_his.put(closeOrder.getOrderID(), closeOrder);
				endDate = date;
			}
		}
	}
	
	public void stop(Map<String,String> kData) {
		BigDecimal low = new BigDecimal(kData.get("low"));
		BigDecimal nowPrice = new BigDecimal(kData.get("close"));
		LocalDate date = LocalDate.parse(kData.get("dateTime"));
		Order openOrder;
		for(Iterator<Map.Entry<String, Order>> it=onHands.entrySet().iterator(); it.hasNext();) {
			openOrder = it.next().getValue();
			//System.out.println(openOrder);
			//System.out.println(openOrder.getItemID());
			if(openOrder.getItemID().equals(kData.get("itemID"))) {
				if(low.compareTo(openOrder.getStopPrice())==-1 && openOrder.getDirection()==1){
					
					//String msg =  kData.get("itemID") + "，" + kData.get("dateTime") + "，盘中最低价" + kData.get("low") + "跌破止损价" + openOrder.getStopPrice() + "，止损！！！";
					//logger.info(msg);
					
					Order closeOrder = new Order(openOrder.getOrderID(),date,nowPrice,openOrder.getQuantity());
					closeOrder.setNote("stop");
					
					cash = cash.add(closeOrder.getAmount()); //卖出时，现金增加
					value = value.subtract(closeOrder.getAmount());		//市值减少	
					it.remove();
					close_his.put(closeOrder.getOrderID(), closeOrder);
					endDate = date;
				}
			}
		}
	}
	
	//----------------------------------------
	
	public Integer getWinRatio() {
		Integer wins = 0;
		Integer all = open_his.size();
		Order openOrder;
		Order closeOrder;
		for(Map.Entry<String,Order> entry : open_his.entrySet()) {
			openOrder = entry.getValue();
			closeOrder = close_his.get(entry.getKey());
			if(closeOrder!=null && closeOrder.getPrice().compareTo(openOrder.getPrice())==1) {
				wins++;
			}
		}

		return all==0 ? 0 :(wins*100)/all;
	}
	
	/*
	 * 复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
	 * b = a(1+x)^n
	 * x = (b/a)^(1/n) - 1
	 * 
	 */
	public Integer getCAGR() {
		Integer cage = 0;
		if(endDate!=null && beginDate!=null) {
			Integer years = endDate.getYear() - beginDate.getYear() + 1;
			Double ba = this.getTotal().divide(this.initCash,BigDecimal.ROUND_HALF_UP).doubleValue();
			Double x = (Math.pow(ba, 1.0/years) - 1) * 100;
			cage =  x.intValue();
		}
		return cage;
	}
	
	/*
	 * 根据账户总市值获得头寸规模单位，即一次可买入多少手
	 */
	private Integer getPositionUnit(BigDecimal atr, BigDecimal lot, BigDecimal deficitFactor) {
		return this.getTotal().multiply(deficitFactor).divide(atr,BigDecimal.ROUND_HALF_UP).divide(lot,BigDecimal.ROUND_HALF_UP).intValue();
	}

	
	public BigDecimal getCash () {
		return this.cash;
	}
	
	public BigDecimal getValue() {
		BigDecimal total = new BigDecimal(0);
		for(Order order : onHands.values()) {
			total = total.add(latestPrices.get(order.getItemID()).getPrice().multiply(new BigDecimal(order.getQuantity())));
		}

		return total;
	}
	
	public BigDecimal getTotal() {
		return this.cash.add(this.getValue());
	}

	public String getCSVTitle() {
		StringBuffer sb = new StringBuffer();
		sb.append("code");
		sb.append(",");
		sb.append("name");
		sb.append(",");
		sb.append("direction");
		sb.append(",");
		sb.append("openDate");
		sb.append(",");
		sb.append("openPrice");
		sb.append(",");
		sb.append("quantity");
		sb.append(",");
		sb.append("openAmount");
		sb.append(",");
		sb.append("buyNote");
		sb.append(",");
		sb.append("closeDate");
		sb.append(",");
		sb.append("closePrice");
		sb.append(",");
		sb.append("closeAmount");
		sb.append(",");
		sb.append("sellNote");
		sb.append(",");
		sb.append("profit");
		sb.append(",");
		sb.append("year");
		sb.append(",");
		sb.append("month");
		sb.append("\n");
		return sb.toString();
	}
	
	public String getCSV() {
		Order openOrder, closeOrder;
		StringBuffer sb = new StringBuffer(this.getCSVTitle());
		for(Map.Entry<String,Order> entry : open_his.entrySet()) {
			openOrder = entry.getValue();
			closeOrder = close_his.get(entry.getKey());
			if(closeOrder==null) {
				closeOrder = new Order(openOrder.getOrderID(),latestPrices.get(openOrder.getItemID()).getDate(),latestPrices.get(openOrder.getItemID()).getPrice(),openOrder.getQuantity());
				closeOrder.setNote("cloze");
			}
			sb.append("'" + openOrder.getItemID());
			sb.append(",");
			sb.append("");
			sb.append(",");
			sb.append(openOrder.getDirection());
			sb.append(",");
			sb.append(openOrder.getDate());
			sb.append(",");
			sb.append(openOrder.getPrice());
			sb.append(",");
			sb.append(openOrder.getQuantity());
			sb.append(",");
			sb.append(openOrder.getAmount());
			sb.append(",");
			sb.append(openOrder.getNote());
			sb.append(",");
			sb.append(closeOrder.getDate());
			sb.append(",");
			sb.append(closeOrder.getPrice());
			sb.append(",");
			sb.append(closeOrder.getAmount());
			sb.append(",");
			sb.append(closeOrder.getNote());
			sb.append(",");
			sb.append(closeOrder.getAmount().subtract(openOrder.getAmount()));
			sb.append(",");
			sb.append(closeOrder.getDate().getYear());
			sb.append(",");
			sb.append(closeOrder.getDate().getMonth().getValue());
			sb.append("\n");

		}
		return sb.toString();
	}
	
	class Price {
		private LocalDate date;
		private BigDecimal price;
		
		public Price(String date, String price) {
			this.date = LocalDate.parse(date);
			this.price = new BigDecimal(price);
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
		
	}
}

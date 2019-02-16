package com.rhb.turtle.domain2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.turtle.operation.service.TurtleOperationServiceImp;

public class Fund {
	protected static final Logger logger = LoggerFactory.getLogger(Fund.class);

	private BigDecimal cash;
	private BigDecimal initCash;
	private BigDecimal value = new BigDecimal(0);

	private Map<String,Order> onHands = new HashMap<String,Order>();
	
	private Map<String,Order> open_his = new HashMap<String,Order>();
	private Map<String,Order> close_his = new HashMap<String,Order>();
	
	private LocalDate beginDate = null;
	private LocalDate endDate = null;
	
	public Fund(BigDecimal cash) {
		this.initCash = cash;
		this.cash = cash;
	}

	public List<String> getArticleIDsOfOnHand() {
		List<String> ids = new ArrayList<String>();
		for(Order order : onHands.values()) {
			ids.add(order.getArticleID());
		}
		return ids;
	}

	
	public Integer getLots(String articleID) {
		Integer lot = 0;
		for(Order order : onHands.values()) {
			if(order.getArticleID().equals(articleID)) {
				lot = lot + order.getDirection();
			}
		}
		return lot;
	}
	
	public BigDecimal getReopenPrice(String articleID) {
		BigDecimal reopenPrice = new BigDecimal(0);
		for(Order order : onHands.values()) {
			if(order.getArticleID().equals(articleID)) {
				if(reopenPrice.compareTo(order.getReopenPrice())==-1) {
					reopenPrice = order.getReopenPrice();
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
	
	public void close(String articleID, LocalDate date, BigDecimal price) {
		Order openOrder;
		for(Iterator<Map.Entry<String, Order>> it=onHands.entrySet().iterator(); it.hasNext();) {
			openOrder = it.next().getValue();
			if(openOrder.getArticleID().equals(articleID)) {
				Order closeOrder = new Order(openOrder.getOrderID(),date,price,openOrder.getQuantity());
				closeOrder.setNote("close");
				
				cash = cash.add(closeOrder.getAmount()); //卖出时，现金增加
				value = value.subtract(closeOrder.getAmount());		//市值减少
				it.remove();
				//onHands.remove(closeOrder.getOrderID());
				close_his.put(closeOrder.getOrderID(), closeOrder);
				endDate = date;
			}
		}
	}
	
	public void stop(String articleID, LocalDate date, BigDecimal price) {
		Order openOrder;
		for(Iterator<Map.Entry<String, Order>> it=onHands.entrySet().iterator(); it.hasNext();) {
			openOrder = it.next().getValue();
			if(openOrder.getArticleID().equals(articleID)) {
				/*if(openOrder.getArticleID().equals("sh600804")) {
					System.out.println(openOrder);
					System.out.println(date + " market price is " + price + ", should stop? " + (price.compareTo(openOrder.getStopPrice())==-1 && openOrder.getDirection()==1));
				}*/
				if(price.compareTo(openOrder.getStopPrice())==-1 && openOrder.getDirection()==1){
					String msg = date + " stop price is " + openOrder.getStopPrice() + ", market price is " + price + ", should stop? " + (price.compareTo(openOrder.getStopPrice())==-1 && openOrder.getDirection()==1);
					System.out.println(msg);
					logger.warn(msg);
					
					Order closeOrder = new Order(openOrder.getOrderID(),date,price,openOrder.getQuantity());
					closeOrder.setNote("stop");
					
					cash = cash.add(closeOrder.getAmount()); //卖出时，现金增加
					value = value.subtract(closeOrder.getAmount());		//市值减少	
					it.remove();
					//onHands.remove(closeOrder.getOrderID()); 
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

		return (wins*100)/all;
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
		for(Order r : onHands.values()) {
			total = total.add(r.getAmount());
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
			sb.append("'" + openOrder.getArticleID());
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
			sb.append(closeOrder==null ? "" : closeOrder.getDate());
			sb.append(",");
			sb.append(closeOrder==null ? "" : closeOrder.getPrice());
			sb.append(",");
			sb.append(closeOrder==null ? "" : closeOrder.getAmount());
			sb.append(",");
			sb.append(closeOrder==null ? "" : closeOrder.getNote());
			sb.append(",");
			sb.append(closeOrder==null ? 0 : closeOrder.getAmount().subtract(openOrder.getAmount()));
			sb.append(",");
			sb.append(closeOrder==null ? 0 : closeOrder.getDate().getYear());
			sb.append(",");
			sb.append(closeOrder==null ? 0 : closeOrder.getDate().getMonth().getValue());
			sb.append("\n");

		}
		return sb.toString();
	}
}

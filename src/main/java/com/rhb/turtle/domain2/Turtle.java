package com.rhb.turtle.domain2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class Turtle {
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌1%，总资金也下跌1%
	 */
	private BigDecimal deficitFactor; 
	
	/*
	 * 开仓判定通道区间
	 */
	private Integer openDuration; 

	/*
	 * 平仓判定通道区间
	 */
	private Integer closeDuration; 
	
	/*
	 * 一个品种最多为多少单，默认值是4单
	 */
	private Integer maxOfLot; 
	
	/*
	 * 初始值现金，默认为一百万
	 */
	private BigDecimal initCash;
	
	private Map<String, Article> articles;
	private Fund fund;

	public Turtle(BigDecimal deficitFactor, 
				Integer openDuration, 
				Integer closeDuration, 
				Integer maxOfLot, 
				BigDecimal initCash) {
		
		this.deficitFactor = deficitFactor;
		this.openDuration = openDuration;
		this.closeDuration = closeDuration;
		this.maxOfLot = maxOfLot;
		this.initCash = initCash;
		fund = new Fund(initCash);
		articles = new HashMap<String,Article>();
	}
	
	public Set<String> getArticleIDsOfOnHand() {
		return fund.getArticleIDsOfOnHand();
	}
	
	public void doit(List<Map<String,String>> kDatas) {
		String articleID;
		LocalDate date;
		BigDecimal open;
		BigDecimal high;
		BigDecimal low;
		BigDecimal close;
		
		Article article;
		Order order;
		for(Map<String,String> kData : kDatas) {
			articleID = kData.get("id");
			date = LocalDate.parse(kData.get("date"));
			open = new BigDecimal(kData.get("open"));
			high = new BigDecimal(kData.get("high"));
			low = new BigDecimal(kData.get("low"));
			close = new BigDecimal(kData.get("close"));
			
			article = articles.get(articleID);
			if(article == null) {
				article = new Article(articleID);
				articles.put(articleID, article);
			}
			
			//止损
			doStop(date, article.getArticleID(), close);
			
			//平仓
			doClose(date, article, closeDuration, close);
			
			//开新仓、加仓
			doOpen(date, article, openDuration, close);
			
			article.addBar(date, open, high, low, close, openDuration);
		}
	}
	
	//止损
	private void doStop(LocalDate date, String articleID, BigDecimal price){
		fund.stop(articleID, date, price);
	}
	
	
	//平仓
	private void doClose(LocalDate date, Article article, Integer closeDuration, BigDecimal price) {
		Integer lots = fund.getLots(article.getArticleID());
		if(lots != 0) { //有持仓
			boolean flag = article.closePing(price, lots, closeDuration);
			if(flag) {  //触发平仓
				fund.close(article.getArticleID(), date, price);
			}
		}
		
	}
	
	//开仓
	private void doOpen(LocalDate date, Article article, Integer openDuration, BigDecimal price) {
		Order openOrder = null;
		Integer flag = article.openPing(price, openDuration);
		if(flag != null) {
			Integer lots = fund.getLots(article.getArticleID());
			BigDecimal atr = article.getATR(openDuration);
			
			if(flag==1 && lots==0) { //初次开多仓
				BigDecimal stopPrice = price.subtract(atr);
				BigDecimal reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
				openOrder = new Order(UUID.randomUUID().toString(),	article.getArticleID(),	date, 1, price,	stopPrice,	reopenPrice	);
				openOrder.setNote("open");
			}
			
			if(flag==1 && lots>0 && lots<maxOfLot) {  //加开多仓
				BigDecimal reopenPrice = fund.getReopenPrice(article.getArticleID());
				if(price.compareTo(reopenPrice)==1) {
					BigDecimal stopPrice = price.subtract(atr);
					reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
					openOrder = new Order(UUID.randomUUID().toString(),	article.getArticleID(),	date, 1, price,	stopPrice,	reopenPrice	);
					openOrder.setNote("reOpen");
				}
			}
			
			/* 目前不做空
			 * 
			if(flag==-1 && lots==0) { //初次开空仓
				BigDecimal stopPrice = price.add(atr);
				BigDecimal reOpenPrice = price.subtract(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
				order = new Order(UUID.randomUUID().toString(),	article.getArticleID(),	date, -1, price,	stopPrice,	reOpenPrice	);
				order.setNote("open");
			}
			
			if(flag==-1 && lots<0 && lots>-1*maxOfLot) { //加开空仓
				BigDecimal stopPrice = price.add(atr);
				BigDecimal reOpenPrice = price.subtract(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
				order = new Order(UUID.randomUUID().toString(),	article.getArticleID(),	date, -1, price,	stopPrice,	reOpenPrice	);
					order.setNote("reOpen");
			}
			*/
			if(openOrder!=null) {
				fund.open(openOrder, deficitFactor, atr, getLot(article.getArticleID()));
			}
		}
		
	}
	
	public Map<String,String> result() {
		if(fund == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", fund.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", fund.getCash().toString());
		result.put("value", fund.getValue().toString());
		result.put("total", fund.getTotal().toString());
		result.put("winRatio", fund.getWinRatio().toString()); //赢率
		result.put("cagr", fund.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
		return result;
	}
	
	/*
	 *
	 * 一手的数量，股票是100股，螺纹钢是10吨，...
	 * 目前只针对股票
	 */
	private BigDecimal getLot(String articleID) {
		return new BigDecimal(100);
	}
	
	
}

package com.rhb.turtle.domain2;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	
	public Map<String,BigDecimal> getArticlePrices(String articleID){
		Article article = articles.get(articleID);
		BigDecimal[] highAndLow = article.getHighestAndLowest(openDuration);
		Map<String,BigDecimal> line = new HashMap<String,BigDecimal>();
		line.put("high", highAndLow[0]);
		line.put("low", highAndLow[1]);
		return line;
	}
	
	public List<String> getArticleIDsOfOnHand() {
		return fund.getArticleIDsOfOnHand();
	}
	
	public void doit(Map<String,String> kData) {
		Kbar bar = getKbar(kData);
		
		Article article = articles.get(bar.getArticleID());
		if(article == null) {
			article = new Article(bar.getArticleID());
			articles.put(bar.getArticleID(), article);
		}
		
		//止损
		doStop(bar.getDate(), article.getArticleID(), bar.getClose());
		
		//平仓
		doClose(bar.getDate(), article, closeDuration, bar.getClose());
		
		//开新仓、加仓
		doOpen(bar.getDate(), article, openDuration, bar.getClose());
	}
	
	public void addBar(Map<String,String> kData) {
		Kbar bar = getKbar(kData);
		Article article = articles.get(bar.getArticleID());
		if(article == null) {
			article = new Article(bar.getArticleID());
			articles.put(bar.getArticleID(), article);
		}	
		article.addBar(article.getArticleID(),bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), openDuration);
	}
	
	public void addBar(List<Map<String,String>> kDatas) {
		for(Map<String,String> kData : kDatas) {
			this.addBar(kData);
		}
	}
	
	private Kbar getKbar(Map<String,String> kData) {
		String articleID = kData.get("id");
		LocalDate date = LocalDate.parse(kData.get("date"));
		BigDecimal open = new BigDecimal(kData.get("open"));
		BigDecimal high = new BigDecimal(kData.get("high"));
		BigDecimal low = new BigDecimal(kData.get("low"));
		BigDecimal close = new BigDecimal(kData.get("close"));
		return new Kbar(articleID,date,open,high,low,close);
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

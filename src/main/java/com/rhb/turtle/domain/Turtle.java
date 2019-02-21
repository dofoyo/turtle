package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/*
 * 按照海龟的标准版本，突破上轨买入，跌破下轨卖出
 * 最高的年化收益率16%
 * 
 */
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
	 * 一只股票最大持仓单位
	 */
	private Integer maxOfLot = 3; 
	
	/*
	 * 初始值现金，默认为一百万
	 */
	private BigDecimal initCash;
	
	private Map<String, Item> items;
	private Account fund;

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
		fund = new Account(initCash);
		items = new HashMap<String,Item>();
	}
	
	public List<Bar> getKbars(String articleID){
		Item article = items.get(articleID);
		return article.getBars();
	}
	
	public Map<String,BigDecimal> getItemPrices(String articleID){
		Item article = items.get(articleID);
		BigDecimal[] highAndLow = article.getHighestAndLowest(openDuration);
		Map<String,BigDecimal> line = new HashMap<String,BigDecimal>();
		line.put("high", highAndLow[0]);
		line.put("low", highAndLow[1]);
		return line;
	}
	
	public List<String> getArticleIDsOfOnHand() {
		return fund.getItemIDsOfOnHand();
	}
	
	public void doit(Map<String,String> kData, boolean isStop) {
		Bar bar = getBar(kData);
		
		Item item = items.get(bar.getItemID());
		if(item == null) {
			item = new Item(bar.getItemID());
			items.put(bar.getItemID(), item);
		}
		
		//止损
		if(isStop) {
			doStop(bar);
		}
		
		//平仓
		doClose(bar, closeDuration);
		
		//开新仓、加仓
		doOpen(bar, openDuration);
	}
	
	public void addBar(Map<String,String> kData) {
		Bar bar = getBar(kData);
		Item article = items.get(bar.getItemID());
		if(article == null) {
			article = new Item(bar.getItemID());
			items.put(bar.getItemID(), article);
		}	
		article.addBar(article.getItemID(),bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), openDuration);
	}
	
	public void addBar(List<Map<String,String>> kDatas) {
		for(Map<String,String> kData : kDatas) {
			this.addBar(kData);
		}
	}
	
	private Bar getBar(Map<String,String> kData) {
		//System.out.println(kData);
		String itemID = kData.get("itemID");
		LocalDate date = LocalDate.parse(kData.get("dateTime"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		BigDecimal open = new BigDecimal(kData.get("open"));
		BigDecimal high = new BigDecimal(kData.get("high"));
		BigDecimal low = new BigDecimal(kData.get("low"));
		BigDecimal close = new BigDecimal(kData.get("close"));
		return new Bar(itemID,date,open,high,low,close);
	}
	
	
	//止损
	private void doStop(Bar bar){
		fund.stop(bar);
	}
	
	//平仓
	private void doClose(Bar bar, Integer closeDuration) {
		Item item = items.get(bar.getItemID());
		Integer lots = fund.getLots(item.getItemID());
		if(lots != 0) { //有持仓
			boolean flag = item.closePing(bar, lots, closeDuration);
			if(flag) {  //触发平仓
				fund.close(item.getItemID(), bar.getDate(), bar.getClose());
			}
		}
	}
	
	//开仓
	private void doOpen(Bar bar, Integer openDuration) {
		Item item = items.get(bar.getItemID());
		Order openOrder = null;
		Integer flag = item.openPing(bar, openDuration);
		if(flag != null) {
			Integer lots = fund.getLots(item.getItemID());
			//System.out.println("lots = " + lots);
			BigDecimal atr = item.getATR(openDuration);
			if(flag==1 && lots==0) { //初次开多仓
				BigDecimal stopPrice = bar.getClose().subtract(atr);
				BigDecimal reopenPrice = bar.getClose().add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
				openOrder = new Order(UUID.randomUUID().toString(),	item.getItemID(),	bar.getDate(), 1, bar.getClose(),	stopPrice,	reopenPrice	);
				openOrder.setNote("open");
			}
			
			if(flag==1 && lots>0 && lots<maxOfLot) {  //加开多仓
				BigDecimal reopenPrice = fund.getReopenPrice(item.getItemID());
				if(bar.getHigh().compareTo(reopenPrice)==1) {
					BigDecimal stopPrice = bar.getClose().subtract(atr);
					reopenPrice = bar.getClose().add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
					openOrder = new Order(UUID.randomUUID().toString(),	item.getItemID(),	bar.getDate(), 1, bar.getClose(),	stopPrice,	reopenPrice	);
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
				fund.open(openOrder, deficitFactor, atr, getLot(item.getItemID()));
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

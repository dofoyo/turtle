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
 * 调试时，通过构造器修改参数，确定参数后，将参数设为默认值。
 * 
 */
public class Turtle {
	/*
	 * 亏损因子，即买了一个品种后 ，该品种价格下跌一个atr，总资金将下跌百分之几
	 */
	private BigDecimal deficitFactor; 
	
	/*
	 * 开仓判定通道区间
	 */
	private Integer openDuration; 

	/*
	 * 平仓判定通道区间
	 */
	private Integer dropDuration; 
	
	/*
	 * 一只股票最大持仓单位
	 */
	private Integer maxOfLot; 

	/*
	 * 是否止损
	 */
	private boolean isStop;  
	
	private Integer gap;  //30在2013-2015年化为35%
	
	/*
	 * 初始值现金。无所谓，一般不低于10万
	 */
	private BigDecimal initCash;
	
	private Map<String, Item> items;
	private Account account;

	public Turtle() {
		deficitFactor  = new BigDecimal(0.01); 
		openDuration = 89; 
		dropDuration = 34; 
		maxOfLot = 3; 
		initCash = new BigDecimal(100000);
		isStop  = false;
		gap = 30;
		account = new Account(initCash);
		items = new HashMap<String,Item>();		
	}
	
	public Turtle(BigDecimal deficitFactor, 
				Integer openDuration, 
				Integer dropDuration, 
				Integer maxOfLot, 
				BigDecimal initCash, 
				boolean isStop,
				Integer gap
				) {
		
		this.deficitFactor = deficitFactor;
		this.openDuration = openDuration;
		this.dropDuration = dropDuration;
		this.maxOfLot = maxOfLot;
		this.initCash = initCash;
		this.isStop = isStop;
		this.gap = gap;
		account = new Account(initCash);
		items = new HashMap<String,Item>();
	}
	
	public void putOrder(Order order) {
		account.putOrder(order);
	}
	
	public List<Bar> getKbars(String itemID){
		Item item = items.get(itemID);
		return item.getBars();
	}
	
	public Map<String,BigDecimal> getItemFeatures(String itemID){
		Item item = items.get(itemID);
		//System.out.println(item);
		BigDecimal[] highAndLow = item.getFeatures(openDuration);
		Map<String,BigDecimal> prices = new HashMap<String,BigDecimal>();
		prices.put("high", highAndLow[0]);
		prices.put("low", highAndLow[1]);
		
		List<Order> orders = account.getOrders(itemID);
		for(Order order : orders) {
			prices.put("buy", order.getPrice());
			prices.put("stop", order.getStopPrice());
		}
		if(orders.size()>0) {
			highAndLow = item.getFeatures(dropDuration);
			prices.put("drop", highAndLow[1]);
		}
		
		return prices;
	}
	
	public List<String> getArticleIDsOfOnHand() {
		return account.getItemIDsOfOnHand();
	}
	
	/*
	 * 返回的信息：
	 * itemID
	 * code
	 * name
	 * now 当前价格
	 * high 在openDuration期间内的最高点
	 * low  在openDuration期间内的最低点
	 * hlgap  在openDuration期间内的最高点和最低点的距离
	 * nhgap 当前价格与最高点的gap，为正表示已突破，为负表示还未突破
	 * 
	 */
	public Map<String,String> hunt(Map<String,String> kData) {
		if(kData == null) {
			System.out.format("ERROR: kdata can NOT be NULL!");
			return null;
		}		
		
		Item item = items.get(kData.get("itemID"));

		if(item == null) {
			System.out.format("ERROR: can NOT get item of %s", kData.get("itemID"));
			return null;
		}
		
		BigDecimal[] features = item.getFeatures(openDuration);
		BigDecimal high = features[0];
		BigDecimal low = features[1];
		Integer hlgap = features[2].multiply(new BigDecimal(100)).intValue();
		BigDecimal now = new BigDecimal(kData.get("close"));
		Integer nhgap = now.subtract(high).divide(high,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
		
		features = item.getFeatures(dropDuration);
		BigDecimal drop = features[1];

		Map<String,String> result = new HashMap<String,String>();
		result.put("high", high.toString());
		result.put("low", low.toString());
		result.put("drop", drop.toString());
		result.put("hlgap", hlgap.toString());
		result.put("nhgap", nhgap.toString());
		result.put("now", kData.get("close"));
		result.put("itemID", kData.get("itemID"));
		result.put("code", kData.get("code"));
		result.put("name", kData.get("name"));			
		result.put("operation", "");
		
		if(hlgap <= gap && now.compareTo(high)==1) {
			result.put("operation", "open");
		}else if(now.compareTo(drop)==-1) {
			result.put("operation", "drop");
		}
		return result;
	}
	
	public void doit(Map<String,String> kData) {
		Bar bar = getBar(kData);
		
		Item item = items.get(bar.getItemID());
		if(item == null) {
			item = new Item(bar.getItemID());
			items.put(bar.getItemID(), item);
		}
		
		account.updatePrice(item.getItemID(), bar.getDate(), bar.getClose());
		
		//止损
		if(isStop) {
			doStop(bar);
		}
		
		//平仓
		doDrop(bar);
		
		//开新仓、加仓
		doOpen(bar);
	}
	
	public void addBar(Map<String,String> kData) {
		Bar bar = getBar(kData);
		Item item = items.get(bar.getItemID());
		if(item == null) {
			item = new Item(bar.getItemID());
			items.put(bar.getItemID(), item);
			//System.out.println("new Item, id is " + bar.getItemID());
		}	
		item.addBar(item.getItemID(),bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), openDuration);
	}
	
	public void addBar(List<Map<String,String>> kDatas) {
		for(Map<String,String> kData : kDatas) {
			this.addBar(kData);
		}
	}
	
	/*
	 * kData: dateTime, itemID, high, amount, low, close, open
	 */
	private Bar getBar(Map<String,String> kData) {
		//System.out.println(kData);
		//System.out.println(kData.get("dateTime"));
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
		account.stop(bar);
	}
	
	//平仓
	private void doDrop(Bar bar) {
		Item item = items.get(bar.getItemID());
		Integer lots = account.getLots(item.getItemID());
		if(lots != 0) { //有持仓
			boolean flag = item.closePing(bar, lots, dropDuration);
			if(flag) {  //触发平仓
				LocalDate[] dates = item.getBeginAndEndDateOfOpenDuration();
				Integer rate = item.getRateOfHighestAndLowest(dropDuration);
				account.close(item.getItemID(), bar.getDate(), bar.getClose(), dates,rate);
			}
		}
	}
	
	//开仓
	private void doOpen(Bar bar) {
		Item item = items.get(bar.getItemID());
		Order openOrder = null;
		Integer flag = item.openPing(bar, openDuration, gap);
		if(flag != null) {
			Integer lots = account.getLots(item.getItemID());
			//System.out.println("lots = " + lots);
			BigDecimal atr = item.getATR(openDuration);
			LocalDate[] dates = item.getBeginAndEndDateOfOpenDuration();
			Integer rate = item.getRateOfHighestAndLowest(openDuration);
			if(flag==1 && lots==0) { //初次开多仓
				BigDecimal stopPrice = bar.getClose().subtract(atr);
				BigDecimal reopenPrice = bar.getClose().add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
				openOrder = new Order(UUID.randomUUID().toString(),	item.getItemID(),	bar.getDate(), 1, bar.getClose(),	stopPrice,	reopenPrice	);
				openOrder.setNote("open，stop=" + stopPrice + "，reOpen="+reopenPrice + "，bDate" + dates[0] + "，eDate=" + dates[1]);
				openOrder.setOpenRateOfHL(rate);
				openOrder.setAtr(atr);
			}
			
			if(flag==1 && lots>0 && lots<maxOfLot) {  //加开多仓
				BigDecimal reopenPrice = account.getReopenPrice(item.getItemID());
/*				if(bar.getItemID().equals("sh600570")) {
					System.out.print(bar.getDate() + ",high=" + bar.getHigh() + ",reopenPrice=" + reopenPrice);
				}
*/				if(bar.getHigh().compareTo(reopenPrice)==1) {
/*					if(bar.getItemID().equals("sh600570")) {
						System.out.println(",reOpen");
					}
*/					BigDecimal stopPrice = bar.getClose().subtract(atr);
					reopenPrice = bar.getClose().add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
					openOrder = new Order(UUID.randomUUID().toString(),	item.getItemID(),	bar.getDate(), 1, bar.getClose(),	stopPrice,	reopenPrice	);
					openOrder.setNote("reOpen，stop=" + stopPrice + "，reOpen="+reopenPrice + "，bDate" + dates[0] + "，eDate=" + dates[1]);
					openOrder.setOpenRateOfHL(rate);
					openOrder.setAtr(atr);
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
				account.open(openOrder, deficitFactor, atr, getLot(item.getItemID()));
			}
		}
	}
	
	public Map<String,String> result() {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", account.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
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

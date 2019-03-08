package com.rhb.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
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
		gap = 60;
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
	
	public List<Bar> getBars(String itemID){
		Item item = items.get(itemID);
		return item.getBars();
	}
	
	public Map<String,String> getFeatures(String itemID){
		Item item = items.get(itemID);
		if(item==null) {
			System.out.println(itemID);
			System.out.println(items);
			return null;
		}
		return items.get(itemID).getFeatures();
	}
	
	public List<String> getArticleIDsOfOnHand() {
		return account.getItemIDsOfOnHand();
	}
	
	/*
	 * 返回的信息：
	 * itemID
	 * code
	 * name

	 * 	features.put("openHigh", openHigh.toString());
		features.put("openLow", openLow.toString());
		features.put("hlgap", hlgap.toString()); 
		features.put("nhgap", nhgap.toString()); 
		features.put("nlgap", nlgap.toString()); 
		features.put("dropHigh", dropHigh.toString());
		features.put("dropLow", dropLow.toString());
		features.put("now", now.toString());
		features.put("status", status.toString());
		features.put("atr", getATR().toString());
	 * 
	 */
	public Map<String,String> hunt(Map<String,String> kData) {
		if(kData == null) {
			System.out.format("ERROR: kdata can NOT be NULL!\n");
			return null;
		}		
		
		Item item = items.get(kData.get("itemID"));

		if(item == null) {
			System.out.format("ERROR: can NOT get item of %s\n", kData.get("itemID"));
			return null;
		}
		
		item.setLatestBar(kData);
		Map<String,String> features = item.getFeatures();
		if(features!=null) {
			features.put("itemID", kData.get("itemID"));
			features.put("code", kData.get("code"));
			features.put("name", kData.get("name"));			
		}
		
		return features;
	}
	
	public void doit(Map<String,String> kData) {
		Item item = items.get(kData.get("itemID"));
		if(item == null) {
			System.out.format("ERROR: item is null of %s.\n", kData.get("itemID") );
			return;
		}		
		if(item.getBars().size()<openDuration) {
			//System.out.format("INF: %s history bars is %d, below open duration %d, skip.\n", kData.get("itemID"),item.getBars().size(),openDuration );
			return;
		}
		
		//item.setLatestBar(kData);
		
		Map<String,String> features = item.getFeatures();
		Integer hlgap = new BigDecimal(features.get("hlgap")).multiply(new BigDecimal(100)).intValue();

		account.updatePrice(item.getItemID(),kData.get("dateTime"), kData.get("close"));
		Integer lots = account.getLots(item.getItemID());
		
		//止损
		if(lots>0 && isStop) {
			account.stop(kData);
		}
		
		//平仓
		if(lots>0 && (features.get("status").equals("-1") || features.get("status").equals("-2"))) {
			account.drop(kData);
		}
		
		//开新仓、加仓
		if(features.get("status").equals("2")) {
			if(lots>0 && lots<maxOfLot) {
				doReopen(kData);
			}else if(lots==0 && hlgap<=gap){
				//System.out.println("hlgap: " + hlgap);
				doOpen(kData);
			}
		}
	}
	
	public void addBar(Map<String,String> kData) {
		Item item = items.get(kData.get("itemID"));
		if(item == null) {
			item = new Item(kData.get("itemID"),openDuration,dropDuration);
			items.put(kData.get("itemID"), item);
			//System.out.println("new Item, id is " + item.getItemID());
			//System.out.println("items.size=" + items.size());
		}	
		item.addBar(kData);
	}
	
	public boolean isExist(String itemID) {
		return items.containsKey(itemID);
	}
	
	public void clearBars(String itemID) {
		Item item = items.get(itemID);
		if(item!=null) {
			item.clearBars();
		}
	}
	
	public void addBars(List<Map<String,String>> kDatas) {
		for(Map<String,String> kData : kDatas) {
			this.addBar(kData);
		}
	}
	
	public boolean setLatestBar(Map<String,String> kData) {
		Item item = items.get(kData.get("itemID"));
		if(item == null) {
			System.out.println("ERROR: item is null!");
			return false;
		}
		item.setLatestBar(kData);
		return true;
	}
	
	//开新仓
	private void doOpen(Map<String,String> kData) {
		Item item = items.get(kData.get("itemID"));
		BigDecimal atr = item.getATR();
		BigDecimal price = new BigDecimal(kData.get("close"));
		LocalDate date = LocalDate.parse(kData.get("dateTime"));
		
		BigDecimal stopPrice = price.subtract(atr);
		BigDecimal reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
		
		Order openOrder = new Order(UUID.randomUUID().toString(),item.getItemID(),date, 1, price,stopPrice,reopenPrice	);
		openOrder.setNote("open，stop=" + stopPrice + "，reOpen="+reopenPrice);
		
		account.open(openOrder, deficitFactor, atr, getLot(item.getItemID()));
	
	}
	
	//加仓
	private void doReopen(Map<String,String> kData) {
		Item item = items.get(kData.get("itemID"));
		BigDecimal atr = item.getATR();
		BigDecimal reopenPrice = account.getReopenPrice(item.getItemID());
		BigDecimal price = new BigDecimal(kData.get("close"));
		LocalDate date = LocalDate.parse(kData.get("dateTime"));
		
		BigDecimal high = new BigDecimal(kData.get("high"));
		if(high.compareTo(reopenPrice)==1) {
			BigDecimal stopPrice = price.subtract(atr);
			reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
			
			Order openOrder = new Order(UUID.randomUUID().toString(),item.getItemID(),date, 1, price,stopPrice,	reopenPrice	);
			openOrder.setNote("reOpen，stop=" + stopPrice + "，reOpen="+reopenPrice);
			account.open(openOrder, deficitFactor, atr, getLot(item.getItemID()));
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

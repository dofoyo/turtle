package com.rhb.turtle.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.rhb.turtle.util.Line;

public class OnhandView {
	private String itemID;
	private String code;
	private String name;
	private String atr;
	private Map<String, BigDecimal> prices;

	public OnhandView(Map<String, String> map) {
		this.itemID = map.get("itemID");
		this.code = map.get("code");
		this.name = map.get("name");
		this.atr = map.get("atr");

		prices = new HashMap<String, BigDecimal>();

		prices.put("now", new BigDecimal(map.get("now")));
		prices.put("high", new BigDecimal(map.get("high")));
		prices.put("low", new BigDecimal(map.get("low")));

		prices.put("buy", new BigDecimal(map.get("buy")));
		prices.put("stop", new BigDecimal(map.get("stop")));
		prices.put("drop", new BigDecimal(map.get("drop")));
		prices.put("reopen", new BigDecimal(map.get("reopen")));

	}

	public String getAtr() {
		return atr;
	}

	public void setAtr(String atr) {
		this.atr = atr;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
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

	public String getLine() {
		return Line.draw(prices);
	}

}

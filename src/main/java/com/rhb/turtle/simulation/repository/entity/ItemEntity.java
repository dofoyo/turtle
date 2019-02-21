package com.rhb.turtle.simulation.repository.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemEntity<T> {
	private String itemID;
	private String code;
	private String name;
	Map<T,BarEntity<T>> bars;
	
	public ItemEntity(String itemID, String code, String name) {
		this.itemID = itemID;
		this.code = code;
		this.name = name;
		bars = new HashMap<T,BarEntity<T>>();
	}
	
	public Set<T> getDateTimes(){
		return bars.keySet();
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
	
	public void setBar(T dateTime, BarEntity<T> bar) {
		this.bars.put(dateTime, bar);
	}

	public BarEntity<T> getBar(T dateTime) {
		return this.bars.get(dateTime);
	}
	
	@Override
	public String toString() {
		return "ItemEntity [itemID=" + itemID + ", code=" + code + ", name=" + name + "bars" + bars.size() + "]";
	}
	
	
}

package com.rhb.turtle.operation;

public class OnhandEntity {
	private String itemID;
	private String code;
	private String name;
	private String line;
	
	public OnhandEntity(String itemID, String code, String name, String line) {
		this.itemID = itemID;
		this.code = code;
		this.name = name;
		this.line = line;
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
		return line;
	}
	public void setLine(String line) {
		this.line = line;
	}
}

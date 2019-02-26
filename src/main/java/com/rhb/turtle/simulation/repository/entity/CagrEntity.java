package com.rhb.turtle.simulation.repository.entity;

import java.time.LocalDate;

public class CagrEntity   implements Comparable<CagrEntity>{
	private String itemID;
	private LocalDate date;
	private Integer openDuration;
	private Integer closeDuration;
	private Integer winRatio;
	private boolean isStop;
	private Integer cagr;
	
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public void setDate(String date) {
		this.date = LocalDate.parse(date);
	}
	
	public Integer getOpenDuration() {
		return openDuration;
	}
	public void setOpenDuration(Integer openDuration) {
		this.openDuration = openDuration;
	}
	public Integer getCloseDuration() {
		return closeDuration;
	}
	public void setCloseDuration(Integer closeDuration) {
		this.closeDuration = closeDuration;
	}
	public Integer getWinRatio() {
		return winRatio;
	}
	public void setWinRatio(Integer winRatio) {
		this.winRatio = winRatio;
	}
	public boolean isStop() {
		return isStop;
	}
	public void setStop(boolean isStop) {
		this.isStop = isStop;
	}
	public Integer getCagr() {
		return cagr;
	}
	public void setCagr(Integer cagr) {
		this.cagr = cagr;
	}
	@Override
	public String toString() {
		return "CagrEntity [itemID=" + itemID + ",date=" + date + ", openDuration=" + openDuration + ", closeDuration=" + closeDuration
				+ ", winRatio=" + winRatio + ", isStop=" + isStop + ", cagr=" + cagr + "]";
	}
	
	@Override
	public int compareTo(CagrEntity o) {
		if(o.getCagr().equals(this.getCagr())){
			return o.getWinRatio().compareTo(this.winRatio);
		}else {
			return o.getCagr().compareTo(this.getCagr());
		}
	}
	
	
}

package com.rhb.turtle.simulation.repository.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EntityRepository {
	public ItemEntity<LocalDate> getDailyKData(String itemID);
	public ItemEntity<LocalDateTime> get5MinKData(String itemID);
	public Map<LocalDate,List<String>> getDailyTopIds();
	public Map<LocalDate,List<String>> getAvaTopIds();
	public Map<LocalDate,List<String>> getBluechipIds();
}

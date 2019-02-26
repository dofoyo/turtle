package com.rhb.turtle.simulation.repository;

import java.time.LocalDate;
import java.util.List;

public interface TurtleSimulationCagrRepository {
	public void generateDailyCAGR();
	public void generateCAGRTop50();
	public List<String> getCAGRTops(Integer top,LocalDate date);
	public String[] getCAGRTopIDs();
}

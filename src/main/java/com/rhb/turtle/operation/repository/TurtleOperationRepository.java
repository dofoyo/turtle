package com.rhb.turtle.operation.repository;

import java.util.List;
import java.util.Map;

public interface TurtleOperationRepository {
	public List<Map<String,String>> getKDatas(String id);
	public List<String> getArticleIDs();
}

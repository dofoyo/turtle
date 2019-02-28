package com.rhb.turtle.simulation.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.domain.Bar;
import com.rhb.turtle.domain.Turtle;
import com.rhb.turtle.simulation.repository.TurtleSimulationRepository;
import com.rhb.turtle.simulation.repository.entity.BarEntity;
import com.rhb.turtle.simulation.repository.entity.EntityRepository;
import com.rhb.turtle.simulation.spider.TurtleSimulationSpider;
import com.rhb.turtle.util.FileUtil;
import com.rhb.turtle.util.JsonUtil;

@Service("turtleSimulationServiceImpDynamic")
public class TurtleSimulationServiceImpDynamic implements TurtleSimulationService {
	@Value("${reportPath}")
	private String reportPath;	
	
	@Value("${cagrsPath}")
	private String cagrsPath;
	
	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository ;
	
	@Autowired
	@Qualifier("entityRepositoryImp")
	EntityRepository itemEntityRepository ;
	
	private LocalDate beginDate = LocalDate.parse("2010-01-01");
	private LocalDate endDate = LocalDate.parse("2018-09-13");
	
	/*
	 * 亏损因子，默认值为1%，即买了一个品种后 ，该品种价格下跌一个atr，总资金下跌1%
	 */
	private BigDecimal deficitFactor = new BigDecimal(0.005); 
	
	/*
	 * 一手的数量，默认值为股票是100股，螺纹钢是10吨，...
	 */
	private BigDecimal lot = new BigDecimal(100); 
	
	private Integer maxOfLot = 5;  
	
	private Integer top = 13;  //成交量排名前13名，不能低于5
	private BigDecimal initCash = new BigDecimal(100000);
	
	private Integer total = 17808;
	private Integer i=1;
	
	@Override
	public Map<String, String> simulate() {
		return null;
	}
	

}

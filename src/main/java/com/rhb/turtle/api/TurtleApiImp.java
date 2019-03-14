package com.rhb.turtle.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.turtle.operation.TurtleOperationService;

@RestController
public class TurtleApiImp implements TurtleApi {
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;
	
	@Override
	@GetMapping("/onhands")
	public ResponseContent<List<OnhandView>> getHolds() {
		List<OnhandView> onhands = new ArrayList<OnhandView>();
		List<Map<String,String>> maps = ts.getHolds();
		for(Map<String,String> map : maps) {
			onhands.add(new OnhandView(map));
		}		
		return new ResponseContent<List<OnhandView>>(ResponseEnum.SUCCESS, onhands);
	}

	@Override
	@GetMapping("/preys")
	public ResponseContent<List<PreyView>> getPreys(@RequestParam(value="status", defaultValue="2") String status) {
		System.out.println("status=" + status);
		
		List<PreyView> preys = new ArrayList<PreyView>();
		List<Map<String,String>> maps = ts.getPreys(status);
		
		for(Map<String,String> map : maps) {
			preys.add(new PreyView(map));
		}
		
		Collections.sort(preys, new Comparator<PreyView>() {
			@Override
			public int compare(PreyView o1, PreyView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				BigDecimal nh1 = new BigDecimal(o1.getNhgap());
				BigDecimal nh2 = new BigDecimal(o2.getNhgap());
				
				if(hl1.equals(hl2)) {
					return (nh2).compareTo(nh1);
				}else {
					return (hl1).compareTo(hl2);
				}
			}
		});		
		
		return new ResponseContent<List<PreyView>>(ResponseEnum.SUCCESS, preys);
	}

	@Override
	@GetMapping("/kdatas/{itemID}")
	public ResponseContent<KdatasView> getKdatas(@PathVariable(value="itemID") String itemID) {
		//System.out.println("itemID:" + itemID);
		
		KdatasView kdatas = ts.getKdatas(itemID);
		
		/*			
		KdatasView kdatas = new KdatasView();
		kdatas.setCode("sh000001");
		kdatas.setCode("000001");
		kdatas.setName("上证指数");
		kdatas.addKdata("2013/6/3","2300.21","2299.25","2294.11","2313.43");
		kdatas.addKdata("2013/6/4","2297.01","2272.42","2264.76","2297.01");
		kdatas.addKdata("2013/6/5","2270.71","2270.93","2260.87","2276.86");
		 */		
		
		//String kData = "['sangzddd'],['2013/6/3', 2300.21, 2299.25, 2294.11, 2313.43],
		//['2013/6/4', 2297.1, 2272.42, 2264.76, 2297.1],
		//['2013/6/5', 2270.71, 2270.93, 2260.87, 2276.86],['2013/6/6', 2264.43, 2242.11, 2240.07, 2266.69],['2013/6/7', 2242.26, 2210.9, 2205.07, 2250.63],['2013/6/13', 2190.1, 2148.35, 2126.22, 2190.1]";
		return new ResponseContent<KdatasView>(ResponseEnum.SUCCESS, kdatas);
	}

}

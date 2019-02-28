package com.rhb.turtle.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.turtle.operation.TurtleOperationService;

@RestController
public class TurtleApiImp implements TurtleApi {
	@Autowired
	@Qualifier("turtleOperationServiceImp")
	TurtleOperationService ts;
	
	
	@Override
	@GetMapping("/onhands")
	public ResponseContent<List<OnhandView>> getOnhands() {
		List<OnhandView> onhands = new ArrayList<OnhandView>();
		List<Map<String,String>> maps = ts.getOnhands();
		for(Map<String,String> map : maps) {
			onhands.add(new OnhandView(map));
		}		
		return new ResponseContent<List<OnhandView>>(ResponseEnum.SUCCESS, onhands);
	}

	@Override
	public void buy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sell() {
		// TODO Auto-generated method stub

	}

	@Override
	@GetMapping("/preys")
	public ResponseContent<List<PreyView>> getPreys() {
		List<PreyView> preys = new ArrayList<PreyView>();
		List<Map<String,String>> maps = ts.getPreys();
		
		for(Map<String,String> map : maps) {
			preys.add(new PreyView(map));
		}
		
		Collections.sort(preys, new Comparator<PreyView>() {
			@Override
			public int compare(PreyView o1, PreyView o2) {
				if(o1.getHL().equals(o2.getHL())) {
					return (o2.getNH()).compareTo(o1.getNH());
				}else {
					return (o1.getHL().compareTo(o2.getHL()));
				}
			}
		});		
		
		return new ResponseContent<List<PreyView>>(ResponseEnum.SUCCESS, preys);
	}

	@Override
	public void addPrey() {
		// TODO Auto-generated method stub

	}

	@Override
	public void deletePrey() {
		// TODO Auto-generated method stub

	}

}

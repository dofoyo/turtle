package com.rhb.turtle.util;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class HttpClientTest {
	//@Test
	public void TestDoPost() {
		String url = "http://api.tushare.pro";
		//Map<String,String> param = new HashMap<String,String>();
		//param.put("api_name", "stock_basic");
		//param.put("token ", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		String params = "{\"api_name\":\"stock_basic\",\"token\":\"175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278\"}";
		String str = HttpClient.doPostJson(url, params);
		System.out.println(str);
		
		//curl -X POST -d '{"api_name": "stock_basic", "token": "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278", "params": {"list_stauts":"L"}, "fields": "ts_code,name,area,industry,list_date"}' http://api.tushare.pro
	}
	
	//@Test
	public void test() {
		String strUrl = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/300384.phtml?year=2019&jidu=1";
		String result = HttpClient.doGet(strUrl);
		System.out.println(result);
	}
	
	@Test
	public void test1() {
		String strUrl = "http://irm.cninfo.com.cn/ircs/sse/sseSubIndex.do?condition.type=7";
		String result = HttpClient.doGet(strUrl);
		System.out.println(result);
	}
}

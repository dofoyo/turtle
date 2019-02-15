package com.rhb.turtle.operation.spider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.util.FileUtil;
import com.rhb.turtle.util.HttpClient;

@Service("marketInfoOperationSpiderImp")
public class MarketInfoOperationSpiderImp implements MarketInfoOperationSpider {
	@Value("${dailyTop100File}")
	private String dailyTop100File;
	
	@Value("${kDataPath}")
	private String kDataPath;
	
	
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public LocalDate getLatestMarketDate() {
		String url = "http://qt.gtimg.cn/q=sh000001";
		String result = HttpClient.doGet(url);
		
		//System.out.println(result);
		
		String[] ss = result.split("~");
		
		return LocalDate.parse(ss[30].substring(0, 8),formatter);
	}

	
	@Override
	public Map<String, String> getLatestMarketData(String id) {
		Map<String,String> map = new HashMap<String,String>();
		
		String url = "http://qt.gtimg.cn/q=" + id;
		String result = HttpClient.doGet(url);
		
		//System.out.println(result);
		
		String[] ss = result.split("~");
		//System.out.println(ss[2] + "," + ss[3] + "," + ss[30]);
		
		map.put("date", LocalDate.parse(ss[30].substring(0, 8),DateTimeFormatter.ofPattern("yyyyMMdd")).toString());
		map.put("code", ss[2]);
		map.put("name", ss[1]);
		map.put("preClose", ss[4]);
		map.put("open", ss[5]);
		map.put("high", ss[33]);
		map.put("low", ss[34]);
		map.put("close", ss[3]);
		map.put("quantity", ss[6]);
		map.put("amount", ss[37]);
		
		return map;
	}


	@Override
	public void downLatestDailyTop100() {
		String strUrl = "http://q.jrjimg.cn/?q=cn|s|sa&c=s,ta,tm,sl,cot,cat,ape&n=hqa&o=tm,d&p=1100&_dc=1549936839524";
		System.out.println(strUrl);
		String result = HttpClient.doGet(strUrl);
		//System.out.println(result);
		StringBuffer sb = new StringBuffer();
		if(result != null) {
			String[] lines = result.split("\n");
			for(int i=4; i<54; i++) {
				//System.out.println(lines[i]);
				//System.out.println(lines[i].substring(2, 10));
				sb.append(lines[i].substring(2, 10));
				if(i<54) {
					sb.append(",");
				}
			}
		}
		FileUtil.writeTextFile(dailyTop100File, sb.toString(), true);

	}


	@Override
	public void downKdata(String id) {
		String strUrl = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/CODE.phtml?year=YEAR&jidu=JIDU";
		String code = id.substring(2);
		String year = "2019";
		String jidu = "1";
		strUrl = strUrl.replace("CODE", code);
		strUrl = strUrl.replace("YEAR", year);
		strUrl = strUrl.replace("JIDU", jidu);
		String file = kDataPath + "/" + code + ".txt";
		System.out.println(file);
		
		try {
			//String str = HttpClient.doGet(strUrl);
			//System.out.println(str);
			Document doc = Jsoup.connect(strUrl).get();
			Element table = doc.getElementById("FundHoldSharesTable");
			Elements trs = table.select("tr");
			Elements tds;
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < trs.size(); i++) {
				tds = trs.get(i).select("td");
				for (int j = 0; j < tds.size(); j++) {
					String text = tds.get(j).text();
					sb.append(text);
					if(j<(tds.size()-1)) sb.append(",");
				}
				if(sb.length()>0)	sb.append("\n");
			}
			System.out.println(sb.toString());
			
			FileUtil.writeTextFile(file, sb.toString(), false);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}
	
	
	
	
	/*
	 * 
接口： 
http://qt.gtimg.cn/q=sh600519

返回： 
v_sh600519="1~贵州茅台~600519~358.74~361.29~361.88~27705~12252~15453~358.75~8~358.74~4~358.72~7~358.71~6~358.70~5~358.77~3~358.78~2~358.79~16~358.80~4~358.86~1~14:59:59/358.75/5/S/179381/28600|14:59:56/358.75/1/S/35875/28594|14:59:53/358.75/1/S/35875/28588|14:59:50/358.75/1/S/35875/28579|14:59:47/358.75/4/B/143499/28574|14:59:41/358.72/4/S/143501/28562~20170221150553~-2.55~-0.71~362.43~357.18~358.75/27705/994112865~27705~99411~0.22~27.24~~362.43~357.18~1.45~4506.49~4506.49~6.57~397.42~325.16~0.86";

解释： 
 0: 未知
 1: 股票名字
 2: 股票代码
 3: 当前价格
 4: 昨收
 5: 今开
 6: 成交量（手）
 7: 外盘
 8: 内盘
 9: 买一
10: 买一量（手）
11-18: 买二 买五
19: 卖一
20: 卖一量
21-28: 卖二 卖五
29: 最近逐笔成交
30: 时间
31: 涨跌
32: 涨跌%
33: 最高
34: 最低
35: 价格/成交量（手）/成交额
36: 成交量（手）
37: 成交额（万）
38: 换手率
39: 市盈率
40: 
41: 最高
42: 最低
43: 振幅
44: 流通市值
45: 总市值
46: 市净率
47: 涨停价
48: 跌停价
	 */

}

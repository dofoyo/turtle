package com.rhb.turtle.operation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.turtle.util.FileUtil;
import com.rhb.turtle.util.HttpClient;
import com.rhb.turtle.util.ParseString;

@Service("turtleOperationSpiderImp")
public class TurtleOperationSpiderImp implements TurtleOperationSpider {
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
		
		map.put("dateTime", LocalDate.parse(ss[30].substring(0, 8),DateTimeFormatter.ofPattern("yyyyMMdd")).toString());
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
	public List<String> downLatestDailyTop100() {
		List<String> ids = new ArrayList<String>();
		String strUrl = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?cb=jQuery112403517194352564321_1550228468554&type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&js=(%7Bdata%3A%5B(x)%5D%2CrecordsFiltered%3A(tot)%7D)&cmd=C._A&st=(Amount)&sr=-1&p=1&ps=100&_=1550228468980";
		//System.out.println(strUrl);
		String result = HttpClient.doGet(strUrl);
		//System.out.println(result);
		List<String> strs = ParseString.subStrings(result, "\"|\"");
		String ss;
		String code;
		StringBuffer sb = new StringBuffer();
		for(String str : strs) {
			ss = str.substring(0,1).equals("1") ? "sh" : "sz";
			code = str.substring(2,8);
			//System.out.println(ss + code);
			sb.append(ss + code);
			sb.append(",");
			ids.add(ss + code);
		}
		sb.deleteCharAt(sb.length()-1);
		FileUtil.writeTextFile(dailyTop100File, sb.toString(), false);
		
		return ids;
	}
	
	private String[] getYearAndJidu(Integer num) {
		LocalDate now = LocalDate.now();
		int year = now.getYear();
		int jidu = now.getMonthValue()/3 + 1;
		String[] ss = new String[num];
		ss[0] = String.valueOf(year) + "." + String.valueOf(jidu);
		for(int i=1; i<num; i++) {
			if(jidu-1<=0) {
				year--;
				jidu=4;
			}else {
				jidu--;
			}
			ss[i]=String.valueOf(year) + "." + String.valueOf(jidu);
		}
		return ss;
	}
	

	@Override
	public Integer downKdatas(String id) {
		Integer down=0;
		String[] yjs = getYearAndJidu(5);
		String year;
		String jidu;
		String file;
		for(int i=0; i<yjs.length; i++) {
			year = yjs[i].substring(0,4);
			jidu = yjs[i].substring(5,6);
			file = kDataPath + "/" + id + "_" + year + "_" + jidu + ".txt";
			if(i==0 || !FileUtil.isExists(file)) {
				downKdatas(id,year,jidu);
				try {Thread.sleep(5000);} catch (Exception e) {e.printStackTrace();}
			}else {
				System.out.println(yjs[i] + " have downloaded!");
			}
		}
		return down;
	}

	@Override
	public void downKdatas(String id, String year, String jidu) {
		String code = id.substring(2);
		
		String strUrl = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/CODE.phtml?year=YEAR&jidu=JIDU";
		strUrl = strUrl.replace("CODE", code);
		strUrl = strUrl.replace("YEAR", year);
		strUrl = strUrl.replace("JIDU", jidu);
		String file = kDataPath + "/" + id + "_" + year + "_" + jidu + ".txt";
		System.out.println("download " + file);
		
		try {
			//String str = HttpClient.doGet(strUrl);
			//System.out.println(str);
			
	        Connection connect = Jsoup.connect(strUrl);
	        Map<String, String> header = new HashMap<String, String>();
	        header.put("Host", "http://www.sina.com.cn");
	        header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:5.0) Gecko/20100101 Firefox/5.0");
	        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	        header.put("Accept-Language", "zh-cn,zh;q=0.5");
	        header.put("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
	        header.put("Connection", "keep-alive");
	        Connection data = connect.headers(header);
			
			Document doc = data.get();
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
			//System.out.println(sb.toString());
			
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

package com.rhb.turtle.util;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class OtherTest {
	
	//@Test
	public void testLinkedList() {
		LinkedList<Integer> l = new LinkedList<Integer>();
		l.add(1);
		l.add(3);
		l.add(2);
		
		for(Integer i : l) {
			System.out.println(i);
		}
	}
	
	//@Test
	public void testTreeSet() {
		TreeSet<Integer> t = new TreeSet<Integer>();
		t.add(9);
		t.add(1);
		t.add(5);
		
		for(Integer i : t) {
			System.out.println(i);
		}
	}
	
	//@Test
	public void testDate() {
		 DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss:SSS");

		Long begin = LocalDateTime.parse("2018-01-01 00:00:00:000",formatter).toInstant(ZoneOffset.of("+8")).toEpochMilli();
		Long end = LocalDateTime.now().toInstant(ZoneOffset.of("+8")).toEpochMilli();
		System.out.println(begin);
		System.out.println(end);
		
		
	}
	
	//@Test
	public void rename() {
		String path = "C:\\workspace\\turtle-data\\dzh_hsag";
		List<File> files = FileUtil.getFiles(path, null, true);
		String oldName;
		String newName;
		File newFile;
		for(File file : files) {
			//codes.add(file.getName().substring(0, 6));
			oldName = file.getName();
			if(oldName.length()==10 && oldName.indexOf("60")==0) {
				newName = "sh" + oldName;
			}else if(oldName.length()==10){
				newName = "sz" + oldName;
			}else {
				newName = null;
			}
			
			if(newName != null) {
				newFile = new File(file.getParent() + "/" + newName);
				System.out.println(file.getAbsolutePath() + " --> " + newFile.getAbsolutePath());
				file.renameTo(newFile);
			}
			
			
		}
	}
	
	
}

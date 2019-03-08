package com.rhb.turtle.util;

import java.io.File;
import java.io.FileFilter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;

public class OtherTest {

	//@Test
	public void rename() {
		String path = "D:\\prod\\turtle\\data\\kdatas";
		List<File> files = FileUtil.getFiles(path, null, true);
		String oldName;
		String newName;
		File newFile;
		for(File file : files) {
			//codes.add(file.getName().substring(0, 6));
			oldName = file.getName();
			newName = oldName.substring(0, 2).toLowerCase() + oldName.substring(2);
			
			if(newName != null) {
				newFile = new File(file.getParent() + "/" + newName);
				System.out.println(file.getAbsolutePath() + " --> " + newFile.getAbsolutePath());
				file.renameTo(newFile);
			}
		}
	}
	
	//@Test
	public void getFilesNumber() {
		String path = "C:\\workspace\\turtle-data\\simulation\\reports";
		File dir = new File(path);
		FileFilter fileFilter = new WildcardFileFilter("*.csv");
		File[] files = dir.listFiles(fileFilter);
		Set<String> ids = new HashSet<String>();
		for(File file : files) {
			ids.add(file.getName().substring(0, 8));
		}
		
		List<IDFile> idfs = new ArrayList<IDFile>();
		IDFile idf;
		for(String id: ids) {
			fileFilter = new WildcardFileFilter(id + "*.csv");
			files = dir.listFiles(fileFilter);
			idf = new IDFile(id, files.length);
			idfs.add(idf);
		}
		
		Collections.sort(idfs, new Comparator<IDFile>() {
			@Override
			public int compare(IDFile o1, IDFile o2) {
				return o2.getNum().compareTo(o1.getNum());
			}
		});
		
		StringBuffer sb = new StringBuffer();
		for(IDFile i : idfs) {
			sb.append(i.getId());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		
		
		String filepath = "C:\\workspace\\turtle-data\\simulation\\cagrTop.txt";
		FileUtil.writeTextFile(filepath, sb.toString(), false);
	}
	
	class IDFile{
		String id;
		Integer num;
		
		public IDFile(String id, Integer num) {
			this.id = id;
			this.num = num;
		}
		
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public Integer getNum() {
			return num;
		}
		public void setNum(Integer num) {
			this.num = num;
		}
	}
	


}

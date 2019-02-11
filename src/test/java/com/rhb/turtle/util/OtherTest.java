package com.rhb.turtle.util;

import java.util.LinkedList;
import java.util.TreeSet;

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
	
	@Test
	public void testTreeSet() {
		TreeSet<Integer> t = new TreeSet<Integer>();
		t.add(9);
		t.add(1);
		t.add(5);
		
		for(Integer i : t) {
			System.out.println(i);
		}
	}
}

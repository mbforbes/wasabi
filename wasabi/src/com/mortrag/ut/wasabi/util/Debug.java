package com.mortrag.ut.wasabi.util;

public class Debug {
	public static final boolean DEBUG = true;
	
	public static StringBuffer debugText = new StringBuffer();
	
	public static final void print(Object o) {
		System.out.println(o);
	}
}

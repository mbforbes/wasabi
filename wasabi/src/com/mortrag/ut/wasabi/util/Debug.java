package com.mortrag.ut.wasabi.util;

public class Debug {
	public static final boolean DEBUG = true;
	
	public static StringBuffer debugText = new StringBuffer();
	
	/**
	 * Appends a newline character (Constants.NL) to the end of the chunk of text you pass, and then
	 * adds the whole thing to the debug display.
	 * 
	 * @param line
	 */
	public static void debugLine(String line) {
		debugText.append(line + Constants.NL);
	}
	
	public static final void print(Object o) {
		System.out.println(o);
	}
	
}

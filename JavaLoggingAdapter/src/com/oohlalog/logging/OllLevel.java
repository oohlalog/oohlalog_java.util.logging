package com.oohlalog.logging;

import java.util.logging.Level;

public class OllLevel extends Level {
	public static Level ALL;
	public static Level COUNT;
	public static Level TRACE;
	public static Level DEBUG;
	public static Level INFO;
	public static Level WARN;
	public static Level ERROR;
	public static Level FATAL;
	public static Level OFF;
	
	static {
		ALL = new OllLevel("ALL", 0);
		TRACE = new OllLevel("TRACE", 1);
		DEBUG = new OllLevel("DEBUG", 2);
		INFO = new OllLevel("INFO", 3);
		WARN = new OllLevel("WARN", 4);
		ERROR = new OllLevel("ERROR", 5);
		FATAL = new OllLevel("FATAL", 6);
		OFF = new OllLevel("OFF", 7);
		COUNT = new OllLevel("COUNT", 8);
	}
	
	public OllLevel(String name, int value) {
		super(name, value);
	}

	public OllLevel(String name, int value, String resourceBundleName) {
		super(name, value, resourceBundleName);
	}

}

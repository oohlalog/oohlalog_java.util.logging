package com.oohlalog.logging;

import java.util.logging.Level;
import java.util.logging.Logger;


public class Test {

	public static void main(String[] args) throws InterruptedException {
		String apiKey = "1f111a85-62c7-4f42-8dd9-8a10bb80dc6e";
		Logger logger = Logger.getLogger(Test.class.getName());
		logger.setLevel(OllLevel.ALL);
		System.out.println(logger.getLevel());
		
		System.out.println("---Beginning---");
		OohLaLogHandler handler = new OohLaLogHandler(apiKey);
		logger.addHandler(handler);
		
		for (int i = 0; i < 50; i++) {
			logger.log(OllLevel.COUNT, "testing");
			logger.fine("TRACE:"+i); 
			logger.finer("DEBUG:"+i);
			Thread.sleep(20);
			logger.info("INFO:"+i);
			Thread.sleep(20);
			logger.warning("WARN:"+i);
			Thread.sleep(20);
			logger.severe("ERROR:"+i);
			Thread.sleep(20);			
		}
//		Level lvl = new CountLevel("test", 11);
//		for (int i = 50; i < 100; i++) {
//			logger.log(Level.FINER,  "TRACE:"+i, new Throwable("throwable")); // Won't get printed because default level is info
//			logger.log(Level.FINE, "DEBUG:"+i, new Throwable("throwable")); // Won't get printed because default level is info
//			logger.log(Level.INFO, "INFO:"+i, new Throwable("throwable"));
//			Thread.sleep(20);
//			logger.log(Level.WARNING, "WARN:"+i, new Throwable("throwable"));
//			Thread.sleep(20);
//			logger.log(Level.SEVERE, "ERROR:"+i, new Throwable("throwable"));
//			Thread.sleep(20);
//			logger.log(lvl, "ERROR:"+i, new Throwable("throwable"));
//			Thread.sleep(20);
//		}
		handler.close();
	}

}

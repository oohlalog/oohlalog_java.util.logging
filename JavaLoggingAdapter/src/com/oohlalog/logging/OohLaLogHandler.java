package com.oohlalog.logging;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;


public class OohLaLogHandler extends Handler {

    LevelConverter levelConverter = new LevelConverter();

    static class LevelConverter {
    	private static Map<Level, Level> map = new HashMap<Level, Level>();
    	public LevelConverter() {
    		map.put(Level.ALL,  OllLevel.ALL);
     		map.put(Level.FINEST, OllLevel.TRACE);
     		map.put(Level.FINER, OllLevel.TRACE);
     		map.put(Level.FINE, OllLevel.DEBUG);
     		map.put(Level.CONFIG, OllLevel.DEBUG);
     		map.put(Level.INFO, OllLevel.INFO);
     		map.put(Level.WARNING, OllLevel.WARN);
     		map.put(Level.SEVERE, OllLevel.ERROR);
     		map.put(Level.OFF,  OllLevel.OFF);
    	}
    		
    	public Level translate(Level old) {	
    		Level ollLevel;
    		ollLevel = map.get(old);
    		if (ollLevel != null) return ollLevel;
    		else if (map.containsValue(old)) return old;
    		// Default if user gives an invalid log
    		else return OllLevel.INFO;
    		
    	}

    }

 
    // ----------------------------------------------------
    
	// The time threshold controlling how often uploads of logs are made to the OLL server
    private long timeBuffer = 10000;
    
    // Logs are flushed once buffer reaches this size
    private int threshold = 100;
    
    // Maximum allowed size of the buffer
	private int maxBuffer = 1000;//5;
	
	// Holds all of the Logs until reaching a time threshold when they are then emptied out in batches
	private LogRecordBuffer logRecordBuffer;
	
	// Controls the actual flushing events
	private LogController logController;
	
    // The time threshold controlling how often uploads of statistics are made to the OLL server
	private long statsInterval = 5000; // 1 minute
	
	// For configuring the URL
	private String host = "localhost";//"api.oohlalog.com";
	private String path = "/api/logging/save.json";
	private String statsPath = "/api/timeSeries/save.json";
	private int port = 8196; //8196
	
	private String authToken = null;
	private String agent = "java.util.logging";
	private boolean secure = false;
	private boolean debug = true;
	private String hostName = null;
	
	private boolean showMemoryStats = true;
	private boolean showFileSystemStats = true;
	private boolean showCPUStats = true;
	private boolean showStats = true;
	
	Object previousCpuUsage;

	
	public OohLaLogHandler(String authToken) {
		this.authToken = authToken;
		logRecordBuffer = new LogRecordBuffer(maxBuffer);
		logController = new LogController(this, threshold, timeBuffer, statsInterval);
	}

	@Override
	public void publish(LogRecord record) {
		record.setLevel(levelConverter.translate(record.getLevel()));
		
		StringBuilder sb = new StringBuilder();
		Object[] params = record.getParameters();
		if (params != null) {
			sb.append(Arrays.toString(params)).append("\n");
		}
		String className = record.getSourceClassName();
		if (className != null) sb.append(className);
		String methodName = record.getSourceMethodName();
		if (methodName != null) sb.append(" ").append(methodName);
		params = new Object[1];
		params[0] = sb.toString();
		record.setParameters(params);

		logRecordBuffer.addLogToBuffer(record);
		// Don't need to have the flushTimer going when there are no log entries in the deque. 
        // Instead, we start the timer after adding an element which increasing deque size 
        // from 0 to 1
        if (getLogRecordBuffer().size() == 1)
        	this.logController.startFlushTimer();
	}

	@Override
	public void flush() {
		logController.flush(Integer.MAX_VALUE);
	}

	@Override
	public void close() throws SecurityException {
		logController.close();
	}
	
	
	
	/**
	 * Getter method for returning the max buffer size belonging to this OohLaLogLogger instance.
	 */
	public int getMaxBuffer() {
		return maxBuffer;
	}

	/**
	 * Getter method for returning the LogRecordBuffer belonging to this OohLaLogLogger instance.
	 */
	protected synchronized LogRecordBuffer getLogRecordBuffer() {
		return logRecordBuffer;
	}


	/**
	 * Setter method for setting the max buffer size belonging to this OohLaLogLogger instance.
	 */
	public void setMaxBuffer(int maxBuffer) {
		this.maxBuffer = maxBuffer;
	}

	
	/**
	 * Getter method for returning the host portion of the URL used for connecting to OohLaLog.
	 */
	public String getHost() {
		return host;
	}

	
	/**
	 * Setter method for setting the host portion of the URL used for connecting to OohLaLog.
	 */
	public void setHost(String host) {
		this.host = host;
	}

	
	/**
	 * Getter method for returning the host name belonging to this OohLaLogLogger instance.
	 */
	public String getHostName() {
		return hostName;
	}

	
	/**
	 * Setter method for setting the host name belonging to this OohLaLogLogger instance.
	 */
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	
	/**
	 * Getter method for returning the path portion of the URL used for connecting to OohLaLog.
	 */
	public String getPath() {
		return path;
	}

	
	/**
	 * Getter method for returning the base logging framework used.
	 */
	public String getAgent() {
		return agent;
	}


	/**
	 * Getter method for returning the stats path portion of the URL used for connecting to OohLaLog.
	 */
	public String getStatsPath() {
		return statsPath;
	}

	
	/**
	 * Getter method for returning the port portion of the URL used for connecting to OohLaLog.
	 */
	public int getPort() {
		return port;
	}


	/**
	 * Getter method for returning the authToken used by this instance of OohLaLogLogger
	 * for connecting to an OohLaLog project. 
	 */
	public String getAuthToken() {
		return authToken;
	}

	
	/**
	 * Setter method for setting the authToken used by this instance of OohLaLogLogger
	 * for connecting to an OohLaLog project. 
	 */
	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}

	
	/**
	 * Getter method for returning the timeBuffer used by this instance of OohLaLogLogger. 
	 * TimeBuffer is the amount of time the logger will wait before issuing requests to
	 * the OohLaLog server.
	 */
	public long getTimeBuffer() {
		return timeBuffer;
	}

	
	/**
	 * Setter method for setting the timeBuffer used by this instance of OohLaLogLogger. 
	 * TimeBuffer is the amount of time the logger will wait before issuing requests to
	 * the OohLaLog server.
	 */
	public void setTimeBuffer(long timeBuffer) {
		this.timeBuffer = timeBuffer;
	}

	
	/**
	 * Getter method for returning whether or not the connection to the OohLaLog server
	 * is secure.
	 */
	public boolean getSecure() {
		return secure;
	}

	
	/**
	 * Setter method for setting whether or not the connection to the OohLaLog server
	 * is secure.
	 */
	public void setSecure(boolean secure) {
		this.secure = secure;
	}

	
	/**
	 * Getter method for returning whether or not the logger is in debug mode.  Debug mode
	 * will cause many debug messages to be printed to StdOut.
	 */
	public boolean getDebug() {
		return debug;
	}

	
	/**
	 * Setter method for setting whether or not the logger is in debug mode.  Debug mode
	 * will cause many debug messages to be printed to StdOut.
	 */
	public void setDebug(boolean debug) {
		this.debug = debug;
	}

	
	/**
	 * Getter method for returning a boolean indicating whether or not logging stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public boolean getShowStats() {
		return showStats;
	}

	
	/**
	 * Setter method for setting a boolean indicating whether or not logging stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public void setShowStats(boolean showStats) {
		this.showStats = showStats;
	}

	
	/**
	 * Getter method for returning a long representing the time in milliseconds this instance of 
	 * OohLaLogLogger will wait before sending stats to the OohLaLog server.
	 */
	public long getstatsInterval() {
		return statsInterval;
	}

	
	/**
	 * Setter method for setting a long representing the time in milliseconds this instance of 
	 * OohLaLogLogger will wait before sending stats to the OohLaLog server.
	 */
	public void setstatsInterval(long statsInterval) {
		this.statsInterval = statsInterval;
	}

	
	/**
	 * Getter method for returning a boolean indicating whether or not memory stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public boolean getShowMemoryStats() {
		return showMemoryStats;
	}

	
	/**
	 * Setter method for setting a boolean indicating whether or not memory stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public void setShowMemoryStats(boolean showMemoryStats) {
		this.showMemoryStats = showMemoryStats;
	}

	
	/**
	 * Getter method for returning a boolean indicating whether or not cpu stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public boolean getShowCPUStats() {
		return showCPUStats;
	}

	
	/**
	 * Setter method for setting a boolean indicating whether or not cpu stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public void setShowCPUStats(boolean showCPUStats) {
		this.showCPUStats = showCPUStats;
	}

	
	/**
	 * Getter method for returning a boolean indicating whether or not file system stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public boolean getShowFileSystemStats() {
		return showFileSystemStats;
	}

	
	/**
	 * Setter method for setting a boolean indicating whether or not file system stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	public void setShowFileSystemStats(boolean showFileSystemStats) {
		this.showFileSystemStats = showFileSystemStats;
	}
}

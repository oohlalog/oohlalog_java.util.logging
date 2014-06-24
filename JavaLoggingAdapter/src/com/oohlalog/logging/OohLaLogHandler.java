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
	private long statsInterval = 60000; // 1 minute

	// For configuring the URL
	private String host = "api.oohlalog.com"; // localhost
	private String path = "/api/logging/save.json";
	private String statsPath = "/api/timeSeries/save.json";
	private int port = 80; //8196

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

	
	/**
	 * Constructor for an OohLaLogHandler that requires only the user's OohLaLog authorization token.
	 * @param authToken the authorization token for the user's OohLaLog instance
	 */
	public OohLaLogHandler(String authToken) {
		this.authToken = authToken;
		logRecordBuffer = new LogRecordBuffer(maxBuffer);
		logController = new LogController(this, threshold, timeBuffer, statsInterval);
	}


	/**
	 * Constructor for an OohLaLogHandler that takes the user's OohLaLog authorization token in addition 
	 * to a Map of settings. 
	 * @param authToken the authorization token for the user's OohLaLog instance
	 * @param map a map that can contain settings for this OohLaLogHandler
	 * TODO: Is there a cleaner way to perform this task?
	 */
	public OohLaLogHandler(String authToken, Map<String, Object> map) {
		this.authToken = authToken;
		// Update threshold parameter
		if (map.containsKey("threshold")) {
			try {
				int newThreshold = (Integer)map.get("threshold");
				if (newThreshold > 0) this.threshold = newThreshold;
			}
			catch (Exception e) {
				// Do nothing
			}
		}
		// Update maxBuffer parameter
		if (map.containsKey("maxBuffer")) {
			try {
				int newMaxBuffer = (Integer)map.get("maxBuffer");
				if (newMaxBuffer > 0) this.maxBuffer = newMaxBuffer;
			} catch (Exception e) {
				// Do nothing
			}
		}
		// Update timeBuffer parameter
		if (map.containsKey("timeBuffer")) {
			try {
				long newTimeBuffer = (Long)map.get("timeBuffer");
				if (newTimeBuffer > 0) this.timeBuffer = newTimeBuffer;
			} catch (Exception e) {
				// Do nothing
			}
		}
		// Update statsInterval parameter
		if (map.containsKey("statsInterval")) {
			try {
				long newstatsInterval = (Long)map.get("statsInterval");
				if (newstatsInterval > 0) this.statsInterval = newstatsInterval;
			} catch (Exception e) {
				// Do nothing
			}
		}
		// Update secure parameter
		if (map.containsKey("secure")) {
			try {
				boolean newSecure = (Boolean)map.get("secure");
				this.secure = newSecure;
			} catch (Exception e) {
				// Do nothing
			}
		}
		// Update debug parameter
		if (map.containsKey("debug")) {
			try {
				boolean newDebug = (Boolean)map.get("debug");
				this.debug= newDebug;
			} catch (Exception e) {
				// Do nothing
			}
		}

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

//-----------------------------------------------------------------------------------------		
	
	/**
	 * Getter method for returning the LogRecordBuffer belonging to this OohLaLogLogger instance.
	 */
	protected LogRecordBuffer getLogRecordBuffer() {
		return logRecordBuffer;
	}
	
	
	/**
	 * Getter method for returning the host portion of the URL used for connecting to OohLaLog.
	 */
	protected String getHost() {
		return host;
	}


	/**
	 * Getter method for returning the path portion of the URL used for connecting to OohLaLog.
	 */
	protected String getPath() {
		return path;
	}

	
	/**
	 * Getter method for returning the stats path portion of the URL for connecting to OohLaLog.
	 */
	protected String getStatsPath() {
		return statsPath;
	}

	
	/**
	 * Getter method for returning the base logging framework used.
	 */
	protected String getAgent() {
		return agent;
	}


	/**
	 * Getter method for returning the port portion of the URL used for connecting to OohLaLog.
	 */
	protected int getPort() {
		return port;
	}


	/**
	 * Getter method for returning the authToken used by this instance of OohLaLogLogger
	 * for connecting to an OohLaLog project. 
	 */
	protected String getAuthToken() {
		return authToken;
	}


	/**
	 * Getter method for returning whether or not the connection to the OohLaLog server
	 * is secure.
	 */
	protected boolean getSecure() {
		return secure;
	}


	/**
	 * Getter method for returning whether or not the logger is in debug mode.  Debug mode
	 * will cause many debug messages to be printed to StdOut.
	 */
	protected boolean getDebug() {
		return debug;
	}


	/**
	 * Getter method for returning a boolean indicating whether or not logging stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	protected boolean getShowStats() {
		return showStats;
	}


	/**
	 * Getter method for returning a boolean indicating whether or not memory stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	protected boolean getShowMemoryStats() {
		return showMemoryStats;
	}


	/**
	 * Getter method for returning a boolean indicating whether or not cpu stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	protected boolean getShowCPUStats() {
		return showCPUStats;
	}


	/**
	 * Getter method for returning a boolean indicating whether or not file system stats 
	 * associated with this instance of OohLaLogLogger will be sent to the OohLaLog server.
	 */
	protected boolean getShowFileSystemStats() {
		return showFileSystemStats;
	}
}

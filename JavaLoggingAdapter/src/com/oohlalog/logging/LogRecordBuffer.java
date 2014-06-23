package com.oohlalog.logging;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.logging.LogRecord;


/**
 * This class is mostly a wrapper for a Queue.  It's purpose is to provide thread safe access
 * to the buffer holding all of the logs.
 */
public class LogRecordBuffer {
	// Maximum allowed size of the buffer
	private final int maxBuffer;

	// Holds all of the Logs 
	private Queue<LogRecord> deque; 

	
	/**
	 * Constructor that creates a LogRecord Buffer with a maximum size.
	 * 
	 * @param maxBuffer the maximum size of the LogRecord Buffer
	 */
	public LogRecordBuffer(int maxBuffer) {
		this.maxBuffer = maxBuffer;
		deque = new ArrayDeque<LogRecord>(maxBuffer);
	}


	/**
	 * Adds a log record to the buffer.  If the buffer is full, the oldest log in the buffer is discarded
	 * so that there becomes room for the new one.
	 * 
	 * @param lr the log record to add to the buffer
	 */
	public synchronized void addLogToBuffer(LogRecord lr) {
		Queue<LogRecord> buff = getDeque();
		if (!buff.offer(lr)) {

			buff.poll();
			buff.offer(lr);
		}
	}

	
	/**
	 * Removes a certain number of logs from the buffer.
	 * 
	 * @param num number of logs to remove form head of queue
	 */
	private synchronized void removeLogsFromBuffer(int num) {
		Queue<LogRecord> buff = getDeque();
		for (int i = 0; i < num; i++) {
			buff.poll();
		}
	}

	
	/**
	 * Flush at most amtToFlush items from the buffer.
	 * 
	 * @param handler the OohLaLogHandler object 
	 * @param maxAmtToFlush the maximum number to flush
	 * @return was the payload sent successfully?
	 */
	protected synchronized boolean flushLogRecordBuffer(final OohLaLogHandler handler, final int maxAmtToFlush ) {		
		int size = size();
		if(size == 0) return false;
		int numToFlush = (maxAmtToFlush < size) ? maxAmtToFlush : size;

		// Creates a copy because we don't want to remove logs from deque
		// unless payload is successfully delivered
		List<LogRecord> logs = new ArrayList<LogRecord>(size);
		logs.addAll(getDeque());
		logs = logs.subList(0, numToFlush);

		Payload pl = new Payload.Builder()
		.messages(logs)
		.authToken(handler.getAuthToken())
		.host(handler.getHost())
		.agent(handler.getAgent())
		.path(handler.getPath())
		.port(handler.getPort())
		.secure(handler.getSecure())
		.debug(handler.getDebug())
		.build();

		boolean success = Payload.send( pl );
		// Payload successfully delivered so we can remove the logs that we already sent.
		if (success) removeLogsFromBuffer(numToFlush);

		return success;
	}

	
	/**
	 * Returns the number of logs in the buffer.
	 * 
	 * @return the number of logs in the queue
	 */
	protected  int size() {
		Queue<LogRecord> buff = getDeque();
		return buff.size();
	}

	
	/**
	 * Returns the maximum allowed size of the Log Record Buffer.
	 * 
	 * @return the maximum allowed size of the queue
	 */
	protected int getMaxBuffer() {
		return maxBuffer;
	}

	
	/**
	 * Returns the underlying Queue structure that holds all of the logs.
	 * 
	 * @return the Queue used for holding the logs
	 */
	private synchronized Queue<LogRecord> getDeque() {
		return deque;
	}
}

package com.oohlalog.logging;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class LogController {
	// The time interval between automatic flushes of logs
	private long timeBuffer;
	// The time interval between automatic flushes of statistical data
	private long statsInterval;
	// Time of last flush
	private long lastFlush = System.currentTimeMillis();
	// Time of last failed flush
	private long lastFailedFlush = System.currentTimeMillis();
	// Time to wait between failed flushes
	private long failedFlushWait = 2000;
	// Is a flushing process currently happening?  TODO: Implement synchronized methods instead
	private final AtomicBoolean flushing = new AtomicBoolean( false );
	// Maximum size of the deque before we automatically flush it
	private int threshold;
	
	private boolean shutdown = false;
	private ExecutorService executorService;

	// The handler instance belonging to this LogControl
	private OohLaLogHandler handler;


	/**
	 * Constructor that creates our LogControl object.
	 * @param handler
	 * @param threshold
	 * @param timeBuffer
	 */
	public LogController(OohLaLogHandler handler, int threshold, long timeBuffer, long statsInterval) {
		this.handler = handler;
		this.threshold = threshold;
		this.timeBuffer = timeBuffer;
		this.statsInterval = statsInterval;
		this.executorService = Executors.newFixedThreadPool(5);
		init();
	}


	/**
	 * Initializes the Log Control object.  It starts the thread that checks for, and handles three events:
	 * 1. Event: Deque of logs reaches threshold	Action: Flush threshold value of logs to OLL server
	 * 2. Event: Log timer goes off					Action: Flush all logs in the deque to the OLL server
	 * 3. Event: Stats timer goes off				Action: Flush stats to the OLL server
	 */
	protected void init() {
		// Starts the thread that checks to see if the size of the deque is greater than 150
		startThresholdCheck();
		
		// Only start the stats thread if the user specified
		if (this.handler.getShowStats())
			startStatsTimer();
		
		// Only start the flush timer if there is something in the deque.
		if (this.handler.getLogRecordBuffer().size() > 0)
			startFlushTimer();
	}
	
	
	/**
	 * Flushes the deque of log entries if the deque is of size greater than buffer threshold.
	 */
	protected void startThresholdCheck() {
		final OohLaLogHandler handler = this.handler;
		executorService.execute( new Runnable() {
			public void run() {
				while (!shutdown) {
					LogRecordBuffer buffer = handler.getLogRecordBuffer();
					if ( (buffer.size() >= threshold) && !flushing.get() && 
							(System.currentTimeMillis() - lastFailedFlush > failedFlushWait) ) {
						if (handler.getDebug()) System.out.println( ">>>Above Threshold" );
						flush(threshold);		
					}
				}
			}
		});
	}

	
	/**
	 * Starts the timer that will cause logs to be flushed at the set interval.  This thread runs to completion
	 * when the deque is empty and get re-instantiated on first add to the deque.  This keeps the thread from running
	 * forever while making sure that even when JVM finishes, all remaining logs are logged.
	 */
	protected void startFlushTimer() {
		final OohLaLogHandler handler = this.handler;
		executorService.execute( new Runnable() {
			public void run() {
				// If appender closes, let thread die
				while ( (handler.getLogRecordBuffer().size() != 0) && !shutdown ) {
					if (handler.getDebug()) System.out.println( ">>Timer Cycle" );

					// If timeout, flush deque
					if ( (System.currentTimeMillis() - lastFlush > timeBuffer) && !flushing.get() ) {
						if (handler.getDebug()) System.out.println( ">>>Flushing from timer expiration" );
						flush(Integer.MAX_VALUE );
						
						// This thread is done after flushing
						break;
					}

					// Wait for a time interval
					try {
						Thread.sleep(timeBuffer);
					}
					catch ( InterruptedException ie ) {
						// Ignore, and continue
					}
				}
			}
		});
	}


	/**
	 * Starts the timer that will cause statistics to be flushed at the set interval.
	 */
	protected void startStatsTimer() {
		final OohLaLogHandler handler = this.handler;
		executorService.execute( new Runnable() {
			public void run() {
				// If appender closes, let thread die
				while (!shutdown) {
					if (handler.getShowStats()) {
						if (handler.getDebug()) System.out.println( ">>Stats Timer" );
						Map<String,Double> metrics = StatsUtils.getStats(handler);
						StatsPayload pl= new StatsPayload.Builder()
						.metrics(metrics)
						.authToken(handler.getAuthToken())
						.host(handler.getHost())
						.agent(handler.getAgent())
						.path(handler.getPath())
						.port(handler.getPort())
						.secure(handler.getSecure())
						.debug(handler.getDebug())
						.build();
						StatsPayload.send( pl );
					}

					// Sleep the thread
					try {
						Thread.sleep(statsInterval);
					}
					catch ( InterruptedException ie ) {
						// Ignore, and continue
					}
				}
			}
		});
	}


	/**
	 * Flush at most amtToFlush items from the deque.
	 */
	protected void flush(final int amtToFlush ) {
		final OohLaLogHandler handler = this.handler;
		if (handler.getDebug()) System.out.println( ">>>>>>Flushing Deque Completely" );
		flushing.set( true );
		Thread t = new Thread( new Runnable() {
			public void run() {
				boolean success = handler.getLogRecordBuffer().flushLogRecordBuffer(handler, amtToFlush);
				// Payload successfully delivered so we can remove the logs that we already sent.
				if (success) {
					lastFlush = System.currentTimeMillis();
				}
				
				else {
					lastFailedFlush = System.currentTimeMillis();
				}
				flushing.set( false );
				return;
			}
		});
		t.start();
	}

	
	protected void close() {

		flush(Integer.MAX_VALUE);
		shutdown = true;
		executorService.shutdown();
	}
}

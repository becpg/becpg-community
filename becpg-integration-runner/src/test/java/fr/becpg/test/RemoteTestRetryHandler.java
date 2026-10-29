/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test;

import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.function.LongConsumer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.protocol.HttpContext;

/**
 * Decides whether a proxied test call is sent again after an I/O failure.
 *
 * A refused connection means the request never left the client. On the integration CI it is the
 * port-forward tunnel being re-established, which the Connect Gateway cuts every 30 minutes
 * (#37761), so it is retried after a pause long enough to outlast the reconnection. The other
 * transient failures are retried immediately, a few times.
 */
public class RemoteTestRetryHandler implements HttpRequestRetryHandler {

	static final int MAX_RETRY_ATTEMPTS = 3;
	static final int MAX_RECONNECT_ATTEMPTS = 12;
	static final long RECONNECT_DELAY_MS = 5000;

	private static final Log logger = LogFactory.getLog(RemoteTestRetryHandler.class);

	private final LongConsumer pause;

	/**
	 * Creates a handler that sleeps the calling thread between reconnection attempts.
	 */
	public RemoteTestRetryHandler() {
		this(RemoteTestRetryHandler::sleep);
	}

	/**
	 * Creates a handler with its own pause, so that tests do not wait.
	 *
	 * @param pause called with the delay, in milliseconds, before a reconnection attempt
	 */
	RemoteTestRetryHandler(LongConsumer pause) {
		this.pause = pause;
	}

	/**
	 * Tells whether the request is sent again.
	 *
	 * @param exception the failure of the last attempt
	 * @param executionCount the number of attempts made so far
	 * @param context the HTTP context, unused
	 * @return true to send the request again
	 */
	@Override
	public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
		if (exception instanceof ConnectException) {
			return retryAfterPause(executionCount);
		}
		if ((executionCount > MAX_RETRY_ATTEMPTS) || !isTransient(exception)) {
			return false;
		}
		logger.warn(exception.getClass().getSimpleName() + " on attempt " + executionCount + ", retrying...");
		return true;
	}

	private boolean retryAfterPause(int executionCount) {
		if (executionCount > MAX_RECONNECT_ATTEMPTS) {
			return false;
		}
		logger.warn("Connection refused on attempt " + executionCount + ", retrying in " + RECONNECT_DELAY_MS + " ms...");
		pause.accept(RECONNECT_DELAY_MS);
		return !Thread.currentThread().isInterrupted();
	}

	private static boolean isTransient(IOException exception) {
		return (exception instanceof NoHttpResponseException) || (exception instanceof SocketTimeoutException)
				|| (exception instanceof ConnectTimeoutException) || (exception instanceof UnknownHostException)
				|| (exception instanceof NoRouteToHostException);
	}

	private static void sleep(long millis) {
		try {
			Thread.sleep(millis);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}

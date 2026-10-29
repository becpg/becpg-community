/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test;

import java.io.IOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;
import org.apache.http.NoHttpResponseException;
import org.apache.http.conn.HttpHostConnectException;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link RemoteTestRetryHandler}.
 */
public class RemoteTestRetryHandlerTest {

	private static final HttpHostConnectException CONNECTION_REFUSED = new HttpHostConnectException(new IOException("Connection refused"),
			new HttpHost("becpg-tunnel", 8080));

	private final List<Long> pauses = new ArrayList<>();

	private final RemoteTestRetryHandler handler = new RemoteTestRetryHandler(pauses::add);

	@Test
	public void testRefusedConnectionIsRetriedAfterAPause() {
		Assert.assertTrue(handler.retryRequest(CONNECTION_REFUSED, 1, null));
		Assert.assertEquals(List.of(RemoteTestRetryHandler.RECONNECT_DELAY_MS), pauses);
	}

	@Test
	public void testRefusedConnectionOutlastsTheImmediateRetries() {
		Assert.assertTrue(handler.retryRequest(CONNECTION_REFUSED, RemoteTestRetryHandler.MAX_RECONNECT_ATTEMPTS, null));
	}

	@Test
	public void testRefusedConnectionGivesUpAfterTheLastReconnection() {
		Assert.assertFalse(handler.retryRequest(CONNECTION_REFUSED, RemoteTestRetryHandler.MAX_RECONNECT_ATTEMPTS + 1, null));
		Assert.assertTrue(pauses.isEmpty());
	}

	@Test
	public void testTransientFailureIsRetriedWithoutPause() {
		Assert.assertTrue(handler.retryRequest(new NoHttpResponseException("stale"), 1, null));
		Assert.assertTrue(pauses.isEmpty());
	}

	@Test
	public void testTransientFailureGivesUpAfterTheImmediateRetries() {
		Assert.assertFalse(handler.retryRequest(new NoHttpResponseException("stale"), RemoteTestRetryHandler.MAX_RETRY_ATTEMPTS + 1, null));
	}

	@Test
	public void testOtherFailureIsNotRetried() {
		Assert.assertFalse(handler.retryRequest(new SocketException("Connection reset"), 1, null));
	}
}

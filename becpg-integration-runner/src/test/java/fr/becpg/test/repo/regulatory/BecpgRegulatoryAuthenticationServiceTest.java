package fr.becpg.test.repo.regulatory;

import fr.becpg.repo.authentication.BeCPGTicketService;
import fr.becpg.repo.regulatory.becpg.regulatory.BecpgRegulatoryAuthenticationService;
import fr.becpg.repo.system.SystemConfigurationService;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class BecpgRegulatoryAuthenticationServiceTest {

    // -----------------------------------------------------------------------
    // Constants
    // -----------------------------------------------------------------------

    private static final int    TOKEN_LIFETIME_S = 300;
    private static final String DUMMY_TOKEN      = "dummy-access-token";
    private static final String DUMMY_TICKET     = "dummy-becpg-ticket";

    // -----------------------------------------------------------------------
    // Infrastructure
    // -----------------------------------------------------------------------

    private MockWebServer            mockServer;
    private SystemConfigurationService configService;
    private BeCPGTicketService       ticketService;

    @Before
    public void setUp() throws IOException {
        mockServer    = new MockWebServer();
        mockServer.start();
        configService = mock(SystemConfigurationService.class);
        ticketService = mock(BeCPGTicketService.class);
    }

    @After
    public void tearDown() throws IOException {
        mockServer.shutdown();
    }

    // -----------------------------------------------------------------------
    // Builder helpers
    // -----------------------------------------------------------------------

    /** Configures all OAuth2 properties so construction succeeds. */
    private void configureOAuth2(String tokenUrl) {
        when(configService.confValue("beCPG.regulatory.authMode")).thenReturn("oauth2");
        when(configService.confValue("beCPG.regulatory.oauth2.tokenUrl")).thenReturn(tokenUrl);
        when(configService.confValue("beCPG.regulatory.oauth2.clientId")).thenReturn("becpg-client");
        when(configService.confValue("beCPG.regulatory.oauth2.clientSecret")).thenReturn("super-secret");
        when(configService.confValue("beCPG.regulatory.oauth2.scope")).thenReturn(null);
        when(configService.confValue("beCPG.regulatory.oauth2.buffer")).thenReturn("0");
    }

    /** Configures all OAuth2 properties pointing at the running MockWebServer. */
    private void configureOAuth2() {
        configureOAuth2(mockServer.url("/realms/becpg/protocol/openid-connect/token").toString());
    }

    /** Configures ticket mode — no OAuth2 properties needed. */
    private void configureTicket() {
        when(configService.confValue("beCPG.regulatory.authMode")).thenReturn("ticket");
        when(configService.confValue("beCPG.regulatory.oauth2.buffer")).thenReturn("0");
    }

    private BecpgRegulatoryAuthenticationService buildService() throws Exception {
        return new BecpgRegulatoryAuthenticationService(configService, ticketService);
    }

    // -----------------------------------------------------------------------
    // Mock-server helpers
    // -----------------------------------------------------------------------

    private void enqueueTokenResponse(String token, int lifetimeSeconds) {
        String body = String.format(
                "{\"access_token\":\"%s\",\"token_type\":\"Bearer\",\"expires_in\":%d}",
                token, lifetimeSeconds);
        mockServer.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    private void enqueueErrorResponse(int status, String error, String description) {
        String body = String.format(
                "{\"error\":\"%s\",\"error_description\":\"%s\"}", error, description);
        mockServer.enqueue(new MockResponse()
                .setResponseCode(status)
                .setHeader("Content-Type", "application/json")
                .setBody(body));
    }

    // -----------------------------------------------------------------------
    // Construction failures (OAuth2 mode with bad config)
    // -----------------------------------------------------------------------

    @Test(expected = Exception.class)
    public void nullTokenUrlThrowsInOAuth2Mode() throws Exception {
        configureOAuth2(null);
        buildService();
    }

    @Test(expected = Exception.class)
    public void malformedTokenUrlThrowsInOAuth2Mode() throws Exception {
        configureOAuth2("not a valid url !!!");
        buildService();
    }

    @Test(expected = Exception.class)
    public void nullClientIdThrowsInOAuth2Mode() throws Exception {
        configureOAuth2();
        when(configService.confValue("beCPG.regulatory.oauth2.clientId")).thenReturn(null);
        buildService();
    }

    @Test(expected = Exception.class)
    public void nullClientSecretThrowsInOAuth2Mode() throws Exception {
        configureOAuth2();
        when(configService.confValue("beCPG.regulatory.oauth2.clientSecret")).thenReturn(null);
        buildService();
    }

    // -----------------------------------------------------------------------
    // getOauth2Token — happy path
    // -----------------------------------------------------------------------

    @Test
    public void validResponseReturnsToken() throws Exception {
        configureOAuth2();
        enqueueTokenResponse(DUMMY_TOKEN, TOKEN_LIFETIME_S);
        BecpgRegulatoryAuthenticationService service = buildService();

        Optional<String> token = service.getOauth2Token();

        assertTrue("Token should be present", token.isPresent());
        assertEquals(DUMMY_TOKEN, token.get());
    }

    @Test
    public void calledTwiceOnlyFetchesOnce() throws Exception {
        configureOAuth2();
        enqueueTokenResponse(DUMMY_TOKEN, TOKEN_LIFETIME_S);
        BecpgRegulatoryAuthenticationService service = buildService();

        service.getOauth2Token();
        service.getOauth2Token();

        assertEquals("Token endpoint should be called exactly once", 1, mockServer.getRequestCount());
    }

    @Test
    public void afterInvalidateRefetches() throws Exception {
        configureOAuth2();
        enqueueTokenResponse(DUMMY_TOKEN, TOKEN_LIFETIME_S);
        enqueueTokenResponse("refreshed-token", TOKEN_LIFETIME_S);
        BecpgRegulatoryAuthenticationService service = buildService();

        Optional<String> first = service.getOauth2Token();
        service.invalidateOauth2Token();
        Optional<String> second = service.getOauth2Token();

        assertTrue(first.isPresent());
        assertTrue(second.isPresent());
        assertEquals(DUMMY_TOKEN,        first.get());
        assertEquals("refreshed-token", second.get());
        assertEquals("Two distinct fetches expected", 2, mockServer.getRequestCount());
    }

    // -----------------------------------------------------------------------
    // getOauth2Token — returns empty
    // -----------------------------------------------------------------------

    @Test
    public void inTicketModeReturnsEmpty() throws Exception {
        configureTicket();
        BecpgRegulatoryAuthenticationService service = buildService();

        Optional<String> token = service.getOauth2Token();

        assertFalse("getOauth2Token should return empty when not in OAuth2 mode", token.isPresent());
        assertEquals("No HTTP request should be made in ticket mode", 0, mockServer.getRequestCount());
    }

    @Test
    public void keycloakReturns400ReturnsEmpty() throws Exception {
        configureOAuth2();
        enqueueErrorResponse(400, "invalid_client", "Client authentication failed");
        BecpgRegulatoryAuthenticationService service = buildService();

        assertFalse(service.getOauth2Token().isPresent());
    }

    @Test
    public void hostNotAccessibleReturnsEmpty() throws Exception {
        // Capture the URL before shutdown so construction still succeeds.
        String deadUrl = mockServer.url("/realms/becpg/protocol/openid-connect/token").toString();
        mockServer.shutdown();
        configureOAuth2(deadUrl);

        BecpgRegulatoryAuthenticationService service = buildService();

        assertFalse("Token should be absent when host is unreachable",
                service.getOauth2Token().isPresent());
    }

    // -----------------------------------------------------------------------
    // getOauth2Token — token expiry
    // -----------------------------------------------------------------------

    /**
     * Documents the {@code Integer.getInteger()} quirk: the config string {@code "10"}
     * is treated as a <em>system property name</em>, not a numeric literal, and therefore
     * resolves to {@code null} → the default of {@code 0} is used.  To exercise a
     * non-zero buffer we set a real system property and clean up afterwards.
     *
     * <p>The server returns {@code expires_in=1} second. With a buffer of 5 s the
     * computed {@code expiresAt} is {@code now + 1 - 5 = now - 4 s}, which is already
     * in the past, so the next call must re-fetch.
     */
    @Test
    public void tokenExpiredInstantlyRefetchesOnNextCall() throws Exception {
        System.setProperty("becpg.test.buffer", "5");
        try {
            when(configService.confValue("beCPG.regulatory.authMode")).thenReturn("oauth2");
            when(configService.confValue("beCPG.regulatory.oauth2.tokenUrl"))
                    .thenReturn(mockServer.url("/realms/becpg/protocol/openid-connect/token").toString());
            when(configService.confValue("beCPG.regulatory.oauth2.clientId")).thenReturn("becpg-client");
            when(configService.confValue("beCPG.regulatory.oauth2.clientSecret")).thenReturn("super-secret");
            when(configService.confValue("beCPG.regulatory.oauth2.scope")).thenReturn(null);
            // Integer.getInteger("becpg.test.buffer", 0) → 5 (system property set above)
            when(configService.confValue("beCPG.regulatory.oauth2.buffer")).thenReturn("becpg.test.buffer");

            enqueueTokenResponse(DUMMY_TOKEN,       1);   // lifetime=1s, buffer=5s → already expired
            enqueueTokenResponse("refreshed-token", TOKEN_LIFETIME_S);

            BecpgRegulatoryAuthenticationService service = buildService();

            service.getOauth2Token();                      // warms the cache (expired immediately)
            Optional<String> second = service.getOauth2Token(); // must re-fetch

            assertTrue(second.isPresent());
            assertEquals("refreshed-token", second.get());
            assertEquals("Two fetches expected (initial + refresh)", 2, mockServer.getRequestCount());
        } finally {
            System.clearProperty("becpg.test.buffer");
        }
    }

    // -----------------------------------------------------------------------
    // Concurrency — getOauth2Token
    // -----------------------------------------------------------------------

    /**
     * When many threads call {@link BecpgRegulatoryAuthenticationService#getOauth2Token()}
     * simultaneously on a cold cache, the token endpoint must be hit exactly once
     * thanks to the double-checked locking inside {@code fetchOauth2Token()}.
     */
    @Test
    public void concurrentOnlyOneFetch() throws Exception {
        configureOAuth2();
        enqueueTokenResponse(DUMMY_TOKEN, TOKEN_LIFETIME_S);
        BecpgRegulatoryAuthenticationService service = buildService();

        int threadCount = 20;
        CountDownLatch ready  = new CountDownLatch(threadCount);
        CountDownLatch start  = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(threadCount);

        List<Optional<String>> results = new ArrayList<>(threadCount);
        for (int i = 0; i < threadCount; i++) results.add(Optional.empty());

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            final int idx = i;
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    results.set(idx, service.getOauth2Token());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finish.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        assertTrue("Threads did not finish in time", finish.await(10, TimeUnit.SECONDS));
        pool.shutdown();

        for (int i = 0; i < threadCount; i++) {
            assertTrue("Thread " + i + " got an empty Optional", results.get(i).isPresent());
            assertEquals("Thread " + i + " got wrong token", DUMMY_TOKEN, results.get(i).get());
        }
        assertEquals("Token endpoint should be called exactly once under concurrent load",
                1, mockServer.getRequestCount());
    }

    /**
     * After {@link BecpgRegulatoryAuthenticationService#invalidateOauth2Token()} called
     * from one thread, many racing threads must collectively trigger exactly one
     * re-fetch, not N.
     */
    @Test
    public void concurrentAfterInvalidateExactlyOneRefetch() throws Exception {
        configureOAuth2();
        enqueueTokenResponse(DUMMY_TOKEN, TOKEN_LIFETIME_S);      // warm-up
        enqueueTokenResponse("new-token",  TOKEN_LIFETIME_S);     // post-invalidate
        BecpgRegulatoryAuthenticationService service = buildService();

        service.getOauth2Token();     // warm the cache (request #1)
        service.invalidateOauth2Token();

        int threadCount = 20;
        CountDownLatch ready  = new CountDownLatch(threadCount);
        CountDownLatch start  = new CountDownLatch(1);
        CountDownLatch finish = new CountDownLatch(threadCount);
        AtomicInteger emptyCount = new AtomicInteger(0);

        ExecutorService pool = Executors.newFixedThreadPool(threadCount);
        for (int i = 0; i < threadCount; i++) {
            pool.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    if (!service.getOauth2Token().isPresent())
                        emptyCount.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    finish.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        assertTrue("Threads did not finish in time", finish.await(10, TimeUnit.SECONDS));
        pool.shutdown();

        assertEquals("No thread should receive an empty token", 0, emptyCount.get());
        assertEquals("1 warm-up + 1 post-invalidate refetch = 2 total requests",
                2, mockServer.getRequestCount());
    }
}
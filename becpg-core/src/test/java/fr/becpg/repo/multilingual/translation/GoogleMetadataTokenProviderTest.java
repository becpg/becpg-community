package fr.becpg.repo.multilingual.translation;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link GoogleMetadataTokenProvider}.
 */
public class GoogleMetadataTokenProviderTest {

    private static final Instant NOW = Instant.parse("2026-11-03T10:00:00Z");

    private RestTemplate restTemplate;

    private MutableClock clock;

    private GoogleMetadataTokenProvider tokenProvider;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        clock = new MutableClock(NOW);
        tokenProvider = new GoogleMetadataTokenProvider(restTemplate, clock);
    }

    @Test
    public void readsTokenFromMetadataServerWithFlavorHeader() {
        respondWith("{\"access_token\":\"token-1\",\"expires_in\":3599,\"token_type\":\"Bearer\"}");

        assertEquals("token-1", tokenProvider.getAccessToken());

        ArgumentCaptor<HttpEntity<?>> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).exchange(eq(GoogleMetadataTokenProvider.TOKEN_URI), eq(HttpMethod.GET), request.capture(), eq(String.class));
        assertEquals("Google", request.getValue().getHeaders().getFirst("Metadata-Flavor"));
    }

    @Test
    public void reusesCachedTokenUntilShortlyBeforeExpiry() {
        respondWith("{\"access_token\":\"token-1\",\"expires_in\":3600}");

        tokenProvider.getAccessToken();
        clock.advance(Duration.ofMinutes(58));
        assertEquals("token-1", tokenProvider.getAccessToken());

        verifyFetchCount(1);
    }

    @Test
    public void fetchesNewTokenWhenCachedOneIsAboutToExpire() {
        respondWith("{\"access_token\":\"token-1\",\"expires_in\":3600}", "{\"access_token\":\"token-2\",\"expires_in\":3600}");

        tokenProvider.getAccessToken();
        clock.advance(Duration.ofMinutes(59));
        assertEquals("token-2", tokenProvider.getAccessToken());

        verifyFetchCount(2);
    }

    @SuppressWarnings("unchecked")
    private void respondWith(String body, String... nextBodies) {
        ResponseEntity<String>[] nextResponses = new ResponseEntity[nextBodies.length];
        for (int i = 0; i < nextBodies.length; i++) {
            nextResponses[i] = ResponseEntity.ok(nextBodies[i]);
        }
        when(restTemplate.exchange(eq(GoogleMetadataTokenProvider.TOKEN_URI), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(body), nextResponses);
    }

    private void verifyFetchCount(int count) {
        verify(restTemplate, times(count)).exchange(eq(GoogleMetadataTokenProvider.TOKEN_URI), eq(HttpMethod.GET), any(HttpEntity.class),
                eq(String.class));
    }

    private static final class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }
    }
}

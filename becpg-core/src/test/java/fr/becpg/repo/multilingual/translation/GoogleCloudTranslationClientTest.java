package fr.becpg.repo.multilingual.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.Optional;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link GoogleCloudTranslationClient}.
 */
public class GoogleCloudTranslationClientTest {

    private static final URI TRANSLATE_URI = URI.create("https://translate-eu.googleapis.com/v3/projects/becpg-314807/locations/europe-west1:translateText");

    private RestTemplate restTemplate;

    private GoogleMetadataTokenProvider tokenProvider;

    private GoogleCloudTranslationClient client;

    @Before
    public void setUp() {
        restTemplate = mock(RestTemplate.class);
        tokenProvider = mock(GoogleMetadataTokenProvider.class);
        when(tokenProvider.getAccessToken()).thenReturn("token-1");
        client = new GoogleCloudTranslationClient(restTemplate, tokenProvider);
        client.setProjectId("becpg-314807");
        client.setLocation("europe-west1");
        client.setEndpoint("https://translate-eu.googleapis.com");
    }

    @Test
    public void isDisabledWithoutProject() {
        client.setProjectId("");
        assertFalse(client.isEnabled());
        client.setProjectId(null);
        assertFalse(client.isEnabled());
        client.setProjectId("becpg-314807");
        assertTrue(client.isEnabled());
    }

    @Test
    public void buildsRegionalTranslateUri() {
        assertEquals(TRANSLATE_URI, client.buildTranslateUri());
    }

    @Test
    public void toleratesTrailingSlashOnEndpoint() {
        client.setEndpoint("https://translate-eu.googleapis.com/");
        assertEquals(TRANSLATE_URI, client.buildTranslateUri());
    }

    @Test
    public void buildsPlainTextRequestWithBearerToken() {
        HttpEntity<String> request = client.buildRequest("Sucre & sel", "fr", "en");

        JSONObject body = new JSONObject(request.getBody());
        assertEquals("Sucre & sel", body.getJSONArray("contents").getString(0));
        assertEquals(1, body.getJSONArray("contents").length());
        assertEquals("fr", body.getString("sourceLanguageCode"));
        assertEquals("en", body.getString("targetLanguageCode"));
        assertEquals("text/plain", body.getString("mimeType"));
        assertEquals("Bearer token-1", request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
        assertEquals("application/json;charset=UTF-8", request.getHeaders().getContentType().toString());
    }

    @Test
    public void parsesFirstTranslation() {
        assertEquals(Optional.of("Sugar & salt"),
                GoogleCloudTranslationClient.parseTranslatedText("{\"translations\":[{\"translatedText\":\"Sugar & salt\"}]}"));
    }

    @Test
    public void parsesMissingTranslationAsEmpty() {
        assertEquals(Optional.empty(), GoogleCloudTranslationClient.parseTranslatedText("{\"translations\":[]}"));
        assertEquals(Optional.empty(), GoogleCloudTranslationClient.parseTranslatedText("{}"));
    }

    @Test
    public void translatesThroughConfiguredEndpoint() {
        when(restTemplate.exchange(eq(TRANSLATE_URI), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok("{\"translations\":[{\"translatedText\":\"Sugar\"}]}"));

        assertEquals(Optional.of("Sugar"), client.translate("Sucre", "fr", "en"));
    }

    @Test
    public void returnsEmptyWhenApiRejectsRequest() {
        when(restTemplate.exchange(eq(TRANSLATE_URI), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertEquals(Optional.empty(), client.translate("Sucre", "fr", "en"));
    }

    @Test
    public void returnsEmptyWhenTokenIsUnavailable() {
        when(tokenProvider.getAccessToken()).thenThrow(new ResourceAccessException("metadata.google.internal unreachable"));

        assertEquals(Optional.empty(), client.translate("Sucre", "fr", "en"));
    }
}

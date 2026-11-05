/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.multilingual.translation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Signature;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Base64;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * Unit tests for {@link GoogleServiceAccountTokenProvider}.
 */
public class GoogleServiceAccountTokenProviderTest {

    private static final Instant NOW = Instant.parse("2026-11-05T10:00:00Z");

    private static final String CLIENT_EMAIL = "sa-onprem-test-ai@becpg-314807.iam.gserviceaccount.com";

    private static final String TOKEN_URI = "https://oauth2.googleapis.com/token";

    private KeyPair keyPair;

    private RestTemplate restTemplate;

    private GoogleServiceAccountTokenProvider tokenProvider;

    @Before
    public void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        keyPair = generator.generateKeyPair();

        String pem = "-----BEGIN PRIVATE KEY-----\n" + Base64.getMimeEncoder().encodeToString(keyPair.getPrivate().getEncoded())
                + "\n-----END PRIVATE KEY-----\n";
        JSONObject key = new JSONObject().put("type", "service_account").put("client_email", CLIENT_EMAIL).put("private_key", pem)
                .put("token_uri", TOKEN_URI);

        restTemplate = mock(RestTemplate.class);
        tokenProvider = new GoogleServiceAccountTokenProvider(key, restTemplate, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    public void exchangesSignedAssertionAtTokenUri() throws Exception {
        respondWith("{\"access_token\":\"token-1\",\"expires_in\":3599,\"token_type\":\"Bearer\"}");

        assertEquals("token-1", tokenProvider.getAccessToken());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<HttpEntity<MultiValueMap<String, String>>> request = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate).postForEntity(eq(TOKEN_URI), request.capture(), eq(String.class));
        MultiValueMap<String, String> form = request.getValue().getBody();
        assertEquals(GoogleServiceAccountTokenProvider.GRANT_TYPE, form.getFirst("grant_type"));

        String[] parts = form.getFirst("assertion").split("\\.");
        assertEquals(3, parts.length);
        JSONObject claims = new JSONObject(new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8));
        assertEquals(CLIENT_EMAIL, claims.getString("iss"));
        assertEquals(TOKEN_URI, claims.getString("aud"));
        assertEquals(GoogleServiceAccountTokenProvider.SCOPE, claims.getString("scope"));
        assertEquals(NOW.getEpochSecond() + 3600, claims.getLong("exp"));

        Signature verifier = Signature.getInstance("SHA256withRSA");
        verifier.initVerify(keyPair.getPublic());
        verifier.update((parts[0] + "." + parts[1]).getBytes(StandardCharsets.US_ASCII));
        assertTrue(verifier.verify(Base64.getUrlDecoder().decode(parts[2])));
    }

    @Test
    public void reusesCachedTokenUntilShortlyBeforeExpiry() {
        respondWith("{\"access_token\":\"token-1\",\"expires_in\":3600}");

        tokenProvider.getAccessToken();
        assertEquals("token-1", tokenProvider.getAccessToken());

        verify(restTemplate, times(1)).postForEntity(eq(TOKEN_URI), any(HttpEntity.class), eq(String.class));
    }

    @Test(expected = IllegalStateException.class)
    public void rejectsInvalidPrivateKey() {
        GoogleServiceAccountTokenProvider.parsePrivateKey("-----BEGIN PRIVATE KEY-----\nnot-a-key\n-----END PRIVATE KEY-----");
    }

    private void respondWith(String body) {
        when(restTemplate.postForEntity(eq(TOKEN_URI), any(HttpEntity.class), eq(String.class))).thenReturn(ResponseEntity.ok(body));
    }
}

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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import fr.becpg.repo.helper.RestTemplateHelper;

/**
 * Access token of a service account key file, for servers outside Google Cloud that have no metadata server.
 * The signed JWT assertion is exchanged at the token URI of the key (RFC 7523).
 *
 * @author matthieu
 */
public class GoogleServiceAccountTokenProvider implements GoogleAccessTokenProvider {

    static final String SCOPE = "https://www.googleapis.com/auth/cloud-translation";

    static final String GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    static final String DEFAULT_TOKEN_URI = "https://oauth2.googleapis.com/token";

    private static final String JSON_CLIENT_EMAIL = "client_email";

    private static final String JSON_PRIVATE_KEY = "private_key";

    private static final String JSON_TOKEN_URI = "token_uri";

    private static final String JSON_ACCESS_TOKEN = "access_token";

    private static final String JSON_EXPIRES_IN = "expires_in";

    private static final Duration ASSERTION_LIFETIME = Duration.ofHours(1);

    // Leaves room for the translation request in flight and for clock skew
    private static final Duration EXPIRY_MARGIN = Duration.ofMinutes(1);

    private final RestTemplate restTemplate;

    private final Clock clock;

    private final String clientEmail;

    private final PrivateKey privateKey;

    private final String tokenUri;

    private String accessToken;

    private Instant refreshAfter = Instant.MIN;

    /**
     * <p>Constructor for GoogleServiceAccountTokenProvider.</p>
     *
     * @param credentialsFile path of the JSON key of the service account
     */
    public GoogleServiceAccountTokenProvider(String credentialsFile) {
        this(readKey(Path.of(credentialsFile)), RestTemplateHelper.getRestTemplate(), Clock.systemUTC());
    }

    GoogleServiceAccountTokenProvider(JSONObject key, RestTemplate restTemplate, Clock clock) {
        this.restTemplate = restTemplate;
        this.clock = clock;
        this.clientEmail = key.getString(JSON_CLIENT_EMAIL);
        this.privateKey = parsePrivateKey(key.getString(JSON_PRIVATE_KEY));
        this.tokenUri = key.optString(JSON_TOKEN_URI, DEFAULT_TOKEN_URI);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized String getAccessToken() {
        if ((accessToken == null) || !clock.instant().isBefore(refreshAfter)) {
            fetchAccessToken();
        }
        return accessToken;
    }

    private void fetchAccessToken() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", GRANT_TYPE);
        form.add("assertion", buildAssertion());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, new HttpEntity<>(form, headers), String.class);

        JSONObject json = new JSONObject(response.getBody());
        Instant expiresAt = clock.instant().plusSeconds(json.getLong(JSON_EXPIRES_IN));
        accessToken = json.getString(JSON_ACCESS_TOKEN);
        refreshAfter = expiresAt.minus(EXPIRY_MARGIN);
    }

    String buildAssertion() {
        Instant now = clock.instant();
        JSONObject header = new JSONObject().put("alg", "RS256").put("typ", "JWT");
        JSONObject claims = new JSONObject().put("iss", clientEmail).put("scope", SCOPE).put("aud", tokenUri)
                .put("iat", now.getEpochSecond()).put("exp", now.plus(ASSERTION_LIFETIME).getEpochSecond());

        String signingInput = base64Url(header.toString().getBytes(StandardCharsets.UTF_8)) + "."
                + base64Url(claims.toString().getBytes(StandardCharsets.UTF_8));
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(signingInput.getBytes(StandardCharsets.US_ASCII));
            return signingInput + "." + base64Url(signature.sign());
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Cannot sign the token assertion of " + clientEmail, e);
        }
    }

    private static JSONObject readKey(Path credentialsFile) {
        try {
            return new JSONObject(Files.readString(credentialsFile, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read the service account key " + credentialsFile, e);
        }
    }

    static PrivateKey parsePrivateKey(String pem) {
        String base64 = pem.replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "").replaceAll("\\s", "");
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(base64)));
        } catch (GeneralSecurityException | IllegalArgumentException e) {
            throw new IllegalStateException("Invalid private key in the service account key", e);
        }
    }

    private static String base64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}

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

import java.net.URI;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import fr.becpg.repo.helper.RestTemplateHelper;

/**
 * Provides an access token of the service account attached to the pod (GKE Workload Identity) or
 * to the VM, read from the Google metadata server and cached until shortly before it expires.
 *
 * @author matthieu
 */
public class GoogleMetadataTokenProvider implements GoogleAccessTokenProvider {

    static final URI TOKEN_URI = URI.create("http://metadata.google.internal/computeMetadata/v1/instance/service-accounts/default/token");

    static final String METADATA_FLAVOR_HEADER = "Metadata-Flavor";

    static final String METADATA_FLAVOR_VALUE = "Google";

    private static final String JSON_ACCESS_TOKEN = "access_token";

    private static final String JSON_EXPIRES_IN = "expires_in";

    // Leaves room for the translation request in flight and for clock skew
    private static final Duration EXPIRY_MARGIN = Duration.ofMinutes(1);

    private final RestTemplate restTemplate;

    private final Clock clock;

    private String accessToken;

    private Instant refreshAfter = Instant.MIN;

    /**
     * <p>Constructor for GoogleMetadataTokenProvider.</p>
     */
    public GoogleMetadataTokenProvider() {
        this(RestTemplateHelper.getRestTemplate(), Clock.systemUTC());
    }

    GoogleMetadataTokenProvider(RestTemplate restTemplate, Clock clock) {
        this.restTemplate = restTemplate;
        this.clock = clock;
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
        HttpHeaders headers = new HttpHeaders();
        headers.set(METADATA_FLAVOR_HEADER, METADATA_FLAVOR_VALUE);
        ResponseEntity<String> response = restTemplate.exchange(TOKEN_URI, HttpMethod.GET, new HttpEntity<>(headers), String.class);

        JSONObject json = new JSONObject(response.getBody());
        Instant expiresAt = clock.instant().plusSeconds(json.getLong(JSON_EXPIRES_IN));
        accessToken = json.getString(JSON_ACCESS_TOKEN);
        refreshAfter = expiresAt.minus(EXPIRY_MARGIN);
    }
}

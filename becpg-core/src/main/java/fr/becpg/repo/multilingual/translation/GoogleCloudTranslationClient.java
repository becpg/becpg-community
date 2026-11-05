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
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import fr.becpg.repo.helper.RestTemplateHelper;

/**
 * Cloud Translation v3 client authenticated by the service account of the pod, without API key, or by a service
 * account key file on a server outside Google Cloud.
 * Enabled only when a Google project is configured.
 *
 * @author matthieu
 */
public class GoogleCloudTranslationClient {

    private static final Log logger = LogFactory.getLog(GoogleCloudTranslationClient.class);

    private static final String TRANSLATE_PATH = "/v3/projects/{projectId}/locations/{location}:translateText";

    private static final String JSON_CONTENTS = "contents";

    private static final String JSON_SOURCE_LANGUAGE_CODE = "sourceLanguageCode";

    private static final String JSON_TARGET_LANGUAGE_CODE = "targetLanguageCode";

    private static final String JSON_MIME_TYPE = "mimeType";

    private static final String MIME_TYPE_TEXT_PLAIN = "text/plain";

    private static final String JSON_TRANSLATIONS = "translations";

    private static final String JSON_TRANSLATED_TEXT = "translatedText";

    private final RestTemplate restTemplate;

    private GoogleAccessTokenProvider tokenProvider;

    private String projectId;

    private String location;

    private String endpoint;

    private String credentialsFile;

    /**
     * <p>Constructor for GoogleCloudTranslationClient.</p>
     */
    public GoogleCloudTranslationClient() {
        this(RestTemplateHelper.getRestTemplate(), null);
    }

    GoogleCloudTranslationClient(RestTemplate restTemplate, GoogleAccessTokenProvider tokenProvider) {
        this.restTemplate = restTemplate;
        this.tokenProvider = tokenProvider;
    }

    /**
     * <p>Setter for the field <code>projectId</code>.</p>
     *
     * @param projectId the Google Cloud project billed for the translations
     */
    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    /**
     * <p>Setter for the field <code>location</code>.</p>
     *
     * @param location the Cloud Translation location, matching the endpoint region
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * <p>Setter for the field <code>endpoint</code>.</p>
     *
     * @param endpoint the Cloud Translation endpoint base URL
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * Authenticates with the key of a service account instead of the metadata server, when set.
     *
     * @param credentialsFile path of the JSON key of the service account
     */
    public void setCredentialsFile(String credentialsFile) {
        this.credentialsFile = credentialsFile;
    }

    /**
     * <p>isEnabled.</p>
     *
     * @return true when a Google project is configured
     */
    public boolean isEnabled() {
        return (projectId != null) && !projectId.isBlank();
    }

    /**
     * Translates a plain text. A failure is logged and yields no translation, so that the text is never
     * sent to another endpoint than the configured one.
     *
     * @param text the text to translate
     * @param sourceLanguage the ISO 639-1 code of the text language
     * @param targetLanguage the ISO 639-1 code of the translation language
     * @return the translated text, empty when the translation failed
     */
    public Optional<String> translate(String text, String sourceLanguage, String targetLanguage) {
        try {
            ResponseEntity<String> response = restTemplate.exchange(buildTranslateUri(), HttpMethod.POST,
                    buildRequest(text, sourceLanguage, targetLanguage), String.class);
            return parseTranslatedText(response.getBody());
        } catch (RestClientException | JSONException | IllegalStateException e) {
            logger.error("Cloud Translation v3 request to " + endpoint + " failed: " + e.getMessage());
            return Optional.empty();
        }
    }

    URI buildTranslateUri() {
        return new DefaultUriBuilderFactory(endpoint).expand(TRANSLATE_PATH, projectId.trim(), location);
    }

    HttpEntity<String> buildRequest(String text, String sourceLanguage, String targetLanguage) {
        JSONObject body = new JSONObject();
        body.put(JSON_CONTENTS, new JSONArray().put(text));
        body.put(JSON_SOURCE_LANGUAGE_CODE, sourceLanguage);
        body.put(JSON_TARGET_LANGUAGE_CODE, targetLanguage);
        body.put(JSON_MIME_TYPE, MIME_TYPE_TEXT_PLAIN);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(new MediaType(MediaType.APPLICATION_JSON, StandardCharsets.UTF_8));
        headers.setBearerAuth(getTokenProvider().getAccessToken());
        return new HttpEntity<>(body.toString(), headers);
    }

    // Read on first use: an unreadable key fails the translation, not the startup of the repository
    private synchronized GoogleAccessTokenProvider getTokenProvider() {
        if (tokenProvider == null) {
            tokenProvider = (credentialsFile == null) || credentialsFile.isBlank() ? new GoogleMetadataTokenProvider()
                    : new GoogleServiceAccountTokenProvider(credentialsFile.trim());
        }
        return tokenProvider;
    }

    static Optional<String> parseTranslatedText(String responseBody) {
        JSONArray translations = new JSONObject(responseBody).optJSONArray(JSON_TRANSLATIONS);
        if ((translations == null) || translations.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(translations.getJSONObject(0).getString(JSON_TRANSLATED_TEXT));
    }
}

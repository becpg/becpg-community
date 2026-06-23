package fr.becpg.repo.regulatory.becpg.regulatory;

import com.nimbusds.oauth2.sdk.ClientCredentialsGrant;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.TokenRequest;
import com.nimbusds.oauth2.sdk.TokenResponse;
import com.nimbusds.oauth2.sdk.auth.ClientSecretBasic;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.token.AccessToken;
import fr.becpg.repo.authentication.BeCPGTicketService;
import fr.becpg.repo.system.SystemConfigurationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.time.Instant;
import java.util.Optional;

/**
 * This service encapsulates an authentication suite for becpg-regulatory.
 * Supports authentication via oauth2 token or becpg ticket.
 * Only one authentication method can be used at a time - depending on the {@code beCPG.regulatory.authMode} property.
 */
@Service
public class BecpgRegulatoryAuthenticationService {
    private static final Log logger = LogFactory.getLog(BecpgRegulatoryAuthenticationService.class);
    public static final String OAUTH_MODE = "oauth2";
    public static final String TICKET_MODE = "ticket";

    private final String mode;
    private final int buffer;
    private final TokenRequest.Builder standardRequestbuilder;

    private volatile String cachedAccessToken;
    private volatile Instant expiresAt;

    private final BeCPGTicketService beCPGTicketService;

    public BecpgRegulatoryAuthenticationService(SystemConfigurationService systemConfigurationService,
                                                BeCPGTicketService beCPGTicketService) throws Exception {
        this.beCPGTicketService = beCPGTicketService;
        this.mode = systemConfigurationService.confValue("beCPG.regulatory.authMode");
        this.buffer = Integer.getInteger(systemConfigurationService.confValue("beCPG.regulatory.oauth2.buffer"), 0);

        if (mode != null && mode.equalsIgnoreCase(OAUTH_MODE)) {
            String scope = systemConfigurationService.confValue("beCPG.regulatory.oauth2.scope");
            String tokenUrl = systemConfigurationService.confValue("beCPG.regulatory.oauth2.tokenUrl");
            String clientId = systemConfigurationService.confValue("beCPG.regulatory.oauth2.clientId");
            String clientSecret = systemConfigurationService.confValue("beCPG.regulatory.oauth2.clientSecret");
            this.standardRequestbuilder = new TokenRequest.Builder(
                    new URI(tokenUrl),
                    new ClientSecretBasic(new ClientID(clientId), new Secret(clientSecret)),
                    new ClientCredentialsGrant()
            );
            if (scope != null)
                this.standardRequestbuilder.scope(Scope.parse(scope));
        } else {
            this.standardRequestbuilder = null;
        }
    }

    /**
     * Returns the configured authentication mode for calls to the regulatory
     * service. Either {@code ticket} (delegated Alfresco ticket, default) or
     * {@code oauth2} (Keycloak client_credentials bearer).
     *
     * @return the lower-cased authentication mode, never null
     */
    private String authMode() {
        return mode != null && !mode.isBlank() ? mode.trim().toLowerCase() : TICKET_MODE;
    }

    private boolean isTicketMode() {
        return TICKET_MODE.equals(authMode());
    }

    private boolean isOAuth2Mode() {
        return OAUTH_MODE.equals(authMode());
    }

    /**
     * In oauth2 mode, returns a valid bearer token, using the cache when possible.
     * Refreshing the token when the cached one is about to expire.
     *
     * @return an {@link Optional} containing the access token, or empty if one
     * could not be obtained
     */
    public Optional<String> getOauth2Token() {
        if (!isOAuth2Mode())
            return Optional.empty();

        try {
            String token = cachedOauth2TokenIsValid() ? cachedAccessToken : fetchOauth2Token();
            if (token != null && !token.isBlank())
                return Optional.of(token);
        } catch (Exception e) {
            logger.error("Failed to obtain OAuth2 token: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    /**
     * In ticket mode is used to retrieve becpg ticket to be later passed as a header
     *
     * @return an {@link Optional} containing the becpg ticket, or empty if one
     * could not be obtained
     */
    public Optional<String> getBecpgTicket() {
        if (!isTicketMode())
            return Optional.empty();

        try {
            String ticket = beCPGTicketService.getCurrentAuthToken();
            if (ticket != null && !ticket.isBlank())
                return Optional.of(ticket);
        } catch (Exception e) {
            logger.warn("Unable to build BECPG_TICKET for regulatory request: " + e.getMessage());
        }
        return Optional.empty();
    }

    public void invalidateOauth2Token() {
        cachedAccessToken = null;
        expiresAt = null;
    }

    private boolean cachedOauth2TokenIsValid() {
        return cachedAccessToken != null && expiresAt != null && Instant.now().isBefore(expiresAt);
    }

    /**
     * Fetches a new token from Keycloak using the OAuth2 client_credentials flow.
     * Synchronized to prevent concurrent threads from issuing duplicate token
     * requests.
     *
     * @return the raw access token string
     * @throws Exception if the token endpoint returns an error, is unreachable or response is malformatted
     */
    private synchronized String fetchOauth2Token() throws Exception {
        // Re-check inside the lock: another thread may have refreshed while current waited
        if (cachedOauth2TokenIsValid())
            return cachedAccessToken;

        TokenResponse response = TokenResponse.parse(standardRequestbuilder.build().toHTTPRequest().send());
        if (!response.indicatesSuccess())
            throw new Exception("Failed to fetch OAuth2 token: " + response.toErrorResponse().getErrorObject().getDescription());

        AccessToken token = response.toSuccessResponse().getTokens().getAccessToken();
        cachedAccessToken = token.getValue();
        expiresAt = Instant.now().plusSeconds(token.getLifetime()).minusSeconds(buffer);

        logger.debug("OAuth2 token obtained, expires at " + expiresAt);
        return cachedAccessToken;
    }
}
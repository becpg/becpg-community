package fr.becpg.web.authentication;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.Instant;

import org.junit.Test;
import org.springframework.security.oauth2.core.OAuth2AccessToken;

/**
 * <p>BeCPGAIMSFilterTokenExpiryTest class.</p>
 *
 * Covers when the AIMS filter refreshes the access token: ahead of its expiry for a regular token, but only at its
 * real expiry for a token capped by the end of the SSO session, which used to be refreshed again on every request
 * of the last minute of the session (#37292).
 *
 * @author matthieu
 */
public class BeCPGAIMSFilterTokenExpiryTest {

    private static final Instant NOW = Instant.parse("2026-10-06T14:04:30Z");

    @Test
    public void regularTokenIsRefreshedAheadOfItsExpiry() {
        assertFalse(BeCPGAIMSFilter.isAuthTokenExpired(token(3600, 61), NOW));
        assertTrue(BeCPGAIMSFilter.isAuthTokenExpired(token(3600, 60), NOW));
        assertTrue(BeCPGAIMSFilter.isAuthTokenExpired(token(300, 30), NOW));
    }

    @Test
    public void tokenCappedByTheEndOfTheSessionIsRefreshedAtItsExpiryOnly() {
        assertFalse(BeCPGAIMSFilter.isAuthTokenExpired(token(54, 50), NOW));
        assertFalse(BeCPGAIMSFilter.isAuthTokenExpired(token(120, 1), NOW));
        assertTrue(BeCPGAIMSFilter.isAuthTokenExpired(token(54, 0), NOW));
        assertTrue(BeCPGAIMSFilter.isAuthTokenExpired(token(54, -5), NOW));
    }

    @Test
    public void tokenWithoutExpiryIsRefreshed() {
        assertTrue(BeCPGAIMSFilter.isAuthTokenExpired(new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", null, null), NOW));
    }

    @Test
    public void tokenWithoutIssueDateKeepsTheMargin() {
        assertTrue(BeCPGAIMSFilter.isAuthTokenExpired(
                new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", null, NOW.plusSeconds(50)), NOW));
    }

    /**
     * Builds a token issued for the given lifetime and expiring the given number of seconds after {@link #NOW}.
     */
    private static OAuth2AccessToken token(long lifetimeSeconds, long secondsLeft) {
        Instant expiresAt = NOW.plusSeconds(secondsLeft);
        return new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, "token", expiresAt.minusSeconds(lifetimeSeconds), expiresAt);
    }
}

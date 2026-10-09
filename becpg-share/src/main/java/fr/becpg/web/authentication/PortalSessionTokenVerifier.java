package fr.becpg.web.authentication;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * <p>PortalSessionTokenVerifier class.</p>
 *
 * Verifies the OIDC access token the supplier portal presents to
 * {@link fr.becpg.web.authentication.PortalSessionPost}.
 *
 * THE TRUST ANCHOR IS THE CONFIGURED ISSUER, NEVER THE TOKEN. Nothing read out of the token
 * is used to decide how to verify it: the issuer URL comes from the configuration, the
 * signing keys come from that issuer's discovery document, and the accepted algorithms are
 * pinned here. In particular the decoder never accepts <code>none</code> and never accepts an
 * HMAC algorithm — with a public JWKS, an HMAC-accepting decoder is a forgery oracle.
 *
 * The checks, in order — signature before any claim is read:
 *
 * <ol>
 *   <li>header <code>alg</code> is in the allow list, <code>kid</code> resolves in the JWKS,
 *       and the signature verifies;</li>
 *   <li><code>iss</code> equals the configured issuer, exactly;</li>
 *   <li><code>exp</code> is in the future and <code>nbf</code>, IF PRESENT, is in the past,
 *       both with the configured skew. Keycloak emits no <code>nbf</code>, so requiring one
 *       would reject every real token;</li>
 *   <li><code>aud</code> contains at least one configured audience. Note that
 *       <code>becpg-portal</code> is NOT in <code>aud</code> on a real Keycloak token — the
 *       client id is in <code>azp</code> — so the audience to configure is the resource
 *       (<code>inst1-openid</code>), not the portal's client id;</li>
 *   <li><code>azp</code> is in the configured allow list. This is the actual authorization
 *       decision: it is what stops another client of the same realm minting Share sessions;</li>
 *   <li>the <code>typ</code> claim is the configured token type, which rejects id tokens and
 *       refresh tokens;</li>
 *   <li>the username claim is present, plausible, and free of anything that could be smuggled
 *       into a header or a log line.</li>
 * </ol>
 *
 * A failure of 1 to 4, 6 or 7 is reported to the caller as a single indistinguishable
 * <code>invalid_token</code>; the specific reason goes to the log. A failure of 5 is
 * <code>unauthorized_client</code>. An unreachable identity provider is a 503, never a 401:
 * the caller did nothing wrong and must retry, not re-authenticate.
 *
 * @author matthieu
 * @since 26.1.0.42
 */
public class PortalSessionTokenVerifier {

	/** Constant <code>LOGGER</code> */
	private static final Log LOGGER = LogFactory.getLog(PortalSessionTokenVerifier.class);

	/** Constant <code>CLAIM_AUTHORIZED_PARTY="azp"</code> */
	private static final String CLAIM_AUTHORIZED_PARTY = "azp";

	/** Constant <code>CLAIM_TOKEN_TYPE="typ"</code> */
	private static final String CLAIM_TOKEN_TYPE = "typ";

	/** A username longer than this is not a username, it is an attack. */
	private static final int MAX_USERNAME_LENGTH = 256;

	private final PortalSessionConfig config;

	/**
	 * Built on first use, never in a constructor or an init method: {@code withIssuerLocation}
	 * fetches the discovery document, and doing that at startup would make Share's startup
	 * depend on the identity provider being up. Only ever assigned once a decoder has been
	 * built successfully, so a transient outage is retried rather than cached.
	 */
	private volatile JwtDecoder decoder;

	/**
	 * <p>Constructor for PortalSessionTokenVerifier.</p>
	 *
	 * @param config a {@link fr.becpg.web.authentication.PortalSessionConfig} object
	 */
	public PortalSessionTokenVerifier(PortalSessionConfig config) {
		this.config = config;
	}

	/**
	 * Verifies a bearer token and returns the username it is for.
	 *
	 * @param token the raw compact JWT
	 * @return the verified username, never null and never taken from anything the caller can
	 *         set other than the signed token itself
	 * @throws fr.becpg.web.authentication.PortalSessionException on any refusal
	 */
	public String verify(String token) throws PortalSessionException {
		if (token == null || token.isEmpty()) {
			throw new PortalSessionException(401, PortalSessionException.ERROR_INVALID_TOKEN, "empty bearer token");
		}

		Jwt jwt = decode(token);

		// Check 4 - audience. An empty configured set cannot happen (PortalSessionConfig
		// refuses to be usable without one) but it is re-asserted here so that this method
		// stays safe if it is ever called with a hand built configuration.
		List<String> audience = jwt.getAudience();
		if (config.getAudiences().isEmpty() || audience == null || audience.stream().noneMatch(config.getAudiences()::contains)) {
			throw refuse(401, PortalSessionException.ERROR_INVALID_TOKEN, "audience mismatch, token aud=" + audience + " expected one of "
					+ config.getAudiences());
		}

		// Check 5 - authorized party. The real authorization decision.
		String authorizedParty = jwt.getClaimAsString(CLAIM_AUTHORIZED_PARTY);
		if (authorizedParty == null || !config.getAuthorizedParties().contains(authorizedParty)) {
			throw refuse(403, PortalSessionException.ERROR_UNAUTHORIZED_CLIENT, "authorized party '" + authorizedParty
					+ "' is not allowed to mint Share sessions, expected one of " + config.getAuthorizedParties());
		}

		// Check 6 - token type. Rejects id tokens, whose typ is ID.
		String tokenType = jwt.getClaimAsString(CLAIM_TOKEN_TYPE);
		if (!config.getTokenType().equals(tokenType)) {
			throw refuse(401, PortalSessionException.ERROR_INVALID_TOKEN, "token type claim is '" + tokenType + "', expected '"
					+ config.getTokenType() + "'");
		}

		// Check 7 - the username, read from the SIGNED token and from nowhere else.
		return readUsername(jwt);
	}

	private Jwt decode(String token) throws PortalSessionException {
		JwtDecoder jwtDecoder = getDecoder();
		try {
			return jwtDecoder.decode(token);
		} catch (BadJwtException e) {
			// Malformed, wrong signature, unknown kid, expired, wrong issuer: the token is
			// bad and the caller must get a new one.
			throw refuse(401, PortalSessionException.ERROR_INVALID_TOKEN, "token rejected: " + e.getMessage(), e);
		} catch (JwtException e) {
			// Everything else a JwtDecoder throws is an infrastructure problem - most often
			// the JWKS endpoint being unreachable. Answering 401 here would send the portal
			// off to re-authenticate a user whose token is perfectly good.
			throw refuse(503, PortalSessionException.ERROR_IDENTITY_PROVIDER_UNAVAILABLE, "could not verify the token against " + config.getIssuer()
					+ ": " + e.getMessage(), e);
		}
	}

	/**
	 * Builds the decoder, once, and keeps it. The Nimbus remote JWK set behind it caches the
	 * keys, refreshes them when an unknown <code>kid</code> shows up, and rate limits those
	 * refreshes — which is exactly the JWKS caching policy we want and the reason not to
	 * hand roll one.
	 */
	private JwtDecoder getDecoder() throws PortalSessionException {
		JwtDecoder current = this.decoder;
		if (current != null) {
			return current;
		}
		synchronized (this) {
			if (this.decoder != null) {
				return this.decoder;
			}
			try {
				NimbusJwtDecoder built = NimbusJwtDecoder.withIssuerLocation(config.getIssuer()).jwsAlgorithms(algorithms -> {
					algorithms.clear();
					algorithms.add(SignatureAlgorithm.RS256);
					algorithms.add(SignatureAlgorithm.RS512);
					algorithms.add(SignatureAlgorithm.ES256);
				}).build();

				built.setJwtValidator(buildValidator());

				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("beCPG portal session: token decoder built for issuer " + config.getIssuer());
				}

				this.decoder = built;
				return built;
			} catch (Exception e) { // NOSONAR - discovery can fail in a dozen ways, all of them a 503
				throw refuse(503, PortalSessionException.ERROR_IDENTITY_PROVIDER_UNAVAILABLE,
						"could not read the OpenID configuration of " + config.getIssuer() + ": " + e.getMessage(), e);
			}
		}
	}

	private OAuth2TokenValidator<Jwt> buildValidator() {
		JwtTimestampValidator timestamps = new JwtTimestampValidator(Duration.ofSeconds(config.getClockSkewSeconds()));
		// exp is mandatory - a token that never expires must not open a session.
		timestamps.setAllowEmptyExpiryClaim(false);
		// nbf is optional - Keycloak does not emit one.
		timestamps.setAllowEmptyNotBeforeClaim(true);

		List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
		validators.add(new JwtIssuerValidator(config.getIssuer()));
		validators.add(timestamps);
		return new DelegatingOAuth2TokenValidator<>(validators);
	}

	private String readUsername(Jwt jwt) throws PortalSessionException {
		String username = jwt.getClaimAsString(config.getPrincipalAttribute());
		if (username == null || username.trim().isEmpty()) {
			throw refuse(401, PortalSessionException.ERROR_INVALID_TOKEN, "no '" + config.getPrincipalAttribute() + "' claim in the token");
		}
		username = username.trim();
		if (username.length() > MAX_USERNAME_LENGTH) {
			throw refuse(401, PortalSessionException.ERROR_INVALID_TOKEN, "'" + config.getPrincipalAttribute() + "' claim is "
					+ username.length() + " characters long");
		}
		for (int i = 0; i < username.length(); i++) {
			char c = username.charAt(i);
			// CR, LF and NUL would let a signed claim inject a header or forge a log line;
			// the rest of the C0 range has no business in a username either.
			if (c < 0x20 || c == 0x7f) {
				throw refuse(401, PortalSessionException.ERROR_INVALID_TOKEN, "'" + config.getPrincipalAttribute()
						+ "' claim contains a control character at position " + i);
			}
		}
		return username;
	}

	/**
	 * Logs the real reason and returns the exception to throw. Refusals are logged at WARN so
	 * an operator can see them without turning on debug, and the token itself is never part
	 * of the message.
	 */
	private PortalSessionException refuse(int status, String errorCode, String reason) {
		return refuse(status, errorCode, reason, null);
	}

	private PortalSessionException refuse(int status, String errorCode, String reason, Throwable cause) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("beCPG portal session: refused (" + status + " " + errorCode + ") - " + reason);
		}
		return cause != null ? new PortalSessionException(status, errorCode, reason, cause) : new PortalSessionException(status, errorCode, reason);
	}

}

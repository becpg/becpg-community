package fr.becpg.web.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.nimbusds.jwt.JWTClaimsSet;

/**
 * <p>PortalSessionTokenVerifierTest class.</p>
 *
 * Exercises the eight checks of {@link fr.becpg.web.authentication.PortalSessionTokenVerifier}
 * against a real signed token, a real JWKS and a real discovery document — no mocked decoder,
 * because a mocked decoder would test the mock rather than the verification.
 *
 * The refusal paths are the point of this class: a verifier that accepts a valid token proves
 * almost nothing, one that refuses an expired, mis-audienced, mis-issued or forged token proves
 * what matters.
 *
 * @author matthieu
 */
public class PortalSessionTokenVerifierTest {

	private PortalSessionTestKeycloak keycloak;

	private PortalSessionConfig config;

	private PortalSessionTokenVerifier verifier;

	@Before
	public void setUp() throws Exception {
		keycloak = new PortalSessionTestKeycloak();
		config = config(keycloak.issuer());
		verifier = new PortalSessionTokenVerifier(config);
	}

	@After
	public void tearDown() {
		if (keycloak != null) {
			keycloak.close();
		}
	}

	private PortalSessionConfig config(String issuer) {
		Map<String, String> values = new HashMap<>();
		values.put(PortalSessionConfig.KEY_ENABLED, "true");
		values.put(PortalSessionConfig.KEY_ISSUER, issuer);
		values.put(PortalSessionConfig.KEY_AUDIENCES, PortalSessionTestKeycloak.AUDIENCE);
		values.put(PortalSessionConfig.KEY_AUTHORIZED_PARTIES, PortalSessionTestKeycloak.CLIENT_ID);
		values.put(PortalSessionConfig.KEY_SHARED_SECRET, "0123456789012345678901234567890123456789");
		values.put(PortalSessionConfig.KEY_ALLOWED_ADDRESSES, "127.0.0.1");
		return new PortalSessionConfig(values);
	}

	private PortalSessionException refusalOf(String token) {
		try {
			String username = verifier.verify(token);
			fail("expected a refusal, got a session for " + username);
			return null;
		} catch (PortalSessionException e) {
			return e;
		}
	}

	@Test
	public void acceptsARealisticKeycloakAccessToken() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().build());

		assertEquals(PortalSessionTestKeycloak.USERNAME, verifier.verify(token));
	}

	/**
	 * The shape that trips up a naive implementation: the portal's client id is in
	 * <code>azp</code> and is NOT one of the audiences. Requiring it in <code>aud</code> would
	 * reject every real token, so this asserts we do not.
	 */
	@Test
	public void acceptsATokenWhoseAudienceDoesNotContainTheClientId() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().build());

		assertTrue(!List.of(PortalSessionTestKeycloak.AUDIENCE, "account").contains(PortalSessionTestKeycloak.CLIENT_ID));
		assertEquals(PortalSessionTestKeycloak.USERNAME, verifier.verify(token));
	}

	/** Keycloak emits no nbf. A verifier that demands one refuses every real token. */
	@Test
	public void acceptsATokenWithNoNotBeforeClaim() throws Exception {
		JWTClaimsSet claims = keycloak.validClaims().build();

		assertEquals(null, claims.getNotBeforeTime());
		assertEquals(PortalSessionTestKeycloak.USERNAME, verifier.verify(keycloak.sign(claims)));
	}

	@Test
	public void refusesAnExpiredToken() throws Exception {
		Instant past = Instant.now().minusSeconds(3600);
		String token = keycloak.sign(keycloak.validClaims().issueTime(Date.from(past)).expirationTime(Date.from(past.plusSeconds(300))).build());

		PortalSessionException refusal = refusalOf(token);

		assertEquals(401, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_INVALID_TOKEN, refusal.getErrorCode());
	}

	/** Expiry must be tolerated only within the configured skew, not beyond it. */
	@Test
	public void acceptsATokenThatExpiredWithinTheConfiguredSkew() throws Exception {
		Instant now = Instant.now();
		String token = keycloak.sign(keycloak.validClaims().issueTime(Date.from(now.minusSeconds(30)))
				.expirationTime(Date.from(now.minusSeconds(10))).build());

		assertEquals(60L, config.getClockSkewSeconds());
		assertEquals(PortalSessionTestKeycloak.USERNAME, verifier.verify(token));
	}

	@Test
	public void refusesATokenWithNoExpiry() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().expirationTime(null).build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	@Test
	public void refusesATokenNotYetValid() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().notBeforeTime(Date.from(Instant.now().plusSeconds(3600))).build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	@Test
	public void refusesAWrongAudience() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().audience(List.of("some-other-resource", "account")).build());

		PortalSessionException refusal = refusalOf(token);

		assertEquals(401, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_INVALID_TOKEN, refusal.getErrorCode());
	}

	@Test
	public void refusesAMissingAudience() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().audience((String) null).build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	/**
	 * A token minted by the RIGHT issuer for a DIFFERENT realm, or one whose iss merely starts
	 * with the configured value: the comparison must be exact, never a prefix.
	 */
	@Test
	public void refusesAWrongIssuer() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().issuer(keycloak.issuer() + "-evil").build());

		PortalSessionException refusal = refusalOf(token);

		assertEquals(401, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_INVALID_TOKEN, refusal.getErrorCode());
	}

	@Test
	public void refusesATamperedSignature() throws Exception {
		String token = PortalSessionTestKeycloak.tamper(keycloak.sign(keycloak.validClaims().build()));

		PortalSessionException refusal = refusalOf(token);

		assertEquals(401, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_INVALID_TOKEN, refusal.getErrorCode());
	}

	/**
	 * A perfectly formed token, correctly signed, with the right kid — by a key the issuer
	 * never published. This is the forgery that a decoder trusting the token's own key material
	 * would accept.
	 */
	@Test
	public void refusesATokenSignedByAKeyTheIssuerNeverPublished() throws Exception {
		String token = keycloak.signWithForeignKey(keycloak.validClaims().build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	/** An unsigned token must never be treated as a token. */
	@Test
	public void refusesAnAlgNoneToken() {
		String header = java.util.Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"alg\":\"none\",\"typ\":\"JWT\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8));
		String payload = java.util.Base64.getUrlEncoder().withoutPadding()
				.encodeToString(("{\"iss\":\"" + keycloak.issuer() + "\",\"aud\":\"" + PortalSessionTestKeycloak.AUDIENCE
						+ "\",\"azp\":\"" + PortalSessionTestKeycloak.CLIENT_ID + "\",\"typ\":\"Bearer\",\"preferred_username\":\""
						+ PortalSessionTestKeycloak.USERNAME + "\",\"exp\":" + (Instant.now().getEpochSecond() + 300) + "}")
						.getBytes(java.nio.charset.StandardCharsets.UTF_8));

		assertEquals(401, refusalOf(header + "." + payload + ".").getStatus());
	}

	/**
	 * A valid user token from ANOTHER client of the same realm. This is the check that stops
	 * any application in the realm turning a user's token into a Share session, and it is an
	 * authorization failure, not an authentication one.
	 */
	@Test
	public void refusesAnUnauthorizedParty() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("azp", "some-other-client").build());

		PortalSessionException refusal = refusalOf(token);

		assertEquals(403, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_UNAUTHORIZED_CLIENT, refusal.getErrorCode());
	}

	@Test
	public void refusesAMissingAuthorizedParty() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("azp", null).build());

		assertEquals(403, refusalOf(token).getStatus());
	}

	/** An id token has typ ID. It must not be tradeable for a session. */
	@Test
	public void refusesAnIdToken() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("typ", "ID").build());

		PortalSessionException refusal = refusalOf(token);

		assertEquals(401, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_INVALID_TOKEN, refusal.getErrorCode());
	}

	@Test
	public void refusesATokenWithNoTypeClaim() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("typ", null).build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	@Test
	public void refusesATokenWithNoUsernameClaim() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("preferred_username", null).build());

		PortalSessionException refusal = refusalOf(token);

		assertEquals(401, refusal.getStatus());
		assertEquals(PortalSessionException.ERROR_INVALID_TOKEN, refusal.getErrorCode());
	}

	@Test
	public void refusesABlankUsernameClaim() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("preferred_username", "   ").build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	/**
	 * The username ends up in session attributes, in connector headers and in log lines. A CR
	 * or an LF in it, even one that is correctly signed, is a header injection and a forged log
	 * line waiting to happen.
	 */
	@Test
	public void refusesAUsernameCarryingAControlCharacter() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("preferred_username", "admin\r\nX-Alfresco-Remote-User: admin").build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	@Test
	public void refusesAnAbsurdlyLongUsername() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("preferred_username", "a".repeat(257)).build());

		assertEquals(401, refusalOf(token).getStatus());
	}

	@Test
	public void refusesAnEmptyBearer() {
		assertEquals(401, refusalOf("").getStatus());
		assertEquals(401, refusalOf(null).getStatus());
	}

	@Test
	public void refusesGarbage() {
		assertEquals(401, refusalOf("not-a-jwt").getStatus());
	}

	/**
	 * An unreachable identity provider is an infrastructure failure, not a bad token: answering
	 * 401 would send the portal off to re-authenticate a user whose token is perfectly good.
	 */
	@Test
	public void reportsAnUnreachableIdentityProviderAsUnavailableNotUnauthorized() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().build());
		String issuer = keycloak.issuer();
		keycloak.close();

		PortalSessionTokenVerifier offline = new PortalSessionTokenVerifier(config(issuer));

		try {
			offline.verify(token);
			fail("expected a refusal");
		} catch (PortalSessionException e) {
			assertEquals(PortalSessionPost.STATUS_SERVICE_UNAVAILABLE, e.getStatus());
			assertEquals(PortalSessionException.ERROR_IDENTITY_PROVIDER_UNAVAILABLE, e.getErrorCode());
		}
	}

	/**
	 * The keys must be fetched once and reused. Without caching, every wizard step would cost a
	 * round trip to the identity provider.
	 */
	@Test
	public void fetchesTheJwksOnceAndCachesIt() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().build());

		for (int i = 0; i < 5; i++) {
			assertEquals(PortalSessionTestKeycloak.USERNAME, verifier.verify(token));
		}

		assertEquals(1, keycloak.jwksHits());
	}

	/**
	 * A failed decoder build must NOT be cached, or a single blip while the identity provider
	 * restarts would leave the endpoint broken until Share is restarted.
	 */
	@Test
	public void recoversOnceTheIdentityProviderIsBackUp() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().build());

		keycloak.setAvailable(false);
		assertEquals(PortalSessionPost.STATUS_SERVICE_UNAVAILABLE, refusalOf(token).getStatus());

		keycloak.setAvailable(true);
		assertEquals(PortalSessionTestKeycloak.USERNAME, verifier.verify(token));
	}

}

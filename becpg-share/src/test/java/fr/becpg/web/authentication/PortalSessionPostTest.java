package fr.becpg.web.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import jakarta.servlet.http.HttpServletRequest;

/**
 * <p>PortalSessionPostTest class.</p>
 *
 * Covers everything the endpoint refuses BEFORE it touches a session, which is where the
 * security of the feature lives. The mint itself needs the whole Surf stack — connector
 * service, request context, user factory — and is verified against a running Share rather than
 * against mocks of all three.
 *
 * The first assertion is the one that matters most for existing installs: with no
 * configuration, this is a 404 that never so much as looks at the connector service.
 *
 * @author matthieu
 */
public class PortalSessionPostTest {

	private static final String SECRET = "0123456789012345678901234567890123456789";

	private static final String PEER = "10.0.0.7";

	private PortalSessionTestKeycloak keycloak;

	private ConnectorService connectorService;

	private WebScriptResponse response;

	private StringWriter body;

	@Before
	public void setUp() throws Exception {
		keycloak = new PortalSessionTestKeycloak();
		connectorService = mock(ConnectorService.class);
		response = mock(WebScriptResponse.class);
		body = new StringWriter();
		when(response.getWriter()).thenReturn(body);
	}

	@After
	public void tearDown() {
		if (keycloak != null) {
			keycloak.close();
		}
	}

	private PortalSessionPost endpoint(ConfigService configService) {
		PortalSessionPost webScript = new PortalSessionPost();
		webScript.setConfigService(configService);
		webScript.setConnectorService(connectorService);
		return webScript;
	}

	private Map<String, String> enabledValues() {
		Map<String, String> values = new HashMap<>();
		values.put(PortalSessionConfig.KEY_ENABLED, "true");
		values.put(PortalSessionConfig.KEY_ISSUER, keycloak.issuer());
		values.put(PortalSessionConfig.KEY_AUDIENCES, PortalSessionTestKeycloak.AUDIENCE);
		values.put(PortalSessionConfig.KEY_AUTHORIZED_PARTIES, PortalSessionTestKeycloak.CLIENT_ID);
		values.put(PortalSessionConfig.KEY_SHARED_SECRET, SECRET);
		values.put(PortalSessionConfig.KEY_ALLOWED_ADDRESSES, PEER);
		return values;
	}

	private ConfigService configServiceReturning(Map<String, String> elements) {
		Config config = mock(Config.class);
		for (Map.Entry<String, String> entry : elements.entrySet()) {
			ConfigElement element = mock(ConfigElement.class);
			when(element.getValue()).thenReturn(entry.getValue());
			when(config.getConfigElement(entry.getKey())).thenReturn(element);
		}
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig(PortalSessionConfig.CONFIG_CONDITION)).thenReturn(config);
		return configService;
	}

	private WebScriptServletRequest request(String method, String remoteAddress, String portalKey, String authorization) {
		HttpServletRequest httpRequest = mock(HttpServletRequest.class);
		when(httpRequest.getMethod()).thenReturn(method);
		when(httpRequest.getRemoteAddr()).thenReturn(remoteAddress);
		when(httpRequest.getHeader(PortalSessionPost.HEADER_PORTAL_KEY)).thenReturn(portalKey);
		when(httpRequest.getHeader("Authorization")).thenReturn(authorization);

		WebScriptServletRequest request = mock(WebScriptServletRequest.class);
		when(request.getHttpServletRequest()).thenReturn(httpRequest);
		return request;
	}

	private WebScriptServletRequest validRequest(String authorization) {
		return request("POST", PEER, SECRET, authorization);
	}

	/* ------------------------------------------------------------------ */
	/* Off by default                                                      */
	/* ------------------------------------------------------------------ */

	/**
	 * The state every existing install is in. Nothing must happen: not a token decoded, not a
	 * connector opened, not a session created.
	 */
	@Test
	public void answers404AndTouchesNothingWhenTheFeatureIsNotConfigured() throws Exception {
		ConfigService configService = mock(ConfigService.class);
		when(configService.getConfig(anyString())).thenReturn(null);

		endpoint(configService).execute(validRequest("Bearer whatever"), response);

		verify(response).setStatus(404);
		verify(connectorService, never()).getConnector(anyString(), anyString(), any());
	}

	/** Enabled but half configured is still off — never "enabled with a check relaxed". */
	@Test
	public void answers404WhenEnabledButIncompletelyConfigured() throws Exception {
		Map<String, String> values = enabledValues();
		values.remove(PortalSessionConfig.KEY_AUDIENCES);

		endpoint(configServiceReturning(values)).execute(validRequest("Bearer " + keycloak.sign(keycloak.validClaims().build())), response);

		verify(response).setStatus(404);
		verify(connectorService, never()).getConnector(anyString(), anyString(), any());
	}

	@Test
	public void answers404WhenTheFeatureIsExplicitlyDisabled() throws Exception {
		Map<String, String> values = enabledValues();
		values.put(PortalSessionConfig.KEY_ENABLED, "false");

		endpoint(configServiceReturning(values)).execute(validRequest("Bearer " + keycloak.sign(keycloak.validClaims().build())), response);

		verify(response).setStatus(404);
	}

	/* ------------------------------------------------------------------ */
	/* Portal-server-only, once the feature is on                          */
	/* ------------------------------------------------------------------ */

	@Test
	public void refusesAnythingButPost() throws Exception {
		endpoint(configServiceReturning(enabledValues())).execute(request("GET", PEER, SECRET, "Bearer x"), response);

		verify(response).setStatus(405);
		verify(connectorService, never()).getConnector(anyString(), anyString(), any());
	}

	@Test
	public void refusesAPeerThatIsNotInTheAllowList() throws Exception {
		endpoint(configServiceReturning(enabledValues()))
				.execute(request("POST", "203.0.113.9", SECRET, "Bearer " + keycloak.sign(keycloak.validClaims().build())), response);

		verify(response).setStatus(403);
		assertTrue(body.toString().contains(PortalSessionException.ERROR_UNAUTHORIZED_CLIENT));
	}

	/**
	 * A valid user token is NOT enough on its own: anyone holding a supplier's access token
	 * could otherwise turn it into a Share session.
	 */
	@Test
	public void refusesAValidTokenWithoutTheSharedSecret() throws Exception {
		endpoint(configServiceReturning(enabledValues()))
				.execute(request("POST", PEER, null, "Bearer " + keycloak.sign(keycloak.validClaims().build())), response);

		verify(response).setStatus(403);
		verify(connectorService, never()).getConnector(anyString(), anyString(), any());
	}

	@Test
	public void refusesAWrongSharedSecret() throws Exception {
		endpoint(configServiceReturning(enabledValues()))
				.execute(request("POST", PEER, SECRET + "x", "Bearer " + keycloak.sign(keycloak.validClaims().build())), response);

		verify(response).setStatus(403);
	}

	/* ------------------------------------------------------------------ */
	/* Token refusals, as seen from the wire                               */
	/* ------------------------------------------------------------------ */

	@Test
	public void refusesARequestWithNoBearer() throws IOException {
		endpoint(configServiceReturning(enabledValues())).execute(validRequest(null), response);

		verify(response).setStatus(400);
		assertTrue(body.toString().contains(PortalSessionException.ERROR_INVALID_REQUEST));
	}

	@Test
	public void refusesARequestWhoseAuthorizationIsNotABearer() throws IOException {
		endpoint(configServiceReturning(enabledValues())).execute(validRequest("Basic YWRtaW46YWRtaW4="), response);

		verify(response).setStatus(400);
	}

	/** A bad token is a 401 with a challenge, and the body says nothing else. */
	@Test
	public void refusesABadTokenWithAnOpaqueChallenge() throws Exception {
		endpoint(configServiceReturning(enabledValues())).execute(validRequest("Bearer not-a-jwt"), response);

		verify(response).setStatus(401);
		verify(response).setHeader("WWW-Authenticate", "Bearer error=\"invalid_token\"");
		assertEquals("{\"error\":\"invalid_token\"}", body.toString());
		verify(connectorService, never()).getConnector(anyString(), anyString(), any());
	}

	@Test
	public void refusesATokenFromAnotherClientOfTheSameRealm() throws Exception {
		String token = keycloak.sign(keycloak.validClaims().claim("azp", "some-other-client").build());

		endpoint(configServiceReturning(enabledValues())).execute(validRequest("Bearer " + token), response);

		verify(response).setStatus(403);
		assertTrue(body.toString().contains(PortalSessionException.ERROR_UNAUTHORIZED_CLIENT));
		verify(connectorService, never()).getConnector(anyString(), anyString(), any());
	}

	/** No response ever carries a cache header that would let a proxy keep a session. */
	@Test
	public void neverAllowsARefusalToBeCached() throws IOException {
		endpoint(configServiceReturning(enabledValues())).execute(validRequest("Bearer not-a-jwt"), response);

		verify(response).setHeader("Cache-Control", "no-store");
	}

	/* ------------------------------------------------------------------ */
	/* Mint budget                                                         */
	/* ------------------------------------------------------------------ */

	/**
	 * A leaked shared secret must not be usable to flood Share. The budget is charged before
	 * the token is parsed, so it also caps the signature-verification work an attacker can buy.
	 */
	@Test
	public void refusesOnceTheGlobalMintBudgetIsExhausted() throws IOException {
		Map<String, String> values = enabledValues();
		values.put(PortalSessionConfig.KEY_MAX_MINTS_PER_MINUTE, "2");
		PortalSessionPost webScript = endpoint(configServiceReturning(values));

		webScript.execute(validRequest("Bearer not-a-jwt"), response);
		webScript.execute(validRequest("Bearer not-a-jwt"), response);

		WebScriptResponse third = mock(WebScriptResponse.class);
		StringWriter thirdBody = new StringWriter();
		when(third.getWriter()).thenReturn(thirdBody);

		webScript.execute(validRequest("Bearer not-a-jwt"), third);

		verify(third).setStatus(429);
		assertTrue(thirdBody.toString().contains(PortalSessionException.ERROR_RATE_LIMITED));
	}

}

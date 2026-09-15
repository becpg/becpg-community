package fr.becpg.web.authentication;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.extensions.webscripts.servlet.WebScriptServletRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * <p>PortalSessionPost class.</p>
 *
 * Trades a verified OIDC access token for a Share session, for the supplier portal only.
 *
 * WHY THIS EXISTS. The portal resolves each wizard step's form definition through Share,
 * because beCPG's form resolution cascade is Share's and nothing else can run it. The
 * <code>node-type</code> evaluator behind it calls <code>/api/metadata</code> through the
 * SESSION connector, so with no Share session it silently reports "no config" and entity form
 * steps come back empty. The portal holds a perfectly good access token and no session, and
 * {@code BeCPGAIMSFilter} has no incoming-bearer branch — it only understands an authorization
 * RESPONSE or an existing security context. This endpoint is the missing branch, kept out of
 * that filter so the filter's behaviour is untouched.
 *
 * OFF BY DEFAULT. Nothing here runs unless <code>becpg.portal.session.enabled</code> is set to
 * <code>true</code> (system property, or the <code>&lt;enabled&gt;</code> element of the
 * <code>BeCPGPortalSession</code> config block) AND every mandatory value is configured. Until
 * then this is a 404 that touches no bean, builds no decoder and opens no session. See
 * {@link fr.becpg.web.authentication.PortalSessionConfig}.
 *
 * MOUNTING. The descriptor is <code>becpg/portal/session</code>, so the endpoint is reachable
 * both at <code>/share/service/becpg/portal/session</code> and, preferably, at
 * <code>/share/noauth/becpg/portal/session</code>. Both rewrite to <code>/page/$1</code>, but
 * <code>/noauth/*</code> is mapped to the UrlRewriteFilter ALONE, so it bypasses the AIMS
 * filter, the SSO filter and the CSRF filter in every deployment mode. That is safe here only
 * because this endpoint derives no authority whatsoever from cookies: it never reads one,
 * never trusts {@code getRemoteUser()}, and its entire authority comes from a signed token plus
 * a shared secret. Keep it that way.
 *
 * WHAT IT DELIBERATELY DOES NOT WRITE. <code>SPRING_SECURITY_CONTEXT</code> is not set. With
 * AIMS on, a session minted here therefore cannot browse <code>/share/page/*</code> — the AIMS
 * filter finds no security context and bounces it to the login page. The session is good for
 * <code>/share/service/*</code> API calls and nothing else. That containment is free, so we
 * take it. With AIMS off the page gate is satisfied by the credential vault alone and this
 * containment does not apply, which is why the cookie must stay server side in the portal.
 *
 * @author matthieu
 */
public class PortalSessionPost extends AbstractWebScript {

	/** Constant <code>LOGGER</code> */
	private static final Log LOGGER = LogFactory.getLog(PortalSessionPost.class);

	/** Constant <code>HEADER_PORTAL_KEY="X-beCPG-Portal-Key"</code> */
	public static final String HEADER_PORTAL_KEY = "X-beCPG-Portal-Key";

	/** Constant <code>HEADER_AUTHORIZATION="Authorization"</code> */
	private static final String HEADER_AUTHORIZATION = "Authorization";

	/** Constant <code>BEARER_PREFIX="Bearer "</code> */
	private static final String BEARER_PREFIX = "Bearer ";

	/** Constant <code>ALFRESCO_ENDPOINT_ID="alfresco"</code> */
	private static final String ALFRESCO_ENDPOINT_ID = "alfresco";

	/** Constant <code>ALFRESCO_API_ENDPOINT_ID="alfresco-api"</code> */
	private static final String ALFRESCO_API_ENDPOINT_ID = "alfresco-api";

	/** Same call {@code BeCPGAIMSFilter.getAlfTicket} makes. */
	private static final String TICKET_URI = "/-default-/public/authentication/versions/1/tickets/-me-?noCache=";

	/** Constant <code>JSON_CONTENT_TYPE="application/json;charset=UTF-8"</code> */
	private static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

	/** Constant <code>STATUS_TOO_MANY_REQUESTS=429</code> — not declared by Surf's Status. */
	private static final int STATUS_TOO_MANY_REQUESTS = 429;

	/** Constant <code>STATUS_BAD_GATEWAY=502</code> — not declared by Surf's Status. */
	private static final int STATUS_BAD_GATEWAY = 502;

	/** Constant <code>STATUS_SERVICE_UNAVAILABLE=503</code> — not declared by Surf's Status. */
	static final int STATUS_SERVICE_UNAVAILABLE = 503;

	private ConfigService configService;

	private ConnectorService connectorService;

	/** Read once, on the first request, so that a disabled deployment pays nothing at startup. */
	private volatile PortalSessionConfig config;

	private volatile PortalSessionTokenVerifier verifier;

	private final AtomicBoolean misconfigurationLogged = new AtomicBoolean(false);

	private final PortalSessionMintBudget budget = new PortalSessionMintBudget();

	/**
	 * <p>Setter for the field <code>configService</code>.</p>
	 *
	 * @param configService a {@link org.springframework.extensions.config.ConfigService} object
	 */
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	/**
	 * <p>Setter for the field <code>connectorService</code>.</p>
	 *
	 * @param connectorService a {@link org.springframework.extensions.webscripts.connector.ConnectorService} object
	 */
	public void setConnectorService(ConnectorService connectorService) {
		this.connectorService = connectorService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		PortalSessionConfig current = resolveConfig();

		if (!current.isUsable()) {
			logMisconfigurationOnce(current);
			refuse(res, Status.STATUS_NOT_FOUND, "not_found", false);
			return;
		}

		HttpServletRequest httpRequest = httpRequest(req);
		if (httpRequest == null) {
			LOGGER.error("beCPG portal session: no HttpServletRequest available, cannot mint a session.");
			refuse(res, Status.STATUS_INTERNAL_SERVER_ERROR, PortalSessionException.ERROR_REPOSITORY_UNAVAILABLE, false);
			return;
		}

		try {
			Authorized authorized = authorize(current, httpRequest);
			mint(current, httpRequest, res, authorized);
		} catch (PortalSessionException e) {
			refuse(res, e.getStatus(), e.getErrorCode(), e.getStatus() == Status.STATUS_UNAUTHORIZED);
		}
	}

	/** The verified caller: a username that came out of a signed token, and that token. */
	private record Authorized(String username, String accessToken) {
	}

	/**
	 * Everything that must hold before a single session attribute is written: the caller is the
	 * portal, and the token is that of a real user of an allowed client.
	 *
	 * @return the verified username and the token it came from
	 */
	private Authorized authorize(PortalSessionConfig current, HttpServletRequest httpRequest) throws PortalSessionException {
		if (!"POST".equalsIgnoreCase(httpRequest.getMethod())) {
			throw refusal(Status.STATUS_METHOD_NOT_ALLOWED, PortalSessionException.ERROR_INVALID_REQUEST, "method " + httpRequest.getMethod()
					+ " is not allowed");
		}

		// Layer 1 - the direct peer. Never X-Forwarded-For: it is caller controlled.
		String remoteAddress = httpRequest.getRemoteAddr();
		if (!current.allowsRemoteAddress(remoteAddress)) {
			throw refusal(Status.STATUS_FORBIDDEN, PortalSessionException.ERROR_UNAUTHORIZED_CLIENT, "peer " + remoteAddress + " is not in "
					+ PortalSessionConfig.PROPERTY_PREFIX + PortalSessionConfig.KEY_ALLOWED_ADDRESSES);
		}

		// Layer 2 - the shared secret, compared in constant time.
		if (!current.matchesSharedSecret(httpRequest.getHeader(HEADER_PORTAL_KEY))) {
			throw refusal(Status.STATUS_FORBIDDEN, PortalSessionException.ERROR_UNAUTHORIZED_CLIENT, "missing or wrong " + HEADER_PORTAL_KEY
					+ " from " + remoteAddress);
		}

		// Charged before the token is even parsed, so that signature verification cannot be
		// used as a workload amplifier.
		if (!budget.tryConsume(null, current.getMaxMintsPerMinute(), current.getMaxMintsPerMinutePerUser())) {
			throw refusal(STATUS_TOO_MANY_REQUESTS, PortalSessionException.ERROR_RATE_LIMITED, "global mint budget of "
					+ current.getMaxMintsPerMinute() + "/min exhausted");
		}

		String authorization = httpRequest.getHeader(HEADER_AUTHORIZATION);
		if (authorization == null || !authorization.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
			throw refusal(Status.STATUS_BAD_REQUEST, PortalSessionException.ERROR_INVALID_REQUEST, "no bearer token in the " + HEADER_AUTHORIZATION
					+ " header");
		}
		String accessToken = authorization.substring(BEARER_PREFIX.length()).trim();

		// Layer 3 - the token itself, and the azp allow list inside it.
		String username = verifier().verify(accessToken);

		if (!budget.tryConsume(username, current.getMaxMintsPerMinute(), current.getMaxMintsPerMinutePerUser())) {
			throw refusal(STATUS_TOO_MANY_REQUESTS, PortalSessionException.ERROR_RATE_LIMITED, "mint budget of "
					+ current.getMaxMintsPerMinutePerUser() + "/min exhausted for one user");
		}

		return new Authorized(username, accessToken);
	}

	/**
	 * Builds the session. The key order matters and is the one {@code BeCPGAIMSFilter.onSuccess}
	 * uses: user id, then the connector session carrying the Alfresco ticket, then the
	 * credential vault, then the user object.
	 */
	private void mint(PortalSessionConfig current, HttpServletRequest httpRequest, WebScriptResponse res, Authorized authorized)
			throws PortalSessionException, IOException {

		String username = authorized.username();

		// Session fixation. This endpoint is anonymous, so a caller can present its own
		// JSESSIONID; upgrading that one would let an attacker pre-seed an id and walk in
		// afterwards. Anything the caller brought is destroyed before a single attribute is
		// written.
		HttpSession presented = httpRequest.getSession(false);
		if (presented != null) {
			presented.invalidate();
		}
		HttpSession session = httpRequest.getSession(true);

		String alfTicket = getAlfTicket(session, username, authorized.accessToken());
		if (alfTicket == null) {
			// The container has already queued a Set-Cookie for the session created above.
			// Killing the session makes that cookie worthless, which is the best available
			// answer once the response has a cookie in it.
			session.invalidate();
			throw refusal(STATUS_BAD_GATEWAY, PortalSessionException.ERROR_REPOSITORY_UNAVAILABLE, "the repository would not issue a ticket for "
					+ username);
		}

		try {
			// 1 - the user id. Everything else in Share hangs off this key.
			session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, username);

			// 2 - the connector session, carrying the ticket. Without it the Alfresco
			// connector falls back to POST /api/login with a password nobody has.
			Connector connector = connectorService.getConnector(ALFRESCO_ENDPOINT_ID, username, session);
			connector.getConnectorSession().setParameter(AlfrescoAuthenticator.CS_PARAM_ALF_TICKET, alfTicket);

			// 3 - the credential vault. ConnectorService needs an entry for an identity=user
			// endpoint, and SlingshotPageView's login gate reads it too.
			CredentialVault vault = FrameworkUtil.getCredentialVault(session, username);
			Credentials credentials = vault.newCredentials(ALFRESCO_ENDPOINT_ID);
			credentials.setProperty(Credentials.CREDENTIAL_USERNAME, username);
			vault.store(credentials);

			// 4 - the user object. This both warms the cache every Share web script reads and
			// PROVES the ticket works, since it goes and fetches the user's metadata. A user
			// the repository does not know fails here, and no cookie is handed out.
			RequestContext context = ThreadLocalRequestContext.getRequestContext();
			if (context == null) {
				throw new IllegalStateException("no Surf RequestContext bound to this thread");
			}
			String userEndpointId = (String) context.getAttribute(RequestContext.USER_ENDPOINT);
			User user = context.getServiceRegistry().getUserFactory().initialiseUser(context, httpRequest, userEndpointId, true);
			if (user == null) {
				throw new IllegalStateException("the repository returned no metadata for " + username);
			}
		} catch (Exception e) { // NOSONAR - whatever went wrong, the session must not survive
			session.invalidate();
			throw refusal(STATUS_BAD_GATEWAY, PortalSessionException.ERROR_REPOSITORY_UNAVAILABLE, "could not initialise the Share session for "
					+ username + ": " + e.getMessage(), e);
		}

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("beCPG portal session: issued a Share session for " + username + " to " + httpRequest.getRemoteAddr());
		}

		JSONObject body = new JSONObject();
		body.put("cookie", "JSESSIONID=" + session.getId());
		body.put("username", username);
		// The portal derives its own cache TTL from this rather than guessing, so a change to
		// Share's session-timeout is picked up without touching the portal.
		body.put("ttlSeconds", session.getMaxInactiveInterval());
		body.put("principalAttribute", current.getPrincipalAttribute());

		res.setStatus(Status.STATUS_OK);
		res.setContentType(JSON_CONTENT_TYPE);
		res.setHeader("Cache-Control", "no-store");
		res.setHeader("Pragma", "no-cache");
		try (Writer writer = res.getWriter()) {
			writer.write(body.toString());
		}
	}

	/**
	 * Exchanges the bearer for an Alfresco ticket, exactly the way
	 * {@code BeCPGAIMSFilter.getAlfTicket} does — same endpoint, same URI, same cache buster.
	 * This is the one place a token leaves Share, and it goes only to the repository.
	 */
	private String getAlfTicket(HttpSession session, String username, String accessToken) {
		try {
			Connector connector = connectorService.getConnector(ALFRESCO_API_ENDPOINT_ID, username, session);
			ConnectorContext connectorContext = new ConnectorContext(HttpMethod.GET, null,
					Collections.singletonMap(HEADER_AUTHORIZATION, BEARER_PREFIX + accessToken));
			connectorContext.setContentType("application/json");
			Response response = connector.call(TICKET_URI + UUID.randomUUID().toString(), connectorContext);

			if (Status.STATUS_OK != response.getStatus().getCode()) {
				LOGGER.error("beCPG portal session: the repository refused to issue a ticket, status " + response.getStatus().getCode());
				return null;
			}
			return new JSONObject(response.getText()).getJSONObject("entry").getString("id");
		} catch (Exception e) { // NOSONAR - a ticket exchange failure is a 502, never a stack trace to the caller
			LOGGER.error("beCPG portal session: could not exchange the token for a ticket: " + e.getMessage(), e);
			return null;
		}
	}

	private PortalSessionConfig resolveConfig() {
		PortalSessionConfig current = this.config;
		if (current == null) {
			synchronized (this) {
				current = this.config;
				if (current == null) {
					current = PortalSessionConfig.read(configService);
					this.config = current;
				}
			}
		}
		return current;
	}

	private PortalSessionTokenVerifier verifier() {
		PortalSessionTokenVerifier current = this.verifier;
		if (current == null) {
			synchronized (this) {
				current = this.verifier;
				if (current == null) {
					current = new PortalSessionTokenVerifier(resolveConfig());
					this.verifier = current;
				}
			}
		}
		return current;
	}

	/**
	 * An operator who switched the feature on and forgot a value gets a 404 and would have no
	 * idea why, so say it in the log — once, naming the keys, and never their values.
	 */
	private void logMisconfigurationOnce(PortalSessionConfig current) {
		if (current.isEnabled() && !current.getMissingKeys().isEmpty() && misconfigurationLogged.compareAndSet(false, true)) {
			LOGGER.error("beCPG portal session: " + PortalSessionConfig.KEY_ENABLED + " is true but the feature stays OFF, these keys are"
					+ " missing or too weak: " + current.getMissingKeys() + ". Set them as <" + PortalSessionConfig.CONFIG_CONDITION
					+ "> config elements or as " + PortalSessionConfig.PROPERTY_PREFIX + "* system properties.");
		}
	}

	private void refuse(WebScriptResponse res, int status, String errorCode, boolean challenge) throws IOException {
		res.setStatus(status);
		res.setContentType(JSON_CONTENT_TYPE);
		res.setHeader("Cache-Control", "no-store");
		if (challenge) {
			// The only thing the caller is ever told. Every distinct reason a token can be bad
			// produces this exact response, so the endpoint is not an oracle.
			res.setHeader("WWW-Authenticate", "Bearer error=\"" + errorCode + "\"");
		}
		JSONObject body = new JSONObject();
		body.put("error", errorCode);
		try (Writer writer = res.getWriter()) {
			writer.write(body.toString());
		}
	}

	private PortalSessionException refusal(int status, String errorCode, String reason) {
		return refusal(status, errorCode, reason, null);
	}

	private PortalSessionException refusal(int status, String errorCode, String reason, Throwable cause) {
		if (LOGGER.isWarnEnabled()) {
			LOGGER.warn("beCPG portal session: refused (" + status + " " + errorCode + ") - " + reason);
		}
		return cause != null ? new PortalSessionException(status, errorCode, reason, cause) : new PortalSessionException(status, errorCode, reason);
	}

	private HttpServletRequest httpRequest(WebScriptRequest req) {
		if (req instanceof WebScriptServletRequest servletRequest) {
			return servletRequest.getHttpServletRequest();
		}
		return ServletUtil.getRequest();
	}

}

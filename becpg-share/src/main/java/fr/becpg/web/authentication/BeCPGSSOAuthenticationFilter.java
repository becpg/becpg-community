package fr.becpg.web.authentication;

import static org.alfresco.web.site.SlingshotPageView.REDIRECT_QUERY;
import static org.alfresco.web.site.SlingshotPageView.REDIRECT_URI;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.alfresco.web.site.servlet.config.AIMSConfig;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.RemoteConfigElement;
import org.springframework.extensions.config.RemoteConfigElement.EndpointDescriptor;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.RequestContextUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.mvc.PageViewResolver;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.types.Page;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.servlet.DependencyInjectedFilter;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.Identifier;
import com.nimbusds.oauth2.sdk.id.State;

import fr.becpg.web.authentication.config.IdentityServiceElement;
import fr.becpg.web.authentication.identity.IdentityServiceFacade;
import fr.becpg.web.authentication.identity.IdentityServiceFacadeFactoryBean;
import fr.becpg.web.authentication.identity.IdentityServiceMetadataKey;
import fr.becpg.web.authentication.identity.OIDCUserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * SSO Authentication Filter for Alfresco Share, supporting authentication
 * via header, existing session and OAuth2 redirection initiation.
 *
 * @author Matthieu Laborie
 * @version $Id: $Id
 */
public class BeCPGSSOAuthenticationFilter implements DependencyInjectedFilter, CallbackHandler, ApplicationContextAware {

	private static final Log LOGGER = LogFactory.getLog(BeCPGSSOAuthenticationFilter.class);

	private static final Set<String> SCOPES = Set.of("openid", "profile", "email", "offline_access");

	// --- Constants ---
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	private static final String OAUTH2_SESSION_ATTRIBUTE = "_alfwfOAuth2User";

	private static final String PAGE_SERVLET_PATH = "/page";
	private static final String LOGIN_PATH_INFORMATION = "/dologin";
	private static final String LOGIN_PARAMETER = "login";
	private static final String ERROR_PARAMETER = "error";
	private static final String UNAUTHENTICATED_ACCESS_PROXY = "/proxy/alfresco-noauth";
	private static final String AI_ACCESS_PROXY = "/proxy/ai";

	private static final String PAGE_VIEW_RESOLVER = "pageViewResolver";

	private static final String SESSION_ATTRIBUTE_KEY_USER_GROUPS = "_alf_USER_GROUPS";
	protected static final String PARAM_USERNAME = "username";

	// --- Spring Dependencies ---
	private ApplicationContext context;
	private ConnectorService connectorService;

	// --- Filter Configuration ---
	private String endpoint;
	private String userHeader;
	private Pattern userIdPattern;
	private String principalAttribute = "preferred_username"; // Attribute to extract OAuth2 username

	// --- OAuth2 Initiation Configuration ---
	private boolean initiateOAuthRedirect = false; // Enable/disable OAuth initiation
	private String oauthClientRegistrationId;

	// --- Internal Services ---
	private IdentityServiceFacade identityServiceFacade;
	private IdentityServiceElement identityServiceConfig;

	public void setInitiateOAuthRedirect(boolean initiateOAuthRedirect) {
		this.initiateOAuthRedirect = initiateOAuthRedirect;
	}

	/**
	 * Filter initialization.
	 */
	public void init() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Initializing BeCPGSSOAuthenticationFilter...");
		}

		// Get services from Spring context
		this.connectorService = (ConnectorService) context.getBean("connector.service");
		ConfigService configService = (ConfigService) context.getBean("web.config"); // Use class field

		// Get remote configuration
		RemoteConfigElement remoteConfig = (RemoteConfigElement) configService.getConfig("Remote").getConfigElement("remote");
		if (remoteConfig == null) {
			LOGGER.error("Missing <remote> configuration. Unable to initialize SSO filter.");
			return;
		}

		// Check configured endpoint
		if (!StringUtils.hasText(this.endpoint)) {
			LOGGER.error("'endpoint' property not configured for BeCPGSSOAuthenticationFilter. Unable to initialize.");
			return;
		}

		// Check if external authentication is enabled for the endpoint
		EndpointDescriptor endpointDescriptor = remoteConfig.getEndpointDescriptor(endpoint);
		if ((endpointDescriptor == null) || !endpointDescriptor.getExternalAuth()) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("External authentication not enabled for endpoint '" + endpoint
						+ "' or endpoint not found. Filter will be disabled for this endpoint.");
			}
			// Disable filter if endpoint is not configured for external auth
			this.endpoint = null;
			return;
		}

		// Specific configuration for header authentication (Header Auth)
		try {
			Connector conn = this.connectorService.getConnector(endpoint);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Configured endpoint: " + endpoint);
			}

			// If connector allows it, get userHeader and userIdPattern
			if (conn instanceof BeCPGExternalConnector) { // Or check if session contains these parameters
				this.userHeader = conn.getConnectorSession().getParameter(BeCPGExternalConnector.CS_PARAM_USER_HEADER);
				String connectorUserIdPattern = conn.getConnectorSession().getParameter(BeCPGExternalConnector.CS_PARAM_USER_ID_PATTERN);
				if (StringUtils.hasText(connectorUserIdPattern)) {
					this.userIdPattern = Pattern.compile(connectorUserIdPattern);
				}
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("User header (userHeader): " + userHeader);
					LOGGER.debug("Extraction pattern (userIdPattern): " + connectorUserIdPattern);
				}
			} else if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
						"The connector for endpoint '" + endpoint + "' is not of type BeCPGExternalConnector or does not provide header parameters.");
			}
		} catch (ConnectorServiceException e) {
			LOGGER.error("Unable to find connector '" + endpointDescriptor.getConnectorId() + "' for endpoint '" + endpoint, e);
			// Don't completely disable the filter here, OAuth might still work
		}

		// Initialize IdentityService facade for OAuth2/OIDC validation
		IdentityServiceElement oauth2Config = (IdentityServiceElement) configService.getConfig(IdentityServiceElement.CONFIG_ELEMENT_ID)
				.getConfigElement(IdentityServiceElement.CONFIG_ELEMENT_ID);
		if (oauth2Config != null) {

			this.identityServiceConfig = oauth2Config;
			// Get principal attribute (e.g.: preferred_username, sub, email)
			String configPrincipalAttr = oauth2Config.getPrincipalAttribute();
			if (StringUtils.hasText(configPrincipalAttr)) {
				this.principalAttribute = configPrincipalAttr;
			}

			String configResource = oauth2Config.getResource();
			if (StringUtils.hasText(configResource)) {
				this.oauthClientRegistrationId = configResource;
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("OAuth2 principal attribute used (principalAttribute): " + this.principalAttribute);
			}

			// The IdentityServiceFacade will be lazily initialized when first needed
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("IdentityServiceFacade will be initialized lazily when first needed.");
			}
		} else {
			LOGGER.warn("Configuration <" + IdentityServiceElement.CONFIG_ELEMENT_ID
					+ "> manquante. La validation des tokens Bearer OAuth2 ne sera pas disponible.");
		}

		// Log initialization completion
		if (StringUtils.hasText(this.endpoint) || this.initiateOAuthRedirect) { // Filter is active if there's an endpoint OR if OAuth initiation is wanted
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(
						"BeCPGSSOAuthenticationFilter initialized. Active mode(s):" + (StringUtils.hasText(this.endpoint) ? " [Endpoint Check]" : "")
								+ (StringUtils.hasText(this.userHeader) ? " [Header Auth]" : "")
								+ (this.initiateOAuthRedirect && StringUtils.hasText(this.oauthClientRegistrationId) ? " [OAuth Initiation]" : "")
								+ (this.identityServiceConfig != null ? " [OAuth Validation]" : ""));
			}
		} else if (LOGGER.isInfoEnabled()) {
			LOGGER.info(
					"BeCPGSSOAuthenticationFilter initialized, but no active configuration detected (no external endpoint, no OAuth initiation). Filter will be inactive.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.context = applicationContext;
	}

	// --- Setters for Spring configuration (Bean XML) ---

	/**
	 * Sets the Alfresco endpoint ID to use for session verification.
	 * @param endpoint Endpoint ID (e.g.: "alfresco").
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	// --- Main Filter Logic ---

	/** {@inheritDoc} */
	@Override
	public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		doFilter(request, response, chain); // Delegate to main method
	}

	/**
	 * Executes the filter logic for each request.
	 */
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException {
		final boolean debug = LOGGER.isDebugEnabled();
		
		
		   // Skip this filter, if AIMS is enabled
        boolean skip = false;
        try
        {
            AIMSConfig aimsConfig = (AIMSConfig) this.context.getBean("aims.config");
            if (aimsConfig.isEnabled())
            {
                skip = true;
            }
        }
        catch (BeansException e)
        {
            if (LOGGER.isErrorEnabled())
            {
            	LOGGER.error(e);
            }
        }

        // If AIMS filter is enabled, skip this filter
        if (skip == true)
        {
            chain.doFilter(sreq, sresp);
            return;
        }


        
		// Cast to HttpServletRequest/Response for early OAuth callback detection
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse res = (HttpServletResponse) sresp;

		// Check if this request is an OAuth callback - either by parameters or URL pattern
		String authCode = req.getParameter("code");
		String state = req.getParameter("state");
		String requestUri = req.getRequestURI();
		boolean isOAuthCallback = (StringUtils.hasText(authCode) && StringUtils.hasText(state))
				|| ((requestUri != null) && requestUri.contains("/oauth/callback"));

		if (isOAuthCallback) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Detected OAuth callback: URI=" + requestUri + ", code=" + (authCode != null ? "present" : "missing") + ", state="
						+ (state != null ? "present" : "missing"));
			}

			try {
				// Handle the OAuth callback directly
				boolean success = handleOAuthCallback(req, res, authCode, state);
				if (success) {
					// Redirect to the original URL stored in session
					String redirectUrl = (String) req.getSession().getAttribute(REDIRECT_URI);
					String redirectQuery = (String) req.getSession().getAttribute(REDIRECT_QUERY);

					if (!StringUtils.hasText(redirectUrl)) {
						redirectUrl = "/share/page/"; // Default landing page
					}

					if (StringUtils.hasText(redirectQuery)) {
						redirectUrl += "?" + redirectQuery;
					}

					res.sendRedirect(redirectUrl);
				} else {
					// Authentication failed, redirect to login page with error
					redirectToLoginPageWithError(req, res, "sso.error.oauth.exchange");
				}
				return;
			} catch (Exception e) {
				LOGGER.error("Error processing OAuth callback", e);
				redirectToLoginPageWithError(req, res, "sso.error.oauth.callback");
				return;
			}
		}

		// Wrap request to handle header authentication or existing OAuth
		sreq = wrapHeaderAuthenticatedRequest(sreq);

		// Check if filter should be active for this request
		// Condition: (configured endpoint AND external) OR (OAuth initiation configured)
		boolean isActive = StringUtils.hasText(this.endpoint) || (this.initiateOAuthRedirect && StringUtils.hasText(this.oauthClientRegistrationId));
		if (!isActive) {
			if (debug) {
				LOGGER.debug(
						"BeCPGSSOAuthenticationFilter inactive (no external endpoint configured and no OAuth initiation). Moving to next filter.");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		// Get session, create if necessary
		HttpSession session = req.getSession();

		// Bypass for unauthenticated proxies
		String servletPath = req.getServletPath();
		if ((servletPath != null) && (servletPath.startsWith(UNAUTHENTICATED_ACCESS_PROXY) || servletPath.startsWith(AI_ACCESS_PROXY))) {
			if (debug) {
				LOGGER.debug("Request to unauthenticated proxy (" + servletPath + "). Bypassing SSO filter.");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		if (debug) {
			LOGGER.debug("Processing request: " + req.getRequestURI() + " (Session ID: " + (session != null ? session.getId() : "null") + ")");
		}

		// Bypass for Share login page itself
		String pathInfo = req.getPathInfo(); // Peut être null
		if (PAGE_SERVLET_PATH.equals(servletPath)
				&& (LOGIN_PATH_INFORMATION.equals(pathInfo) || ((pathInfo == null) && LOGIN_PARAMETER.equals(req.getParameter("pt"))))) {
			if (debug) {
				LOGGER.debug("Request to Share login page. Bypassing SSO filter.");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		// Initialize Surf request context (silent to avoid premature user creation)
		RequestContext rcontext = null;
		try {
			rcontext = RequestContextUtil.initRequestContext(this.context, req, true);
		} catch (Exception ex) {
			LOGGER.error("Error initializing Surf RequestContext.", ex);
			// What to do here? Redirect to error page? Return 500?
			// For now, throw ServletException to indicate a serious problem.
			throw new ServletException("Internal error during context initialization.", ex);
		}

		// Check if requested Surf page does NOT require authentication
		Page page = findSurfPage(rcontext, pathInfo);
		if ((page != null) && (page.getAuthentication() == RequiredAuthentication.none)) {
			if (debug) {
				LOGGER.debug("Surf page '" + page.getId() + "' does not require authentication. Bypassing SSO filter.");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		// --- Start Authentication Checks ---

		// Check 1: User already authenticated via OAuth2 (Spring Security Context, Session, or valid Bearer Token)
		String oauth2Username = getOAuth2Username(req);
		if (oauth2Username != null) {
			if (debug) {
				LOGGER.debug("Authenticated OAuth2 user found: '" + oauth2Username + "'. Session presumed valid.");
			}
			setExternalAuthSession(session); // Indicate to Share that external auth is used
			onSuccess(req, res, session, oauth2Username); // Update Share session
			chain.doFilter(sreq, sresp); // Move to next filter
			return;
		}

		 
		// Check 2: External header authentication (e.g.: X-Alfresco-Remote-User)
		// Note: req.getRemoteUser() will return the header user thanks to wrapHeaderAuthenticatedRequest
		if (StringUtils.hasText(this.userHeader)) {
			String headerUserId = req.getRemoteUser();
			if (StringUtils.hasText(headerUserId)) {
				// User found in header. Still need to verify if corresponding Alfresco session is active.
				if (debug) {
					LOGGER.debug("User found via header '" + this.userHeader + "': '" + headerUserId
							+ "'. Validating Alfresco session via challengeOrPassThrough.");
				}
				// Mark as external auth BEFORE /touch call, as /touch might fail if repo session expired
				setExternalAuthSession(session);
				   onSuccess(req, res, session, req.getRemoteUser());
		              chain.doFilter(sreq, sresp);
		              return;
			} else {
				// Header configured but not found/empty in request.
				if (debug) {
					LOGGER.debug("Header '" + this.userHeader + "' configured but not found or empty. Continuing checks.");
				}
			}
			// The challengeOrPassThrough call will verify the Alfresco session (/touch)
			challengeOrPassThrough(chain, req, res, session);
			return; // challengeOrPassThrough handles the rest
		}


       
		
		// Check 3: Existing Share session (cookie-based) or Authorization header (Basic)
		String authHdr = req.getHeader(HEADER_AUTHORIZATION); // Mainly for Basic Auth
		if ((authHdr == null) && AuthenticationUtil.isAuthenticated(req)) {
			// No Auth header, but Share session exists (AuthenticationUtil uses session attribute)
			if (debug) {
				LOGGER.debug("Existing Share session detected. Validating Alfresco session via challengeOrPassThrough.");
			}
		} else if (authHdr != null) {
			// Authorization header present (could be Basic). Let challengeOrPassThrough attempt validation.
			if (debug) {
				LOGGER.debug("Authorization header detected. Processing via challengeOrPassThrough.");
			}
		} else {
			// Final case: No authentication detected (no valid OAuth, no user header, no Share session, no Auth header)
			if (debug) {
				LOGGER.debug("No pre-existing authentication detected. Initiating authentication process via challengeOrPassThrough.");
			}
		}
		challengeOrPassThrough(chain, req, res, session);
	}

	/**
	 * Attempts to find the Surf page corresponding to the pathInfo.
	 */
	private Page findSurfPage(RequestContext rcontext, String pathInfo) {
		Page page = rcontext.getPage();
		if ((page == null) && (pathInfo != null)) {
			PageViewResolver pageViewResolver = (PageViewResolver) this.context.getBean(PAGE_VIEW_RESOLVER);
			try {
				if (pageViewResolver.resolveViewName(pathInfo, null) != null) {
					page = rcontext.getPage(); // Get the updated page from the context
				}
			} catch (Exception e) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Unable to resolve Surf page for pathInfo '" + pathInfo + "': " + e.getMessage());
				}
				// Ignore error, continue without specific page
			}
		}
		return page;
	}

	/**
	 * Wraps the request to extract the user from an HTTP header
	 * or to provide details of a user authenticated via OAuth2.
	 */
	protected ServletRequest wrapHeaderAuthenticatedRequest(ServletRequest sreq) {
		// Case 1: Header authentication configured
		if (StringUtils.hasText(userHeader) && sreq instanceof HttpServletRequest req) {
			return new HttpServletRequestWrapper(req) {
				@Override
				public String getRemoteUser() {
					String remoteUserHeaderValue = req.getHeader(userHeader);
					if (StringUtils.hasText(remoteUserHeaderValue)) {
						// Handle encoding (copied from original, potentially Alfresco-specific)
						String processedUser = remoteUserHeaderValue;
						if (!org.apache.commons.codec.binary.Base64.isBase64(processedUser)) {
							try {
								processedUser = new String(processedUser.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
							} catch (Exception e) {
								// Ignore if encoding fails, use raw value
								LOGGER.warn("Unable to re-encode userHeader from ISO-8859-1 to UTF-8: " + e.getMessage());
							}
						}
						return extractUserFromProxyHeader(processedUser);
					} else {
						// If header not present, return null (don't call super.getRemoteUser() which could come from elsewhere)
						return null;
					}
				}

				private String extractUserFromProxyHeader(String userIdHeader) {
					if (userIdHeader == null) {
						return null;
					}
					if (userIdPattern == null) {
						return userIdHeader.trim(); // No pattern, take everything after trimming
					} else {
						Matcher matcher = userIdPattern.matcher(userIdHeader);
						if (matcher.matches() && (matcher.groupCount() >= 1)) {
							// Return the first capturing group, trimmed
							String extracted = matcher.group(1);
							return extracted != null ? extracted.trim() : null;
						} else {
							// Pattern doesn't match or has no capturing group
							if (LOGGER.isDebugEnabled()) {
								LOGGER.debug("Header '" + userHeader + "' (" + userIdHeader
										+ ") does not match configured pattern or has no capturing group.");
							}
							return null;
						}
					}
				}

				// Important: Do not override getUserPrincipal or isUserInRole here,
				// because header authentication does not necessarily imply locally known roles or principal.
			};
		}
		// Case 2: No header authentication, but maybe OAuth2 already established?
		else if (sreq instanceof HttpServletRequest req) {
			final String oauthUsername = getOAuth2Username(req); // Try to get OAuth user

			if (oauthUsername != null) {
				// If we have an OAuth user, expose it via standard Servlet API
				return new HttpServletRequestWrapper(req) {
					@Override
					public String getRemoteUser() {
						return oauthUsername;
					}

					@Override
					public Principal getUserPrincipal() {
						// Provide a simple Principal based on username
						// Note: this does not contain complete OAuth2User details
						return () -> oauthUsername;
					}

					@Override
					public boolean isUserInRole(String role) {
						// This filter does not handle roles directly.
						// We could potentially extract roles/groups from OAuth2User here if needed.
						// By default, return false or delegate to original request.
						// Returning true could cause security issues.
						// return super.isUserInRole(role); // Optional: delegate
						return false; // Safer by default
					}
				};
			}
		}

		// Default case: return unmodified original request
		return sreq;
	}

	/**
	 * Attempts to retrieve the username from an existing OAuth2 authentication
	 * (Spring Security Context, HTTP Session, or Authorization Bearer header).
	 *
	 * @param req The HTTP request.
	 * @return The username if found, otherwise null.
	 */
	private String getOAuth2Username(HttpServletRequest req) {
		

		// 2. Check HTTP session (if user comes from a previous request)
		HttpSession session = req.getSession(false); // Don't create session
		if (session != null) {
			OIDCUserInfo oauth2User = (OIDCUserInfo) session.getAttribute(OAUTH2_SESSION_ATTRIBUTE);
			if (oauth2User != null) {
				String username =oauth2User.username();
				if (username != null) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("OAuth2 user found in HTTP session: '" + username + "'.");
					}
					return username;
				}
			}
		}

		// 3. Check Authorization: Bearer header
		String authHeader = req.getHeader(HEADER_AUTHORIZATION);
		if ((authHeader != null) && authHeader.toLowerCase().startsWith("bearer ") && (getIdentityServiceFacade() != null)) {
			try {
				String token = authHeader.substring(7).trim();
				if (!token.isEmpty()) {
					// Validate token and get user information via facade
					Optional<OIDCUserInfo> userInfoOpt = getIdentityServiceFacade().getUserInfo(token, principalAttribute);

					if (userInfoOpt.isPresent()) {
						String username = userInfoOpt.get().username();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Valid Bearer token found. User retrieved via IdentityServiceFacade: '" + username + "'.");
						}
						// We could potentially store info in session here too, but it's less common for Bearer tokens
						return username;
					} else if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Bearer token presented but invalid or user not found via IdentityServiceFacade.");
					}
				}
			} catch (Exception e) {
				// Log the error but don't propagate it to avoid breaking normal flow
				LOGGER.warn("Error validating Bearer token via IdentityServiceFacade: " + e.getMessage(), e);
			}
		}
		// No OAuth2 user found
		return null;
	}


	/**
	 * Verifies Alfresco session validity via /touch call.
	 * If session is invalid (401), triggers appropriate authentication process
	 * (OAuth initiation, or login page redirection).
	 *
	 * @param chain The filter chain.
	 * @param req The HTTP request.
	 * @param res The HTTP response.
	 * @param session The HTTP session.
	 * @throws IOException In case of I/O error.
	 * @throws ServletException In case of Servlet error.
	 */
	private void challengeOrPassThrough(FilterChain chain, HttpServletRequest req, HttpServletResponse res, HttpSession session)
			throws IOException, ServletException {

		// Endpoint must be configured for this method
		if (!StringUtils.hasText(this.endpoint)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
						"challengeOrPassThrough called without configured endpoint. Attempting OAuth initiation if enabled, otherwise passing through.");
			}
			// If endpoint is not there, we can't do /touch.
			// Simulate a 401 response to trigger required authentication logic.
			handleAuthenticationRequired(req, res, session); // null for Response since we haven't called /touch
			return; // handleAuthenticationRequired handles the rest (redirect or chain.doFilter if OAuth disabled)
		}

		try {
			// Determine user for /touch call.
			// Priority: Share Session > Header (via wrapped req.getRemoteUser()) > null (anonymous)
			String userIdForTouch = AuthenticationUtil.getUserId(req); // From Share session

			if ((userIdForTouch == null) && StringUtils.hasText(this.userHeader)) {
				// No user in Share session, check if header provides one
				userIdForTouch = req.getRemoteUser(); // Should come from header via wrapper
				if (StringUtils.hasText(userIdForTouch)) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Using header user '" + userIdForTouch + "' for /touch verification.");
					}
					// Ensure external auth flag is set if this is the first time we see this user header
					if ((session != null) && (session.getAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH) == null)) {
						setExternalAuthSession(session);
					}
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Preparing /touch call to endpoint '" + this.endpoint + "' for user: "
						+ (userIdForTouch != null ? "'" + userIdForTouch + "'" : "<anonymous>"));
			}

			// Get Alfresco connector. IMPORTANT: Use the Share SESSION user
			// to obtain the connector, as it potentially holds the valid Alfresco ticket.
			// Don't pass userIdForTouch directly if it comes from header, unless the connector is VERY specific.
			String sessionUserId = AuthenticationUtil.getUserId(req);
			Connector conn = connectorService.getConnector(this.endpoint, sessionUserId, session);

			// Prepare connector context (with Accept-Language)
			ConnectorContext ctx = buildConnectorContext(req);

			// Call /touch to validate Alfresco session
			Response remoteRes = conn.call("/touch", ctx);
			final int statusCode = remoteRes.getStatus().getCode();

			if (Status.STATUS_OK == statusCode) {
				// Alfresco session OK.
				String finalUserId = userIdForTouch; // Identified user (session or header)
				if (finalUserId == null) {
					// Strange case: /touch OK but we only had a user header? Can happen if anonymous /touch OK.
					// Use the header user as final user.
					finalUserId = req.getRemoteUser();
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Alfresco session OK (anonymous /touch?). Using header user: " + finalUserId);
					}
				}

				if (finalUserId != null) {
					onSuccess(req, res, session, finalUserId); // Ensures user is properly in Share session
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Alfresco session validated for user '" + finalUserId + "'. Continuing request.");
					}
				} else // /touch OK but no user identified (session or header). Could be authorized anonymous access.
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Alfresco session validated (potentially anonymous). Continuing without locally defined user.");
				}
				// Don't call onSuccess without userId.

				// Valid session, continue filter chain
				chain.doFilter(req, res);

			} else if (Status.STATUS_UNAUTHORIZED == statusCode) {
				// Alfresco session expired or initial authentication required (401).
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Alfresco session invalid or missing (/touch response: 401). Triggering authentication process.");
				}
				handleAuthenticationRequired(req, res, session); // Handles OAuth redirection, challenge or login page

			} else {
				// Other error during /touch call (500, 404, etc.)
				LOGGER.error("Unexpected error during Alfresco session validation (/touch). Status: " + statusCode + ", Message: "
						+ remoteRes.getStatus().getMessage());
				// Consider as authentication failure, redirect to login page with error.
				redirectToLoginPageWithError(req, res, "sso.error.repository.unavailable"); // Message key to define
			}
		} catch (ConnectorServiceException cse) {
			LOGGER.error("Connector service error during challengeOrPassThrough for endpoint '" + this.endpoint + "'. Check configuration.", cse);
			//throw new PlatformRuntimeException("Endpoint or connector misconfigured: " + this.endpoint, cse); // Maybe too harsh
			redirectToLoginPageWithError(req, res, "sso.error.config"); // Message key
		} catch (Exception e) {
			// Handle other unexpected exceptions
			LOGGER.error("Unexpected exception during challengeOrPassThrough.", e);
			redirectToLoginPageWithError(req, res, "sso.error.unexpected"); // Message key
		}
	}

	private String getAuthenticationRequest(HttpServletRequest request) {
		ClientRegistration clientRegistration = getIdentityServiceFacade().getClientRegistration();
		State state = new State();

		UriComponentsBuilder authRequestBuilder = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
				.queryParam("client_id", clientRegistration.getClientId())
				.queryParam("redirect_uri", getRedirectUri(request.getRequestURL().toString())).queryParam("response_type", "code")
				.queryParam("scope", String.join("+", getScopes(clientRegistration))).queryParam("state", state.toString());

		if (identityServiceConfig.getAudience() != null) {
			authRequestBuilder.queryParam("audience", identityServiceConfig.getAudience());
		}

		return authRequestBuilder.build().toUriString();
	}

	private Set<String> getScopes(ClientRegistration clientRegistration) {
		return Optional.ofNullable(clientRegistration.getProviderDetails()).map(ProviderDetails::getConfigurationMetadata)
				.map(metadata -> metadata.get(IdentityServiceMetadataKey.SCOPES_SUPPORTED.getValue())).filter(Scope.class::isInstance)
				.map(Scope.class::cast).map(this::getSupportedScopes).orElse(clientRegistration.getScopes());
	}

	private Set<String> getSupportedScopes(Scope scopes) {
		return scopes.stream().filter(scope -> SCOPES.contains(scope.getValue())).map(Identifier::getValue).collect(Collectors.toSet());
	}

	/**
	 * Builds the OAuth callback URL for use in the authorization request.
	 * This needs to match what the OAuth provider has configured for the client's callback URL.
	 *
	 * @param requestURL The original request URL
	 * @return The URI to use for the OAuth callback
	 */
	private String getRedirectUri(String requestURL) {
		try {
			URI originalUri = new URI(requestURL);
			String callbackPath = "/share/page/oauth/callback";

			// Build a URL that will be handled by this filter's doFilter method
			URI redirectUri = new URI(originalUri.getScheme(), originalUri.getAuthority(), callbackPath, null, null);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Using OAuth callback URL: " + redirectUri.toASCIIString());
			}

			return redirectUri.toASCIIString();
		} catch (URISyntaxException e) {
			LOGGER.error("Error while trying to get the redirect URI and respond with the authentication challenge: {}", e);
			return null;
		}
	}

	/**
	 * Handles the situation where authentication is required (typically after a 401 from /touch).
	 * Decides whether to initiate OAuth redirection, send an NTLM/Kerberos challenge,
	 * or redirect to Share's local login page.
	 */
	/**
	 * Lazily initializes and returns the IdentityServiceFacade instance.
	 * This method uses double-checked locking for thread safety.
	 *
	 * @return The IdentityServiceFacade instance, or null if initialization fails
	 */
	private IdentityServiceFacade getIdentityServiceFacade() {
		if ((this.identityServiceFacade == null) && (this.identityServiceConfig != null)) {
			synchronized (this) {
				if (this.identityServiceFacade == null) {
					try {
						IdentityServiceFacadeFactoryBean factory = new IdentityServiceFacadeFactoryBean(this.identityServiceConfig);
						this.identityServiceFacade = factory.createIdentityServiceFacade();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("IdentityServiceFacade initialized successfully.");
						}
					} catch (Exception e) {
						LOGGER.error("Failed to initialize IdentityServiceFacade. OAuth2 Bearer token validation might fail.", e);
						this.identityServiceFacade = null; // Ensure it's null in case of error
					}
				}
			}
		}
		return this.identityServiceFacade;
	}

	private void handleAuthenticationRequired(HttpServletRequest req, HttpServletResponse res, HttpSession session) throws IOException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Authentication required. Checking strategy: OAuth Init? Challenge? Login Page?");
		}

		// Check if this request already has code and state parameters, which would indicate it's a callback from OAuth
		String authCode = req.getParameter("code");
		String state = req.getParameter("state");
		boolean isOAuthCallback = StringUtils.hasText(authCode) && StringUtils.hasText(state);

		// Priority 1: Initiate OAuth2 redirection if configured and applicable
		boolean canInitiateOAuth = this.initiateOAuthRedirect && StringUtils.hasText(this.oauthClientRegistrationId)
				&& (getIdentityServiceFacade() != null) && !isOAuthCallback; // Don't initiate if this is already a callback

		if (isOAuthCallback && LOGGER.isInfoEnabled()) {
			LOGGER.info("handleAuthenticationRequired: Not initiating OAuth redirection for request that is already an OAuth callback.");
		}

		if (canInitiateOAuth) {
			// OAuth redirection configured and ready
			if (LOGGER.isInfoEnabled()) { // Log at INFO level as this is a key action
				LOGGER.info("Authentication required. Initiating OAuth2 redirection via Spring Security for registration: "
						+ this.oauthClientRegistrationId);
			}
			setRedirectUrl(req); // Save original URL before redirecting
			String oauthRedirectUrl = getAuthenticationRequest(req);
			try {
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Redirecting to OAuth2 authorization URL: " + oauthRedirectUrl);
				}
				res.sendRedirect(oauthRedirectUrl);
				// Request processing stops here for this filter.
				return; // Important: stop processing here
			} catch (IOException e) {
				LOGGER.error("Error during redirection to OAuth2 authorization URL: " + oauthRedirectUrl, e);
				// What to do? Try local login page as fallback?
				redirectToLoginPageWithError(req, res, "sso.error.oauth.redirect");
				return;
			}
		}

		// Fallback if OAuth is not initiated
		if (LOGGER.isDebugEnabled()) {
			if (!this.initiateOAuthRedirect) {
				LOGGER.debug("OAuth initiation disabled (initiateOAuthRedirect=false).");
			} else if (!StringUtils.hasText(this.oauthClientRegistrationId)) {
				LOGGER.debug("OAuth initiation enabled, but oauthClientRegistrationId not defined.");
			} else if (this.identityServiceConfig == null) {
				LOGGER.debug("OAuth initiation enabled, but identityServiceConfig not available (check IdentityServiceElement config).");
			}
			LOGGER.debug("Fallback to standard authentication handling (NTLM/Kerberos challenge or login page).");
		}

		// Default case: Redirect to Share's local login page
		if (LOGGER.isDebugEnabled()) {
			if (StringUtils.hasText(this.userHeader)) {
				LOGGER.debug("Header authentication failed or repo session invalid. Redirecting to login page.");
			} else {
				LOGGER.debug("No other applicable authentication method. Redirecting to login page.");
			}
		}

		// Clean session before redirecting to login page,
		// EXCEPT if authentication comes from external header (we don't want to invalidate proxy/container session)
		if ((req.getRemoteUser() == null) && (session != null)) { // Don't invalidate if user comes from header
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Invalidating HTTP session before redirecting to login page.");
			}
			try {
				// Clear Alfresco authentication-related attributes
				AuthenticationUtil.clearUserContext(req);
				session.invalidate();
			} catch (IllegalStateException e) {
				// Session might have already been invalid
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Session was already invalid.", e);
				}
			}
		} else if (session != null) {
			// Even if we don't invalidate (header auth case), clean potentially expired Alfresco creds
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug(
						"External user '" + req.getRemoteUser() + "' detected. Cleaning Alfresco credentials from session, but no invalidation.");
			}
			AuthenticationUtil.clearUserContext(req);
		}

		redirectToLoginPage(req, res); // Redirect to standard Share login page

	}

	/**
	 * Builds the context for connector call, including Accept-Language header.
	 */
	private ConnectorContext buildConnectorContext(HttpServletRequest req) {
		ConnectorContext ctx;
		String acceptLanguage = req.getHeader(HEADER_ACCEPT_LANGUAGE);
		if (StringUtils.hasText(acceptLanguage)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Adding Accept-Language header to connector context: " + acceptLanguage);
			}
			ctx = new ConnectorContext(null, Collections.singletonMap(HEADER_ACCEPT_LANGUAGE, acceptLanguage));
		} else {
			ctx = new ConnectorContext(); // Empty context by default
		}
		return ctx;
	}

	// Method removed as it was never used locally

	/**
	 * Redirects the user to Share's standard login page.
	 */
	private void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
		redirectToLoginPageWithError(req, res, null); // Calls version with error message (null here)
	}

	/**
	 * Redirects the user to Share's standard login page, with an optional error message.
	 * Attempts to detect if the request is for UI (302 redirect) or API (401 response).
	 *
	 * @param errorMessageKey Error message key (for future internationalization) or raw message. Null if no specific error.
	 */
	private void redirectToLoginPageWithError(HttpServletRequest req, HttpServletResponse res, String errorMessageKey) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Redirecting to Share login page." + (errorMessageKey != null ? " Cause: " + errorMessageKey : ""));
		}

		// Simple heuristic to detect API vs UI calls
		String requestUri = req.getRequestURI();
		boolean isApiCall = requestUri.contains("/proxy/") || requestUri.contains("/api/") || requestUri.contains("/service/")
				|| requestUri.contains("/slingshot/"); // Slingshot calls are often XHR

		if (!isApiCall) {
			// --- Complete redirection for UI (302 Found) ---
			setRedirectUrl(req); // Save original URL for redirection after login

			StringBuilder redirectUrl = new StringBuilder(req.getContextPath());
			// Standard path for Share login page
			redirectUrl.append(PAGE_SERVLET_PATH).append("?pt=").append(LOGIN_PARAMETER);

			// Add error parameter if provided
			// Remove unused variable to fix lint warning
			if (StringUtils.hasText(errorMessageKey)) {
				try {
					redirectUrl.append("&").append(ERROR_PARAMETER).append("=")
							.append(URLEncoder.encode(errorMessageKey, StandardCharsets.UTF_8.name()));
				} catch (java.io.UnsupportedEncodingException e) {
					// Should never happen with UTF-8
					LOGGER.error("UTF-8 encoding not supported?!", e);
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("UI redirection to: " + redirectUrl.toString());
			}
			res.sendRedirect(redirectUrl.toString());
		} else {
			// --- 401 Unauthorized response for API calls ---
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("API request detected (" + requestUri + "). Sending 401 Unauthorized status.");
			}
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.flushBuffer(); // Assure que le statut est envoyé
		}
	}

	/**
	 * Saves the original request's URI and query string in the session
	 * to redirect the user after successful authentication.
	 */
	private void setRedirectUrl(HttpServletRequest req) {
		HttpSession session = req.getSession(); // Gets or creates the session
		String requestUri = req.getRequestURI();
		String queryString = req.getQueryString();

		session.setAttribute(REDIRECT_URI, requestUri);
		if (StringUtils.hasText(queryString)) {
			session.setAttribute(REDIRECT_QUERY, queryString);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Redirect URL saved in session: " + requestUri + "?" + queryString);
			}
		} else {
			session.removeAttribute(REDIRECT_QUERY); // Clean up if no query string
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Redirect URL saved in session: " + requestUri);
			}
		}
	}

	/**
	 * Sets a flag in the session to indicate that authentication
	 * comes from an external mechanism (SSO, OAuth, Header).
	 * Used by the Share UI (e.g., to hide the "Login" button).
	 */
	private void setExternalAuthSession(HttpSession session) {
		if (session != null) {
			session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH, Boolean.TRUE);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("External authentication flag set in session (SESSION_ATTRIBUTE_EXTERNAL_AUTH=true).");
			}
		} else if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Unable to set external authentication flag: session is null.");
		}
	}

	/**
	 * Called after successful authentication (session validated or established).
	 * Updates the Share session with the user ID and calls `beforeSuccess`
	 * to potentially load additional information (such as groups).
	 */
	private void onSuccess(HttpServletRequest req, HttpServletResponse res, HttpSession session, String username) {
	    if ((username == null) || (session == null)) {
	        LOGGER.warn("onSuccess called with null username or session. Unable to finalize local authentication.");
	        return;
	    }

	    // Ensure the user ID is in the Share session
	    session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, username);
	    if (LOGGER.isDebugEnabled()) {
	        LOGGER.debug("User '" + username + "' put in Share (SESSION_ATTRIBUTE_KEY_USER_ID).");
	    }

	    try {
	        beforeSuccess(req, res);
	    } catch (Exception e) {
	        LOGGER.error("Error while executing beforeSuccess() after authentication of '" + username + "'.", e);
	    }
	}


	/**
	 * Method called after successful authentication to perform additional tasks,
	 * particularly retrieving the user's groups from Alfresco.
	 */
	
	
	
	@SuppressWarnings("deprecation")
	protected void beforeSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {

		final HttpSession session = request.getSession(false); // Don't create if it doesn't exist
		if (session == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("beforeSuccess: Session not found, unable to retrieve groups.");
			}
			return;
		}

		try {
			// Retrieve username from session (set by onSuccess)
			String username = (String) session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);

			if (username == null) {
				// Try to get it from the parameter (less reliable here)
				username = request.getParameter(PARAM_USERNAME);
				if ((username == null) && LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Username not found in session or parameter.");
				}
			}

			// Check if groups are already loaded for this user in this session
			if ((username != null) && (session.getAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS) == null)) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Retrieving groups for user '" + username + "' from Alfresco...");
				}

				Connector conn = null;
				try {
					conn = FrameworkUtil.getConnector(session, username, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
				} catch (Exception e) {
					// Handle the case where the connector cannot be created (often related to missing configuration)
					LOGGER.error("beforeSuccess: Unable to obtain Alfresco connector to retrieve groups for '" + username
							+ "'. Check the configuration of endpoint '" + AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID + "'.", e);
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Mark as empty to avoid retrying
					return; // Exit if no connector
				}

				// Prepare the API call /api/people/{user}?groups=true
				ConnectorContext c = new ConnectorContext(HttpMethod.GET);
				c.setContentType("application/json"); // Not strictly necessary for GET but good practice

				String encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.name());

				String apiUrl = "/api/people/" + encodedUsername + "?groups=true";
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Alfresco API call: " + apiUrl);
				}

				Response resGroups = conn.call(apiUrl, c);

				if (Status.STATUS_OK == resGroups.getStatus().getCode()) {
					String resStr = resGroups.getResponse();
					if (LOGGER.isTraceEnabled()) { // Log JSON only in TRACE
						LOGGER.trace("beforeSuccess: Groups JSON response: " + resStr);
					}

					JSONParser jp = new JSONParser();
					Object userData = null;
					try {
						userData = jp.parse(resStr);
					} catch (ParseException pe) {
						LOGGER.error("beforeSuccess: Unable to parse groups JSON response for '" + username + "'.", pe);
						session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Mark as empty
						return;
					}

					// Extract group names from JSON
					StringBuilder groups = new StringBuilder(512);
					if (userData instanceof JSONObject jsonData) {
						Object groupsArray = jsonData.get("groups"); // The key is indeed "groups"
						if (groupsArray instanceof JSONArray jsonArray) {
							for (Object groupData : jsonArray) {
								if (groupData instanceof JSONObject jsonGroup) {
									// The group name is in "itemName" for the /api/people API
									Object groupName = jsonGroup.get("itemName");
									if ((groupName != null) && StringUtils.hasText(groupName.toString())) {
										groups.append(groupName.toString().trim()).append(',');
									}
								}
							}
						}
					}

					// Remove the final comma if it exists
					if ((groups.length() > 0) && (groups.charAt(groups.length() - 1) == ',')) {
						groups.deleteCharAt(groups.length() - 1);
					}

					String groupsString = groups.toString();
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, groupsString);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("beforeSuccess: Groups stored in session for '" + username + "': "
								+ (groupsString.isEmpty() ? "<none>" : groupsString));
					}

				} else {
					// Error during API call (401, 404, 500...)
					LOGGER.warn("beforeSuccess: Failed to retrieve groups for '" + username + "'. API Status: " + resGroups.getStatus().getCode());
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Mark as empty in case of failure
				}
			} else if ((username != null) && LOGGER.isDebugEnabled()) {
				LOGGER.debug("beforeSuccess: Groups for user '" + username + "' are already present in session.");
			}
		} catch (Exception ex) {
			// Other exceptions
			LOGGER.error("beforeSuccess: Unexpected exception while retrieving groups.", ex);
			if (session != null) {
				session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Mark as empty
			}
		}
	}

	/**
	 * Method from the CallbackHandler interface (not used here).
	 */
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		// Not implemented, this filter does not actively use JAAS CallbackHandler.
		LOGGER.warn("CallbackHandler.handle() method called, but not implemented in this filter.");
	}

	/**
	 * Handles the OAuth callback directly in the filter.
	 * Exchanges the authorization code for an access token, retrieves user information,
	 * and establishes the user session.
	 *
	 * @param req The HTTP request containing the authorization code
	 * @param res The HTTP response
	 * @param authCode The authorization code received from the OAuth provider
	 * @param state The state parameter for CSRF protection
	 * @return true if authentication was successful, false otherwise
	 * @throws IOException If an I/O error occurs
	 */
	protected boolean handleOAuthCallback(HttpServletRequest req, HttpServletResponse res, String authCode, String state) throws IOException {

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Processing OAuth callback with code parameter");
		}

		HttpSession session = req.getSession(true);
		try {
			// Get the identity service facade
			IdentityServiceFacade identityService = getIdentityServiceFacade();
			if (identityService == null) {
				LOGGER.error("Cannot process OAuth callback: IdentityServiceFacade is null");
				return false;
			}

			// Build the redirect URI that was used when initiating the flow
			String redirectUri = getRedirectUri(req.getRequestURL().toString());

			// Create an authorization grant from the code
			IdentityServiceFacade.AuthorizationGrant grant = IdentityServiceFacade.AuthorizationGrant.authorizationCode(authCode, redirectUri);

			// Exchange the grant for access tokens
			IdentityServiceFacade.AccessTokenAuthorization tokenAuth = identityService.authorize(grant);
			if ((tokenAuth == null) || (tokenAuth.getAccessToken() == null)) {
				LOGGER.error("Failed to obtain access token from authorization code");
				return false;
			}

			// Get the access token value
			String accessToken = tokenAuth.getAccessToken().getTokenValue();
			if (!StringUtils.hasText(accessToken)) {
				LOGGER.error("Obtained null or empty access token");
				return false;
			}

			// Get user information using the access token
			Optional<OIDCUserInfo> userInfoOpt = identityService.getUserInfo(accessToken, principalAttribute);
			if (!userInfoOpt.isPresent()) {
				LOGGER.error("Failed to retrieve user info from OAuth provider");
				return false;
			}

			OIDCUserInfo userInfo = userInfoOpt.get();

			if (userInfo == null) {
				LOGGER.error("No OAuth user info");
				return false;
			}

			String username = userInfo.username();
			if (!StringUtils.hasText(username)) {
				LOGGER.error("Could not extract username from OAuth user info");
				return false;
			}

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Successfully authenticated OAuth user: " + username);
			}

			// Store OAuth user information in session
			session.setAttribute(OAUTH2_SESSION_ATTRIBUTE, userInfo);

			// Set up the Share session for this user
			setExternalAuthSession(session);
			onSuccess(req, res, session, username);

			return true;

		} catch (Exception e) {
			LOGGER.error("Error processing OAuth callback: " + e.getMessage(), e);
			return false;
		}
	}

}
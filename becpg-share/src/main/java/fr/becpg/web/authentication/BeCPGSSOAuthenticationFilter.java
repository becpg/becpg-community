package fr.becpg.web.authentication;

import static org.alfresco.web.site.SlingshotPageView.REDIRECT_QUERY;
import static org.alfresco.web.site.SlingshotPageView.REDIRECT_URI;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder; // Utiliser java.net.URLEncoder standard
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
	private IdentityServiceFacade identityServiceFacade; // For validating Bearer tokens
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
		if (endpointDescriptor == null || !endpointDescriptor.getExternalAuth()) {
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
			} else {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("The connector for endpoint '" + endpoint
							+ "' is not of type BeCPGExternalConnector or does not provide header parameters.");
				}
			}
		} catch (ConnectorServiceException e) {
			LOGGER.error("Unable to find connector '" + endpointDescriptor.getConnectorId() + "' for endpoint '" + endpoint, e);
			// Don't completely disable the filter here, OAuth might still work
		}

		// Initialize IdentityService facade for OAuth2/OIDC validation
		IdentityServiceElement oauth2Config = (IdentityServiceElement) configService.getConfig(IdentityServiceElement.CONFIG_ELEMENT_ID)
				.getConfigElement(IdentityServiceElement.CONFIG_ELEMENT_ID);
		if (oauth2Config != null) {
			
			this.identityServiceConfig  = oauth2Config;
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

			// Initialize the facade
			try {
				IdentityServiceFacadeFactoryBean factory = new IdentityServiceFacadeFactoryBean(oauth2Config);
				this.identityServiceFacade = factory.createIdentityServiceFacade();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("IdentityServiceFacade initialized successfully.");
				}
			} catch (Exception e) {
				LOGGER.error("Failed to initialize IdentityServiceFacade. OAuth2 Bearer token validation might fail.", e);
				this.identityServiceFacade = null; // Ensure it's null in case of error
			}
		} else {
			LOGGER.warn("Configuration <" + IdentityServiceElement.CONFIG_ELEMENT_ID
					+ "> manquante. La validation des tokens Bearer OAuth2 ne sera pas disponible.");
			this.identityServiceFacade = null;
		}

		// Log initialization completion
		if (StringUtils.hasText(this.endpoint) || this.initiateOAuthRedirect) { // Filter is active if there's an endpoint OR if OAuth initiation is wanted
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(
						"BeCPGSSOAuthenticationFilter initialized. Active mode(s):" + (StringUtils.hasText(this.endpoint) ? " [Endpoint Check]" : "")
								+ (StringUtils.hasText(this.userHeader) ? " [Header Auth]" : "")
								+ (this.initiateOAuthRedirect && StringUtils.hasText(this.oauthClientRegistrationId) ? " [OAuth Initiation]" : "")
								+ (this.identityServiceFacade != null ? " [OAuth Validation]" : ""));
			}
		} else {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info(
						"BeCPGSSOAuthenticationFilter initialized, but no active configuration detected (no external endpoint, no OAuth initiation). Filter will be inactive.");
			}
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

		// Cast vers HttpServletRequest/Response
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse res = (HttpServletResponse) sresp;
		HttpSession session = req.getSession(); // Get session, create if necessary

		// Bypass for unauthenticated proxies
		String servletPath = req.getServletPath();
		if (servletPath != null && (servletPath.startsWith(UNAUTHENTICATED_ACCESS_PROXY) || servletPath.startsWith(AI_ACCESS_PROXY))) {
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
				&& (LOGIN_PATH_INFORMATION.equals(pathInfo) || (pathInfo == null && LOGIN_PARAMETER.equals(req.getParameter("pt"))))) {
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
		if (page != null && page.getAuthentication() == RequiredAuthentication.none) {
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
				// The challengeOrPassThrough call will verify the Alfresco session (/touch)
				challengeOrPassThrough(chain, req, res, session);
			} else {
				// Header configured but not found/empty in request.
				if (debug) {
					LOGGER.debug("Header '" + this.userHeader + "' configured but not found or empty. Continuing checks.");
				}
				// No user by header, must check session or initiate authentication.
				challengeOrPassThrough(chain, req, res, session);
			}
			return; // challengeOrPassThrough handles the rest
		}

		// Check 3: Existing Share session (cookie-based) or Authorization header (Basic)
		String authHdr = req.getHeader(HEADER_AUTHORIZATION); // Mainly for Basic Auth
		if (authHdr == null && AuthenticationUtil.isAuthenticated(req)) {
			// No Auth header, but Share session exists (AuthenticationUtil uses session attribute)
			if (debug) {
				LOGGER.debug("Existing Share session detected. Validating Alfresco session via challengeOrPassThrough.");
			}
			challengeOrPassThrough(chain, req, res, session);
		} else if (authHdr != null) {
			// Authorization header present (could be Basic). Let challengeOrPassThrough attempt validation.
			if (debug) {
				LOGGER.debug("Authorization header detected. Processing via challengeOrPassThrough.");
			}
			// Note: A Bearer token should have already been handled by getOAuth2Username.
			// If we get here with a Bearer, it means it was invalid or not processable by identityServiceFacade.
			challengeOrPassThrough(chain, req, res, session);
		} else {
			// Final case: No authentication detected (no valid OAuth, no user header, no Share session, no Auth header)
			if (debug) {
				LOGGER.debug("No pre-existing authentication detected. Initiating authentication process via challengeOrPassThrough.");
			}
			challengeOrPassThrough(chain, req, res, session); // Will trigger /touch verification and potentially OAuth initiation
		}
	}

	/**
	 * Attempts to find the Surf page corresponding to the pathInfo.
	 */
	private Page findSurfPage(RequestContext rcontext, String pathInfo) {
		Page page = rcontext.getPage();
		if (page == null && pathInfo != null) {
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
					if (userIdHeader == null)
						return null;
					if (userIdPattern == null) {
						return userIdHeader.trim(); // No pattern, take everything after trimming
					} else {
						Matcher matcher = userIdPattern.matcher(userIdHeader);
						if (matcher.matches() && matcher.groupCount() >= 1) {
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
		// 1. Check Spring security context
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth instanceof OAuth2AuthenticationToken oauthToken) {
			OAuth2User oauth2User = oauthToken.getPrincipal();
			String username = extractUsernameFromOAuth2User(oauth2User);
			if (username != null) {
				// Store OAuth2User object in session for subsequent requests (if session exists)
				HttpSession session = req.getSession(false); // Don't create session if it doesn't exist
				if (session != null) {
					session.setAttribute(OAUTH2_SESSION_ATTRIBUTE, oauth2User);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("OAuth2 user found in SecurityContextHolder: '" + username + "'. Stored in session.");
					}
				} else {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug(
								"OAuth2 user found in SecurityContextHolder: '" + username + "'. No HTTP session to store it.");
					}
				}
				return username;
			}
		}

		// 2. Check HTTP session (if user comes from a previous request)
		HttpSession session = req.getSession(false); // Don't create session
		if (session != null) {
			OAuth2User oauth2User = (OAuth2User) session.getAttribute(OAUTH2_SESSION_ATTRIBUTE);
			if (oauth2User != null) {
				String username = extractUsernameFromOAuth2User(oauth2User);
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
		if (authHeader != null && authHeader.toLowerCase().startsWith("bearer ") && this.identityServiceFacade != null) {
			try {
				String token = authHeader.substring(7).trim();
				if (!token.isEmpty()) {
					// Validate token and get user information via facade
					Optional<OIDCUserInfo> userInfoOpt = identityServiceFacade.getUserInfo(token, principalAttribute);

					if (userInfoOpt.isPresent()) {
						String username = userInfoOpt.get().username();
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Valid Bearer token found. User retrieved via IdentityServiceFacade: '" + username + "'.");
						}
						// We could potentially store info in session here too, but it's less common for Bearer tokens
						return username;
					} else {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Bearer token presented but invalid or user not found via IdentityServiceFacade.");
						}
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
	 * Extracts the username from the OAuth2User object using the configured attribute.
	 */
	private String extractUsernameFromOAuth2User(OAuth2User oauth2User) {
		if (oauth2User == null)
			return null;

		Object userAttr = oauth2User.getAttribute(this.principalAttribute);
		if (userAttr != null) {
			return userAttr.toString();
		} else {
			// Fallback to getName() if principal attribute not found
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("OAuth2 principal attribute '" + this.principalAttribute
						+ "' not found in OAuth2User. Using getName() as fallback.");
			}
			return oauth2User.getName(); // getName() often corresponds to the 'sub' attribute
		}
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
				LOGGER.debug("challengeOrPassThrough called without configured endpoint. Attempting OAuth initiation if enabled, otherwise passing through.");
			}
			// If endpoint is not there, we can't do /touch.
			// Simulate a 401 response to trigger required authentication logic.
			handleAuthenticationRequired(req, res, session, null); // null for Response since we haven't called /touch
			return; // handleAuthenticationRequired handles the rest (redirect or chain.doFilter if OAuth disabled)
		}

		try {
			// Determine user for /touch call.
			// Priority: Share Session > Header (via wrapped req.getRemoteUser()) > null (anonymous)
			String userIdForTouch = AuthenticationUtil.getUserId(req); // From Share session
			boolean isHeaderAuthUser = false;

			if (userIdForTouch == null && StringUtils.hasText(this.userHeader)) {
				// No user in Share session, check if header provides one
				userIdForTouch = req.getRemoteUser(); // Should come from header via wrapper
				if (StringUtils.hasText(userIdForTouch)) {
					isHeaderAuthUser = true;
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Using header user '" + userIdForTouch + "' for /touch verification.");
					}
					// Ensure external auth flag is set if this is the first time we see this user header
					if (session != null && session.getAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH) == null) {
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
				if (finalUserId == null && isHeaderAuthUser) {
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
				} else {
					// /touch OK but no user identified (session or header). Could be authorized anonymous access.
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Alfresco session validated (potentially anonymous). Continuing without locally defined user.");
					}
					// Don't call onSuccess without userId.
				}

				// Valid session, continue filter chain
				chain.doFilter(req, res);

			} else if (Status.STATUS_UNAUTHORIZED == statusCode) {
				// Alfresco session expired or initial authentication required (401).
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Alfresco session invalid or missing (/touch response: 401). Triggering authentication process.");
				}
				handleAuthenticationRequired(req, res, session, remoteRes); // Handles OAuth redirection, challenge or login page

			} else {
				// Other error during /touch call (500, 404, etc.)
				LOGGER.error("Unexpected error during Alfresco session validation (/touch). Status: " + statusCode + ", Message: "
						+ remoteRes.getStatus().getMessage());
				// Consider as authentication failure, redirect to login page with error.
				redirectToLoginPageWithError(req, res, "sso.error.repository.unavailable"); // Message key to define
			}
		} catch (ConnectorServiceException cse) {
			LOGGER.error(
					"Connector service error during challengeOrPassThrough for endpoint '" + this.endpoint + "'. Check configuration.",
					cse);
			//throw new PlatformRuntimeException("Endpoint or connector misconfigured: " + this.endpoint, cse); // Maybe too harsh
			redirectToLoginPageWithError(req, res, "sso.error.config"); // Message key
		} catch (Exception e) {
			// Handle other unexpected exceptions
			LOGGER.error("Unexpected exception during challengeOrPassThrough.", e);
			redirectToLoginPageWithError(req, res, "sso.error.unexpected"); // Message key
		}
	}
	

    private String getAuthenticationRequest(HttpServletRequest request)
    {
        ClientRegistration clientRegistration = identityServiceFacade.getClientRegistration();
        State state = new State();

        UriComponentsBuilder authRequestBuilder = UriComponentsBuilder.fromUriString(clientRegistration.getProviderDetails().getAuthorizationUri())
            .queryParam("client_id", clientRegistration.getClientId())
            .queryParam("redirect_uri", getRedirectUri(request.getRequestURL().toString()))
            .queryParam("response_type", "code")
            .queryParam("scope", String.join("+", getScopes(clientRegistration)))
            .queryParam("state", state.toString());

        if(identityServiceConfig.getAudience()!=null)
        {
            authRequestBuilder.queryParam("audience", identityServiceConfig.getAudience());
        }

        return authRequestBuilder.build().toUriString();
    }

    private Set<String> getScopes(ClientRegistration clientRegistration)
    {
        return Optional.ofNullable(clientRegistration.getProviderDetails())
            .map(ProviderDetails::getConfigurationMetadata)
            .map(metadata -> metadata.get(IdentityServiceMetadataKey.SCOPES_SUPPORTED.getValue()))
            .filter(Scope.class::isInstance)
            .map(Scope.class::cast)
            .map(this::getSupportedScopes)
            .orElse(clientRegistration.getScopes());
    }

    private Set<String> getSupportedScopes(Scope scopes)
    {
        return scopes.stream()
            .filter(scope -> SCOPES.contains(scope.getValue()))
            .map(Identifier::getValue)
            .collect(Collectors.toSet());
    }

    private String getRedirectUri(String requestURL)
    {
        try
        {
            URI originalUri = new URI(requestURL);
            URI redirectUri = new URI(originalUri.getScheme(), originalUri.getAuthority(), identityServiceConfig.getAdminConsoleRedirectPath(), originalUri.getQuery(), originalUri.getFragment());
            return redirectUri.toASCIIString();
        }
        catch (URISyntaxException e)
        {
            LOGGER.error("Error while trying to get the redirect URI and respond with the authentication challenge: {}", e);
            return null;
        }
    }


	/**
	 * Handles the situation where authentication is required (typically after a 401 from /touch).
	 * Decides whether to initiate OAuth redirection, send an NTLM/Kerberos challenge,
	 * or redirect to Share's local login page.
	 */
	private void handleAuthenticationRequired(HttpServletRequest req, HttpServletResponse res, HttpSession session, Response remoteRepoResponse)
			throws IOException {

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Authentication required. Checking strategy: OAuth Init? Challenge? Login Page?");
		}

		// Priority 1: Initiate OAuth2 redirection if configured and applicable
		boolean canInitiateOAuth = this.initiateOAuthRedirect && StringUtils.hasText(this.oauthClientRegistrationId)
				&& this.identityServiceFacade != null; // Also check that basic OAuth config seems OK

		if (canInitiateOAuth) {
			// OAuth redirection configured and ready
			if (LOGGER.isInfoEnabled()) { // Log at INFO level as this is a key action
				LOGGER.info("Authentication required. Initiating OAuth2 redirection via Spring Security for registration: "
						+ this.oauthClientRegistrationId);
			}
			setRedirectUrl(req); // Save original URL before redirecting
			String oauthRedirectUrl = getAuthenticationRequest(req);
			try {
				res.sendRedirect(oauthRedirectUrl);
				// Request processing stops here for this filter.
				// Spring Security will take over to handle /oauth2/authorization/*.
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
			if (!this.initiateOAuthRedirect)
				LOGGER.debug("OAuth initiation disabled (initiateOAuthRedirect=false).");
			else if (!StringUtils.hasText(this.oauthClientRegistrationId))
				LOGGER.debug("OAuth initiation enabled, but oauthClientRegistrationId not defined.");
			else if (this.identityServiceFacade == null)
				LOGGER.debug("OAuth initiation enabled, but identityServiceFacade not initialized (check IdentityServiceElement config).");
			LOGGER.debug("Fallback to standard authentication handling (NTLM/Kerberos challenge or login page).");
		}

		// Default case: Redirect to Share's local login page
		if (LOGGER.isDebugEnabled()) {
			if (StringUtils.hasText(this.userHeader))
				LOGGER.debug("Header authentication failed or repo session invalid. Redirecting to login page.");
			else
				LOGGER.debug("No other applicable authentication method. Redirecting to login page.");
		}

		// Clean session before redirecting to login page,
		// EXCEPT if authentication comes from external header (we don't want to invalidate proxy/container session)
		if (req.getRemoteUser() == null && session != null) { // Don't invalidate if user comes from header
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
				LOGGER.debug("External user '" + req.getRemoteUser()
						+ "' detected. Cleaning Alfresco credentials from session, but no invalidation.");
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
		// Could add other headers here if needed
		// Example: ctx.getHeaders().put(HEADER_AUTHORIZATION, "Basic ...");
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
			// Optional: Add WWW-Authenticate header to indicate expected auth type (if relevant)
			// res.setHeader(HEADER_WWWAUTHENTICATE, "Bearer realm=\"Alfresco Share API\""); // Exemple
			// Envoyer une réponse vide ou un JSON minimal si nécessaire
			// res.setContentType("application/json");
			// res.getWriter().write("{\"status\": 401, \"message\": \"Authentication Required\"}");
			res.flushBuffer(); // Assure que le statut est envoyé
		}
	}

	/**
	 * Sauvegarde l'URI et la query string de la requête originale en session
	 * pour pouvoir y rediriger l'utilisateur après une authentification réussie.
	 */
	private void setRedirectUrl(HttpServletRequest req) {
		HttpSession session = req.getSession(); // Obtient ou crée la session
		String requestUri = req.getRequestURI();
		String queryString = req.getQueryString();

		session.setAttribute(REDIRECT_URI, requestUri);
		if (StringUtils.hasText(queryString)) {
			session.setAttribute(REDIRECT_QUERY, queryString);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("URL de redirection sauvegardée en session: " + requestUri + "?" + queryString);
			}
		} else {
			session.removeAttribute(REDIRECT_QUERY); // Nettoyer si pas de query string
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("URL de redirection sauvegardée en session: " + requestUri);
			}
		}
	}

	/**
	 * Positionne un flag en session pour indiquer que l'authentification
	 * provient d'un mécanisme externe (SSO, OAuth, Header).
	 * Utilisé par l'UI de Share (ex: pour masquer le bouton "Login").
	 */
	private void setExternalAuthSession(HttpSession session) {
		if (session != null) {
			session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH, Boolean.TRUE);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Flag d'authentification externe positionné en session (SESSION_ATTRIBUTE_EXTERNAL_AUTH=true).");
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Impossible de positionner le flag d'authentification externe: session est null.");
			}
		}
	}

	/**
	 * Appelé après une authentification réussie (session validée ou établie).
	 * Met à jour la session Share avec l'ID utilisateur et appelle `beforeSuccess`
	 * pour potentiellement charger des informations supplémentaires (comme les groupes).
	 */
	private void onSuccess(HttpServletRequest req, HttpServletResponse res, HttpSession session, String username) {
		if (username == null || session == null) {
			LOGGER.warn("onSuccess appelé avec username ou session null. Impossible de finaliser l'authentification locale.");
			return;
		}

		// Assurer que l'ID utilisateur est dans la session Share
		session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, username);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Utilisateur '" + username + "' positionné dans la session Share (SESSION_ATTRIBUTE_KEY_USER_ID).");
		}

		try {
			// Appeler la logique de post-connexion (ex: chargement des groupes)
			beforeSuccess(req, res);
		} catch (Exception e) {
			// Log l'erreur mais ne pas la relancer pour ne pas casser le flux principal si possible
			LOGGER.error("Erreur lors de l'exécution de beforeSuccess() après l'authentification de '" + username + "'.", e);
			// On pourrait lever une AlfrescoRuntimeException si c'est critique
			// throw new AlfrescoRuntimeException("Erreur pendant beforeSuccess() pour " + username, e);
		}
	}

	/**
	 * Méthode appelée après une authentification réussie pour effectuer des tâches supplémentaires,
	 * notamment récupérer les groupes de l'utilisateur depuis Alfresco.
	 */
	protected void beforeSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
		// Note: Cette méthode semble provenir du LoginController standard de Share.
		// Elle récupère les groupes de l'utilisateur via un appel API Alfresco.

		final HttpSession session = request.getSession(false); // Ne pas créer si n'existe pas
		if (session == null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("beforeSuccess: Session non trouvée, impossible de récupérer les groupes.");
			}
			return;
		}

		try {
			// Récupérer le nom d'utilisateur depuis la session (défini par onSuccess)
			String username = (String) session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);

			if (username == null) {
				// Essayer de le récupérer du paramètre (moins fiable ici)
				username = request.getParameter(PARAM_USERNAME);
				if (username == null && LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Username non trouvé en session ni en paramètre.");
				}
			}

			// Vérifier si les groupes sont déjà chargés pour cet utilisateur dans cette session
			if (username != null && session.getAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS) == null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Récupération des groupes pour l'utilisateur '" + username + "' depuis Alfresco...");
				}

				Connector conn = null;
				try {
					conn = FrameworkUtil.getConnector(session, username, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
				} catch (Exception e) {
					// Gérer le cas où le connecteur ne peut pas être créé (souvent lié à une config manquante)
					LOGGER.error("beforeSuccess: Impossible d'obtenir le connecteur Alfresco pour récupérer les groupes de '" + username
							+ "'. Vérifier la configuration de l'endpoint '" + AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID + "'.", e);
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Marquer comme vide pour éviter de réessayer
					return; // Sortir si pas de connecteur
				}

				// Préparer l'appel API /api/people/{user}?groups=true
				ConnectorContext c = new ConnectorContext(HttpMethod.GET);
				c.setContentType("application/json"); // Pas strictement nécessaire pour GET mais bonne pratique

				String encodedUsername = "";
				try {
					encodedUsername = URLEncoder.encode(username, StandardCharsets.UTF_8.name());
				} catch (java.io.UnsupportedEncodingException e) {
					/* Impossible avec UTF-8 */ }

				String apiUrl = "/api/people/" + encodedUsername + "?groups=true";
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Appel API Alfresco: " + apiUrl);
				}

				Response resGroups = conn.call(apiUrl, c);

				if (Status.STATUS_OK == resGroups.getStatus().getCode()) {
					String resStr = resGroups.getResponse();
					if (LOGGER.isTraceEnabled()) { // Log JSON seulement en TRACE
						LOGGER.trace("beforeSuccess: Réponse JSON des groupes: " + resStr);
					}

					JSONParser jp = new JSONParser();
					Object userData = null;
					try {
						userData = jp.parse(resStr);
					} catch (ParseException pe) {
						LOGGER.error("beforeSuccess: Impossible de parser la réponse JSON des groupes pour '" + username + "'.", pe);
						session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Marquer comme vide
						return;
					}

					// Extraire les noms des groupes du JSON
					StringBuilder groups = new StringBuilder(512);
					if (userData instanceof JSONObject) {
						Object groupsArray = ((JSONObject) userData).get("groups"); // La clé est bien "groups"
						if (groupsArray instanceof JSONArray) {
							for (Object groupData : (JSONArray) groupsArray) {
								if (groupData instanceof JSONObject) {
									// Le nom du groupe est dans "itemName" pour l'API /api/people
									Object groupName = ((JSONObject) groupData).get("itemName");
									if (groupName != null && StringUtils.hasText(groupName.toString())) {
										groups.append(groupName.toString().trim()).append(',');
									}
								}
							}
						}
					}

					// Supprimer la virgule finale si elle existe
					if (groups.length() > 0 && groups.charAt(groups.length() - 1) == ',') {
						groups.deleteCharAt(groups.length() - 1);
					}

					String groupsString = groups.toString();
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, groupsString);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("beforeSuccess: Groupes stockés en session pour '" + username + "': "
								+ (groupsString.isEmpty() ? "<aucun>" : groupsString));
					}

				} else {
					// Erreur lors de l'appel API (401, 404, 500...)
					LOGGER.warn("beforeSuccess: Échec de la récupération des groupes pour '" + username + "'. Statut API: "
							+ resGroups.getStatus().getCode());
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Marquer comme vide en cas d'échec
				}
			} else if (username != null) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("beforeSuccess: Les groupes pour l'utilisateur '" + username + "' sont déjà présents en session.");
				}
			}
		} catch (Exception ex) {
			// Autres exceptions
			LOGGER.error("beforeSuccess: Exception inattendue lors de la récupération des groupes.", ex);
			if (session != null) {
				session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, ""); // Marquer comme vide
			}
		}
	}


	/**
	 * Méthode de l'interface CallbackHandler (non utilisée ici).
	 */
	@Override
	public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
		// Non implémenté, ce filtre n'utilise pas JAAS CallbackHandler activement.
		LOGGER.warn("Méthode CallbackHandler.handle() appelée, mais non implémentée dans ce filtre.");
	}

}
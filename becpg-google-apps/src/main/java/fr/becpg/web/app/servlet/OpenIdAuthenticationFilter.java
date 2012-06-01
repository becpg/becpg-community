package fr.becpg.web.app.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.faces.context.FacesContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.admin.SysAdminParams;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.UrlUtil;
import org.alfresco.web.app.Application;
import org.alfresco.web.app.servlet.AuthenticationHelper;
import org.alfresco.web.app.servlet.BaseServlet;
import org.alfresco.web.app.servlet.FacesHelper;
import org.alfresco.web.bean.NavigationBean;
import org.alfresco.web.bean.repository.PreferencesService;
import org.alfresco.web.bean.repository.User;
import org.alfresco.web.config.ClientConfigElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.ConsumerException;
import org.openid4java.message.MessageException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.extensions.config.ConfigService;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;

import fr.becpg.repo.security.authentication.openid.OpenID4JavaConsumer;
import fr.becpg.repo.security.authentication.openid.OpenIdAuthenticator;
import fr.becpg.repo.security.authentication.openid.OpenIdUtils;
import fr.becpg.repo.security.authentication.openid.oauth.OAuthTokenUtils;

/**
 * 
 * @author matthieu
 * 
 */
public class OpenIdAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter, ActivateableBean, InitializingBean {

	// Debug logging
	private static Log logger = LogFactory.getLog(OpenIdAuthenticationFilter.class);

	private boolean isActive = true;

	// Various services required by openID authenticator

	private String m_loginPage;

	// ~ Static fields/initializers
	// =====================================================================================

	public static final String DEFAULT_CLAIMED_IDENTITY_FIELD = "openid_identifier";

	public static final String OAUHT_SESSION_TOKEN = "oauth_session_token";

	// ~ Instance fields
	// ================================================================================================

	private SysAdminParams sysAdminParams;
	private ConfigService configService;
	private OpenIDConsumer consumer;
	private String oauthCertFile;
	private String oauthConsumerKey;
	private String oauthConsumerKeySecret;
	private String claimedIdentityFieldName = DEFAULT_CLAIMED_IDENTITY_FIELD;
	private Map<String, String> realmMapping = Collections.emptyMap();
	private Set<String> returnToUrlParameters = Collections.emptySet();

	private String claimedIdentity = null;

	// SSO enabled authentication component (required)
	private OpenIdAuthenticator openIdAuthenticator;

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public void setOauthCertFile(String oauthCertFile) {
		this.oauthCertFile = oauthCertFile;
	}

	public void setOauthConsumerKey(String oauthConsumerKey) {
		this.oauthConsumerKey = oauthConsumerKey;
	}

	public void setOauthConsumerKeySecret(String oauthConsumerKeySecret) {
		this.oauthConsumerKeySecret = oauthConsumerKeySecret;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setSysAdminParams(SysAdminParams sysAdminParams) {
		this.sysAdminParams = sysAdminParams;
	}

	@Override
	public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
		// If a filter up the chain has marked the request as not requiring auth
		// then respect it
		if (getLogger().isDebugEnabled()) {
			logger.debug("Request auth for:" + ((HttpServletRequest) request).getRequestURL());
		}

		OAuthTokenUtils.setCurrentOAuthToken(getOAuthSessionToken((HttpServletRequest) request));

		
		if (request.getAttribute(NO_AUTH_REQUIRED) != null) {
			if (getLogger().isDebugEnabled())
				getLogger().debug("Authentication not required (filter), chaining ...");
			
			//No auth need tenant anyway
			// Check if the user is already authenticated
			SessionUser user = getSessionUser(context,  (HttpServletRequest)request, (HttpServletResponse)response, true);
			
			// If the user has been validated then continue to
			// the next filter
			if (user != null) {

				// Filter validate hook
				onValidate(context, (HttpServletRequest)request, (HttpServletResponse)response, null);

				if (getLogger().isDebugEnabled())
					getLogger().debug("Authentication not required (user), chaining ...");

			}
			
			chain.doFilter(request, response);
		} else if (authenticateRequest(context, (HttpServletRequest) request, (HttpServletResponse) response)) {
			chain.doFilter(request, response);
		}

	}

	private GoogleOAuthParameters getOAuthSessionToken(HttpServletRequest request) {
		return (GoogleOAuthParameters) request.getSession().getAttribute(OAUHT_SESSION_TOKEN);
	}

	private void setOAuthSessionToken(HttpServletRequest request, String authorizedtoken) {
		try {

			// Parse access token
			GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
			oauthParameters.setOAuthConsumerKey(oauthConsumerKey);
			oauthParameters.setOAuthConsumerSecret(oauthConsumerKeySecret);
			oauthParameters.setOAuthToken(authorizedtoken);

			GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(OAuthTokenUtils.getRSASigner());
			String accessToken = oauthHelper.getAccessToken(oauthParameters);
			if (logger.isDebugEnabled()) {
				logger.debug("Getting access token form authorized token : " + authorizedtoken);
				logger.debug("Access token is :" + accessToken);
			}
			// Set access token
			oauthParameters.setOAuthToken(accessToken);
			request.getSession().setAttribute(OAUHT_SESSION_TOKEN, oauthParameters);
			OAuthTokenUtils.setCurrentOAuthToken(oauthParameters);

		} catch (Exception e) {
			logger.error("Cannot get oauth accessToken", e);
		}

	}

	// Check if guest is in url then skip auth for openId
	private boolean isGuestAccess(ServletRequest request) {
		return request.getParameter("guest") != null;
	}

	@Override
	protected Log getLogger() {
		return logger;
	}

	// ~ Constructors
	// ===================================================================================================

	// ~ Methods
	// ========================================================================================================

	@Override
	public void afterPropertiesSet() throws ServletException {
		if (isActive()) {

			if (consumer == null) {
				try {
					consumer = new OpenID4JavaConsumer();
					((OpenID4JavaConsumer) consumer).setSysAdminParams(sysAdminParams);
				} catch (ConsumerException e) {
					throw new IllegalArgumentException("Failed to initialize OpenID", e);
				} catch (MessageException e) {
					throw new IllegalArgumentException("Failed to initialize OpenID", e);
				}
			}

			// Check that the authentication component supports the required
			// mode

			if (!(authenticationComponent instanceof OpenIdAuthenticator)) {
				throw new ServletException("Authentication component does not support OpenId");
			}
			this.openIdAuthenticator = (OpenIdAuthenticator) this.authenticationComponent;

			OAuthTokenUtils.initPrivateKey(oauthCertFile);

			ClientConfigElement clientConfig = (ClientConfigElement) configService.getGlobalConfig().getConfigElement(ClientConfigElement.CONFIG_ELEMENT_ID);
			if (clientConfig != null) {
				setLoginPage(clientConfig.getLoginPage());
			}

			// Use the web client user attribute name
			setUserAttributeName(AuthenticationHelper.AUTHENTICATION_USER);

			logger.debug("OpenIdAuthenticationFilter is Active");
		}
	}

	/**
	 * Authentication has two phases.
	 * <ol>
	 * <li>The initial submission of the claimed OpenID. A redirect to the URL
	 * returned from the consumer will be performed and false will be returned.</li>
	 * <li>The redirection from the OpenID server to the return_to URL, once it
	 * has authenticated the user</li>
	 * </ol>
	 * 
	 * @param context
	 * @throws ServletException
	 */
	public boolean authenticateRequest(ServletContext context, HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException,
			ServletException {

		// Check if the user is already authenticated
		SessionUser user = getSessionUser(context, request, response, true);

		// If the user has been validated then continue to
		// the next filter
		if (user != null) {

			// Filter validate hook
			onValidate(context, request, response, null);

			if (getLogger().isDebugEnabled())
				getLogger().debug("Authentication not required (user), chaining ...");

			// Chain to the next filter
			return true;
		}

		// Check if the login page is being accessed, do not intercept the login
		// page
		if (hasLoginPage() && request.getRequestURI().endsWith(getLoginPage()) == true) {
			if (getLogger().isDebugEnabled())
				getLogger().debug("Login page requested, chaining ...");

			// Chain to the next filter
			return true;
		}

//		if (isGuestAccess(request)) {
//			if (logger.isDebugEnabled()){
//				logger.debug("Authenticating as Guest not supported ");
//			}
//			Writer writer = response.getWriter();
//			try {
//				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
//				writer.write(AbstractAuthenticationService.GUEST_AUTHENTICATION_NOT_SUPPORTED);
//			} finally {
//				if (writer != null) {
//					writer.flush();
//					writer.close();
//				}
//			}
//			return false;
//
//		}
		
		if (isGuestAccess(request)) {
			if (logger.isDebugEnabled())
				logger.debug("Authenticating as Guest");

			try {
				authenticationService.authenticateAsGuest();
				user = createUserEnvironment(request.getSession(), authenticationService.getCurrentUserName(), authenticationService.getCurrentTicket(), true);

				onValidate(context, request, response, null);

				return true;
			} catch (AuthenticationException ex) {
				if (logger.isDebugEnabled())
					logger.debug("Guest auth failed", ex);
			}

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;

		}
		
		OpenIDAuthenticationToken token;

		String identity = request.getParameter("openid.identity");

		logger.debug("Check openID auth for Session ID:" + request.getSession().getId());

		if (!StringUtils.hasText(identity)) {
			if (claimedIdentity == null) {
				claimedIdentity = obtainUsername(request);
			}
			try {

				String returnToUrl = buildReturnToUrl(request);
				String realm = lookupRealm(returnToUrl);
				String openIdUrl = consumer.beginConsumption(request, claimedIdentity, returnToUrl, realm);

				if (logger.isDebugEnabled()) {
					logger.debug("return_to is '" + returnToUrl + "', realm is '" + realm + "'");
					logger.debug("Redirecting to " + openIdUrl);
				}

				if (request.getHeader(OpenIdUtils.HEADER_SHARE_AUTH) != null) {
					logger.debug("Share Auth Step 1 : redirect response");

					response.setHeader(OpenIdUtils.HEADER_OPENID_REDIRECT, Boolean.TRUE.toString());
					response.getWriter().write(openIdUrl);
					response.getWriter().flush();
					response.getWriter().close();
				} else {
					response.sendRedirect(openIdUrl);
				}

				// Indicate to parent class that authentication is continuing.
				return false;
			} catch (OpenIDConsumerException e) {
				logger.debug("Failed to consume claimedIdentity: " + claimedIdentity, e);
				throw new AuthenticationException("Unable to process claimed identity '" + claimedIdentity + "'");
			}
		} else {

			if (logger.isDebugEnabled()) {
				logger.debug("Supplied OpenID identity is " + identity);
			}

			if (request.getHeader(OpenIdUtils.HEADER_SHARE_AUTH) != null && request.getHeader(OpenIdUtils.HEADER_OPENID_CALLBACK) == null) {
				logger.warn("Bad share auth missing callback header");
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				return false;
			}

			try {
				token = consumer.endConsumption(request);
			} catch (OpenIDConsumerException oice) {
				logger.error(oice, oice);
				throw new AuthenticationException("Consumer error", oice);
			}

			// delegate to the authentication provider
			try {
				Authentication authentication = openIdAuthenticator.authenticate(token);

				if (authentication.isAuthenticated()) {
					user = createUserEnvironment(request.getSession(), authenticationService.getCurrentUserName(), authenticationService.getCurrentTicket(), true);

					onValidate(context, request, response, token);

					if (request.getHeader(OpenIdUtils.HEADER_SHARE_AUTH) != null) {

						response.setHeader(OpenIdUtils.HEADER_OPENID_AUTH_SUCCESS, Boolean.TRUE.toString());
						response.getWriter().write(authenticationService.getCurrentUserName());
						response.getWriter().flush();
						response.getWriter().close();

						return false;
					} else if (onLoginComplete(context, request, response, true)) {
						// Allow the user to access the requested page
						return true;
					}
				}
			} catch (AuthenticationException e) {
				logger.error(e, e);
				throw e;
			}

			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			return false;
		}
	}

	protected String lookupRealm(String returnToUrl) {
		String mapping = realmMapping.get(returnToUrl);

		if (mapping == null) {
			try {
				URL url = new URL(returnToUrl);
				
				if(returnToUrl.contains("becpg.fr")){
					return "https://*.becpg.fr";
				}
				
				int port = url.getPort();

				StringBuilder realmBuffer = new StringBuilder(returnToUrl.length()).append(url.getProtocol()).append("://").append(url.getHost());
				if (port > 0) {
					realmBuffer.append(":").append(port);
				}
				realmBuffer.append("/");
				mapping = realmBuffer.toString();
			} catch (MalformedURLException e) {
				logger.warn("returnToUrl was not a valid URL: [" + returnToUrl + "]", e);
			}
		}

		return mapping;
	}

	/**
	 * Builds the <tt>return_to</tt> URL that will be sent to the OpenID service
	 * provider. By default returns the URL of the current request.
	 * 
	 * @param request
	 *            the current request which is being processed by this filter
	 * @return The <tt>return_to</tt> URL.
	 */
	protected String buildReturnToUrl(HttpServletRequest request) {
		StringBuffer sb = request.getRequestURL();
		if (request.getHeader(OpenIdUtils.HEADER_SHARE_AUTH) != null) {
			sb = new StringBuffer();
			sb.append(UrlUtil.getShareUrl(sysAdminParams));
		}

		Iterator<String> iterator = returnToUrlParameters.iterator();
		boolean isFirst = true;

		while (iterator.hasNext()) {
			String name = iterator.next();
			// Assume for simplicity that there is only one value
			String value = request.getParameter(name);

			if (value == null) {
				continue;
			}

			if (isFirst) {
				sb.append("?");
				isFirst = false;
			}
			sb.append(name).append("=").append(value);

			if (iterator.hasNext()) {
				sb.append("&");
			}
		}
		if (request.getHeader(OpenIdUtils.HEADER_SHARE_AUTH) != null) {
			if (isFirst) {
				sb.append("?" + OpenIdUtils.getShareAuthParam(request) + "=true");
			} else {
				sb.append("&" + OpenIdUtils.getShareAuthParam(request) + "=true");
			}
		}

		return sb.toString();
	}

	/**
	 * Reads the <tt>claimedIdentityFieldName</tt> from the submitted request.
	 */
	protected String obtainUsername(HttpServletRequest req) {
		String claimedIdentity = req.getParameter(claimedIdentityFieldName);

		if (!StringUtils.hasText(claimedIdentity)) {
			logger.error("No claimed identity supplied in authentication request");
			return "";
		}

		return claimedIdentity.trim();
	}

	/**
	 * Maps the <tt>return_to url</tt> to a realm, for example:
	 * 
	 * <pre>
	 * http://www.example.com/j_spring_openid_security_check -> http://www.example.com/realm</tt>
	 * </pre>
	 * 
	 * If no mapping is provided then the returnToUrl will be parsed to extract
	 * the protocol, hostname and port followed by a trailing slash. This means
	 * that <tt>http://www.example.com/j_spring_openid_security_check</tt> will
	 * automatically become <tt>http://www.example.com:80/</tt>
	 * 
	 * @param realmMapping
	 *            containing returnToUrl -> realm mappings
	 */
	public void setRealmMapping(Map<String, String> realmMapping) {
		this.realmMapping = realmMapping;
	}

	/**
	 * The name of the request parameter containing the OpenID identity, as
	 * submitted from the initial login form.
	 * 
	 * @param claimedIdentityFieldName
	 *            defaults to "openid_identifier"
	 */
	public void setClaimedIdentityFieldName(String claimedIdentityFieldName) {
		this.claimedIdentityFieldName = claimedIdentityFieldName;
	}

	public void setConsumer(OpenIDConsumer consumer) {
		this.consumer = consumer;
	}

	/**
	 * Specifies any extra parameters submitted along with the identity field
	 * which should be appended to the {@code return_to} URL which is assembled
	 * by {@link #buildReturnToUrl}.
	 * 
	 * @param returnToUrlParameters
	 *            the set of parameter names. If not set, it will default to the
	 *            parameter name used by the {@code RememberMeServices} obtained
	 *            from the parent class (if one is set).
	 */
	public void setReturnToUrlParameters(Set<String> returnToUrlParameters) {
		Assert.notNull(returnToUrlParameters, "returnToUrlParameters cannot be null");
		this.returnToUrlParameters = returnToUrlParameters;
	}

	/**
	 * Set the OpenID Provider
	 * 
	 * @param claimedIdentity
	 */
	public void setClaimedIdentity(String claimedIdentity) {
		this.claimedIdentity = claimedIdentity;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.webdav.auth.BaseAuthenticationFilter#createUserObject
	 * (java.lang.String, java.lang.String,
	 * org.alfresco.service.cmr.repository.NodeRef,
	 * org.alfresco.service.cmr.repository.NodeRef)
	 */
	@Override
	protected SessionUser createUserObject(String userName, String ticket, NodeRef personNode, NodeRef homeSpaceRef) {
		// Create a web client user object
		User user = new User(userName, ticket, personNode);
		user.setHomeSpaceId(homeSpaceRef.getId());

		return user;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.webdav.auth.BaseSSOAuthenticationFilter#onValidate(
	 * javax.servlet.ServletContext, javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected void onValidate(ServletContext sc, HttpServletRequest req, HttpServletResponse res, OpenIDAuthenticationToken token) {

		// Set the locale using the session
		AuthenticationHelper.setupThread(sc, req, res, !req.getServletPath().equals("/wcs") && !req.getServletPath().equals("/wcservice"));
		if (token != null) {
			// Set the oauth token
			setOAuthSessionToken(req, OpenIdUtils.getOAuthToken(token));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.alfresco.repo.webdav.auth.BaseNTLMAuthenticationFilter#onLoginComplete
	 * (javax.servlet.http.HttpServletRequest,
	 * javax.servlet.http.HttpServletResponse)
	 */
	protected boolean onLoginComplete(ServletContext sc, HttpServletRequest req, HttpServletResponse res, boolean userInit) throws IOException {
		// If the original URL requested was the login page then redirect to the
		// browse view
		String requestURI = req.getRequestURI();
		if (requestURI.startsWith(req.getContextPath() + BaseServlet.FACES_SERVLET) && (userInit || requestURI.endsWith(getLoginPage()))) {
			if (logger.isDebugEnabled() && requestURI.endsWith(getLoginPage()))
				logger.debug("Login page requested - redirecting to initially configured page");
			if (logger.isDebugEnabled() && userInit)
				logger.debug("Session reinitialised - redirecting to initially configured page");

			FacesContext fc = FacesHelper.getFacesContext(req, res, sc);
			ConfigService configService = Application.getConfigService(fc);
			ClientConfigElement configElement = (ClientConfigElement) configService.getGlobalConfig().getConfigElement("client");
			String location = configElement.getInitialLocation();

			String preference = (String) PreferencesService.getPreferences(fc).getValue("start-location");
			if (preference != null) {
				location = preference;
			}

			if (NavigationBean.LOCATION_MYALFRESCO.equals(location)) {
				// Clear previous location - Fixes the issue ADB-61
				NavigationBean navigationBean = (NavigationBean) FacesHelper.getManagedBean(fc, "NavigationBean");
				if (navigationBean != null) {
					navigationBean.setLocation(null);
					navigationBean.setToolbarLocation(null);
				}
				res.sendRedirect(req.getContextPath() + BaseServlet.FACES_SERVLET + "/jsp/dashboards/container.jsp");
			} else {
				res.sendRedirect(req.getContextPath() + BaseServlet.FACES_SERVLET + FacesHelper.BROWSE_VIEW_ID);
			}

			return false;
		} else {
			return true;
		}
	}

	/**
	 * Determine if the login page is available
	 * 
	 * @return boolean
	 */
	protected final boolean hasLoginPage() {
		return m_loginPage != null ? true : false;
	}

	/**
	 * Return the login page address
	 * 
	 * @return String
	 */
	protected final String getLoginPage() {
		return m_loginPage;
	}

	/**
	 * Set the login page address
	 * 
	 * @param loginPage
	 *            String
	 */
	protected final void setLoginPage(String loginPage) {
		m_loginPage = loginPage;
	}

}

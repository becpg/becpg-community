package fr.becpg.web.site.servlet;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.config.RemoteConfigElement;
import org.springframework.extensions.config.RemoteConfigElement.EndpointDescriptor;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.RequestContextUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.types.Page;
import org.springframework.extensions.surf.util.URLEncoder;
import org.springframework.extensions.webscripts.Description.RequiredAuthentication;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * SSO Authentication Filter Class for web-tier, supporting openID
 * 
 * @author matthieu
 * 
 */
public class OpenIDSSOAuthenticationFilter implements Filter {
	private static Log logger = LogFactory.getLog(OpenIDSSOAuthenticationFilter.class);

	// Authentication request/response headers
	private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";

	private static final String PAGE_SERVLET_PATH = "/page";
	private static final String LOGIN_PATH_INFORMATION = "/dologin";
	private static final String LOGIN_PARAMETER = "login";

	private static final String HEADER_SHARE_AUTH = "ShareAuth";

	private static final String HEADER_OPENID_CALLBACK = "OpenIdCallBack";

	private static final String HEADER_OPENID_REDIRECT = "OpenIdRedirect";

	private static final String HEADER_OPENID_AUTH_SUCCESS = "OpenIdAuthSuccess";


	private ConnectorService connectorService;
	private String endpoint;
	private ServletContext servletContext;

	/**
	 * Initialize the filter
	 */
	public void init(FilterConfig args) throws ServletException {
		// get reference to our ServletContext
		this.servletContext = args.getServletContext();

		ApplicationContext context = getApplicationContext();

		// retrieve the connector service
		this.connectorService = (ConnectorService) context.getBean("connector.service");

		ConfigService configService = (ConfigService) context.getBean("web.config");

		// Retrieve the remote configuration
		RemoteConfigElement remoteConfig = (RemoteConfigElement) configService.getConfig("Remote").getConfigElement("remote");
		if (remoteConfig == null) {
			return;
		}

		// get the endpoint id to use
		String endpoint = args.getInitParameter("endpoint");
		if (endpoint == null) {
			return;
		}

		// Get the endpoint descriptor and check if external auth is enabled
		EndpointDescriptor endpointDescriptor = remoteConfig.getEndpointDescriptor(endpoint);
		if (endpointDescriptor == null || !endpointDescriptor.getExternalAuth()) {
			return;
		}

		// Save the endpoint, activating the filter
		this.endpoint = endpoint;

		if (logger.isInfoEnabled())
			logger.info("OpenID SSOAuthenticationFilter initialised.");
	}

	/**
	 * Run the filter
	 * 
	 * @param sreq
	 *            ServletRequest
	 * @param sresp
	 *            ServletResponse
	 * @param chain
	 *            FilterChain
	 * 
	 * @exception IOException
	 * @exception ServletException
	 */
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException {

		final boolean debug = logger.isDebugEnabled();

		// Bypass the filter if we don't have an endpoint with openId auth
		// enabled
		if (this.endpoint == null) {
			chain.doFilter(sreq, sresp);
			return;
		}

		// Get the HTTP request/response/session
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse res = (HttpServletResponse) sresp;
		HttpSession session = req.getSession();

		if (debug){
			logger.debug("Processing request " + req.getRequestURI() + " SID:" + session.getId());
			logger.debug("Current user:"+AuthenticationUtil.getUserId(req));
		}

		// Login page or login submission

	      
        // Login page or login submission
        String pathInfo;
        if (PAGE_SERVLET_PATH.equals(req.getServletPath())
                && ((LOGIN_PATH_INFORMATION.equals(pathInfo = req.getPathInfo()) || pathInfo == null
                        && LOGIN_PARAMETER.equals(req.getParameter("pt")))
                        || "/type/login".equals(pathInfo)))
        {
            if (debug)
                logger.debug("Login page requested, chaining ...");

            // Chain to the next filter
            chain.doFilter(sreq, sresp);
            return;
        }
        
        
		// initialize a new request context
		RequestContext context = null;
		try {
			// perform a "silent" init - i.e. no user creation or remote
			// connections
			context = RequestContextUtil.initRequestContext(getApplicationContext(), req, true);
		} catch (Exception ex) {
			logger.error("Error calling initRequestContext", ex);
			throw new ServletException(ex);
		}

		// get the page from the model if any - it may not require
		// authentication
		Page page = context.getPage();
		if (page != null && page.getAuthentication() == RequiredAuthentication.none) {
			if (logger.isDebugEnabled())
				logger.debug("Unauthenticated page requested - skipping auth filter...");
			chain.doFilter(sreq, sresp);
			return;
		}



		// Check the authorization header
		if (debug) {
			if (!AuthenticationUtil.isAuthenticated(req)) {
				logger.debug("New auth request from " + req.getRemoteHost() + " (" + req.getRemoteAddr() + ":" + req.getRemotePort() + ")");
			}
		}
		
		openIdAuth(chain, req, res, session);

	}

	private void openIdAuth(FilterChain chain, HttpServletRequest req, HttpServletResponse res,HttpSession session) throws IOException, ServletException {
		try {
			
			
			Connector conn = connectorService.getConnector(this.endpoint, session);

			Map<String, String> headers = new HashMap<String, String>();

			// We are comming from share
			// Share openID marker
			headers.put(HEADER_SHARE_AUTH, Boolean.TRUE.toString());

			// ALF-10785: We must pass through the language header to set up the
			// session in the correct locale
			if (req.getHeader(HEADER_ACCEPT_LANGUAGE) != null) {
				headers.put(HEADER_ACCEPT_LANGUAGE, req.getHeader(HEADER_ACCEPT_LANGUAGE));
			}

			headers.put("user-agent", "");
			if (conn.getConnectorSession().getCookie("JSESSIONID") == null) {
				// Ensure we do not proxy over the Session ID from the browser
				// request:
				// If Alfresco and SURF app are deployed into the same
				// app-server and user is
				// user same browser instance to access both apps then we could
				// get wrong session ID!
				headers.put("Cookie", null);
			}
			// ALF-12278: Prevent the copying over of headers specific to a POST
			// request on to the touch GET request
			headers.put("Content-Type", null);
			headers.put("Content-Length", null);

			String forwardRequest = "/touch";

			String identity = req.getParameter("openid.identity");
			// Step 2
			if (StringUtils.hasText(identity)) {
				forwardRequest += buildForwardParams(req);
				logger.debug("Running OpenID Step 2 : forward openID params to alfresco");
				logger.debug("Forward Request = " + forwardRequest);
				headers.put(HEADER_OPENID_CALLBACK, Boolean.TRUE.toString());
			}

			ConnectorContext ctx = new ConnectorContext(null, headers);
			Response remoteRes = conn.call(forwardRequest, ctx);

			if (logger.isDebugEnabled()) {
				logger.debug("Reponse:" + remoteRes.getResponse());
				logger.debug("Status:" + remoteRes.getStatus().toString());
			}

			if (remoteRes.getStatus().getHeaders().containsKey(HEADER_OPENID_REDIRECT)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Running OpenID Step 1 : redirect to openId provider");
					logger.debug("Redirect Request = " + remoteRes.getResponse());
				}
				res.sendRedirect(remoteRes.getResponse());

				return;
			} else if (remoteRes.getStatus().getHeaders().containsKey(HEADER_OPENID_AUTH_SUCCESS)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Running OpenID Step 3 : authentication on share side");
				}

				String userId = remoteRes.getResponse();

				doLogin(req,res, session,userId);

				chain.doFilter(req, res);
				return;

			} else if (Status.STATUS_UNAUTHORIZED == remoteRes.getStatus().getCode()) {

				if (logger.isDebugEnabled()) {
					logger.debug("Repository session timed out - restarting auth process...");
				}

				session.invalidate();
				// restart manual login
				redirectToLoginPage(req, res);

				return;
			} else {
				
				// we have local auth in the session and the repo session is
				// also valid
				// this means we do not need to perform any further auth
				// handshake
				if (logger.isDebugEnabled()) {
					logger.debug("Authentication not required, chaining ...");
				}

				chain.doFilter(req, res);
				return;
			}
		} catch (ConnectorServiceException cse) {
			throw new PlatformRuntimeException("Incorrectly configured endpoint ID: " + this.endpoint);
		}
	}

	private void doLogin(HttpServletRequest req, HttpServletResponse res,HttpSession session, String userId) {
		if(logger.isDebugEnabled()){
			logger.debug("Log user : "+userId);
		}
		
		
		// Create User ID in session so the web-framework dispatcher
		// knows we have logged in
		session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, userId);

		// Set the external auth flag so the UI knows we are using SSO
		// etc.
		session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH, Boolean.TRUE);

		
	}

	protected String buildForwardParams(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();

		@SuppressWarnings("unchecked")
		Enumeration<String> iterator = request.getParameterNames();
		boolean isFirst = true;

		while (iterator.hasMoreElements()) {
			String name = iterator.nextElement();
			// Assume for simplicity that there is only one value
			String value = request.getParameter(name);

			if (value == null) {
				continue;
			}

			if (isFirst) {
				sb.append("?");
				isFirst = false;
			}
			sb.append(name).append("=").append(URLEncoder.encode(value));

			if (iterator.hasMoreElements()) {
				sb.append("&");
			}
		}

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy() {
	}

	/**
	 * Redirect to the root of the website - ignore further SSO auth requests
	 */
	private void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
		// Redirect to the login page
		res.sendRedirect(req.getContextPath() + "/page?pt=login");
	}

	/**
	 * Retrieves the root application context
	 * 
	 * @return application context
	 */
	private ApplicationContext getApplicationContext() {
		return WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext);
	}

}

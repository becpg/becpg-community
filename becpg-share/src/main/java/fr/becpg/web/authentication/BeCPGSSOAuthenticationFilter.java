/*
 * #%L
 * Alfresco Share WAR
 * %%
 * Copyright (C) 2005 - 2016 Alfresco Software Limited
 * %%
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of
 * the paid license agreement will prevail.  Otherwise, the software is
 * provided under the following open source license terms:
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */
package fr.becpg.web.authentication;

import static org.alfresco.web.site.SlingshotPageView.REDIRECT_QUERY;
import static org.alfresco.web.site.SlingshotPageView.REDIRECT_URI;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.Enumeration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.log.NDC;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
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
import org.springframework.extensions.surf.exception.PlatformRuntimeException;
import org.springframework.extensions.surf.mvc.PageViewResolver;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.types.Page;
import org.springframework.extensions.surf.util.URLEncoder;
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
import org.springframework.security.oauth2.core.user.OAuth2User;

import fr.becpg.web.authentication.config.OAuth2ConfigElement;
import fr.becpg.web.authentication.identity.IdentityServiceFacade;
import fr.becpg.web.authentication.identity.IdentityServiceFacadeFactoryBean;
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
 * SSO Authentication Filter Class for web-tier, supporting OAuth2 authentication.
 *
 * @author Matthieu Laborie
 * @version $Id: $Id
 */
public class BeCPGSSOAuthenticationFilter implements DependencyInjectedFilter, CallbackHandler, ApplicationContextAware {
	private static Log logger = LogFactory.getLog(BeCPGSSOAuthenticationFilter.class);

	private static final String HEADER_WWWAUTHENTICATE = "WWW-Authenticate";
	private static final String HEADER_AUTHORIZATION = "Authorization";
	private static final String HEADER_ACCEPT_LANGUAGE = "Accept-Language";


    private static final String OAUTH2_SESSION_ATTRIBUTE = "_alfwfOAuth2User";
    
	private static final String MIME_HTML_TEXT = "text/html";

	private static final String PAGE_SERVLET_PATH = "/page";
	private static final String LOGIN_PATH_INFORMATION = "/dologin";
	private static final String LOGIN_PARAMETER = "login";
	private static final String ERROR_PARAMETER = "error";
	private static final String UNAUTHENTICATED_ACCESS_PROXY = "/proxy/alfresco-noauth";

	private static final String AI_ACCESS_PROXY = "/proxy/ai";
	private static final String PAGE_VIEW_RESOLVER = "pageViewResolver";

	private ApplicationContext context;
	private ConnectorService connectorService;
	private String endpoint;
	private String userHeader;
	private Pattern userIdPattern;
    private String resource;
    private String principalAttribute = "preferred_username";
    
    private IdentityServiceFacade identityServiceFacade;
    
    
	

	/**
	 * Initialize the filter
	 */
	public void init() {
		if (logger.isDebugEnabled()) {
			logger.debug("Initializing the BeCPGSSOAuthenticationFilter");
		}


		// retrieve the connector service
		this.connectorService = (ConnectorService) context.getBean("connector.service");

		ConfigService configService = (ConfigService) context.getBean("web.config");

		// Retrieve the remote configuration
		RemoteConfigElement remoteConfig = (RemoteConfigElement) configService.getConfig("Remote").getConfigElement("remote");
		if (remoteConfig == null) {
			logger.error("There is no Remote configuration element. This is required to use SSOAuthenticationFilter.");
			return;
		}

		// get the endpoint id to use
		if (this.endpoint == null) {
			logger.error("There is no 'endpoint' property in the BeCPGSSOAuthenticationFilter bean parameters. Cannot initialise filter.");
			return;
		}

		// Get the endpoint descriptor and check if external auth is enabled
		EndpointDescriptor endpointDescriptor = remoteConfig.getEndpointDescriptor(endpoint);
		if ((endpointDescriptor == null) || !endpointDescriptor.getExternalAuth()) {
			if (logger.isDebugEnabled()) {
				logger.debug("No External Auth endpoint configured for " + endpoint);
			}

			// endpoint is set via bean config - so if no config is using the filter we disable it now
			this.endpoint = null;

			return;
		}

		try {
			Connector conn = this.connectorService.getConnector(endpoint);

			if (logger.isDebugEnabled()) {
				logger.debug("Endpoint is " + endpoint);
			}

            // Obtain the resource (if configured) from the OAuth2 connector
           if(conn instanceof BeCPGExternalConnector) {
			
			// Obtain the userHeader (if configured) from the alfresco connector
			this.userHeader = conn.getConnectorSession().getParameter(BeCPGExternalConnector.CS_PARAM_USER_HEADER);
			String connectorUserIdPattern = conn.getConnectorSession().getParameter(BeCPGExternalConnector.CS_PARAM_USER_ID_PATTERN);
			if (connectorUserIdPattern != null) {
				this.userIdPattern = Pattern.compile(connectorUserIdPattern);
			}
			if (logger.isDebugEnabled()) {
				logger.debug("userHeader is " + userHeader);
				logger.debug("userIdPattern is " + connectorUserIdPattern);
			}
			
            }
		} catch (ConnectorServiceException e) {
			logger.error("Unable to find connector " + endpointDescriptor.getConnectorId() + " for the endpoint " + endpoint, e);
		}

		  // retrieve the OAuth2 configuration
        OAuth2ConfigElement oauth2Config = (OAuth2ConfigElement) configService.getConfig("oauth2").getConfigElement(OAuth2ConfigElement.CONFIG_ELEMENT_ID);
        if (oauth2Config != null) {
            // If we don't have a resource from connector, use the one from config
            if (this.resource == null || this.resource.isEmpty()) {
                this.resource = oauth2Config.getResource();
            }
            
            // Get the principal attribute if specified
            String configPrincipalAttr = oauth2Config.getPrincipalAttribute();
            if (configPrincipalAttr != null && !configPrincipalAttr.isEmpty()) {
                this.principalAttribute = configPrincipalAttr;
            }
            
            if (logger.isDebugEnabled()) {
                logger.debug("Using resource: " + this.resource);
                logger.debug("Using principalAttribute: " + this.principalAttribute);
            }
            
          
            
            
            // Initialize the identity service facade
            try {
            	
            	IdentityServiceFacadeFactoryBean factory = new IdentityServiceFacadeFactoryBean(oauth2Config.toConfig());
            	
                // Create identity service facade
                this.identityServiceFacade =factory.createIdentityServiceFacade();
                
                if (logger.isDebugEnabled()) {
                    logger.debug("Identity service facade initialized successfully");
                }
            } catch (Exception e) {
                logger.error("Failed to initialize identity service facade", e);
            }
        }
        
		
		
		if (logger.isInfoEnabled()) {
			logger.info("SSOAuthenticationFilter initialised.");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.context = applicationContext;
	}

	/**
	 * <p>Setter for the field <code>endpoint</code>.</p>
	 *
	 * @param endpoint a {@link java.lang.String} object.
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * <p>wrapHeaderAuthenticatedRequest.</p>
	 *
	 * @param sreq a {@link jakarta.servlet.ServletRequest} object.
	 * @return a {@link jakarta.servlet.ServletRequest} object.
	 */
	protected ServletRequest wrapHeaderAuthenticatedRequest(ServletRequest sreq) {
		if ((userHeader != null) && (sreq instanceof final HttpServletRequest req)) {
			sreq = new HttpServletRequestWrapper(req) {
				@Override
				public String getRemoteUser() {
					// MNT-11041 Share SSOAuthenticationFilter and non-ascii username strings
					String remoteUser = req.getHeader(userHeader);
					if (remoteUser != null) {
						if (!org.apache.commons.codec.binary.Base64.isBase64(remoteUser)) {
							remoteUser = new String(remoteUser.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
						}
						remoteUser = extractUserFromProxyHeader(remoteUser);
					} else {
						remoteUser = super.getRemoteUser();
					}
					return remoteUser;
				}

				/**
				 * Extracts a user ID from the proxy header. If a user ID pattern has been configured returns the contents of the
				 * first matching regular expression group or <code>null</code>. Otherwise returns the trimmed header contents or
				 * <code>null</code>.
				 */
				private String extractUserFromProxyHeader(String userId) {
					if (userIdPattern == null) {
						userId = userId.trim();
					} else {
						Matcher matcher = userIdPattern.matcher(userId);
						if (matcher.matches()) {
							userId = matcher.group(1).trim();
						} else {
							return null;
						}
					}
					return userId.isBlank() ? null : userId;
				}
			};
		} else  {
			   if (sreq instanceof HttpServletRequest) {
		            final HttpServletRequest req = (HttpServletRequest)sreq;
		            
		            // Get OAuth2 user info from Spring Security context or session
		            final String username = getOAuth2Username(req);
		            
		            if (username != null) {
		                return new HttpServletRequestWrapper(req) {
		                    @Override
		                    public String getRemoteUser() {
		                        return username;
		                    }
		                    
		                    @Override
		                    public boolean isUserInRole(String role) {
		                        return true;
		                    }
		                    
		                    @Override
		                    public Principal getUserPrincipal() {
		                        return () -> username;
		                    }
		                };
		            }
		        }
		        return sreq;
			
		}
				
		return sreq;
	}
	
    
    /**
     * Get username from OAuth2 authentication
     * 
     * @param req the HTTP request
     * @return the authenticated username or null if not authenticated
     */
    private String getOAuth2Username(HttpServletRequest req) {
        // First try to get from Spring Security context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) auth;
            OAuth2User oauth2User = oauthToken.getPrincipal();
            
            // Save OAuth2 user in session for future requests
            req.getSession().setAttribute(OAUTH2_SESSION_ATTRIBUTE, oauth2User);
            
            Object userAttr = oauth2User.getAttribute(principalAttribute);
            if (userAttr != null) {
                return userAttr.toString();
            }
            
            // Fallback to name if attribute not found
            return oauth2User.getName();
        }
        
        // If not in security context, try session
        HttpSession session = req.getSession(false);
        if (session != null) {
            OAuth2User oauth2User = (OAuth2User) session.getAttribute(OAUTH2_SESSION_ATTRIBUTE);
            if (oauth2User != null) {
                Object userAttr = oauth2User.getAttribute(principalAttribute);
                if (userAttr != null) {
                    return userAttr.toString();
                }
                return oauth2User.getName();
            }
        }
        
        // Check for bearer token in Authorization header and validate using identity service facade
        String authHeader = req.getHeader(HEADER_AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ") && identityServiceFacade != null) {
            try {
                String token = authHeader.substring(7);
               
                return identityServiceFacade.getUserInfo(token, principalAttribute)
                    .map(OIDCUserInfo::username)
                    .orElse(null);
            } catch (Exception e) {
                logger.warn("Error validating OAuth2 token", e);
            }
        }
        
        return null;
    }

	/** {@inheritDoc} */
	@Override
	public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		doFilter(request, response, chain);
	}

	/**
	 * Run the filter
	 *
	 * @param sreq ServletRequest
	 * @param sresp ServletResponse
	 * @param chain FilterChain
	 * @exception IOException
	 * @exception ServletException
	 * @throws java.io.IOException if any.
	 * @throws jakarta.servlet.ServletException if any.
	 */
	public void doFilter(ServletRequest sreq, ServletResponse sresp, FilterChain chain) throws IOException, ServletException {
		NDC.remove();
		NDC.push(Thread.currentThread().getName());
		final boolean debug = logger.isDebugEnabled();

		// Wrap externally authenticated requests that provide the user in an HTTP header
		// with one that returns the correct name from getRemoteUser(). For use in our own
		// calls to this method and any chained filters.
		sreq = wrapHeaderAuthenticatedRequest(sreq);

		// Bypass the filter if we don't have an endpoint with external auth enabled
		if (this.endpoint == null) {
			if (debug) {
				logger.debug("There is no endpoint with external auth enabled.");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		// Get the HTTP request/response/session
		HttpServletRequest req = (HttpServletRequest) sreq;
		HttpServletResponse res = (HttpServletResponse) sresp;
		HttpSession session = req.getSession();

		if ((req.getServletPath() != null)
				&& (req.getServletPath().startsWith(UNAUTHENTICATED_ACCESS_PROXY) || req.getServletPath().startsWith(AI_ACCESS_PROXY))) {
			if (debug) {
				logger.debug("SSO is by-passed for unauthenticated access endpoint.");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		if (debug) {
			logger.debug("Processing request " + req.getRequestURI() + " SID:" + session.getId());
		}

		// Login page or login submission
		String pathInfo = req.getPathInfo();
		if (PAGE_SERVLET_PATH.equals(req.getServletPath())
				&& (LOGIN_PATH_INFORMATION.equals(pathInfo) || ((pathInfo == null) && LOGIN_PARAMETER.equals(req.getParameter("pt"))))) {
			if (debug) {
				logger.debug("Login page requested, chaining ...");
			}

			// Chain to the next filter
			chain.doFilter(sreq, sresp);
			return;
		}

		// initialize a new request context
		RequestContext rcontext = null;
		try {
			// perform a "silent" init - i.e. no user creation or remote connections
			rcontext = RequestContextUtil.initRequestContext(this.context, req, true);
		} catch (Exception ex) {
			logger.error("Error calling initRequestContext", ex);
			throw new ServletException(ex);
		}

		// get the page from the model if any - it may not require authentication
		Page page = rcontext.getPage();
		if ((page == null) && (pathInfo != null)) {
			// we didn't find a page - this may be a top-level URL call - so attempt to manually resolve the page
			PageViewResolver pageViewResolver = (PageViewResolver) this.context.getBean(PAGE_VIEW_RESOLVER);
			if (pageViewResolver != null) {
				try {
					// as a side-effect of resolving the view ID into an View object
					// the Page context will be updated on the request context for us
					if (pageViewResolver.resolveViewName(pathInfo, null) != null) {
						page = rcontext.getPage();
					}
				} catch (Exception e) {
					// OK to fall back to null page reference if this happens
				}
			}
		}
		if ((page != null) && (page.getAuthentication() == RequiredAuthentication.none)) {
			if (logger.isDebugEnabled()) {
				logger.debug("Unauthenticated page requested - skipping auth filter...");
			}
			chain.doFilter(sreq, sresp);
			return;
		}

		// Check for OAuth2 authentication first
		String oauth2Username = getOAuth2Username(req);
		if (oauth2Username != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("OAuth2 authenticated user: " + oauth2Username + " - skipping further auth checks");
			}
			// We have a valid OAuth2 authenticated user
			setExternalAuthSession(session);
			onSuccess(req, res, session, oauth2Username);
			chain.doFilter(sreq, sresp);
			return;
		}

		// If userHeader (X-Alfresco-Remote-User or similar) external auth - does not require a challenge/response
		if (this.userHeader != null) {
			String userId = AuthenticationUtil.getUserId(req);
			if ((userId != null) && (req.getRemoteUser() != null) && !req.getRemoteUser().isEmpty()) {
				if (logger.isDebugEnabled()) {
					logger.debug("userHeader external auth - skipping auth filter...");
				}
				setExternalAuthSession(session);
				onSuccess(req, res, session, req.getRemoteUser());
				chain.doFilter(sreq, sresp);
			} else {
				// initial external user login requires a ping check to authenticate remote Session
				challengeOrPassThrough(chain, req, res, session);
			}
			return;
		}

		// Check if there is an authorization header with a challenge response
		String authHdr = req.getHeader(HEADER_AUTHORIZATION);

		// We are not passing on a challenge response and we have sufficient client session information
		if ((authHdr == null) && AuthenticationUtil.isAuthenticated(req)) {
			if (debug) {
				logger.debug("Touching the repo to ensure we still have an authenticated session.");
			}
			challengeOrPassThrough(chain, req, res, session);
			return;
		}

		// Check the authorization header
		if (authHdr == null) {
			if (debug) {
				logger.debug("New auth request from " + req.getRemoteHost() + " (" + req.getRemoteAddr() + ":" + req.getRemotePort() + ")");
			}
			challengeOrPassThrough(chain, req, res, session);
		}
		// Possibly basic auth - allow through
		else {
			if (debug) {
				logger.debug("Processing Basic Authentication.");
			}
			// ACE-3257 fix, it looks like basic auth header was sent.
			// However lets check for presence of remote_user CGI variable in AJP.
			// If remote user is not null then it most likely that apache proxy with mod_auth_basic module is used
			if (AuthenticationUtil.isAuthenticated(req) || (req.getRemoteUser() != null)) {
				if (debug) {
					logger.debug("Ensuring the session is still valid.");
				}
				challengeOrPassThrough(chain, req, res, session);
			} else {
				if (debug) {
					logger.debug("Establish a new session or bring up the login page.");
				}
				chain.doFilter(req, res);
			}
		}
	}

	/**
	 * Removes all attributes stored in session
	 *
	 * @param session Session
	 */
	private void clearSession(HttpSession session) {
		if (logger.isDebugEnabled()) {
			logger.debug("Clearing the session.");
		}
		Enumeration<String> names = session.getAttributeNames();
		while (names.hasMoreElements()) {
			session.removeAttribute(names.nextElement());
		}
	}

	private void challengeOrPassThrough(FilterChain chain, HttpServletRequest req, HttpServletResponse res, HttpSession session)
		throws IOException, ServletException {
		try {
			// In this mode we can only use vaulted credentials. Do not proxy any request headers.
			String userId = AuthenticationUtil.getUserId(req);

			// Check for OAuth2 authentication first
			String oauth2Username = getOAuth2Username(req);
			if (oauth2Username != null) {
				// We have a valid OAuth2 authenticated user
				userId = oauth2Username;
				
				// Set the external auth flag so the UI knows we are using OAuth2
				session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH, Boolean.TRUE);
				if (logger.isDebugEnabled()) {
					logger.debug("OAuth2 authenticated user: " + userId);
				}
				
				// Process the successful OAuth2 authentication
				onSuccess(req, res, session, userId);
				chain.doFilter(req, res);
				return;
			}

			if (userId == null) {
				// If we are as yet unauthenticated but have external authentication, do a ping check as the external user.
				// This will either establish the session or throw us out to log in as someone else!
				userId = req.getRemoteUser();
				// Set the external auth flag so the UI knows we are using SSO etc.
				session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH, Boolean.TRUE);
				if ((userId != null) && logger.isDebugEnabled()) {
					logger.debug("Initial login from externally authenticated user " + userId);
				}
				setExternalAuthSession(session);
			} else if (logger.isDebugEnabled()) {
				logger.debug("Validating repository session for " + userId);
			}

			if ((userId != null) ) // Firefox & Chrome hack for MNT-15561
			{
				session.removeAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH);
			}

			Connector conn = connectorService.getConnector(this.endpoint, userId, session);

			// ALF-10785: We must pass through the language header to set up the session in the correct locale
			ConnectorContext ctx;
			if (req.getHeader(HEADER_ACCEPT_LANGUAGE) != null) {
				if (logger.isDebugEnabled()) {
					logger.debug("Accept-Language header present: " + req.getHeader(HEADER_ACCEPT_LANGUAGE));
				}
				ctx = new ConnectorContext(null, Collections.singletonMap(HEADER_ACCEPT_LANGUAGE, req.getHeader(HEADER_ACCEPT_LANGUAGE)));
			} else {
				ctx = new ConnectorContext();
			}

			Response remoteRes = conn.call("/touch", ctx);
			if (Status.STATUS_UNAUTHORIZED == remoteRes.getStatus().getCode()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Repository session timed out - restarting auth process...");
				}

				String authHdr = remoteRes.getStatus().getHeaders().get(HEADER_WWWAUTHENTICATE);
				//beCPG this.userHeader == null do not need to provide basic auth if external is setup

				if ((authHdr != null) && (this.userHeader == null)) {
					// restart SSO login as the repo has timed us out
					restartAuthProcess(session, req, res, authHdr);
				} else {
					// Don't invalidate the session if we've already got external authentication - it may result in us
					// having to reauthenticate externally too!
					if (req.getRemoteUser() == null) {
						try {
							session.invalidate();
						} catch (IllegalStateException e) {
							// may already been invalidated elsewhere
						}
					}
					// restart manual login
					redirectToLoginPage(req, res);
				}
			} else {
				onSuccess(req, res, session, userId);

				// we have local auth in the session and the repo session is also valid
				// this means we do not need to perform any further auth handshake
				if (logger.isDebugEnabled()) {
					logger.debug("Authentication not required, chaining ...");
				}

				chain.doFilter(req, res);
			}
		} catch (ConnectorServiceException cse) {
			throw new PlatformRuntimeException("Incorrectly configured endpoint ID: " + this.endpoint);
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see jakarta.servlet.Filter#destroy()
	 */
	/**
	 * <p>destroy.</p>
	 */
	public void destroy() {
		// Nothing to do
	}
	
	/**
	 * Get the Identity Service Facade instance used by this filter.
	 * 
	 * @return the Identity Service Facade or null if not initialized
	 */
	public IdentityServiceFacade getIdentityServiceFacade() {
		return this.identityServiceFacade;
	}

	/**
	 * Restart the authentication process for NTLM or Kerberos - clear current security details
	 */
	private void restartAuthProcess(HttpSession session, HttpServletRequest req, HttpServletResponse res, String authHdr) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Restarting " + authHdr + " authentication.");
		}

		// Clear any cached logon details from the sessiom
		clearSession(session);
		setRedirectUrl(req);

		// restart the authentication process for NTLM
		res.setHeader(HEADER_WWWAUTHENTICATE, authHdr);
		res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		res.setContentType(MIME_HTML_TEXT);

		final PrintWriter out = res.getWriter();
		out.println("<html><head>");
		out.println("<meta http-equiv=\"Refresh\" content=\"0; url=" + req.getContextPath() + "/page?pt=login" + "\">");
		out.println("</head><body><p>Please <a href=\"" + req.getContextPath() + "/page?pt=login" + "\">log in</a>.</p>");
		out.println("</body></html>");
		out.close();

		res.flushBuffer();
	}

	/**
	 * Redirect to the root of the website - ignore further SSO auth requests
	 */
	private void redirectToLoginPage(HttpServletRequest req, HttpServletResponse res) throws IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Redirecting to the login page.");
		}

		//Patch beCPG refs #3825
		if (PAGE_SERVLET_PATH.equals(req.getServletPath()) || req.getRequestURI().contains("proxy/alfresco/slingshot/node/content")) {
			// redirect via full page redirect
			setRedirectUrl(req);

			String error = req.getParameter(ERROR_PARAMETER);
			res.sendRedirect(req.getContextPath() + "/page?pt=login" + (error == null ? "" : "&" + ERROR_PARAMETER + "=" + error));
		} else {
			// redirect via 401 response code handled by XHR processing on the client
			res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			res.flushBuffer();
		}
	}

	/**
	 * Set the {@link org.alfresco.web.site.SlingshotPageView#REDIRECT_URI}
	 * <br> and {@link org.alfresco.web.site.SlingshotPageView#REDIRECT_QUERY}
	 * <br> parameters to the session.
	 *
	 * @param req
	 */
	private void setRedirectUrl(HttpServletRequest req) {
		HttpSession session = req.getSession();
		session.setAttribute(REDIRECT_URI, req.getRequestURI());
		if (req.getQueryString() != null) {
			session.setAttribute(REDIRECT_QUERY, req.getQueryString());
		}
	}

	/**
	 * Set the external auth Session flag so the UI knows we are using SSO.
	 * A number of elements in an application may depend on this state e.g. Logout button shown etc.
	 *
	 * @param session
	 */
	private void setExternalAuthSession(HttpSession session) {
		session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH, Boolean.TRUE);
	}

	/**
	 * Success login method handler.
	 *
	 * @param req current http request
	 * @param res current http response
	 * @param session current session
	 * @param username logged in user name
	 */
	private void onSuccess(HttpServletRequest req, HttpServletResponse res, HttpSession session, String username) {
		// Ensure User ID is in session so the web-framework knows we have logged in
		session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, username);

		try {
			// Inform the Slingshot login controller of a successful login attempt as further processing may be required
			beforeSuccess(req, res);
		} catch (Exception e) {
			throw new AlfrescoRuntimeException("Error during loginController.onSuccess()", e);
		}
	}

	/** Constant <code>SESSION_ATTRIBUTE_KEY_USER_GROUPS="_alf_USER_GROUPS"</code> */
	private static final String SESSION_ATTRIBUTE_KEY_USER_GROUPS = "_alf_USER_GROUPS";

	/** Constant <code>PARAM_USERNAME="username"</code> */
	protected static final String PARAM_USERNAME = "username";

	/**
	 * <p>beforeSuccess.</p>
	 *
	 * @param request a {@link jakarta.servlet.http.HttpServletRequest} object.
	 * @param response a {@link jakarta.servlet.http.HttpServletResponse} object.
	 * @throws java.lang.Exception if any.
	 */
	protected void beforeSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception {
		try {
			final HttpSession session = request.getSession();

			// Get the authenticated user name and use it to retrieve all of the groups that the user is a member of...
			String username = request.getParameter(PARAM_USERNAME);
			if (username == null) {
				username = (String) session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
			}

			if ((username != null) && (session.getAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS) == null)) {
				Connector conn = FrameworkUtil.getConnector(session, username, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
				ConnectorContext c = new ConnectorContext(HttpMethod.GET);
				c.setContentType("application/json");
				Response res = conn.call("/api/people/" + URLEncoder.encode(username) + "?groups=true", c);
				if (Status.STATUS_OK == res.getStatus().getCode()) {
					// Assuming we get a successful response then we need to parse the response as JSON and then
					// retrieve the group data from it...
					//
					// Step 1: Get a String of the response...
					String resStr = res.getResponse();

					// Step 2: Parse the JSON...
					JSONParser jp = new JSONParser();
					Object userData = jp.parse(resStr);

					// Step 3: Iterate through the JSON object getting all the groups that the user is a member of...
					StringBuilder groups = new StringBuilder(512);
					if (userData instanceof JSONObject) {
						Object groupsArray = ((JSONObject) userData).get("groups");
						if (groupsArray instanceof org.json.simple.JSONArray) {
							for (Object groupData : (org.json.simple.JSONArray) groupsArray) {
								if (groupData instanceof JSONObject) {
									Object groupName = ((JSONObject) groupData).get("itemName");
									if (groupName != null) {
										groups.append(groupName.toString()).append(',');
									}
								}
							}
						}
					}

					// Step 4: Trim off any trailing commas...
					if (groups.length() != 0) {
						groups.delete(groups.length() - 1, groups.length());
					}

					// Step 5: Store the groups on the session...
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, groups.toString());
				} else {
					session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, "");
				}
			}
		} catch (ConnectorServiceException e1) {
			throw new Exception("Error creating remote connector to request user group data.");
		}
	}

	@Override
	public void handle(Callback[] arg0) throws IOException, UnsupportedCallbackException {
		//Do Nothing

	}

}

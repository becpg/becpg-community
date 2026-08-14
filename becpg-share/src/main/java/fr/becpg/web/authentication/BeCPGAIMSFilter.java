/*
 * Copyright 2005 - 2020 Alfresco Software Limited.
 *
 * This file is part of the Alfresco software.
 * If the software was purchased under a paid Alfresco license, the terms of the paid license agreement will prevail.
 * Otherwise, the software is provided under the following open source license terms:
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
 */
package fr.becpg.web.authentication;

import static java.util.Objects.requireNonNull;
import static org.alfresco.web.site.servlet.config.SecurityUtils.convert;
import static org.alfresco.web.site.servlet.config.SecurityUtils.isAuthorizationResponse;
import static org.alfresco.web.site.servlet.config.SecurityUtils.toMultiMap;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.web.site.servlet.SlingshotLoginController;
import org.alfresco.web.site.servlet.config.AIMSConfig;
import org.alfresco.web.site.servlet.config.SecurityUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.context.ApplicationContext;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.FrameworkUtil;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.ServletUtil;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.ConnectorServiceException;
import org.springframework.extensions.surf.exception.RequestContextException;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.site.AuthenticationUtil;
import org.springframework.extensions.surf.support.AlfrescoUserFactory;
import org.springframework.extensions.surf.support.ServletRequestContextFactory;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.connector.AlfrescoAuthenticator;
import org.springframework.extensions.webscripts.connector.Connector;
import org.springframework.extensions.webscripts.connector.ConnectorContext;
import org.springframework.extensions.webscripts.connector.ConnectorService;
import org.springframework.extensions.webscripts.connector.CredentialVault;
import org.springframework.extensions.webscripts.connector.Credentials;
import org.springframework.extensions.webscripts.connector.HttpMethod;
import org.springframework.extensions.webscripts.connector.Response;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.ClientAuthorizationRequiredException;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.RestClientRefreshTokenTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2RefreshTokenGrantRequest;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenDecoderFactory;
import org.springframework.security.oauth2.client.oidc.authentication.OidcIdTokenValidator;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.core.converter.ClaimTypeConverter;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.RequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.security.web.util.UrlUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * <p>BeCPGAIMSFilter class.</p>
 *
 * @author matthieu
 */
public class BeCPGAIMSFilter implements Filter
{
    /** Constant <code>LOGGER</code> */
    private static final Log LOGGER = LogFactory.getLog(BeCPGAIMSFilter.class);

    private ApplicationContext context;
    private ConnectorService connectorService;
    @SuppressWarnings("unused")
    private SlingshotLoginController loginController;
    
    private ConfigService configService;

    private boolean enabled = false;

    private String principalAttribute;

    /** Constant <code>ALFRESCO_ENDPOINT_ID="alfresco"</code> */
    public static final String ALFRESCO_ENDPOINT_ID = "alfresco";
    /** Constant <code>ALFRESCO_API_ENDPOINT_ID="alfresco-api"</code> */
    public static final String ALFRESCO_API_ENDPOINT_ID = "alfresco-api";
    /** Constant <code>SHARE_AIMS_LOGOUT="/page/aims/logout"</code> */
    public static final String SHARE_AIMS_LOGOUT = "/page/aims/logout";
    /** Constant <code>SHARE_PAGE="/page"</code> */
    public static final String SHARE_PAGE = "/page";
    /** Constant <code>DEFAULT_AUTHORIZATION_REQUEST_BASE_URI="/oauth2/authorization"</code> */
    public static final String DEFAULT_AUTHORIZATION_REQUEST_BASE_URI = "/oauth2/authorization";
    /** Constant <code>SHARE_AIMS_LOGIN_PAGE="/page/aims-login"</code> */
    public static final String SHARE_AIMS_LOGIN_PAGE = "/page/aims-login";
    /** Constant <code>SHARE_AIMS_DOLOGIN="/page/aims-dologin"</code> */
    public static final String SHARE_AIMS_DOLOGIN = "/page/aims-dologin";

    private ClientRegistrationRepository clientRegistrationRepository;
    private OAuth2AuthorizedClientService oauth2ClientService;
    private final RedirectStrategy authorizationRedirectStrategy;
    private OAuth2AuthorizationRequestResolver authorizationRequestResolver;
    private RequestCache requestCache;
    private AuthorizationRequestRepository<OAuth2AuthorizationRequest> authorizationRequestRepository;
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource = new WebAuthenticationDetailsSource();
    private final RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();
    private final OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient = new RestClientAuthorizationCodeTokenResponseClient();
    private final OAuth2AccessTokenResponseClient<OAuth2RefreshTokenGrantRequest> refreshTokenResponseClient = new RestClientRefreshTokenTokenResponseClient();
    private ThrowableAnalyzer throwableAnalyzer;
    private final JwtDecoderFactory<ClientRegistration> jwtDecoderFactory = new OidcIdTokenDecoderFactory();
    private final GrantedAuthoritiesMapper authoritiesMapper = (authorities) -> {
        return authorities;
    };
    private final OAuth2UserService<OidcUserRequest, OidcUser> userService = new OidcUserService();
    private String clientId;
    private String audience;
    private String shareContext;

    /**
     * Margin applied before the real access-token expiry so the token is refreshed slightly ahead of time.
     * This absorbs clock skew between Share and the identity provider and avoids using a token that would
     * expire in the middle of a downstream request.
     */
    private static final Duration TOKEN_EXPIRY_REFRESH_MARGIN = Duration.ofSeconds(60);

    /**
     * Identity provider error codes telling the refresh token itself was rejected. Any other failure is treated
     * as transient, so that a temporary problem with the identity provider does not destroy the user session.
     */
    private static final Set<String> UNRECOVERABLE_REFRESH_ERROR_CODES =
        Set.of(OAuth2ErrorCodes.INVALID_GRANT, OAuth2ErrorCodes.INVALID_TOKEN, OAuth2ErrorCodes.INVALID_CLIENT,
               OAuth2ErrorCodes.UNAUTHORIZED_CLIENT);

    /**
     * <p>Constructor for BeCPGAIMSFilter.</p>
     */
    public BeCPGAIMSFilter()
    {
        this.authorizationRedirectStrategy = new DefaultRedirectStrategy();
    }

    /**
     * {@inheritDoc}
     *
     * Initialize the filter
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
        // Info
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Initializing the AIMS filter.");
        }

        this.context = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.getServletContext());

        AIMSConfig config = (AIMSConfig) this.context.getBean("aims.config");
        this.enabled = config.isEnabled();
        if (this.enabled)
        {
            this.clientId = config.getResource();
            this.principalAttribute = config.getPrincipalAttribute();
            this.audience = config.getAudience();
            // OIDC Specific Setup
            clientRegistrationRepository = context.getBean(ClientRegistrationRepository.class);
            oauth2ClientService = context.getBean(OAuth2AuthorizedClientService.class);
            configService = (ConfigService) context.getBean("web.config");
            this.requestCache = new HttpSessionRequestCache();
            
            Config reauthConfig = this.configService.getConfig("AIMS");
            String specificReauthIdcId = reauthConfig.getConfigElement("specificReauthIdcId").getValue();
            
            
            this.authorizationRequestResolver = new BeCPGCustomAuthorizationRequestResolver(clientRegistrationRepository,
                                                                                       DEFAULT_AUTHORIZATION_REQUEST_BASE_URI,
                                                                                       config,
                                                                                       specificReauthIdcId);
            this.authorizationRequestRepository = new HttpSessionOAuth2AuthorizationRequestRepository();
            this.throwableAnalyzer = new SecurityUtils.DefaultThrowableAnalyzer();
            this.shareContext = config.getShareContext();
        }
        this.connectorService = (ConnectorService) context.getBean("connector.service");
        this.loginController = (SlingshotLoginController) context.getBean("loginController");

        // Info
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("beCPG - AIMS filter initialized.");
        }
    }

    /** {@inheritDoc} */
    public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
        throws IOException, ServletException
    {
        HttpServletRequest request = (HttpServletRequest) sreq;
        HttpServletResponse response = (HttpServletResponse) sres;
        HttpSession session = request.getSession();
        boolean isAuthenticated = false;
        /**
         * check if authentication is done.
         */
        if (null != session && this.enabled)
        {
            SecurityContext attribute = (SecurityContext) session.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
            if (null != attribute)
            {
                isAuthenticated = attribute.getAuthentication()
                    .isAuthenticated();

                /**
                 * Check if token existing token is valid or expired.
                 */
                if (isAuthenticated)
                {
                    try
                    {
                        OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken =
                            (OAuth2LoginAuthenticationToken) attribute.getAuthentication();
                        OAuth2AccessToken oAuth2AccessToken = oAuth2LoginAuthenticationToken.getAccessToken();
                        if (isAuthTokenExpired(oAuth2AccessToken.getExpiresAt()))
                        {
                            // The token refresh below reaches Surf services (connector, authorized-client store)
                            // that expect a RequestContext bound to the thread. Unlike the initial login path,
                            // the AIMS filter runs before Share establishes it, so bind it here to avoid a
                            // NullPointerException that would otherwise invalidate the session on every refresh.
                            this.initRequestContext(request, response);
                            refreshToken(attribute, session, request);
                        }
                    }
                    catch (Exception refreshException)
                    {
                        if (isUnrecoverableRefreshFailure(refreshException))
                        {
                            LOGGER.error("Error while refreshing OAuth2 token, invalidating session (URI="
                                             + request.getRequestURI() + "): " + refreshException.getMessage(),
                                         refreshException);
                            session.invalidate();
                            if (!request.getRequestURI()
                                .contains(this.shareContext + SHARE_AIMS_LOGOUT))
                            {
                                isAuthenticated = false;
                            }
                        }
                        else
                        {
                            // A transient failure, such as an unreachable identity provider, must not cost the user
                            // its session: the current token is still usable and the next request retries the refresh.
                            LOGGER.warn("Transient failure while refreshing OAuth2 token, keeping the session (URI="
                                            + request.getRequestURI() + "): " + refreshException.getMessage(),
                                        refreshException);
                        }
                    }
                }
            }
        }
        
        if (this.enabled && request.getRequestURI().contains(SHARE_AIMS_LOGIN_PAGE) &&
            "true".equalsIgnoreCase(request.getParameter("prompt"))) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Explicit re-authentication requested for URI: " + request.getRequestURI() + " due to prompt=true.");
            }

            if (isAuthenticated) {
                LOGGER.info("User is currently authenticated, but prompt=true found. Invalidating session to force IdP re-authentication.");
                if (session != null) {
                    session.invalidate();
                    session = null; // Nullify after invalidation for subsequent logic
                }
                SecurityContextHolder.clearContext();
                isAuthenticated = false; // Update local state for the rest of this filter invocation
            } else {
                LOGGER.debug("prompt=true found, user is already unauthenticated. Proceeding to login normally.");
            }
        }

        if (!isAuthenticated && this.enabled && (request.getRequestURI().contains(this.shareContext + SHARE_PAGE) || request.getRequestURI().contains(this.shareContext + SHARE_AIMS_LOGOUT)))
        {
            /**
             // Match the request that came from Idp (redirect uri)
             */
            if (this.matchesAuthorizationResponse(request))
            {
                this.processAuthorizationResponse(request, response, session);
            }
            else
            {
                if (LOGGER.isDebugEnabled())
                {
                    LOGGER.debug("AIMS re-auth for URI=" + request.getRequestURI() + " sessionIdValid="
                                     + request.isRequestedSessionIdValid() + " newSession=" + session.isNew());
                }
                try
                {

                    // Bypass the AIMS login page where we handle the redirect url to include query and fragments
                    if (request.getRequestURI().contains(SHARE_AIMS_LOGIN_PAGE))
                    {
                        chain.doFilter(request, response);
                        return;
                    }

                    // Check if the request is to aims-dologin where we have the correct redirect URL, if not, redirect
                    // to the aims login page
                    if (!request.getRequestURI().contains(SHARE_AIMS_DOLOGIN))
                    {
                        this.sendRedirectForPreLogin(request, response);
                        return;
                    }

                    this.requestCache.saveRequest(request, response);
                    OAuth2AuthorizationRequest authorizationRequest =
                        this.authorizationRequestResolver.resolve(request, this.clientId);
                    if (authorizationRequest != null)
                    {
                        this.sendRedirectForAuthorization(request, response, authorizationRequest);
                        return;
                    }
                }
                catch (Exception var11)
                {
                    this.unsuccessfulRedirectForAuthorization(response);
                    return;
                }

                try
                {
                    chain.doFilter(request, response);
                }
                catch (IOException var9)
                {
                    throw var9;
                }
                catch (Exception var10)
                {
                    Throwable[] causeChain = this.throwableAnalyzer.determineCauseChain(var10);
                    ClientAuthorizationRequiredException authzEx =
                        (ClientAuthorizationRequiredException) this.throwableAnalyzer.getFirstThrowableOfType(
                            ClientAuthorizationRequiredException.class, causeChain);
                    if (authzEx != null)
                    {
                        try
                        {
                            OAuth2AuthorizationRequest authorizationRequest =
                                this.authorizationRequestResolver.resolve(request, authzEx.getClientRegistrationId());
                            if (authorizationRequest == null)
                            {
                                throw authzEx;
                            }

                            this.sendRedirectForAuthorization(request, response, authorizationRequest);
                            this.requestCache.saveRequest(request, response);
                        }
                        catch (Exception var8)
                        {
                            this.unsuccessfulRedirectForAuthorization(response);
                        }

                    }
                    else if (var10 instanceof ServletException)
                    {
                        throw (ServletException) var10;
                    }
                    else if (var10 instanceof RuntimeException)
                    {
                        throw (RuntimeException) var10;
                    }
                    else
                    {
                        throw new RuntimeException(var10);
                    }
                }
            }
        }
        else
        {
            // OAUTH redirected to aims-dologin, so now we need to redirect back to the original URL
            if (request.getRequestURI().contains(SHARE_AIMS_DOLOGIN))
            {
                this.sendRedirectToOriginalTarget(request, response);
                return;
            }

            chain.doFilter(sreq, sres);
        }
    }

	/**
	 * <p>onSuccess.</p>
	 *
	 * @param request              HTTP Servlet Request
	 * @param response             HTTP Servlet Response
	 * @param session              HTTP Session
	 * @param authenticationResult OAuth2LoginAuthenticationToken
	 */
    @SuppressWarnings("deprecation")
	private void onSuccess(HttpServletRequest request, HttpServletResponse response, HttpSession session,
                           OAuth2LoginAuthenticationToken authenticationResult)
    {
        // Info
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Completing the AIMS authentication.");
        }

        String username = authenticationResult.getPrincipal()
            .getAttribute(this.principalAttribute);
        String accessToken = authenticationResult.getAccessToken()
            .getTokenValue();
        synchronized (this)
        {
            try
            {
                // Init request context for further use on getting user
                this.initRequestContext(request, response);

                // Get the alfTicket from repo, using the JWT token from Idp
                String alfTicket = this.getAlfTicket(session, username, accessToken);
                if (alfTicket != null)
                {
                    this.bindAlfrescoSession(session, username, alfTicket);

                    // Inform the Slingshot login controller of a successful login attempt as further processing may be required ?
                     beforeSuccess(request, response);

                    // Initialise the user metadata object used by some web scripts
                    this.initUser(request);

                }
                else
                {
                    LOGGER.error("Could not get an alfTicket from Repository.");
                }
            }
            catch (Exception e)
            {
                throw new AlfrescoRuntimeException("Failed to complete AIMS authentication process.", e);
            }
        }
    }
    
    /**
     * Binds the Alfresco ticket and the credentials of the authenticated user to the session, so that repository
     * calls keep authenticating. Shared by the initial login and by the renewal that follows a token refresh.
     *
     * @param session the HTTP session
     * @param username the authenticated user name
     * @param alfTicket the Alfresco ticket to bind
     * @throws org.springframework.extensions.surf.exception.ConnectorServiceException if the connector cannot be obtained
     */
    private void bindAlfrescoSession(HttpSession session, String username, String alfTicket)
        throws ConnectorServiceException
    {
        // Ensure User ID is in session so the web-framework knows we have logged in
        session.setAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID, username);
        session.setAttribute(UserFactory.SESSION_ATTRIBUTE_EXTERNAL_AUTH_AIMS, true);

        // Set the alfTicket into connector's session for further use on repo calls (will be set on the RemoteClient)
        Connector connector = this.connectorService.getConnector(ALFRESCO_ENDPOINT_ID, username, session);
        connector.getConnectorSession()
            .setParameter(AlfrescoAuthenticator.CS_PARAM_ALF_TICKET, alfTicket);

        // Set credential username for further use on repo
        // if there is no pass, as in our case, there will be a "X-Alfresco-Remote-User" header set using this value
        CredentialVault vault = FrameworkUtil.getCredentialVault(session, username);
        Credentials credentials = vault.newCredentials(AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
        credentials.setProperty(Credentials.CREDENTIAL_USERNAME, username);
        vault.store(credentials);
    }

    /** Constant <code>SESSION_ATTRIBUTE_KEY_USER_GROUPS="_alf_USER_GROUPS"</code> */
	private static final String SESSION_ATTRIBUTE_KEY_USER_GROUPS = "_alf_USER_GROUPS";

	/** Constant <code>PARAM_USERNAME="username"</code> */
	protected static final String PARAM_USERNAME = "username";

	/**
	 * <p>beforeSuccess.</p>
	 *
	 * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
	 * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
	 * @throws java.lang.Exception if any.
	 */
    @SuppressWarnings("deprecation")
	protected void beforeSuccess(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        try
        {
            final HttpSession session = request.getSession();

            // Get the authenticated user name and use it to retrieve all of the groups that the user is a member of...
            String username = (String)request.getParameter(PARAM_USERNAME);
            if (username == null)
            {
                username = (String)session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_ID);
            }

            if (username != null && session.getAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS) == null)
            {
                Connector conn = FrameworkUtil.getConnector(session, username, AlfrescoUserFactory.ALFRESCO_ENDPOINT_ID);
                ConnectorContext c = new ConnectorContext(HttpMethod.GET);
                c.setContentType("application/json");
                Response res = conn.call("/api/people/" + URLEncoder.encode(username, StandardCharsets.UTF_8.toString()) + "?groups=true", c);
                if (Status.STATUS_OK == res.getStatus().getCode())
                {
                    String resStr = res.getResponse();
                    JSONParser jp = new JSONParser();
                    Object userData = jp.parse(resStr);

                    StringBuilder groups = new StringBuilder(512);
                    if (userData instanceof JSONObject)
                    {
                        Object groupsArray = ((JSONObject) userData).get("groups");
                        if (groupsArray instanceof org.json.simple.JSONArray)
                        {
                            for (Object groupData: (org.json.simple.JSONArray)groupsArray)
                            {
                                if (groupData instanceof JSONObject)
                                {
                                    Object groupName = ((JSONObject) groupData).get("itemName");
                                    if (groupName != null)
                                    {
                                        groups.append(groupName.toString()).append(',');
                                    }
                                }
                            }
                        }
                    }

                    if (!groups.isEmpty())
                    {
                        groups.delete(groups.length() - 1, groups.length());
                    }

                    session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, groups.toString());
                }
                else
                {
                    session.setAttribute(SESSION_ATTRIBUTE_KEY_USER_GROUPS, "");
                }
            }
        }
        catch (ConnectorServiceException e1)
        {
            throw new Exception("Error creating remote connector to request user group data.");
        }
    }

    /**
     * Initialise the request context and request attributes for further use by some web scripts
     * that require authentication
     *
     * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
     * @throws org.springframework.extensions.surf.exception.RequestContextException
     * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
     */
    private void initRequestContext(HttpServletRequest request, HttpServletResponse response)
        throws RequestContextException
    {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        if (context == null)
        {
            ServletRequestContextFactory factory =
                (ServletRequestContextFactory) this.context.getBean("webframework.factory.requestcontext.servlet");
            context = factory.newInstance(new ServletWebRequest(request));
            request.setAttribute(RequestContext.ATTR_REQUEST_CONTEXT, context);
        }

        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request, response));
        ServletUtil.setRequest(request);
    }

	/**
	 * Initialise the user meta data object and set it into the session and request context (_alf_USER_OBJECT)
	 *
	 * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
	 * @throws org.springframework.extensions.surf.exception.UserFactoryException
	 */
    @SuppressWarnings("deprecation")
	private void initUser(HttpServletRequest request) throws UserFactoryException
    {
        RequestContext context = ThreadLocalRequestContext.getRequestContext();
        if (context != null && context.getUser() == null)
        {
            String userEndpointId = (String) context.getAttribute(RequestContext.USER_ENDPOINT);
            UserFactory userFactory = context.getServiceRegistry()
                .getUserFactory();
            User user = userFactory.initialiseUser(context, request, userEndpointId);
            context.setUser(user);
        }
    }

    /**
     * Get an alfTicket using the JWT token from Identity Service
     *
     * @param session HTTP Session
     * @param username username
     * @param accessToken access token
     * @return The alfTicket
     * @throws org.springframework.extensions.surf.exception.ConnectorServiceException
     */
    private String getAlfTicket(HttpSession session, String username, String accessToken) throws ConnectorServiceException
    {
        if (LOGGER.isInfoEnabled())
        {
            LOGGER.info("Retrieving the Alfresco Ticket from Repository.");
        }

        String alfTicket = null;
        Connector connector = this.connectorService.getConnector(ALFRESCO_API_ENDPOINT_ID, username, session);
        ConnectorContext c = new ConnectorContext(HttpMethod.GET, null, Collections.singletonMap("Authorization", "Bearer " + accessToken));
        c.setContentType("application/json");
        Response r = connector.call("/-default-/public/authentication/versions/1/tickets/-me-?noCache=" + UUID.randomUUID().toString(), c);

        if (Status.STATUS_OK != r.getStatus().getCode())
        {
            if (LOGGER.isErrorEnabled())
            {
                LOGGER.error("Failed to retrieve Alfresco Ticket from Repository.");
            }
        }
        else
        {
            try
            {
                JSONObject json = new JSONObject(r.getText());
                alfTicket = json.getJSONObject("entry")
                    .getString("id");
            }
            catch (JSONException e)
            {
                if (LOGGER.isErrorEnabled())
                {
                    LOGGER.error("Failed to parse Alfresco Ticket from Repository response.");
                }
            }
        }

        return alfTicket;
    }

    /**
     * <p>matchesAuthorizationResponse.</p>
     *
     * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
     * @return a boolean
     */
    private boolean matchesAuthorizationResponse(HttpServletRequest request) {
        MultiValueMap<String, String> params = toMultiMap(request.getParameterMap());
        if (!isAuthorizationResponse(params)) {
            return false;
        } else {
            OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository
                .loadAuthorizationRequest(request);
            if (authorizationRequest == null) {
                return false;
            } else {
                UriComponents requestUri = UriComponentsBuilder.fromUriString(UrlUtils.buildFullRequestUrl(request)).build();
                UriComponents redirectUri = UriComponentsBuilder.fromUriString(authorizationRequest.getRedirectUri()).build();
                Set<Map.Entry<String, List<String>>> requestUriParameters = new LinkedHashSet<>(requestUri.getQueryParams().entrySet());
                Set<Map.Entry<String, List<String>>> redirectUriParameters = new LinkedHashSet<>(redirectUri.getQueryParams().entrySet());
                requestUriParameters.retainAll(redirectUriParameters);
                return Objects.equals(requestUri.getScheme(), redirectUri.getScheme()) &&
                    Objects.equals(requestUri.getUserInfo(), redirectUri.getUserInfo()) &&
                    Objects.equals(requestUri.getHost(), redirectUri.getHost()) &&
                    Objects.equals(requestUri.getPort(), redirectUri.getPort()) &&
                    Objects.equals(requestUri.getPath(), redirectUri.getPath()) &&
                    Objects.equals(requestUriParameters.toString(), redirectUriParameters.toString());
            }
        }
    }

    /**
     * <p>processAuthorizationResponse.</p>
     *
     * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
     * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
     * @param session a {@link jakarta.servlet.http.HttpSession} object
     * @throws java.io.IOException if any.
     */
    private synchronized void processAuthorizationResponse(HttpServletRequest request, HttpServletResponse response, HttpSession session)
        throws IOException {
        OAuth2AuthorizationRequest authorizationRequest = this.authorizationRequestRepository
            .removeAuthorizationRequest(request, response);
        MultiValueMap<String, String> params = toMultiMap(request.getParameterMap());
        String redirectUri = UrlUtils.buildFullRequestUrl(request);
        OAuth2AuthorizationResponse authorizationResponse = convert(params, redirectUri);

        ClientRegistration clientRegistration = this.clientRegistrationRepository.findByRegistrationId(this.clientId);

        OAuth2LoginAuthenticationToken authenticationRequest = new OAuth2LoginAuthenticationToken(clientRegistration,
            new OAuth2AuthorizationExchange(authorizationRequest,authorizationResponse));
        authenticationRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        OAuth2LoginAuthenticationToken authenticationResult;
        try {
            authenticationResult = (OAuth2LoginAuthenticationToken)this.authenticate(authenticationRequest);
        } catch (OAuth2AuthorizationException var16) {
            OAuth2Error error = var16.getError();
            UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(authorizationRequest
                .getRedirectUri()).queryParam("error", new Object[]{error.getErrorCode()});
            if (error.getDescription()!=null && !error.getDescription().isBlank()) {
                uriBuilder.queryParam("error_description", new Object[]{error.getDescription()});
            }
            if (error.getUri()!=null && !error.getUri().isBlank()) {
                uriBuilder.queryParam("error_uri", new Object[]{error.getUri()});
            }
            this.redirectStrategy.sendRedirect(request, response, uriBuilder.build().encode().toString());
            return;
        }

        SecurityContextHolder.clearContext();
        SecurityContextHolder.getContext().setAuthentication(authenticationResult);
        Authentication currentAuthentication = SecurityContextHolder.getContext().getAuthentication();
        String principalName = currentAuthentication != null ? currentAuthentication.getPrincipal().toString() : "anonymousUser";

        OAuth2AuthorizedClient authorizedClient = new OAuth2AuthorizedClient(authenticationResult.getClientRegistration(),
            principalName, authenticationResult.getAccessToken(), authenticationResult.getRefreshToken());
        this.oauth2ClientService.saveAuthorizedClient(authorizedClient, currentAuthentication);

        String redirectUrl = authorizationRequest.getRedirectUri();

        SavedRequest savedRequest = this.requestCache.getRequest(request, response);

        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
            SecurityContextHolder.getContext());

        if (SecurityContextHolder.getContext() != null && !AuthenticationUtil.isAuthenticated(request))
        {
            this.onSuccess(request, response, session, authenticationResult);
        }

        if (savedRequest != null) {
            redirectUrl = savedRequest.getRedirectUrl();
            this.requestCache.removeRequest(request, response);
        }
        this.redirectStrategy.sendRedirect(request, response, redirectUrl);
    }

    /**
     * Performs the Authentication based on Authentication Request
     *
     * @param authentication a {@link org.springframework.security.core.Authentication} object
     * @return a {@link org.springframework.security.core.Authentication} object
     * @throws org.springframework.security.core.AuthenticationException if any.
     */
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        OAuth2LoginAuthenticationToken authorizationCodeAuthentication =
            (OAuth2LoginAuthenticationToken) authentication;
        if (!authorizationCodeAuthentication.getAuthorizationExchange()
            .getAuthorizationRequest()
            .getScopes()
            .contains("openid"))
        {
            return null;
        }
        else
        {
            OAuth2AuthorizationRequest authorizationRequest = authorizationCodeAuthentication.getAuthorizationExchange()
                .getAuthorizationRequest();
            OAuth2AuthorizationResponse authorizationResponse =
                authorizationCodeAuthentication.getAuthorizationExchange()
                    .getAuthorizationResponse();
            if (authorizationResponse.statusError())
            {
                throw new OAuth2AuthenticationException(authorizationResponse.getError(),
                                                        authorizationResponse.getError()
                                                            .toString());
            }
            else if (!authorizationResponse.getState()
                .equals(authorizationRequest.getState()))
            {
                OAuth2Error oauth2Error = new OAuth2Error("invalid_state_parameter");
                throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
            }
            else
            {
                OAuth2AccessTokenResponse accessTokenResponse;
                try
                {
                    accessTokenResponse = this.accessTokenResponseClient.getTokenResponse(
                        new OAuth2AuthorizationCodeGrantRequest(authorizationCodeAuthentication.getClientRegistration(),
                                                                authorizationCodeAuthentication.getAuthorizationExchange()));
                }
                catch (OAuth2AuthorizationException var14)
                {
                    OAuth2Error oauth2Error = var14.getError();
                    throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
                }

                ClientRegistration clientRegistration = authorizationCodeAuthentication.getClientRegistration();
                Map<String, Object> additionalParameters = accessTokenResponse.getAdditionalParameters();
                if (!additionalParameters.containsKey("id_token"))
                {
                    OAuth2Error invalidIdTokenError = new OAuth2Error("invalid_id_token",
                                                                      "Missing (required) ID Token in Token Response for Client Registration: "
                                                                          + clientRegistration.getRegistrationId(),
                                                                      (String) null);
                    throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString());
                }
                else
                {
                    OidcIdToken idToken = this.createOidcToken(clientRegistration, accessTokenResponse);
                    String requestNonce = authorizationRequest.getAttribute("nonce");
                    if (requestNonce != null)
                    {
                        String nonceHash;
                        OAuth2Error oauth2Error;
                        try
                        {
                            nonceHash = createHash(requestNonce);
                        }
                        catch (NoSuchAlgorithmException var13)
                        {
                            oauth2Error = new OAuth2Error("invalid_nonce");
                            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
                        }

                        String nonceHashClaim = idToken.getNonce();
                        if (nonceHashClaim == null || !nonceHashClaim.equals(nonceHash))
                        {
                            oauth2Error = new OAuth2Error("invalid_nonce");
                            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString());
                        }
                    }

                    OidcUser oidcUser = this.userService.loadUser(
                        new OidcUserRequest(clientRegistration, accessTokenResponse.getAccessToken(), idToken,
                                            additionalParameters));
                    Collection<? extends GrantedAuthority> mappedAuthorities =
                        this.authoritiesMapper.mapAuthorities(oidcUser.getAuthorities());
                    OAuth2LoginAuthenticationToken authenticationResult =
                        new OAuth2LoginAuthenticationToken(authorizationCodeAuthentication.getClientRegistration(),
                                                           authorizationCodeAuthentication.getAuthorizationExchange(),
                                                           oidcUser, mappedAuthorities,
                                                           accessTokenResponse.getAccessToken(),
                                                           accessTokenResponse.getRefreshToken());
                    authenticationResult.setDetails(authorizationCodeAuthentication.getDetails());
                    return authenticationResult;
                }
            }
        }
    }

    /**
     * <p>createOidcToken.</p>
     *
     * @param clientRegistration a {@link org.springframework.security.oauth2.client.registration.ClientRegistration} object
     * @param accessTokenResponse a {@link org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse} object
     * @return a {@link org.springframework.security.oauth2.core.oidc.OidcIdToken} object
     * @throws org.springframework.security.oauth2.core.OAuth2AuthenticationException if any.
     */
    private OidcIdToken createOidcToken(ClientRegistration clientRegistration,
                                        OAuth2AccessTokenResponse accessTokenResponse)
        throws OAuth2AuthenticationException
    {
        Jwt jwt;
        try
        {
            jwt = validateIdToken(clientRegistration, (String) accessTokenResponse.getAdditionalParameters()
                .get("id_token"));
            validateAccessToken(clientRegistration, accessTokenResponse.getAccessToken());
        }
        catch (JwtException jwtException)
        {
            OAuth2Error invalidIdTokenError =
                new OAuth2Error("invalid_id_token", jwtException.getMessage(), (String) null);
            throw new OAuth2AuthenticationException(invalidIdTokenError, invalidIdTokenError.toString(), jwtException);
        }

        OidcIdToken idToken =
            new OidcIdToken(jwt.getTokenValue(), jwt.getIssuedAt(), jwt.getExpiresAt(), jwt.getClaims());
        return idToken;
    }

    /**
     * <p>createHash.</p>
     *
     * @param nonce a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     * @throws java.security.NoSuchAlgorithmException if any.
     */
    static String createHash(String nonce) throws NoSuchAlgorithmException
    {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] digest = md.digest(nonce.getBytes(StandardCharsets.US_ASCII));
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(digest);
    }

    /**
     * <p>sendRedirectForAuthorization.</p>
     *
     * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
     * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
     * @param authorizationRequest a {@link org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest} object
     * @throws java.io.IOException if any.
     */
    private void sendRedirectForAuthorization(HttpServletRequest request, HttpServletResponse response,
                                              OAuth2AuthorizationRequest authorizationRequest) throws IOException
    {
        if (AuthorizationGrantType.AUTHORIZATION_CODE.equals(authorizationRequest.getGrantType()))
        {
            this.authorizationRequestRepository.saveAuthorizationRequest(authorizationRequest, request, response);
        }
        this.authorizationRedirectStrategy.sendRedirect(request, response,
                                                        authorizationRequest.getAuthorizationRequestUri());
    }

    /**
     * <p>unsuccessfulRedirectForAuthorization.</p>
     *
     * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
     * @throws java.io.IOException if any.
     */
    private void unsuccessfulRedirectForAuthorization(HttpServletResponse response) throws IOException
    {
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                           HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
    }

    /**
     * Redirect to the aims-login page that is not filtered so we can add the fragments to the redirect URL as a query
     * parameter. Only the path, query string and fragment of the original URL are preserved; the host is never
     * taken from user input, preventing Open Redirection attacks.
     *
     * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
     * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
     * @throws java.io.IOException
     */
    private void sendRedirectForPreLogin(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String originalQueryString = request.getQueryString();
        String redirectUrl = request.getRequestURL().toString()
                + (originalQueryString != null ? "?" + originalQueryString : "");

        // URL encode the redirectUrl to ensure all parameters are preserved
        String encodedRedirectUrl = URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8.toString());

        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("Saving original URL for later redirect: " + redirectUrl);
        }

        String loginUri = UriComponentsBuilder.fromUriString(request.getContextPath() + SHARE_AIMS_LOGIN_PAGE)
                .queryParam("redirectUrl", encodedRedirectUrl)
                .build()
                .toUriString();
        response.sendRedirect(loginUri);
    }

    /**
     * After successful authentication with the IdP, redirect back to the original URL.
     * The redirect target is validated to prevent Open Redirection attacks: only URLs
     * on the same host (or relative URLs) are accepted. Any attempt to redirect to an
     * external domain is blocked and falls back to the application root.
     *
     * @param request a {@link jakarta.servlet.http.HttpServletRequest} object
     * @param response a {@link jakarta.servlet.http.HttpServletResponse} object
     * @throws java.io.IOException
     */
    private void sendRedirectToOriginalTarget(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        String encodedOriginalUrl = request.getParameter("redirectUrl");

        // If we don't have redirect URL, redirect to the home page
        if (encodedOriginalUrl == null || encodedOriginalUrl.isEmpty())
        {
            this.redirectStrategy.sendRedirect(request, response, "/");
            return;
        }

        // Decode the URL that was encoded in sendRedirectForPreLogin
        String originalUrl;
        try {
            originalUrl = java.net.URLDecoder.decode(encodedOriginalUrl, StandardCharsets.UTF_8.toString());

            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("Redirecting to original URL after authentication: " + originalUrl);
            }
        } catch (Exception e) {
            // If decoding fails, fall back to home page rather than using a potentially unsafe value
            LOGGER.error("Failed to decode redirectUrl parameter: " + e.getMessage(), e);
            this.redirectStrategy.sendRedirect(request, response, "/");
            return;
        }

        // Security: validate that the redirect URL targets the same host or is relative.
        // This prevents Open Redirection attacks where an attacker supplies an external URL.
        if (!isSafeRedirectUrl(request, originalUrl))
        {
            LOGGER.warn("Open redirection attempt blocked. Refusing to redirect to external URL: " + originalUrl);
            this.redirectStrategy.sendRedirect(request, response, "/");
            return;
        }

        String originalFragment = request.getParameter("fragment");
        UriComponents redirectUri = UriComponentsBuilder.fromUriString(originalUrl).fragment(originalFragment).build();
        this.redirectStrategy.sendRedirect(request, response, redirectUri.toUriString());
    }

    /**
     * Validates that a redirect URL is safe to use, i.e. it is either:
     * <ul>
     *   <li>a relative URL starting with {@code /} (but not {@code //}, which browsers treat as
     *       protocol-relative and could point to an arbitrary host), or</li>
     *   <li>an absolute {@code http} or {@code https} URL whose host and port exactly match those
     *       of the current request.</li>
     * </ul>
     * Any other URL (different host, different port, non-http scheme, protocol-relative, etc.)
     * is considered unsafe and rejected.
     *
     * @param request     the current HTTP request, used to determine the expected host and port
     * @param redirectUrl the URL to validate
     * @return {@code true} if the URL is safe; {@code false} otherwise
     */
    private boolean isSafeRedirectUrl(HttpServletRequest request, String redirectUrl)
    {
        if (redirectUrl == null || redirectUrl.isEmpty())
        {
            return false;
        }

        // Allow relative URLs (must start with '/' but not '//')
        // '//' or '/\' are treated as protocol-relative by browsers and could point to external hosts
        if (redirectUrl.startsWith("/") && !redirectUrl.startsWith("//") && !redirectUrl.contains("\\"))
        {
            return true;
        }

        // For absolute URLs, verify host and port match the current server
        try
        {
            java.net.URI uri = new java.net.URI(redirectUrl);
            String scheme = uri.getScheme();

            // Only allow http/https schemes
            if (scheme == null || (!scheme.equalsIgnoreCase("http") && !scheme.equalsIgnoreCase("https")))
            {
                return false;
            }

            String redirectHost = uri.getHost();
            if (redirectHost == null)
            {
                return false;
            }

            // Host comparison is case-insensitive per RFC 3986
            String serverHost = request.getServerName();
            if (!redirectHost.equalsIgnoreCase(serverHost))
            {
                return false;
            }

            // Compare ports, normalising -1 (absent) to the default for the scheme
            int normalisedRedirectPort = normalisePort(uri.getPort(), scheme);
            int normalisedServerPort   = normalisePort(request.getServerPort(), request.getScheme());

            return normalisedRedirectPort == normalisedServerPort;
        }
        catch (java.net.URISyntaxException e)
        {
            LOGGER.warn("Could not parse redirectUrl as a valid URI – blocking redirect: " + redirectUrl);
            return false;
        }
    }

    /**
     * Returns the effective port for a given (port, scheme) pair.
     * When {@code port} is {@code -1} (i.e. not explicitly specified in the URL),
     * the standard default is used: 443 for {@code https}, 80 for everything else.
     *
     * @param port   the raw port value from a URI or servlet request (-1 means "not specified")
     * @param scheme the URI scheme (e.g. {@code "http"} or {@code "https"})
     * @return the normalised port number
     */
    private int normalisePort(int port, String scheme)
    {
        if (port != -1)
        {
            return port;
        }
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    /**
     * <p>refreshToken.</p>
     *
     * @param attribute a {@link org.springframework.security.core.context.SecurityContext} object
     * @param session a {@link jakarta.servlet.http.HttpSession} object
     * @param request the HTTP request being served, used to re-initialise the user metadata
     */
    private synchronized void refreshToken(SecurityContext attribute, HttpSession session, HttpServletRequest request)
    {
        OAuth2LoginAuthenticationToken oAuth2LoginAuthenticationToken =
            (OAuth2LoginAuthenticationToken) attribute.getAuthentication();

        // A concurrent request for the same user may have already refreshed the token while this thread was
        // waiting on the lock. Re-check the expiry inside the critical section to avoid a redundant round-trip
        // to the identity provider (token + userinfo endpoints).
        if (!isAuthTokenExpired(oAuth2LoginAuthenticationToken.getAccessToken().getExpiresAt()))
        {
            return;
        }

        ClientRegistration clientRegistration = oAuth2LoginAuthenticationToken.getClientRegistration();
        OAuth2RefreshTokenGrantRequest refreshTokenGrantRequest =
            new OAuth2RefreshTokenGrantRequest(clientRegistration, oAuth2LoginAuthenticationToken.getAccessToken(),
                                               oAuth2LoginAuthenticationToken.getRefreshToken());
        OAuth2AccessTokenResponse accessTokenResponse =
            this.refreshTokenResponseClient.getTokenResponse(refreshTokenGrantRequest);
        OidcIdToken idToken = createOidcToken(clientRegistration, accessTokenResponse);
        OidcUser oidcUser = this.userService.loadUser(
            new OidcUserRequest(clientRegistration, accessTokenResponse.getAccessToken(), idToken,
                                accessTokenResponse.getAdditionalParameters()));

        Collection<? extends GrantedAuthority> mappedAuthorities =
            this.authoritiesMapper.mapAuthorities(oidcUser.getAuthorities());
        OAuth2LoginAuthenticationToken authenticationResult = new OAuth2LoginAuthenticationToken(clientRegistration,
                                                                                                 oAuth2LoginAuthenticationToken.getAuthorizationExchange(),
                                                                                                 oidcUser,
                                                                                                 mappedAuthorities,
                                                                                                 accessTokenResponse.getAccessToken(),
                                                                                                 accessTokenResponse.getRefreshToken());
        authenticationResult.setDetails(oAuth2LoginAuthenticationToken.getDetails());
        OAuth2AuthorizedClient updatedAuthorizedClient =
            new OAuth2AuthorizedClient(clientRegistration, oAuth2LoginAuthenticationToken.getName(),
                                       accessTokenResponse.getAccessToken(), accessTokenResponse.getRefreshToken());
        this.oauth2ClientService.saveAuthorizedClient(updatedAuthorizedClient, authenticationResult);
        attribute.setAuthentication(authenticationResult);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, attribute);

        // The Alfresco ticket is baked into the connector session only at initial login. Renew it here so that
        // long-lived sessions do not keep sending a stale ticket to the repository (which would fail with 401
        // once the ticket expires on its own AFTER_INACTIVITY timeout).
        renewAlfTicket(session, oidcUser, accessTokenResponse.getAccessToken(), request);
    }

    /**
     * Re-fetch a fresh Alfresco ticket after an OAuth2 token refresh and rebind the session, so repository calls
     * keep authenticating once the previously obtained ticket has expired.
     *
     * @param session the HTTP session
     * @param oidcUser the refreshed OIDC user
     * @param accessToken the refreshed access token
     * @param request the HTTP request being served, used to re-initialise the user metadata
     */
    private void renewAlfTicket(HttpSession session, OidcUser oidcUser, OAuth2AccessToken accessToken,
                                HttpServletRequest request)
    {
        try
        {
            String username = oidcUser.getAttribute(this.principalAttribute);
            if (username == null)
            {
                return;
            }

            String alfTicket = this.getAlfTicket(session, username, accessToken.getTokenValue());
            if (alfTicket != null)
            {
                // Rebind the whole session, not only the ticket: the request served during this refresh would
                // otherwise reach the web scripts without a user in its request context, and fail on "user is
                // not defined".
                this.bindAlfrescoSession(session, username, alfTicket);
                this.initUser(request);
            }
        }
        catch (ConnectorServiceException | UserFactoryException e)
        {
            LOGGER.error("Failed to renew Alfresco ticket after token refresh: " + e.getMessage(), e);
        }
    }

    /**
     * Tells a definitive refresh failure, where the identity provider rejected the refresh token itself, from a
     * transient one such as an unreachable provider or an error raised by the surrounding framework.
     *
     * @param refreshException the failure raised while refreshing the token
     * @return true when the session has to be invalidated
     */
    private boolean isUnrecoverableRefreshFailure(Exception refreshException)
    {
        if (refreshException instanceof OAuth2AuthorizationException oAuth2AuthorizationException)
        {
            return UNRECOVERABLE_REFRESH_ERROR_CODES.contains(oAuth2AuthorizationException.getError()
                .getErrorCode());
        }

        return false;
    }

    /**
     * <p>createCustomValidator.</p>
     *
     * @param providerDetails a {@link org.springframework.security.oauth2.client.registration.ClientRegistration.ProviderDetails} object
     * @return a {@link org.springframework.security.oauth2.core.OAuth2TokenValidator} object
     */
    private OAuth2TokenValidator<Jwt> createCustomValidator(ProviderDetails providerDetails)
    {
        List<OAuth2TokenValidator<Jwt>> validators = new ArrayList<>();
        validators.add(new JwtTimestampValidator(Duration.ofSeconds(60)));
        validators.add(new JwtIssuerValidator(providerDetails.getIssuerUri()));

        if (this.audience!=null && !this.audience.isEmpty())
        {
            validators.add(new JwtAudienceValidator(this.audience));
        }
        return new DelegatingOAuth2TokenValidator<>(validators);
    }

    static class JwtAudienceValidator implements OAuth2TokenValidator<Jwt>
    {
        private final String configuredAudience;

        public JwtAudienceValidator(String configuredAudience)
        {
            this.configuredAudience = configuredAudience;
        }

        @SuppressWarnings("unchecked")
		@Override
        public OAuth2TokenValidatorResult validate(Jwt token)
        {
            requireNonNull(token, "token cannot be null");
            final Object audience = token.getClaim(JwtClaimNames.AUD);
            if (audience != null)
            {
                if (audience instanceof List && ((List<String>) audience).contains(configuredAudience))
                {
                    return OAuth2TokenValidatorResult.success();
                }
                if (audience instanceof String && audience.equals(configuredAudience))
                {
                    return OAuth2TokenValidatorResult.success();
                }
            }

            final OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                                                      "The aud claim is not valid. Expected configured audience `%s` not found.".formatted(
                                                          configuredAudience),
                                                      "https://tools.ietf.org/html/rfc6750#section-3.1");
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    static class JwtIssuerValidator implements OAuth2TokenValidator<Jwt>
    {
        private final String requiredIssuer;

        public JwtIssuerValidator(String issuer)
        {
            this.requiredIssuer = requireNonNull(issuer, "issuer cannot be null");
        }

        @Override
        public OAuth2TokenValidatorResult validate(Jwt token)
        {
            requireNonNull(token, "token cannot be null");
            final Object issuer = token.getClaim(JwtClaimNames.ISS);
            if (issuer != null && requiredIssuer.equals(issuer.toString()))
            {
                return OAuth2TokenValidatorResult.success();
            }

            final OAuth2Error error = new OAuth2Error(OAuth2ErrorCodes.INVALID_TOKEN,
                                                      "The iss claim is not valid. Expected `%s` but got `%s`.".formatted(
                                                          requiredIssuer, issuer),
                                                      "https://tools.ietf.org/html/rfc6750#section-3.1");
            return OAuth2TokenValidatorResult.failure(error);
        }
    }

    /**
     * <p>validateAccessToken.</p>
     *
     * @param clientRegistration a {@link org.springframework.security.oauth2.client.registration.ClientRegistration} object
     * @param oAuth2AccessToken a {@link org.springframework.security.oauth2.core.OAuth2AccessToken} object
     * @return a {@link org.springframework.security.oauth2.jwt.Jwt} object
     * @throws org.springframework.security.oauth2.jwt.JwtException if any.
     */
    private Jwt validateAccessToken(ClientRegistration clientRegistration, OAuth2AccessToken oAuth2AccessToken)
        throws JwtException
    {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) this.jwtDecoderFactory.createDecoder(clientRegistration);
        jwtDecoder.setJwtValidator(createCustomValidator(clientRegistration.getProviderDetails()));
        jwtDecoder.setClaimSetConverter(
            new ClaimTypeConverter(OidcIdTokenDecoderFactory.createDefaultClaimTypeConverters()));
        return jwtDecoder.decode(oAuth2AccessToken.getTokenValue());
    }

    /**
     * <p>validateIdToken.</p>
     *
     * @param clientRegistration a {@link org.springframework.security.oauth2.client.registration.ClientRegistration} object
     * @param idToken a {@link java.lang.String} object
     * @return a {@link org.springframework.security.oauth2.jwt.Jwt} object
     * @throws org.springframework.security.oauth2.jwt.JwtException if any.
     */
    @SuppressWarnings("unchecked")
	private Jwt validateIdToken(ClientRegistration clientRegistration, String idToken) throws JwtException
    {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) this.jwtDecoderFactory.createDecoder(clientRegistration);
        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            new OAuth2TokenValidator[] { new JwtTimestampValidator(), new OidcIdTokenValidator(clientRegistration) }));
        return jwtDecoder.decode(idToken);
    }

    /**
     * <p>isAuthTokenExpired.</p>
     *
     * @param authTokenExpiration a {@link java.time.Instant} object
     * @return a boolean
     */
    private static boolean isAuthTokenExpired(Instant authTokenExpiration)
    {
        if (authTokenExpiration == null)
        {
            return true;
        }
        return Instant.now()
            .plus(TOKEN_EXPIRY_REFRESH_MARGIN)
            .compareTo(authTokenExpiration) >= 0;
    }
}

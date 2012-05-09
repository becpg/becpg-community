package org.alfresco.web.app.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.servlet.FilterChain;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.apache.commons.logging.Log;
import org.openid4java.consumer.ConsumerException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.AbstractRememberMeServices;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;


public class OpenIdAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter,
ActivateableBean {

	@Override
	public boolean isActive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void doFilter(ServletContext arg0, ServletRequest arg1, ServletResponse arg2, FilterChain arg3) throws IOException, ServletException {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Log getLogger() {
		// TODO Auto-generated method stub
		return null;
	}

	
	 //~ Static fields/initializers =====================================================================================

    public static final String DEFAULT_CLAIMED_IDENTITY_FIELD = "openid_identifier";

    //~ Instance fields ================================================================================================

    private OpenIDConsumer consumer;
    private String claimedIdentityFieldName = DEFAULT_CLAIMED_IDENTITY_FIELD;
    private Map<String,String> realmMapping = Collections.emptyMap();
    private Set<String> returnToUrlParameters = Collections.emptySet();

//    //~ Constructors ===================================================================================================
//
//
//    //~ Methods ========================================================================================================
//
//    @Override
//    public void afterPropertiesSet() {
//        super.afterPropertiesSet();
//
//        if (consumer == null) {
//            try {
//                consumer = new OpenID4JavaConsumer();
//            } catch (ConsumerException e) {
//                throw new IllegalArgumentException("Failed to initialize OpenID", e);
//            }
//        }
//
//        if (returnToUrlParameters.isEmpty() &&
//                getRememberMeServices() instanceof AbstractRememberMeServices) {
//            returnToUrlParameters = new HashSet<String>();
//            returnToUrlParameters.add(((AbstractRememberMeServices)getRememberMeServices()).getParameter());
//        }
//    }
//
//    /**
//     * Authentication has two phases.
//     * <ol>
//     * <li>The initial submission of the claimed OpenID. A redirect to the URL returned from the consumer
//     * will be performed and null will be returned.</li>
//     * <li>The redirection from the OpenID server to the return_to URL, once it has authenticated the user</li>
//     * </ol>
//     */
//    @Override
//    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
//            throws AuthenticationException, IOException {
//        OpenIDAuthenticationToken token;
//
//        String identity = request.getParameter("openid.identity");
//
//        if (!StringUtils.hasText(identity)) {
//            String claimedIdentity = obtainUsername(request);
//            // Make the username available to the view
//            setLastUsername(claimedIdentity, request);
//
//            try {
//                String returnToUrl = buildReturnToUrl(request);
//                String realm = lookupRealm(returnToUrl);
//                String openIdUrl = consumer.beginConsumption(request, claimedIdentity, returnToUrl, realm);
//                if (logger.isDebugEnabled()) {
//                    logger.debug("return_to is '" + returnToUrl + "', realm is '" + realm + "'");
//                    logger.debug("Redirecting to " + openIdUrl);
//                }
//                response.sendRedirect(openIdUrl);
//
//                // Indicate to parent class that authentication is continuing.
//                return null;
//            } catch (OpenIDConsumerException e) {
//                logger.debug("Failed to consume claimedIdentity: " + claimedIdentity, e);
//                throw new AuthenticationServiceException("Unable to process claimed identity '" + claimedIdentity + "'");
//            }
//        }
//
//        if (logger.isDebugEnabled()) {
//            logger.debug("Supplied OpenID identity is " + identity);
//        }
//
//        try {
//            token = consumer.endConsumption(request);
//        } catch (OpenIDConsumerException oice) {
//            throw new AuthenticationServiceException("Consumer error", oice);
//        }
//
//        token.setDetails(authenticationDetailsSource.buildDetails(request));
//
//        // delegate to the authentication provider
//        Authentication authentication = this.getAuthenticationManager().authenticate(token);
//
//        if (authentication.isAuthenticated()) {
//            setLastUsername(token.getIdentityUrl(), request);
//        }
//
//        return authentication;
//    }
//
//    private void setLastUsername(String username, HttpServletRequest request) {
//        HttpSession session = request.getSession(false);
//
//        if (session != null || getAllowSessionCreation()) {
//            request.getSession().setAttribute(UsernamePasswordAuthenticationFilter.SPRING_SECURITY_LAST_USERNAME_KEY, username);
//        }
//    }
//
//    protected String lookupRealm(String returnToUrl) {
//        String mapping = realmMapping.get(returnToUrl);
//
//        if (mapping == null) {
//            try {
//                URL url = new URL(returnToUrl);
//                int port = url.getPort();
//
//                StringBuilder realmBuffer = new StringBuilder(returnToUrl.length())
//                        .append(url.getProtocol())
//                        .append("://")
//                        .append(url.getHost());
//                if (port > 0) {
//                    realmBuffer.append(":").append(port);
//                }
//                realmBuffer.append("/");
//                mapping = realmBuffer.toString();
//            } catch (MalformedURLException e) {
//                logger.warn("returnToUrl was not a valid URL: [" + returnToUrl + "]", e);
//            }
//        }
//
//        return mapping;
//    }
//
//    /**
//     * Builds the <tt>return_to</tt> URL that will be sent to the OpenID service provider.
//     * By default returns the URL of the current request.
//     *
//     * @param request the current request which is being processed by this filter
//     * @return The <tt>return_to</tt> URL.
//     */
//    protected String buildReturnToUrl(HttpServletRequest request) {
//        StringBuffer sb = request.getRequestURL();
//
//        Iterator<String> iterator = returnToUrlParameters.iterator();
//        boolean isFirst = true;
//
//        while (iterator.hasNext()) {
//            String name = iterator.next();
//            // Assume for simplicity that there is only one value
//            String value = request.getParameter(name);
//
//            if (value == null) {
//                continue;
//            }
//
//            if (isFirst) {
//                sb.append("?");
//                isFirst = false;
//            }
//            sb.append(name).append("=").append(value);
//
//            if (iterator.hasNext()) {
//                sb.append("&");
//            }
//        }
//
//        return sb.toString();
//    }
//
//    /**
//     * Reads the <tt>claimedIdentityFieldName</tt> from the submitted request.
//     */
//    protected String obtainUsername(HttpServletRequest req) {
//        String claimedIdentity = req.getParameter(claimedIdentityFieldName);
//
//        if (!StringUtils.hasText(claimedIdentity)) {
//            logger.error("No claimed identity supplied in authentication request");
//            return "";
//        }
//
//        return claimedIdentity.trim();
//    }
//
//    /**
//     * Maps the <tt>return_to url</tt> to a realm, for example:
//     * <pre>
//     * http://www.example.com/j_spring_openid_security_check -> http://www.example.com/realm</tt>
//     * </pre>
//     * If no mapping is provided then the returnToUrl will be parsed to extract the protocol, hostname and port followed
//     * by a trailing slash.
//     * This means that <tt>http://www.example.com/j_spring_openid_security_check</tt> will automatically become
//     * <tt>http://www.example.com:80/</tt>
//     *
//     * @param realmMapping containing returnToUrl -> realm mappings
//     */
//    public void setRealmMapping(Map<String,String> realmMapping) {
//        this.realmMapping = realmMapping;
//    }
//
//    /**
//     * The name of the request parameter containing the OpenID identity, as submitted from the initial login form.
//     *
//     * @param claimedIdentityFieldName defaults to "openid_identifier"
//     */
//    public void setClaimedIdentityFieldName(String claimedIdentityFieldName) {
//        this.claimedIdentityFieldName = claimedIdentityFieldName;
//    }
//
//    public void setConsumer(OpenIDConsumer consumer) {
//        this.consumer = consumer;
//    }
//
//    /**
//     * Specifies any extra parameters submitted along with the identity field which should be appended to the
//     * {@code return_to} URL which is assembled by {@link #buildReturnToUrl}.
//     *
//     * @param returnToUrlParameters
//     *      the set of parameter names. If not set, it will default to the parameter name used by the
//     *      {@code RememberMeServices} obtained from the parent class (if one is set).
//     */
//    public void setReturnToUrlParameters(Set<String> returnToUrlParameters) {
//        Assert.notNull(returnToUrlParameters, "returnToUrlParameters cannot be null");
//        this.returnToUrlParameters = returnToUrlParameters;
//    }
	
}

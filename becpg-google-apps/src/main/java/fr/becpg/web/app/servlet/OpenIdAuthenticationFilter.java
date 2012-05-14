package fr.becpg.web.app.servlet;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
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

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.SessionUser;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.web.filter.beans.DependencyInjectedFilter;
import org.alfresco.repo.webdav.auth.BaseAuthenticationFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openid4java.consumer.ConsumerException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.openid.OpenID4JavaConsumer;
import org.springframework.security.openid.OpenIDAuthenticationToken;
import org.springframework.security.openid.OpenIDConsumer;
import org.springframework.security.openid.OpenIDConsumerException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import fr.becpg.repo.security.authentication.openid.OpenIdAuthenticator;
import fr.becpg.repo.security.authentication.openid.OpenIdUtils;

/**
 * 
 * @author matthieu
 *
 */
public class OpenIdAuthenticationFilter extends BaseAuthenticationFilter implements DependencyInjectedFilter,
ActivateableBean, InitializingBean {

	
	   // Debug logging
    private static Log logger = LogFactory.getLog(OpenIdAuthenticationFilter.class);


    private boolean isActive = true;

	 //~ Static fields/initializers =====================================================================================

   public static final String DEFAULT_CLAIMED_IDENTITY_FIELD = "openid_identifier";

   //~ Instance fields ================================================================================================

   private OpenIDConsumer consumer;
   private String claimedIdentityFieldName = DEFAULT_CLAIMED_IDENTITY_FIELD;
   private Map<String,String> realmMapping = Collections.emptyMap();
   private Set<String> returnToUrlParameters = Collections.emptySet();
   
   private String claimedIdentity = null;
	
	

   // SSO enabled authentication component (required)
   private OpenIdAuthenticator openIdAuthenticator;
   
   
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	@Override
	public boolean isActive() {
		return isActive;
	}

	@Override
	public void doFilter(ServletContext context, ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
		  // If a filter up the chain has marked the request as not requiring auth then respect it        
        if (request.getAttribute( NO_AUTH_REQUIRED) != null)
        {
            if ( getLogger().isDebugEnabled())
                getLogger().debug("Authentication not required (filter), chaining ...");
            chain.doFilter(request, response);
        }
        else if (authenticateRequest(context, (HttpServletRequest) request, (HttpServletResponse) response))
        {
            chain.doFilter(request, response);
        }
		
	}

	@Override
	protected Log getLogger() {
		 return logger;
	}

	

    //~ Constructors ===================================================================================================


    //~ Methods ========================================================================================================

    @Override
    public void afterPropertiesSet() throws ServletException {
    	  if (isActive())
          {
	        if (consumer == null) {
	            try {
	                consumer = new OpenID4JavaConsumer();
	            } catch (ConsumerException e) {
	                throw new IllegalArgumentException("Failed to initialize OpenID", e);
	            }
	        }

	        // Check that the authentication component supports the required mode
	    	
	        if (!(authenticationComponent instanceof OpenIdAuthenticator))
	        {
	            throw new ServletException("Authentication component does not support OpenId");            
	        }
	        this.openIdAuthenticator = (OpenIdAuthenticator)this.authenticationComponent;

          }
    }
 
    

    /**
     * Authentication has two phases.
     * <ol>
     * <li>The initial submission of the claimed OpenID. A redirect to the URL returned from the consumer
     * will be performed and false will be returned.</li>
     * <li>The redirection from the OpenID server to the return_to URL, once it has authenticated the user</li>
     * </ol>
     * @param context 
     * @throws ServletException 
     */
    public boolean authenticateRequest(ServletContext context, HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
    	
    	   // Check if the user is already authenticated
        SessionUser user = getSessionUser(context, request, response, true);

        // If the user has been validated then continue to
        // the next filter
        if (user != null )
        {

            if (getLogger().isDebugEnabled())
                getLogger().debug("Authentication not required (user), chaining ...");
            
            // Chain to the next filter
            return true;
        }
    	
    	
        OpenIDAuthenticationToken token;

        String identity = request.getParameter("openid.identity");

        if (!StringUtils.hasText(identity)) {
            if(claimedIdentity==null){
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
                response.sendRedirect(openIdUrl);

                // Indicate to parent class that authentication is continuing.
                return false;
            } catch (OpenIDConsumerException e) {
                logger.debug("Failed to consume claimedIdentity: " + claimedIdentity, e);
                throw new AuthenticationException("Unable to process claimed identity '" + claimedIdentity + "'");
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Supplied OpenID identity is " + identity);
        }

        try {
            token = consumer.endConsumption(request);
        } catch (OpenIDConsumerException oice) {
            throw new AuthenticationException("Consumer error", oice);
        }

        //token.setDetails(authenticationDetailsSource.buildDetails(request));

        // delegate to the authentication provider
        Authentication authentication = openIdAuthenticator.authenticate(token);

        if (authentication.isAuthenticated()) {        
             createUserEnvironment(request.getSession(), OpenIdUtils.getUserName(token));
           //  response.setStatus(HttpServletResponse.SC_NO_CONTENT);
             return true;
        }

       
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        return false;
        
    }


    protected String lookupRealm(String returnToUrl) {
        String mapping = realmMapping.get(returnToUrl);

        if (mapping == null) {
            try {
                URL url = new URL(returnToUrl);
                int port = url.getPort();

                StringBuilder realmBuffer = new StringBuilder(returnToUrl.length())
                        .append(url.getProtocol())
                        .append("://")
                        .append(url.getHost());
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
     * Builds the <tt>return_to</tt> URL that will be sent to the OpenID service provider.
     * By default returns the URL of the current request.
     *
     * @param request the current request which is being processed by this filter
     * @return The <tt>return_to</tt> URL.
     */
    protected String buildReturnToUrl(HttpServletRequest request) {
        StringBuffer sb = request.getRequestURL();

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
     * <pre>
     * http://www.example.com/j_spring_openid_security_check -> http://www.example.com/realm</tt>
     * </pre>
     * If no mapping is provided then the returnToUrl will be parsed to extract the protocol, hostname and port followed
     * by a trailing slash.
     * This means that <tt>http://www.example.com/j_spring_openid_security_check</tt> will automatically become
     * <tt>http://www.example.com:80/</tt>
     *
     * @param realmMapping containing returnToUrl -> realm mappings
     */
    public void setRealmMapping(Map<String,String> realmMapping) {
        this.realmMapping = realmMapping;
    }

    /**
     * The name of the request parameter containing the OpenID identity, as submitted from the initial login form.
     *
     * @param claimedIdentityFieldName defaults to "openid_identifier"
     */
    public void setClaimedIdentityFieldName(String claimedIdentityFieldName) {
        this.claimedIdentityFieldName = claimedIdentityFieldName;
    }

    public void setConsumer(OpenIDConsumer consumer) {
        this.consumer = consumer;
    }

    /**
     * Specifies any extra parameters submitted along with the identity field which should be appended to the
     * {@code return_to} URL which is assembled by {@link #buildReturnToUrl}.
     *
     * @param returnToUrlParameters
     *      the set of parameter names. If not set, it will default to the parameter name used by the
     *      {@code RememberMeServices} obtained from the parent class (if one is set).
     */
    public void setReturnToUrlParameters(Set<String> returnToUrlParameters) {
        Assert.notNull(returnToUrlParameters, "returnToUrlParameters cannot be null");
        this.returnToUrlParameters = returnToUrlParameters;
    }

    /** 
     * Set the OpenID Provider
     * @param claimedIdentity
     */
	public void setClaimedIdentity(String claimedIdentity) {
		this.claimedIdentity = claimedIdentity;
	}
    
}

package fr.becpg.olap.authentication;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.Assert;

import fr.becpg.olap.helper.UserNameHelper;
import fr.becpg.olap.http.LoginCommand;

/**
 * 
 * @author matthieu
 *
 */
public class AlfrescoAuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    //~ Instance fields ================================================================================================

	private static Log logger = LogFactory.getLog(AlfrescoAuthenticationProvider.class);

    private UserDetailsService userDetailsService;

    //~ Methods ========================================================================================================

    protected void additionalAuthenticationChecks(UserDetails userDetails,
            UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
       
        if (authentication.getCredentials() == null) {
            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();
        String presentedLogin = UserNameHelper.extractLogin(userDetails.getUsername());
        if(userDetails instanceof AlfrescoUserDetails){
	        LoginCommand loginCommand = new LoginCommand(((AlfrescoUserDetails) userDetails).getInstance().getInstanceUrl());
	        String alfTicket = loginCommand.getAlfTicket(presentedLogin, presentedPassword);
	        if(logger.isDebugEnabled()){
	        	logger.debug("Retrieving alfTicket :"+alfTicket);
	        }
	        
	        if (alfTicket==null || alfTicket.isEmpty()) {
	            throw new BadCredentialsException(messages.getMessage(
	                    "AbstractUserDetailsAuthenticationProvider.badCredentials", "Bad credentials"));
	        }
	        
        } else {
        	throw new BadCredentialsException("UserDetails is not instance of AlfrescoUserDetails");
        }
        
    }

    protected void doAfterPropertiesSet() throws Exception {
        Assert.notNull(this.userDetailsService, "A UserDetailsService must be set");
    }

    protected final UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication)
            throws AuthenticationException {
        UserDetails loadedUser;

        try {
            loadedUser = this.getUserDetailsService().loadUserByUsername(username);
        }
        catch (DataAccessException repositoryProblem) {
            throw new AuthenticationServiceException(repositoryProblem.getMessage(), repositoryProblem);
        }

        if (loadedUser == null) {
            throw new AuthenticationServiceException(
                    "UserDetailsService returned null, which is an interface contract violation");
        }
        return loadedUser;
    }


    public void setUserDetailsService(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    protected UserDetailsService getUserDetailsService() {
        return userDetailsService;
    }



}
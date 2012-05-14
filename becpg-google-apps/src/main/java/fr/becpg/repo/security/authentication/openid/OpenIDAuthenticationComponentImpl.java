package fr.becpg.repo.security.authentication.openid;

import net.sf.acegisecurity.Authentication;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.util.PropertyMap;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationStatus;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * 
 * @author matthieu
 * 
 */
public class OpenIDAuthenticationComponentImpl extends AbstractAuthenticationComponent implements OpenIdAuthenticator {

	private static Log logger = LogFactory.getLog(OpenIDAuthenticationComponentImpl.class);


    /** The authority service. */
    private AuthorityService authorityService;
    
    
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}


	@Override
	protected boolean implementationAllowsGuestLogin() {
		return false;
	}

	@Override
	protected void authenticateImpl(String userName, char[] password) {

		// Debug

		if (logger.isDebugEnabled())
			logger.debug("Authenticate user=" + userName + " via local credentials");

		throw new AuthenticationException("Unsupported authentication token type");
	}

	/**
	 * Authenticate using a token
	 * 
	 * @param token
	 *            Authentication
	 * @return Authentication
	 * @throws AuthenticationException
	 */
	@Override
	public Authentication authenticate(org.springframework.security.core.Authentication auth) throws AuthenticationException {

		
		// Check if the token is for openId authentication

		if (auth instanceof OpenIDAuthenticationToken) {

			OpenIDAuthenticationToken response = (OpenIDAuthenticationToken) auth;
			
			if (logger.isDebugEnabled())
				logger.debug("Authenticate " + OpenIdUtils.getUserName(response) + " via token");


			OpenIDAuthenticationStatus status = response.getStatus();

			// handle the various possibilities
			if (status == OpenIDAuthenticationStatus.SUCCESS) {

				clearCurrentSecurityContext();
				return setCurrentUser(response);

			} else if (status == OpenIDAuthenticationStatus.CANCELLED) {

				throw new AuthenticationException("Log in cancelled");

			} else if (status == OpenIDAuthenticationStatus.ERROR) {

				throw new AuthenticationException("Error message from server: " + response.getMessage());

			} else if (status == OpenIDAuthenticationStatus.FAILURE) {

				throw new AuthenticationException("Log in failed - identity could not be verified");

			} else if (status == OpenIDAuthenticationStatus.SETUP_NEEDED) {

				throw new AuthenticationException(

				"The server responded setup was needed, which shouldn't happen");

			} else {

				throw new AuthenticationException("Unrecognized return value " + status.toString());

			}

		} else {
			// Unsupported authentication token

			throw new AuthenticationException("Unsupported authentication token type");
		}

	}

	private Authentication setCurrentUser(OpenIDAuthenticationToken token) {
		Authentication authentication;

		OpenIDUserCallback openIDUserCallback = new OpenIDUserCallback(token);

		// If the repository is read only, we have to settle for a read only
		// transaction. Auto user creation
		// will not be possible.
		if (getTransactionService().isReadOnly()) {
			authentication = getTransactionService().getRetryingTransactionHelper().doInTransaction(openIDUserCallback, true, false);
		}
		// Otherwise,
		// - for check-only mode we want a readable txn or
		// - for check-and-fix mode we want a writeable transaction, so if the
		// current transaction is read only we set the
		// requiresNew flag to true
		else {
			boolean requiresNew = (AlfrescoTransactionSupport.getTransactionReadState() == TxnReadState.TXN_READ_ONLY);

			authentication = getTransactionService().getRetryingTransactionHelper().doInTransaction(openIDUserCallback, false, requiresNew);
		}
		if ((authentication == null) || (openIDUserCallback.ae != null)) {
			throw openIDUserCallback.ae;
		}
		return authentication;
	}

	class OpenIDUserCallback implements RetryingTransactionCallback<Authentication> {
		AuthenticationException ae = null;
		OpenIDAuthenticationToken token = null;

		OpenIDUserCallback(OpenIDAuthenticationToken token) {
			this.token = token;
		}

		public Authentication execute() throws Throwable {
			try {
				final String userName = OpenIdUtils.getUserName(token);
				return setCurrentUser(AuthenticationUtil.runAs(new RunAsWork<String>() {
					public String doWork() throws Exception {

						if (!getPersonService().personExists(userName)) {
							if (logger.isDebugEnabled()) {
								logger.debug("User \"" + userName + "\" does not exist in Alfresco. Attempting to create the user.");
							}
							if (!createMissingPerson()) {
								if (logger.isDebugEnabled()) {
									logger.debug("Failed to create user \"" + userName + '"');
								}
								throw new AuthenticationException("User \"" + userName + "\" does not exist in Alfresco");
							}
						}
						NodeRef userNode = getPersonService().getPerson(userName);
						// Get the person name and use that as the current user
						// to
						// line up with permission
						// checks
						return (String) getNodeService().getProperty(userNode, ContentModel.PROP_USERNAME);
					}
				}, getSystemUserName(getUserDomain(userName))), UserNameValidationMode.NONE);
			} catch (AuthenticationException ae) {
				this.ae = ae;
				return null;
			}
		}

		protected boolean createMissingPerson() {
			PropertyMap personProps = new PropertyMap();
			
			personProps.put(ContentModel.PROP_USERNAME, OpenIdUtils.getUserName(token));
			for (OpenIDAttribute attribute : token.getAttributes()) {
		            if (attribute.getName().equals("email")) {
		            	personProps.put(ContentModel.PROP_EMAIL, attribute.getValues().get(0));
		            }
		            if (attribute.getName().equals("firstName")) {
		            	personProps.put(ContentModel.PROP_FIRSTNAME, attribute.getValues().get(0));
		            }
		            if (attribute.getName().equals("lastName")) {
		            	personProps.put(ContentModel.PROP_LASTNAME, attribute.getValues().get(0));
		            }
		        }
			
			
			
			if(logger.isDebugEnabled()){
				logger.debug("Create openId user :"+OpenIdUtils.getUserName(token));
				logger.debug("With details : "+personProps.toString());
			}
			
			NodeRef person = getPersonService().createPerson(personProps);

			//authorityService.addAuthority( AuthorityType.GROUP_EVERYONE, personProps.get(ContentModel.PROP_USERNAME));
			
			return person != null;
		}

	}

}

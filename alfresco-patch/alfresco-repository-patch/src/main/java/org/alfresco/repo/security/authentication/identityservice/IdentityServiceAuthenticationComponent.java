/*
 * #%L
 * Alfresco Repository
 * %%
 * Copyright (C) 2005 - 2023 Alfresco Software Limited
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
package org.alfresco.repo.security.authentication.identityservice;

import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.management.subsystems.ActivateableBean;
import org.alfresco.repo.security.authentication.AbstractAuthenticationComponent;
import org.alfresco.repo.security.authentication.AuthenticationException;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.AuthorizationGrant;
import org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade.IdentityServiceFacadeException;
import org.alfresco.repo.security.authentication.identityservice.user.OIDCUserInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionSupportUtil;

import net.sf.acegisecurity.Authentication;

/**
 *
 * Authenticates a user against Identity Service (Keycloak/Authorization Server).
 * {@link org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade} is used to verify provided user credentials. User is set as the current user if the
 * user credentials are valid.
 * <br>
 * The {@link org.alfresco.repo.security.authentication.identityservice.IdentityServiceAuthenticationComponent#identityServiceFacade} can be null in which case this authenticator
 * will just fall through to the next one in the chain.
 *
 * @author matthieu
 */
public class IdentityServiceAuthenticationComponent extends AbstractAuthenticationComponent implements ActivateableBean
{
    private static final QName PROP_IS_SSO_USER = QName.createQName("http://www.bcpg.fr/model/becpg/1.0", "isSsoUser");
	/** client used to authenticate user credentials against Authorization Server **/
    private IdentityServiceFacade identityServiceFacade;
    /** enabled flag for the identity service subsystem**/
    private boolean active;

    private IdentityServiceJITProvisioningHandler jitProvisioningHandler;

    private boolean allowGuestLogin;

    /**
     * <p>Setter for the field <code>identityServiceFacade</code>.</p>
     *
     * @param identityServiceFacade a {@link org.alfresco.repo.security.authentication.identityservice.IdentityServiceFacade} object
     */
    public void setIdentityServiceFacade(IdentityServiceFacade identityServiceFacade)
    {
        this.identityServiceFacade = identityServiceFacade;
    }

    /**
     * <p>Setter for the field <code>allowGuestLogin</code>.</p>
     *
     * @param allowGuestLogin a boolean
     */
    public void setAllowGuestLogin(boolean allowGuestLogin)
    {
        this.allowGuestLogin = allowGuestLogin;
    }

    /**
     * <p>Setter for the field <code>jitProvisioningHandler</code>.</p>
     *
     * @param jitProvisioningHandler a {@link org.alfresco.repo.security.authentication.identityservice.IdentityServiceJITProvisioningHandler} object
     */
    public void setJitProvisioningHandler(IdentityServiceJITProvisioningHandler jitProvisioningHandler)
    {
        this.jitProvisioningHandler = jitProvisioningHandler;
    }
    
    /** {@inheritDoc} */
    @Override
	public Authentication setCurrentUser(String userName, UserNameValidationMode validationMode) {
    	TransactionSupportUtil.bindResource("BeCPGUserPolicy.disable", true);
		Authentication auth = super.setCurrentUser(userName, validationMode);
		NodeRef personNodeRef = getPersonService().getPerson(userName);
		Serializable isSsoUser = getNodeService().getProperty(personNodeRef, PROP_IS_SSO_USER);
		if (isSsoUser == null || !(boolean) isSsoUser) {
			getNodeService().setProperty(personNodeRef, PROP_IS_SSO_USER, true);
		}
		if (getNodeService().hasAspect(personNodeRef, ContentModel.ASPECT_PERSON_DISABLED)) {
			throw new AuthenticationException("User not authenticated because it is disabled.");
		}
		return auth;
	}

    /** {@inheritDoc} */
    @Override
    public void authenticateImpl(String userName, char[] password) throws AuthenticationException
    {
        if (identityServiceFacade == null)
        {
            if (logger.isDebugEnabled())
            {
            	logger.debug("IdentityServiceFacade was not set, possibly due to the 'identity-service.authentication.enable-username-password-authentication=false' property.");
            }

            throw new AuthenticationException("User not authenticated because IdentityServiceFacade was not set.");
        }

        try
        {
            // Attempt to verify user credentials
            IdentityServiceFacade.AccessTokenAuthorization accessTokenAuthorization = identityServiceFacade.authorize(AuthorizationGrant.password(userName, String.valueOf(password)));

            String normalizedUsername = jitProvisioningHandler.extractUserInfoAndCreateUserIfNeeded(accessTokenAuthorization.getAccessToken().getTokenValue())
                        .map(OIDCUserInfo::username)
                        .orElseThrow(() -> new AuthenticationException("Failed to extract username from token and user info endpoint."));
            // Verification was successful so treat as authenticated user
            setCurrentUser(normalizedUsername);
        }
        catch (IdentityServiceFacadeException e)
        {
            throw new AuthenticationException("Failed to verify user credentials against the OAuth2 Authorization Server.", e);
        }
        catch (RuntimeException e)
        {
            throw new AuthenticationException("Failed to verify user credentials.", e);
        }
    }

    /**
     * <p>Setter for the field <code>active</code>.</p>
     *
     * @param active a boolean
     */
    public void setActive(boolean active)
    {
        this.active = active;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isActive()
    {
        return active;
    }

    /** {@inheritDoc} */
    @Override
    protected boolean implementationAllowsGuestLogin()
    {
        return allowGuestLogin;
    }
}

package fr.becpg.repo.security.authentication.openid;

import net.sf.acegisecurity.Authentication;

import org.alfresco.repo.security.authentication.AuthenticationException;

/**
 * 
 * @author matthieu
 *
 */
public interface OpenIdAuthenticator {

	Authentication authenticate(org.springframework.security.core.Authentication auth) throws AuthenticationException;

}

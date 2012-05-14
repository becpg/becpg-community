package fr.becpg.repo.security.authentication.openid;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * 
 * @author matthieu
 *
 */
public class OpenIdUtils {


	public static String getUserName(OpenIDAuthenticationToken token) {
		for (OpenIDAttribute attribute : token.getAttributes()) {
            if (attribute.getName().equals("email")) {
            	return attribute.getValues().get(0);
            }
           
        }
		throw new AuthenticationException("No userName found in openId attributes");
	}

	
}

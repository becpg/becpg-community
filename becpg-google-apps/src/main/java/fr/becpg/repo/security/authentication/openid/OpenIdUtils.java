package fr.becpg.repo.security.authentication.openid;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.alfresco.repo.security.authentication.AuthenticationException;
import org.springframework.security.openid.OpenIDAttribute;
import org.springframework.security.openid.OpenIDAuthenticationToken;

/**
 * 
 * @author matthieu
 *
 */
public class OpenIdUtils {

	public static final String HEADER_SHARE_AUTH = "ShareAuth";

	public static final String HEADER_OPENID_CALLBACK = "OpenIdCallBack";

	public static final String HEADER_OPENID_REDIRECT = "OpenIdRedirect";
	
	public static final String HEADER_OPENID_AUTH_SUCCESS  = "OpenIdAuthSuccess";

	private static final String SHARE_AUTH_SESSION_PARAM = "ShareAuthSessionParam";
	
	public static String getUserName(OpenIDAuthenticationToken token) {
		for (OpenIDAttribute attribute : token.getAttributes()) {
            if (attribute.getName().equals("email")) {
            	return attribute.getValues().get(0);
            }
        }
		throw new AuthenticationException("No userName found in openId attributes");
	}

	public static String getShareAuthParam(HttpServletRequest request) {
		String shareAuthParam = (String) request.getSession().getAttribute(SHARE_AUTH_SESSION_PARAM);
		if(shareAuthParam==null){
			shareAuthParam = UUID.randomUUID().toString();
			request.getSession().setAttribute(SHARE_AUTH_SESSION_PARAM,shareAuthParam);
		}
		return shareAuthParam;
	}

	public static boolean hasShareAuthParam(HttpServletRequest request) {
		return request.getSession().getAttribute(SHARE_AUTH_SESSION_PARAM)!=null
				&& request.getParameter((String)request.getSession().getAttribute(SHARE_AUTH_SESSION_PARAM))!=null;
	}

	public static String getOAuthToken(OpenIDAuthenticationToken token) {
		return token.getMessage();
	}

	
}

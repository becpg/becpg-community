package fr.becpg.web.resolver;

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.mvc.LocaleResolver;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.connector.User;

/**
 *
 * @author matthieu
 *
 */
public class BeCPGLocaleResolver extends LocaleResolver {

	@Override
	public Locale resolveLocale(HttpServletRequest request) {

		Locale locale = null;

		RequestContext rc = ThreadLocalRequestContext.getRequestContext();

		User user = rc.getUser();

		if("true".equals(request.getParameter("resetLocale"))) {

			  // allow the endpoint id to be explicitly overridden via a request attribute
            String userEndpointId = (String) rc.getAttribute(RequestContext.USER_ENDPOINT);
            
            
            UserFactory userFactory = rc.getServiceRegistry().getUserFactory();
			try {
				user = userFactory.initialiseUser(rc, (HttpServletRequest)request, userEndpointId, true);
				rc.setUser(user);
			} catch (UserFactoryException e) {}
		}
		

		if (user != null) {

			for (Map.Entry<String, Boolean> entry : user.getCapabilities().entrySet()) {
				if (entry.getKey().startsWith("userLocale_")) {
					locale = I18NUtil.parseLocale(entry.getKey().substring(11));
					I18NUtil.setLocale(locale);
				} else if (entry.getKey().startsWith("userContentLocale_")) {
					locale = I18NUtil.parseLocale(entry.getKey().substring(18));	
					I18NUtil.setContentLocale(locale);
				}
			}
			
			if(locale!=null) {
				return locale;
			}

		}

		return super.resolveLocale(request);
	}

}

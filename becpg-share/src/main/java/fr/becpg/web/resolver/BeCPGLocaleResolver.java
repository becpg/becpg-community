package fr.becpg.web.resolver;

import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * <p>BeCPGLocaleResolver class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGLocaleResolver extends AcceptHeaderLocaleResolver {

	/** {@inheritDoc} */
	@SuppressWarnings("deprecation")
	@Override
	public Locale resolveLocale(HttpServletRequest request) {

		Locale locale = null;

		RequestContext rc = ThreadLocalRequestContext.getRequestContext();

		User user = rc.getUser();

		if (user == null) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				user = (User) session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_OBJECT);
			}
		}

		if ("true".equals(request.getParameter("resetLocale"))) {

			// allow the endpoint id to be explicitly overridden via a request
			// attribute
			String userEndpointId = (String) rc.getAttribute(RequestContext.USER_ENDPOINT);

			UserFactory userFactory = rc.getServiceRegistry().getUserFactory();
			try {
				user = userFactory.initialiseUser(rc, request, userEndpointId, true);
				rc.setUser(user);
			} catch (UserFactoryException e) {
				// Do nothing
			}
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

			if (locale != null) {
				return locale;
			}

		}
		locale = Locale.getDefault();

		// set language locale from browser header if available
		final String acceptLang = request.getHeader("Accept-Language");
		if (acceptLang != null && acceptLang.length() != 0) {
			StringTokenizer t = new StringTokenizer(acceptLang, ",; ");

			// get language and convert to java locale format
			String language = t.nextToken().replace('-', '_');
			locale = I18NUtil.parseLocale(language);
		}

		I18NUtil.setContentLocale(locale);
		// set locale onto Alfresco thread local
		if (!locale.getLanguage().equals(Locale.FRENCH.getLanguage())) {
			if(Locale.US.getCountry().equals(locale.getCountry())) {
				locale = Locale.US;
			} else {
				locale = Locale.ENGLISH;
			}
		} else {
			locale = Locale.FRENCH;
		}
		I18NUtil.setLocale(locale);

		return locale;
	}

}

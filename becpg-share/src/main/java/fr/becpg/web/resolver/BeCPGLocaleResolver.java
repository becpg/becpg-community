package fr.becpg.web.resolver;

import java.util.Locale;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.RequestContext;
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

		Locale locale = Locale.getDefault();

		RequestContext rc = ThreadLocalRequestContext.getRequestContext();

		User user = rc.getUser();
		if (user != null) {
			/**
			 * TODO for LDAP support map it to localeID under
			 * personAttributeMapping form common-ldap-context.xml
			 */

			if (user.getProperties().containsKey("bcpg:userLocale")) {
				locale = I18NUtil.parseLocale((String) user.getProperties().get("bcpg:userLocale"));
				// set locale onto Alfresco thread local
				I18NUtil.setLocale(locale);

				return locale;
			}

		}

		// set language locale from browser header if available
		final String acceptLang = request.getHeader("Accept-Language");
		if ((acceptLang != null) && (acceptLang.length() != 0)) {
			StringTokenizer t = new StringTokenizer(acceptLang, ",; ");

			// get language and convert to java locale format
			String language = t.nextToken().replace('-', '_');

			Locale tmpLocale = I18NUtil.parseLocale(language);

			if (!locale.getLanguage().equals(tmpLocale.getLanguage())) {
				ConfigService configService = rc.getServiceRegistry().getConfigService();

				for (org.springframework.extensions.config.ConfigElement config : configService.getConfig("Languages")
						.getConfigElement("ui-languages").getChildren()) {
					Locale configLocale = I18NUtil.parseLocale(config.getAttribute("locale"));
					if (configLocale.getLanguage().equals(tmpLocale.getLanguage())) {
						locale = tmpLocale;
						break;
					}

				}

			}

			locale = I18NUtil.parseLocale(language);

		}

		// set locale onto Alfresco thread local
		I18NUtil.setLocale(locale);

		return locale;
	}

}

package fr.becpg.web.resolver;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.Config;
import org.springframework.extensions.config.ConfigElement;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.surf.RequestContext;
import org.springframework.extensions.surf.UserFactory;
import org.springframework.extensions.surf.WebFrameworkServiceRegistry;
import org.springframework.extensions.surf.exception.UserFactoryException;
import org.springframework.extensions.surf.support.ThreadLocalRequestContext;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.connector.User;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

/**
 * <p>Resolves the locale of the current Share request.</p>
 *
 * The language stored on the user profile always wins. Otherwise the browser language is
 * used, provided beCPG ships an interface translation for it, as declared by the
 * <code>ui-languages</code> element of the <code>Languages</code> configuration. Any other
 * language falls back to English.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class BeCPGLocaleResolver extends AcceptHeaderLocaleResolver {

	private static final Log logger = LogFactory.getLog(BeCPGLocaleResolver.class);

	private static final String USER_LOCALE_PREFIX = "userLocale_";

	private static final String USER_CONTENT_LOCALE_PREFIX = "userContentLocale_";

	private static final String RESET_LOCALE_PARAM = "resetLocale";

	private static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";

	private static final String ACCEPT_LANGUAGE_SEPARATORS = ",; ";

	private static final String LANGUAGES_CONFIG = "Languages";

	private static final String UI_LANGUAGES_ELEMENT = "ui-languages";

	private static final String LOCALE_ATTRIBUTE = "locale";

	private static final UserLocales NO_USER_LOCALES = new UserLocales(null, null);

	private volatile SupportedLocales supportedUILocales;

	/**
	 * <p>The languages stored on the profile of the current user.</p>
	 *
	 * @param uiLocale the language of the interface, <code>null</code> when the profile has none
	 * @param contentLocale the language of the data, <code>null</code> when the profile has none
	 */
	private record UserLocales(Locale uiLocale, Locale contentLocale) {
	}

	/**
	 * <p>The interface languages beCPG is translated in, indexed for a lookup that allocates
	 * nothing, as the resolver runs on every request.</p>
	 *
	 * @param byLocale the languages indexed by locale, so that <code>en_US</code> keeps its country
	 * @param byLanguage the languages indexed by language, so that <code>es_ES</code> falls back to Spanish
	 */
	private record SupportedLocales(Map<Locale, Locale> byLocale, Map<String, Locale> byLanguage) {
	}

	/** {@inheritDoc} */
	@Override
	public Locale resolveLocale(HttpServletRequest request) {

		UserLocales userLocales = applyUserLocales(resolveUser(request));

		if (userLocales.uiLocale() != null) {
			return userLocales.uiLocale();
		}

		Locale requestLocale = extractRequestLocale(request);

		if (userLocales.contentLocale() == null) {
			I18NUtil.setContentLocale(requestLocale);
		}

		return applyUILocale(requestLocale);
	}

	/**
	 * <p>Returns the user of the current request, reinitialising it when the caller asks for a
	 * locale reset after a language change.</p>
	 *
	 * @param request the current request
	 * @return the current user, or <code>null</code> when the request is not authenticated
	 */
	@SuppressWarnings("deprecation")
	private User resolveUser(HttpServletRequest request) {

		RequestContext rc = ThreadLocalRequestContext.getRequestContext();

		User user = rc.getUser();

		if (user == null) {
			HttpSession session = request.getSession(false);
			if (session != null) {
				user = (User) session.getAttribute(UserFactory.SESSION_ATTRIBUTE_KEY_USER_OBJECT);
			}
		}

		if (Boolean.TRUE.toString().equals(request.getParameter(RESET_LOCALE_PARAM))) {
			String userEndpointId = (String) rc.getAttribute(RequestContext.USER_ENDPOINT);
			try {
				user = rc.getServiceRegistry().getUserFactory().initialiseUser(rc, request, userEndpointId, true);
				rc.setUser(user);
			} catch (UserFactoryException e) {
				logger.debug("Cannot reinitialise the user, keeping the locale of the current session", e);
			}
		}

		return user;
	}

	/**
	 * <p>Applies the languages stored on the user profile to the current thread.</p>
	 *
	 * @param user the current user, may be <code>null</code>
	 * @return the languages of the user, each of them <code>null</code> when the profile has none
	 */
	private UserLocales applyUserLocales(User user) {

		if (user == null) {
			return NO_USER_LOCALES;
		}

		Locale uiLocale = null;
		Locale contentLocale = null;

		for (Map.Entry<String, Boolean> entry : user.getCapabilities().entrySet()) {
			String capability = entry.getKey();
			if (capability.startsWith(USER_LOCALE_PREFIX)) {
				uiLocale = I18NUtil.parseLocale(capability.substring(USER_LOCALE_PREFIX.length()));
				I18NUtil.setLocale(uiLocale);
			} else if (capability.startsWith(USER_CONTENT_LOCALE_PREFIX)) {
				contentLocale = I18NUtil.parseLocale(capability.substring(USER_CONTENT_LOCALE_PREFIX.length()));
				I18NUtil.setContentLocale(contentLocale);
			}
		}

		return new UserLocales(uiLocale, contentLocale);
	}

	/**
	 * <p>Extracts the language asked for by the browser, falling back to the server language.</p>
	 *
	 * @param request the current request
	 * @return the requested locale
	 */
	private Locale extractRequestLocale(HttpServletRequest request) {

		String acceptLang = request.getHeader(ACCEPT_LANGUAGE_HEADER);

		if ((acceptLang == null) || acceptLang.isEmpty()) {
			return Locale.getDefault();
		}

		StringTokenizer tokenizer = new StringTokenizer(acceptLang, ACCEPT_LANGUAGE_SEPARATORS);

		if (!tokenizer.hasMoreTokens()) {
			return Locale.getDefault();
		}

		return I18NUtil.parseLocale(tokenizer.nextToken().replace('-', '_'));
	}

	/**
	 * <p>Applies the interface language of the request to the current thread, downgrading it when
	 * beCPG is not translated in the requested language.</p>
	 *
	 * @param requestLocale the language asked for by the request
	 * @return the interface language of the request
	 */
	private Locale applyUILocale(Locale requestLocale) {

		Locale uiLocale = toSupportedUILocale(requestLocale);

		I18NUtil.setLocale(uiLocale);

		return uiLocale;
	}

	/**
	 * <p>Returns the translated language matching the given one, English when there is none.</p>
	 *
	 * @param locale the requested language
	 * @return a language beCPG is translated in
	 */
	private Locale toSupportedUILocale(Locale locale) {

		SupportedLocales supported = getSupportedUILocales();

		Locale uiLocale = supported.byLocale().get(locale);

		if (uiLocale == null) {
			uiLocale = supported.byLanguage().get(locale.getLanguage());
		}

		if (uiLocale == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("No interface translation for locale " + locale + ", falling back to English");
			}
			return Locale.US.getCountry().equals(locale.getCountry()) ? Locale.US : Locale.ENGLISH;
		}

		return uiLocale;
	}

	/**
	 * <p>Getter for the field <code>supportedUILocales</code>.</p>
	 *
	 * @return the translated languages
	 */
	private SupportedLocales getSupportedUILocales() {

		SupportedLocales supported = supportedUILocales;

		if (supported == null) {
			supported = readSupportedUILocales();
			supportedUILocales = supported;
		}

		return supported;
	}

	/**
	 * <p>Indexes the configured interface languages by locale and by language, so that a browser
	 * asking for <code>es_ES</code> or <code>sv</code> is served in Spanish or Swedish.</p>
	 *
	 * The map is read once and kept, a request only pays for its lookups.
	 *
	 * @return the translated languages
	 */
	private SupportedLocales readSupportedUILocales() {

		Set<Locale> locales = new LinkedHashSet<>();

		for (String localeKey : readConfiguredLocaleKeys()) {
			locales.add(I18NUtil.parseLocale(localeKey));
		}

		Map<Locale, Locale> byLocale = new HashMap<>();
		Map<String, Locale> byLanguage = new HashMap<>();

		for (Locale locale : locales) {
			byLocale.put(locale, locale);
		}

		for (Locale locale : locales) {
			byLanguage.putIfAbsent(locale.getLanguage(), locale);
		}

		return new SupportedLocales(byLocale, byLanguage);
	}

	/**
	 * <p>Reads the interface languages declared by the <code>Languages</code> configuration.</p>
	 *
	 * French and English are always supported, so that an unreadable configuration cannot leave
	 * the interface untranslated.
	 *
	 * @return the configured locale keys, in configuration order
	 */
	private Set<String> readConfiguredLocaleKeys() {

		Set<String> localeKeys = new LinkedHashSet<>();
		localeKeys.add(Locale.FRENCH.getLanguage());
		localeKeys.add(Locale.ENGLISH.getLanguage());

		ConfigElement uiLanguages = readUILanguagesConfig();

		if (uiLanguages != null) {
			for (ConfigElement language : uiLanguages.getChildren()) {
				String localeKey = language.getAttribute(LOCALE_ATTRIBUTE);
				if ((localeKey != null) && !localeKey.isEmpty()) {
					localeKeys.add(localeKey);
				}
			}
		}

		return localeKeys;
	}

	/**
	 * <p>Returns the <code>ui-languages</code> configuration element.</p>
	 *
	 * @return the configuration element, or <code>null</code> when it is not declared
	 */
	private ConfigElement readUILanguagesConfig() {

		RequestContext rc = ThreadLocalRequestContext.getRequestContext();

		WebFrameworkServiceRegistry serviceRegistry = rc.getServiceRegistry();

		if (serviceRegistry == null) {
			return null;
		}

		ConfigService configService = serviceRegistry.getConfigService();

		if (configService == null) {
			return null;
		}

		Config config = configService.getConfig(LANGUAGES_CONFIG);

		return config != null ? config.getConfigElement(UI_LANGUAGES_ELEMENT) : null;
	}

}

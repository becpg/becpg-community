package fr.becpg.repo.helper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;

/**
 *
 * @author matthieu
 *
 */
@Component
public class MLTextHelper {

	private static String supportedLocales;

	private static boolean shouldExtractMLText;

	private static final String[] supportedUILocales = { "en", "en_US", "fr", "sv_SE", "fi", "es", "it", "pt", "ru", "de" };

	@Value("${beCPG.multilinguale.supportedLocales}")
	public void setSupportedLocales(String supportedLocales) {
		MLTextHelper.supportedLocales = supportedLocales;
	}

	@Value("${beCPG.multilinguale.shouldExtractMLText}")
	public void setshouldExtractMLText(boolean shouldExtractMLText) {
		MLTextHelper.shouldExtractMLText = shouldExtractMLText;
	}

	public static boolean shouldExtractMLText() {
		return shouldExtractMLText;
	}
	
	/**
	 * Try to find the best match for locale or try with default server local
	 * 
	 * @param mltext
	 * @param locale
	 * @return
	 */
	public static String getClosestValue(MLText mltext, Locale locale) {
		String ret = null;

		if (mltext != null) {
			if (mltext.containsKey(locale)) {
				ret = mltext.get(locale);
			} else {
				Locale match = getNearestLocale(locale, mltext.getLocales());

				// Try with system local
				if (match == null) {
					match = getNearestLocale(Locale.getDefault(), mltext.getLocales());
				}

				// Any locale
				if (match == null) {
					match = getNearestLocale(null, mltext.getLocales());
				}

				// Did we get a match
				if (match == null) {
					// We could find no locale matches
					return null;
				} else {

					return mltext.get(match);
				}

			}
		}

		return ret;

	}

	public static boolean isDefaultLocale(Locale locale) {
		return (locale != null) && locale.equals(getNearestLocale(Locale.getDefault(), new HashSet<>(getSupportedLocales())));
	}

	public static Locale getNearestLocale(Locale locale) {
		return getNearestLocale(locale, new HashSet<>(getSupportedLocales()));
	}

	public static Locale getNearestLocale(Locale templateLocale, Set<Locale> options) {
		if (options.isEmpty()) // No point if there are no options
		{
			return null;
		} else if (templateLocale == null) {
			// Return first locale found
			for (Locale locale : options) {
				return locale;
			}
		} else if (options.contains(templateLocale)) // First see if there is an
														// exact match
		{
			return templateLocale;
		}

		Locale lastMatchingOption = null;
		Locale languageMatchingOption = null;

		// First test language only
		for (Locale temp : options) {
			if ((temp.getLanguage() != null) && temp.getLanguage().equals(templateLocale.getLanguage())) {
				if ((temp.getCountry() != null) && temp.getCountry().equals(templateLocale.getCountry())) {
					return temp;
				}

				if ((temp.getCountry() == null) || temp.getCountry().isEmpty()) {
					languageMatchingOption = temp;
				}

				if (lastMatchingOption == null) {
					lastMatchingOption = temp;
				}
			}

		}

		return languageMatchingOption != null ? languageMatchingOption : lastMatchingOption;

	}

	public static boolean isSupportedLocale(Locale contentLocale) {
		if (supportedLocales.contains(contentLocale.toString())) {
			return true;
		}
		return false;
	}

	public static List<Locale> getSupportedLocales() {

		List<Locale> ret = new LinkedList<>();

		if (supportedLocales != null) {
			String[] locales = supportedLocales.split(",");
			for (String key : locales) {
				ret.add(parseLocale(key));
			}
		}

		ret.sort((a, b) -> {
			return localeLabel(a).compareTo(localeLabel(b));

		});

		return ret;
	}

	
	public static List<String> getSupportedLocalesList() {
		
		List<String> ret = new ArrayList<String>();
		if (supportedLocales != null) {
			ret = Arrays.asList(supportedLocales.split(","));
		}

		return ret;
	}



	public static Locale parseLocale(String key) {
		if (key.contains("_")) {
			return new Locale(key.split("_")[0], key.split("_")[1]);
		}
		return new Locale(key);

	}

	public static String getValueOrDefault(NodeService nodeService, NodeRef nodeRef, QName propCharactName) {
		String ret = (String) nodeService.getProperty(nodeRef, propCharactName);

		if (ret == null) {
			Locale locale = I18NUtil.getContentLocale();
			try {
				I18NUtil.setContentLocale(Locale.getDefault());
				ret = (String) nodeService.getProperty(nodeRef, propCharactName);
			} finally {
				I18NUtil.setContentLocale(locale);
			}

		}

		return ret;
	}

	public static String localeKey(Locale locale) {
		String ret = locale.getLanguage();
		if ((locale.getCountry() != null) && !locale.getCountry().isEmpty()) {
			ret += "_" + locale.getCountry();
		}
		return ret;
	}

	public static String localeLabel(Locale locale) {
		String ret = locale.getDisplayLanguage();
		if ((locale.getCountry() != null) && !locale.getCountry().isEmpty()) {
			ret += " - " + locale.getDisplayCountry();
		}
		return ret;
	}

	public static boolean isEmpty(MLText mlText) {
		for (String value : mlText.values()) {
			if ((value != null) && !value.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public static Set<Locale> extractLocales(List<String> locales) {
		Set<Locale> ret = new LinkedHashSet<>();

		for (String tmp : locales) {
			ret.add(MLTextHelper.parseLocale(tmp));
		}
		return ret;
	}

	public static MLText getI18NMessage(String messageKey, Object... variables) {
		MLText ret = new MLText();
		for (String key : supportedUILocales) {
			
			if (supportedLocales.contains(key)) {
				Locale locale = parseLocale(key);
				List<Object> parsedVariable = new LinkedList<>();
				if (variables != null) {
					for (Object tmp : variables) {
						if (tmp instanceof MLText) {
							parsedVariable.add(getClosestValue((MLText) tmp, locale));
						} else {
							parsedVariable.add(tmp);
						}
					}
				}
				
				
				ret.addValue(locale, I18NUtil.getMessage(messageKey, locale, parsedVariable.toArray()));

			}

		}
		return ret;
	}

	public interface MLTextCallback {
		public String run(Locale locale);
	}

	public static MLText createMLTextI18N(MLTextCallback callback) {
		MLText ret = new MLText();

		for (String key : supportedUILocales) {
			if (supportedLocales.contains(key)) {
				Locale locale = parseLocale(key);
				ret.addValue(locale, callback.run(locale));
			}
		}
		return ret;
	}

}
package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Component;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;

/**
 * <p>MLTextHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Component
public class MLTextHelper {

	private static List<Locale> supportedLocales = new LinkedList<>();
	private static String supportedLocalesText = "";
	private static boolean shouldExtractMLText;

	private static Map<String, MLText> mlTextCache = new ConcurrentHashMap<>();
	
	private static boolean useBrowserLocale;

	/**
	 * <p>Setter for the field <code>useBrowserLocale</code>.</p>
	 *
	 * @param useBrowserLocale a boolean.
	 */
	@Value("${beCPG.multilinguale.useBrowserLocale}")
	public void setUseBrowserLocale(boolean useBrowserLocale) {
		MLTextHelper.useBrowserLocale = useBrowserLocale;
	}


	/**
	 * <p>Setter for the field <code>supportedLocales</code>.</p>
	 *
	 * @param supportedLocales a {@link java.lang.String} object.
	 */
	@Value("${beCPG.multilinguale.supportedLocales}")
	public void setSupportedLocales(String supportedLocales) {

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

		MLTextHelper.supportedLocales = ret;
		MLTextHelper.supportedLocalesText = supportedLocales;
	}

	/**
	 * <p>Getter for the field <code>supportedLocales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public static List<Locale> getSupportedLocales() {
		return supportedLocales;
	}

	/**
	 * <p>Setter for the field <code>shouldExtractMLText</code>.</p>
	 *
	 * @param shouldExtractMLText a boolean.
	 */
	@Value("${beCPG.multilinguale.shouldExtractMLText}")
	public void setshouldExtractMLText(boolean shouldExtractMLText) {
		MLTextHelper.shouldExtractMLText = shouldExtractMLText;
	}

	/**
	 * <p>shouldExtractMLText.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean shouldExtractMLText() {
		return shouldExtractMLText;
	}
	
  
	/**
	 * Try to find the best match for locale or try with default server local
	 *
	 * @param mltext a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
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

	/**
	 * <p>isDefaultLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public static boolean isDefaultLocale(Locale locale) {
		return (locale != null) && locale.equals(getNearestLocale(Locale.getDefault(), new HashSet<>(getSupportedLocales())));
	}

	/**
	 * <p>getNearestLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.util.Locale} object.
	 */
	public static Locale getNearestLocale(Locale locale) {
		return getNearestLocale(locale, new HashSet<>(getSupportedLocales()));
	}

	/**
	 * <p>getNearestLocale.</p>
	 *
	 * @param templateLocale a {@link java.util.Locale} object.
	 * @param options a {@link java.util.Set} object.
	 * @return a {@link java.util.Locale} object.
	 */
	public static Locale getNearestLocale(Locale templateLocale, Set<Locale> options) {
		if (options.isEmpty()) // No point if there are no options
		{
			return null;
		} else if (templateLocale == null) {
			// Return first locale found
			if (!options.isEmpty()) {
				return options.iterator().next();
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
			if ((temp.getLanguage() != null) && (templateLocale != null) && temp.getLanguage().equals(templateLocale.getLanguage())) {
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

	/**
	 * <p>isSupportedLocale.</p>
	 *
	 * @param contentLocale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public static boolean isSupportedLocale(Locale contentLocale) {
		return (contentLocale != null) && supportedLocales.contains(contentLocale);
	}

	/**
	 * <p>getSupportedLocalesList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public static List<String> getSupportedLocalesList() {

		if (supportedLocalesText != null) {
			return Arrays.asList(supportedLocalesText.split(","));
		}

		return new ArrayList<>();
	}

	/**
	 * <p>parseLocale.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link java.util.Locale} object.
	 */
	public static Locale parseLocale(String key) {
		if (key.contains("_")) {
			return new Locale(key.split("_")[0], key.split("_")[1]);
		}
		return new Locale(key);

	}

	/**
	 * <p>getValueOrDefault.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param propCharactName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link java.lang.String} object.
	 */
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

	/**
	 * <p>localeKey.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String localeKey(Locale locale) {
		String ret = locale.getLanguage();
		if ((locale.getCountry() != null) && !locale.getCountry().isBlank()) {
			ret += "_" + locale.getCountry();
		}
		return ret;
	}

	/**
	 * <p>localeLabel.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String localeLabel(Locale locale) {
		String ret = locale.getDisplayLanguage();
		if ((locale.getCountry() != null) && !locale.getCountry().isBlank()) {
			ret += " - " + locale.getDisplayCountry();
		}
		return ret;
	}

	/**
	 * <p>isEmpty.</p>
	 *
	 * @param mlText a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @return a boolean.
	 */
	public static boolean isEmpty(MLText mlText) {
		for (String value : mlText.values()) {
			if ((value != null) && !value.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * <p>extractLocales.</p>
	 *
	 * @param locales a {@link java.util.List} object.
	 * @return a {@link java.util.Set} object.
	 */
	public static Set<Locale> extractLocales(List<String> locales) {
		Set<Locale> ret = new LinkedHashSet<>();

		for (String tmp : locales) {
			ret.add(MLTextHelper.parseLocale(tmp));
		}
		return ret;
	}

	/**
	 * <p>getI18NMessage.</p>
	 *
	 * @param messageKey a {@link java.lang.String} object.
	 * @param variables a {@link java.lang.Object} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public static MLText getI18NMessage(String messageKey, Object... variables) {

		if (variables == null) {
			return mlTextCache.computeIfAbsent(messageKey, MLTextHelper::internalI18NMessage);
		}

		return internalI18NMessage(messageKey, variables);
	}

	private static MLText internalI18NMessage(String messageKey, Object... variables) {

		MLText ret = new MLText();
		for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {

			if (supportedLocalesText.contains(key)) {
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
				if (parsedVariable.isEmpty()) {
					ret.addValue(locale, I18NUtil.getMessage(messageKey, locale));
				} else {
					ret.addValue(locale, I18NUtil.getMessage(messageKey, locale, parsedVariable.toArray()));
				}

			}

		}
		return ret;
	}

	public interface MLTextCallback {
		public String run(Locale locale);
	}

	/**
	 * <p>createMLTextI18N.</p>
	 *
	 * @param callback a {@link fr.becpg.repo.helper.MLTextHelper.MLTextCallback} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public static MLText createMLTextI18N(MLTextCallback callback) {
		MLText ret = new MLText();

		for (String key : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
			if (supportedLocalesText.contains(key)) {
				Locale locale = parseLocale(key);
				ret.addValue(locale, callback.run(locale));
			}
		}
		return ret;
	}

	/**
	 * <p>merge.</p>
	 *
	 * @param toMergeTo a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param toMergeFrom a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @return a {@link java.io.Serializable} object.
	 */
	public static Serializable merge(MLText toMergeTo, MLText toMergeFrom) {

		for (Map.Entry<Locale, String> entry : toMergeFrom.entrySet()) {
			String value = toMergeTo.get(entry.getKey());
			if ((value == null) || value.isEmpty()) {
				toMergeTo.put(entry.getKey(), entry.getValue());
			}
		}

		return toMergeTo;
	}

	/**
	 * @param contentLocale
	 * @return if contentLocale is supported or language only locale
	 */
	public static Locale getSupportedLocale(Locale contentLocale) {
		if (contentLocale != null) {
			if (MLTextHelper.isSupportedLocale(contentLocale)) {
				return contentLocale;
			}
			String language = contentLocale.getLanguage();
			return new Locale(language);
		}
		return null;
	}
	
	public static Locale getUserLocale(NodeService nodeService, NodeRef personNodeRef) {
		String loc = (String) nodeService.getProperty(personNodeRef, BeCPGModel.PROP_USER_LOCALE);
		if ((loc == null) || loc.isEmpty()) {
			Locale currentLocale = Locale.getDefault();

			if (useBrowserLocale) {
				currentLocale = I18NUtil.getLocale();
			}
			if (!Locale.FRENCH.getLanguage().equals(currentLocale.getLanguage())) {
				if (Locale.US.getCountry().equals(currentLocale.getCountry())) {
					return Locale.US;
				}
				return Locale.ENGLISH;
			}
			return Locale.FRENCH;

		}
		return MLTextHelper.parseLocale(loc);
	}

	/**
	 * <p>getUserContentLocale.</p>
	 *
	 * @param personNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.Locale} object.
	 */
	public static Locale getUserContentLocale(NodeService nodeService, NodeRef personNodeRef) {
		String loc = (String) nodeService.getProperty(personNodeRef, BeCPGModel.PROP_USER_CONTENT_LOCAL);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale) {
				return MLTextHelper.getNearestLocale(I18NUtil.getContentLocale());
			} else {
				return MLTextHelper.getNearestLocale(Locale.getDefault());
			}
		}
		return MLTextHelper.parseLocale(loc);
	}

}

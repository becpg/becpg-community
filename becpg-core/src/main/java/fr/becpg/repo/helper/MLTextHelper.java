package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.system.SystemConfigurationRegistry;

/**
 * <p>MLTextHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MLTextHelper {

	private static Map<String, MLText> mlTextCache = new ConcurrentHashMap<>();

	private static List<Locale> supportedLocales = null;

	private static String supportedLocalesText = null;

	private static final Log logger = LogFactory.getLog(MLTextHelper.class);

	/**
	 * <p>Constructor for MLTextHelper.</p>
	 */
	private MLTextHelper() {
		//DO Nothing
	}

	public static void flushCache() {
		mlTextCache = new ConcurrentHashMap<>();
		supportedLocales = null;
		supportedLocalesText = null;
	}

	/**
	 * <p>setSupportedLocalesInstance.</p>
	 *
	 * @param supportedLocales a {@link java.lang.String} object
	 */
	public static synchronized void setSupportedLocales(String supportedLocalesText) {
		MLTextHelper.supportedLocalesText = supportedLocalesText;
	}

	private static boolean useBrowserLocale() {
		return Boolean.parseBoolean(SystemConfigurationRegistry.instance().confValue("beCPG.multilinguale.useBrowserLocale"));
	}

	/**
	 * <p>shouldExtractMLText.</p>
	 *
	 * @return a boolean.
	 */
	public static boolean shouldExtractMLText() {
		return Boolean.parseBoolean(SystemConfigurationRegistry.instance().confValue("beCPG.multilinguale.shouldExtractMLText"));
	}

	/**
	 * <p>Getter for the field <code>supportedLocales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public static synchronized List<Locale> getSupportedLocales() {
		if (supportedLocales == null) {

			List<Locale> ret = new ArrayList<>();
			String localesText = supportedLocalesText;

			if (supportedLocalesText == null) {
				localesText = SystemConfigurationRegistry.instance().confValue("beCPG.multilinguale.supportedLocales");
			}

			if (localesText != null) {
				String[] locales = localesText.split(",");
				for (String key : locales) {
					ret.add(parseLocale(key.trim()));
				}
			}

			supportedLocales = ret;

			// Sort locales
			supportedLocales.sort((a, b) -> {
				if (isDefaultLocale(a)) {
					return -1;
				}
				if (isDefaultLocale(b)) {
					return 1;
				}
				return localeLabel(a).compareTo(localeLabel(b));
			});

			if (logger.isInfoEnabled()) {
				logger.info("Init supported locale with: " + ret.toString());
			}
		}
		return supportedLocales;
	}

	/**
	 * <p>getSupportedLocalesList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public static List<String> getSupportedLocalesList() {
		return getSupportedLocales().stream().map(MLTextHelper::localeKey).collect(Collectors.toList());
	}

	public static boolean isDisabledMLTextField(String propertyQNamePrexiString) {
		String disabledMLTextFields = SystemConfigurationRegistry.instance().confValue("beCPG.multilinguale.disabledMLTextFields");
		if ((disabledMLTextFields != null) && !disabledMLTextFields.isBlank() && disabledMLTextFields.contains(propertyQNamePrexiString)) {
			return true;
		}
		return false;
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
		return (contentLocale != null) && getSupportedLocales().contains(contentLocale);
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
		StringBuilder ret = new StringBuilder().append(locale.getLanguage());
		if ((locale.getCountry() != null) && !locale.getCountry().isBlank()) {
			ret.append("_").append(locale.getCountry());
		}
		return ret.toString();
	}

	/**
	 * <p>localeLabel.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String localeLabel(Locale locale) {
		StringBuilder ret = new StringBuilder().append(locale.getDisplayLanguage());
		if ((locale.getCountry() != null) && !locale.getCountry().isBlank()) {
			ret.append(" - ").append(locale.getDisplayCountry());
		}
		return ret.toString();
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
			Locale locale = parseLocale(key);
			if (isSupportedLocale(locale)) {
				List<Object> parsedVariable = new LinkedList<>();
				if (variables != null) {
					for (Object tmp : variables) {
						if (tmp instanceof MLText mlText) {
							parsedVariable.add(getClosestValue(mlText, locale));
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
		String run(Locale locale);
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
			Locale locale = parseLocale(key);
			if (isSupportedLocale(locale)) {
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
	 * <p>getSupportedLocale.</p>
	 *
	 * @param contentLocale a {@link java.util.Locale} object
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

	/**
	 * <p>getUserLocale.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param personNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.Locale} object
	 */
	public static Locale getUserLocale(NodeService nodeService, NodeRef personNodeRef) {
		String loc = (String) nodeService.getProperty(personNodeRef, BeCPGModel.PROP_USER_LOCALE);
		if ((loc == null) || loc.isEmpty()) {
			Locale currentLocale = Locale.getDefault();

			if (useBrowserLocale()) {
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
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public static Locale getUserContentLocale(NodeService nodeService, NodeRef personNodeRef) {
		String loc = (String) nodeService.getProperty(personNodeRef, BeCPGModel.PROP_USER_CONTENT_LOCAL);
		if ((loc == null) || loc.isEmpty()) {
			if (useBrowserLocale()) {
				return MLTextHelper.getNearestLocale(I18NUtil.getContentLocale());
			} else {
				return MLTextHelper.getNearestLocale(Locale.getDefault());
			}
		}
		return MLTextHelper.parseLocale(loc);
	}

}

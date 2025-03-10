/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.helper;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;

/**
 * <p>TranslateHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TranslateHelper {

	private static final String PATH_MSG_PFX = "path.";
	private static final String LIST_CONSTRAINT_MSG_PFX = "listconstraint.%s.%s";

	private static final String MESSAGE_TRUE = "data.boolean.true";
	private static final String MESSAGE_FALSE = "data.boolean.false";

	private static final Log logger = LogFactory.getLog(TranslateHelper.class);

	private TranslateHelper() {
		// Only static method
	}
	/**
	 * Translate the name of the path.
	 *
	 * @param name
	 *            the name
	 * @return the translated path
	 */
	public static String getTranslatedPath(String name) {
		String translation = I18NUtil.getMessage(PATH_MSG_PFX + name.toLowerCase(), Locale.getDefault());
		if (logger.isDebugEnabled() && (translation == null)) {
			logger.debug("Failed to translate path. path: " + name);
		}
		return translation;

	}

	/**
	 * <p>getTranslatedPathMLText.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public static MLText getTranslatedPathMLText(String name) {
		return getTranslatedKey(PATH_MSG_PFX + name.toLowerCase());
	}

	/**
	 * <p>getTranslatedBoolean.</p>
	 *
	 * @param b a {@link java.lang.Boolean} object.
	 * @param useDefaultLocale a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getTranslatedBoolean(Boolean b, boolean useDefaultLocale) {
	    if (b == null) {
	        return "";
	    }

	    // Determine the locale to use
	    Locale locale = useDefaultLocale ? Locale.getDefault() : I18NUtil.getLocale();

	    // Fetch the translation for the boolean value
	    String translation = I18NUtil.getMessage(Boolean.TRUE.equals(b) ? MESSAGE_TRUE : MESSAGE_FALSE, locale);

	    // Provide English fallback if the translation is missing
	    if (translation == null || translation.isEmpty()) {
	        translation = I18NUtil.getMessage(Boolean.TRUE.equals(b) ? MESSAGE_TRUE : MESSAGE_FALSE, Locale.ENGLISH);
	    }

	    return translation;
	}
	
	/**
	 * <p>getConstraint.</p>
	 *
	 * @param constraintName a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param useDefaultLocale a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getConstraint(String constraintName, String value, boolean useDefaultLocale) {

		if (useDefaultLocale) {
			return getConstraint(constraintName, value, Locale.getDefault());
		}

		return getConstraint(constraintName, value, null);

	}

	/**
	 * <p>getTranslatedKey.</p>
	 *
	 * @param key
	 *            the key to search for translations
	 * @return the MLText with the translations for all the languages supported.
	 * List defined in {@link fr.becpg.repo.RepoConsts}
	 */
	public static MLText getTranslatedKey(String key) {
		MLText res = new MLText();

		for (String localeString : RepoConsts.SUPPORTED_UI_LOCALES.split(",")) {
			Locale currentLocale = MLTextHelper.parseLocale(localeString);

			String translation = I18NUtil.getMessage(key, currentLocale);

			if (translation != null) {
				res.addValue(currentLocale, I18NUtil.getMessage(key, currentLocale));
			}
		}

		return res;
	}

	/**
	 * <p>getTemplateModelMLText.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @param key a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public static MLText getTemplateModelMLText(QName classQName, String key) {
		String localName = classQName.getLocalName();
		String shortPrefix = classQName.getPrefixString().split(":")[0];

		if (logger.isDebugEnabled()) {
			logger.debug("getting title mltext for class: " + classQName + " (" + localName + ", pfx:" + shortPrefix + ")");
			logger.debug("Full path: " + shortPrefix + "_" + shortPrefix + "model.type." + shortPrefix + "_" + localName + "." + key);
		}
	
		if("gs1".equals(shortPrefix)) {
			return getTranslatedKey("gs1_gs1Model.type." + shortPrefix + "_" + localName + "." + key);
		} else if("bp".equals(shortPrefix)) {
			return getTranslatedKey("bp_publicationModel.type." + shortPrefix + "_" + localName + "." + key);
		} else if("smp".equals(shortPrefix)) {
			return getTranslatedKey("smp_samplemodel.type." + shortPrefix + "_" + localName + "." + key);	
		} else {
			return getTranslatedKey(shortPrefix + "_" + shortPrefix + "model.type." + shortPrefix + "_" + localName + "." + key);
		}

	}

	/**
	 * <p>getTemplateTitleMLText.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public static MLText getTemplateTitleMLText(QName classQName) {
		return getTemplateModelMLText(classQName, "title");
	}

	/**
	 * <p>getTemplateDescriptionMLText.</p>
	 *
	 * @param classQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.MLText} object.
	 */
	public static MLText getTemplateDescriptionMLText(QName classQName) {
		return getTemplateModelMLText(classQName, "description");
	}

	/**
	 * <p>getConstraint.</p>
	 *
	 * @param constraintName a {@link java.lang.String} object.
	 * @param value a {@link java.lang.String} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @return a {@link java.lang.String} object.
	 */
	public static String getConstraint(String constraintName, String value, Locale locale) {

		String translation = null;
		String messageKey = String.format(LIST_CONSTRAINT_MSG_PFX, constraintName, value);

		if (locale != null) {
			translation = I18NUtil.getMessage(messageKey, locale);
		} else {
			translation = I18NUtil.getMessage(messageKey);
		}

		if (translation == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Failed to translate constraint. constraintName: " + constraintName + " - value: " + value);
			}
			translation = value;
		}

		return translation;
	}
	
	/**
	 * <p>getLocaleAwarePath.</p>
	 *
	 * @param path a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String getLocaleAwarePath(String path) {
		String lang = "en";
		if(Locale.FRENCH.getLanguage().equals(Locale.getDefault().getLanguage())) {
			lang = "fr";
		}
		return String.format(path, lang);
	}

}

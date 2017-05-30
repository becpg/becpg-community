/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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

public class TranslateHelper {

	private static final String PATH_MSG_PFX = "path.";
	private static final String LIST_CONSTRAINT_MSG_PFX = "listconstraint.%s.%s";

	private static final String MESSAGE_TRUE = "data.boolean.true";
	private static final String MESSAGE_FALSE = "data.boolean.false";

	/** The logger. */
	private static final Log logger = LogFactory.getLog(TranslateHelper.class);

	/**
	 * Translate the name of the path.
	 *
	 * @param name
	 *            the name
	 * @return the translated path
	 */
	public static String getTranslatedPath(String name) {
		Locale currentLocal = I18NUtil.getLocale();

		try {
			I18NUtil.setLocale(Locale.getDefault());
			String translation = I18NUtil.getMessage(PATH_MSG_PFX + name.toLowerCase(), Locale.getDefault());
			if (logger.isDebugEnabled() && (translation == null)) {
				logger.debug("Failed to translate path. path: " + name);
			}

			return translation;
		} finally {
			I18NUtil.setLocale(currentLocal);
		}
	}
	
	public static MLText getTranslatedPathMLText(String name){
		return getTranslatedKey(PATH_MSG_PFX + name.toLowerCase());
	}

	public static String getTranslatedBoolean(Boolean b, boolean useDefaultLocale) {

		String translation;

		if (useDefaultLocale) {
			translation = b ? I18NUtil.getMessage(MESSAGE_TRUE, Locale.getDefault()) : I18NUtil.getMessage(MESSAGE_FALSE, Locale.getDefault());
		} else {
			translation = b ? I18NUtil.getMessage(MESSAGE_TRUE) : I18NUtil.getMessage(MESSAGE_FALSE);
		}

		return translation;
	}

	public static String getConstraint(String constraintName, String value, boolean useDefaultLocale) {

		if (useDefaultLocale) {
			return getConstraint(constraintName, value, Locale.getDefault());
		}

		return getConstraint(constraintName, value, null);

	}
	
	/**
	 * Returns the MLText with the translations for all the languages supported.
	 * List defined in {@link RepoConsts}
	 * @param key the key to search for translations
	 * @return 
	 */
	public static MLText getTranslatedKey(String key){
		MLText res = new MLText();
		
		logger.debug("Getting translations for key: "+key);
		for(String localeString : RepoConsts.SUPPORTED_LANGUAGES){
			Locale currentLocale = new Locale(localeString);
			
			String translation = I18NUtil.getMessage(key, currentLocale);
			
			logger.debug("Found translation: "+translation);
			if(translation != null){
				res.addValue(currentLocale, I18NUtil.getMessage(key, currentLocale));
			}
		}
		
		return res;
	}
	
	public static MLText getTemplateModelMLText(QName classQName, String key){
		String localName = classQName.getLocalName();
		String shortPrefix = classQName.getPrefixString().split(":")[0];
		
		logger.debug("getting title mltext for class: "+classQName+" ("+localName+", pfx:"+shortPrefix+")");
		logger.debug("Full path: "+shortPrefix+"_"+shortPrefix+"model.type."+shortPrefix+"_"+localName+"."+key);
		
		return getTranslatedKey(shortPrefix+"_"+shortPrefix+"model.type."+shortPrefix+"_"+localName+"."+key);
	}
	
	
	public static MLText getTemplateTitleMLText(QName classQName){
		return getTemplateModelMLText(classQName, "title");
	}
	
	public static MLText getTemplateDescriptionMLText(QName classQName){
		return getTemplateModelMLText(classQName, "description");
	}

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

}

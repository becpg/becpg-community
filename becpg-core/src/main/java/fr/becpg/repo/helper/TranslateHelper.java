/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

public class TranslateHelper {

	private static final String PATH_MSG_PFX= "path.";
	private static final String LIST_CONSTRAINT_MSG_PFX = "listconstraint.%s.%s";
	
	private static final String MESSAGE_TRUE = "data.boolean.true";
	private static final String MESSAGE_FALSE = "data.boolean.false";
	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(TranslateHelper.class);
	
	/**
	 * Translate the name of the path.
	 *
	 * @param name the name
	 * @return the translated path
	 */
	public static String getTranslatedPath(String name) {
		
		String translation = I18NUtil.getMessage(PATH_MSG_PFX + name.toLowerCase(), Locale.getDefault());
		if(logger.isDebugEnabled() && translation == null){
			logger.debug("Failed to translate path. path: " + name);
		}
		
		return translation;
	}
	
//	public static String getTranslatedSystemState(SystemState state) {
//		
//		String translation = I18NUtil.getMessage(SYSTEM_STATE_MSG_PFX + state, Locale.getDefault());
//		if(translation == null){
//			logger.error("Failed to translate path. path: " + state);
//		}
//		
//		return translation;
//	}
	
	public static String getTranslatedBoolean(Boolean b, boolean useDefaultLocale) {
		
		String translation;
		
		if(useDefaultLocale){
			translation = b ? I18NUtil.getMessage(MESSAGE_TRUE, Locale.getDefault()) : I18NUtil.getMessage(MESSAGE_FALSE, Locale.getDefault());
		}
		else{
			translation = b ? I18NUtil.getMessage(MESSAGE_TRUE) : I18NUtil.getMessage(MESSAGE_FALSE);
		}
		
		return translation;
	}
	
	public static String getConstraint(String constraintName, String value,  boolean useDefaultLocale) {
		
			String translation;
			String messageKey = String.format(LIST_CONSTRAINT_MSG_PFX, constraintName,  value);
			
			if(useDefaultLocale){
				translation = I18NUtil.getMessage(messageKey, Locale.getDefault());
			}
			else{
				translation = I18NUtil.getMessage(messageKey);
			}
					
			if(translation == null){
				if(logger.isDebugEnabled()){
					logger.debug("Failed to translate constraint. constraintName: " + constraintName + " - value: " + value);
				}			
				translation = value;
			}
			
			return translation;
	
	}
	
}

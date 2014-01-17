/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.util;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Allow to override I18N
 * @author matthieu
 *
 */
public class ResourceBundleBootstrapComponent
{

	private static Log logger = LogFactory.getLog(ResourceBundleBootstrapComponent.class);
	
	public void setResourceBundles(List<String> resourceBundles)
	{
		
		//Ensure is loaded first
		I18NUtil.getAllMessages();
		I18NUtil.getAllMessages(Locale.FRENCH);
		I18NUtil.getAllMessages(Locale.ENGLISH);
		
		for (String resourceBundle : resourceBundles)
		{
			try {
				ResourceBundle.getBundle(resourceBundle, I18NUtil.getLocale());
				logger.info("Loading : "+resourceBundle);
				
				I18NUtil.getAllMessages();
				I18NUtil.registerResourceBundle(resourceBundle);
			} catch (MissingResourceException e){
				logger.info("Missing i18n extension file:"+resourceBundle);
			}

		}
		
	}

}

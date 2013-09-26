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
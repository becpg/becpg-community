package fr.becpg.repo.helper;

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
	
	
	@Value("${beCPG.multilinguale.supportedLocales}")
	public void setSupportedLocales(String supportedLocales) {
		MLTextHelper.supportedLocales = supportedLocales;
	}  

	/**
	 * Try to find the best match for locale or try with default server local 
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
         		
         		//Try with system local
         		if(match == null) {
         			
         			match = getNearestLocale(Locale.getDefault(), mltext.getLocales());
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
	

	 public static Locale getNearestLocale(Locale templateLocale, Set<Locale> options)
	    {
	        if (options.isEmpty())                          // No point if there are no options
	        {
	            return null;
	        }
	        else if (templateLocale == null)
	        {
	        	 return null;
	        }
	        else if (options.contains(templateLocale))      // First see if there is an exact match
	        {
	            return templateLocale;
	        }
	        
	        
	        Locale lastMatchingOption = null;
	        Locale languageMatchingOption = null;
	        
	        //First test language only 
	        for(Locale temp :options ){
	        	 if(temp.getLanguage()!=null && temp.getLanguage().equals(templateLocale.getLanguage())){
	        		if(temp.getCountry()!=null && temp.getCountry().equals(templateLocale.getCountry())){
	        			return temp;
	        		} 
	        		
	        		if(temp.getCountry() == null ||temp.getCountry().isEmpty() ){
	        			languageMatchingOption = temp;
	        		}
	        		
	        		if(lastMatchingOption == null){
	        			lastMatchingOption = temp;
	        		}
	        	}
	        	
	        }	
	        
	        return languageMatchingOption!=null ? languageMatchingOption : lastMatchingOption;
	        
	    }

	public static boolean isSupportedLocale(Locale contentLocale) {
		if(supportedLocales.contains(contentLocale.toString())){
			return true;
		}
		return false;
	}

	public static String getValueOrDefault(NodeService nodeService, NodeRef nodeRef, QName propCharactName) {
		String ret = (String) nodeService.getProperty(nodeRef, propCharactName);
		
		if(ret == null){
			Locale locale = I18NUtil.getLocale();
			try {
				I18NUtil.setLocale(Locale.getDefault());
				ret = (String) nodeService.getProperty(nodeRef, propCharactName);
			} finally {
				I18NUtil.setLocale(locale);
			}
			
		}
		
		return ret;
	}

}

package fr.becpg.repo.helper;

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
         		
         		//Any locale
         		if(match == null) {
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
	

	 public static Locale getNearestLocale(Locale templateLocale, Set<Locale> options)
	    {
	        if (options.isEmpty())                          // No point if there are no options
	        {
	            return null;
	        }
	        else if (templateLocale == null)
	        {
	        	//Return first locale found
	        	for (Locale locale : options)
	            {
	                return locale;
	            }
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
	
	public static List<Locale> getSupportedLocales() {
		
		List<Locale> ret = new LinkedList<Locale>();
		
		if(supportedLocales!=null){
			String[] locales = supportedLocales.split(",");
			for (String key : locales) {
					ret.add(parseLocale(key));
			}
		}
		
		return ret;
	}

	public static Locale parseLocale(String key){
		if(key.contains("_")){
			return new Locale(key.split("_")[0],key.split("_")[1]);
		} 
		return new Locale(key);
		
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

	public static String localeKey(Locale locale) {
		String ret = locale.getLanguage();
		if(locale.getCountry()!=null && !locale.getCountry().isEmpty()){
			ret+="_"+locale.getCountry();
		}
		return ret;
	}

	public static String localeLabel(Locale locale) {
		String ret = locale.getDisplayLanguage();
		if(locale.getCountry()!=null && !locale.getCountry().isEmpty()){
			ret+=" - "+locale.getDisplayCountry();
		}
		return ret;
	}


	public static boolean isEmpty(MLText mlText) {
		for(String value : mlText.values()){
			if(value!=null && !value.isEmpty()){
				return false;
			}
		}
		return true;
	}

	

}

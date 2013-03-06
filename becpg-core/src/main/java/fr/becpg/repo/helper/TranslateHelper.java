package fr.becpg.repo.helper;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.SystemState;

public class TranslateHelper {

	private static final String PATH_MSG_PFX= "path.";
	private static final String SYSTEM_STATE_MSG_PFX = "listconstraint.bcpg_systemState.";
	private static final String CONSTRAINT_MSG_PFX = "constraint.%s.%s";
	
	private static final String MESSAGE_TRUE = "data.boolean.true";
	private static final String MESSAGE_FALSE = "data.boolean.false";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(TranslateHelper.class);
	
	/**
	 * Translate the name of the path.
	 *
	 * @param name the name
	 * @return the translated path
	 */
	public static String getTranslatedPath(String name) {
		
		String translation = I18NUtil.getMessage(PATH_MSG_PFX + name.toLowerCase(), Locale.getDefault());
		if(translation == null){
			logger.error("Failed to translate path. path: " + name);
		}
		
		return translation;
	}
	
	public static String getTranslatedSystemState(SystemState state) {
		
		String translation = I18NUtil.getMessage(SYSTEM_STATE_MSG_PFX + state, Locale.getDefault());
		if(translation == null){
			logger.error("Failed to translate path. path: " + state);
		}
		
		return translation;
	}
	
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
	
	@SuppressWarnings("unchecked")
	public static String getConstraint(QName propertyName, Serializable value, boolean useDefaultLocale) {
		
		if(value instanceof String){
			String v = (String)value; 
				
			String translation = null;
			String messageKey = String.format(CONSTRAINT_MSG_PFX, propertyName.getLocalName().toLowerCase(), v.toLowerCase());
			
			if(useDefaultLocale){
				translation = I18NUtil.getMessage(messageKey, Locale.getDefault());
			}
			else{
				translation = I18NUtil.getMessage(messageKey);
			}
					
			if(translation == null){
				if(logger.isDebugEnabled()){
					logger.debug("Failed to translate constraint. propertyName: " + propertyName + " - value: " + v);
				}			
				translation = v;
			}
			
			return translation;
		} else if(value instanceof ArrayList){
			String ret ="";
			for (String v : (ArrayList<String>)value) {
				if(!ret.isEmpty()){
					ret+=",";
				}
				ret+=getConstraint(propertyName,v,useDefaultLocale);
			}

			return ret;	
		}	
		throw new IllegalStateException("Unknow constraint type ");
		
	}
	
}

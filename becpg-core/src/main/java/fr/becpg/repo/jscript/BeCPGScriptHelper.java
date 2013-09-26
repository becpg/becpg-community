package fr.becpg.repo.jscript;

import java.util.Locale;

import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Utility script methods
 * @author matthieu
 *
 */
public final class BeCPGScriptHelper extends BaseScopableProcessorExtension{

	
	public String getMessage(String messageKey){
		return I18NUtil.getMessage(messageKey,  Locale.getDefault());
	}
	
	public String getMessage(String messageKey, Object param ){
		return I18NUtil.getMessage(messageKey,param,  Locale.getDefault());
	}
	
	
}

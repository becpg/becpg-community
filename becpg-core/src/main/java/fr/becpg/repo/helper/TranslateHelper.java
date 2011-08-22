package fr.becpg.repo.helper;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.SystemState;

public class TranslateHelper {

	public static final String PATH_MSG_PFX= "path.";
	public static final String PRODUCT_STATE_MSG_PFX = "state.product.";
	
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
	
	public static String getTranslatedProductState(SystemState state) {
		
		String translation = I18NUtil.getMessage(PRODUCT_STATE_MSG_PFX + state.toString().toLowerCase(), Locale.getDefault());
		if(translation == null){
			logger.error("Failed to translate path. path: " + state);
		}
		
		return translation;
	}
}

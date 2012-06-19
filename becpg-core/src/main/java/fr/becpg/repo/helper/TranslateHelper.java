package fr.becpg.repo.helper;

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.productList.DeclarationType;

public class TranslateHelper {

	private static final String PATH_MSG_PFX= "path.";
	private static final String PRODUCT_STATE_MSG_PFX = "state.product.";
	private static final String DECLARATION_TYPE_MSG_PFX = "declarationType.";
	
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
	
	public static String getTranslatedProductState(SystemState state) {
		
		String translation = I18NUtil.getMessage(PRODUCT_STATE_MSG_PFX + state.toString().toLowerCase(), Locale.getDefault());
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
	
	public static String getTranslatedDeclarationType(DeclarationType declType) {
		
		String translation = I18NUtil.getMessage(DECLARATION_TYPE_MSG_PFX + declType.toString().toLowerCase(), Locale.getDefault());
		if(translation == null){
			logger.error("Failed to translate declarationType. declarationType: " + declType);
		}
		
		return translation;
	}
	
}

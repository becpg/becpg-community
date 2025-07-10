package fr.becpg.repo.helper;

import java.util.Locale;

import org.alfresco.repo.i18n.MessageService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

/**
 * <p>MessageHelper class.</p>
 *
 * @author matthieu
 */
@Service
public class MessageHelper implements InitializingBean {

	private static MessageHelper instance = null;
	
	@Autowired
	private MessageService messageService;

	/** {@inheritDoc} */
	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}
	
    /**
     * <p>getMessage.</p>
     *
     * @param messageKey a {@link java.lang.String} object
     * @return a {@link java.lang.String} object
     */
    public static String getMessage(String messageKey) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey);
    	}
    	return instance.messageService.getMessage(messageKey);
    }

    /**
     * <p>getMessage.</p>
     *
     * @param messageKey a {@link java.lang.String} object
     * @param locale a {@link java.util.Locale} object
     * @return a {@link java.lang.String} object
     */
    public static String getMessage(final String messageKey, final Locale locale) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey, locale);
    	}
    	return instance.messageService.getMessage(messageKey, locale);
    }

    /**
     * <p>getMessage.</p>
     *
     * @param messageKey a {@link java.lang.String} object
     * @param params a {@link java.lang.Object} object
     * @return a {@link java.lang.String} object
     */
    public static String getMessage(String messageKey, Object... params) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey, params);
    	}
    	return instance.messageService.getMessage(messageKey, params);
    }

    /**
     * <p>getMessage.</p>
     *
     * @param messageKey a {@link java.lang.String} object
     * @param locale a {@link java.util.Locale} object
     * @param params a {@link java.lang.Object} object
     * @return a {@link java.lang.String} object
     */
    public static String getMessage(String messageKey, Locale locale, Object... params) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey, locale, params);
    	}
    	return instance.messageService.getMessage(messageKey, locale, params);
    }


}

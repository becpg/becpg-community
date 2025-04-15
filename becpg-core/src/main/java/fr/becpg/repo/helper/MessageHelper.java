package fr.becpg.repo.helper;

import java.util.Locale;

import org.alfresco.repo.i18n.MessageService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

@Service
public class MessageHelper implements InitializingBean {

	private static MessageHelper instance = null;
	
	@Autowired
	private MessageService messageService;

	@Override
	public void afterPropertiesSet() throws Exception {
		instance = this;
	}
	
    public static String getMessage(String messageKey) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey);
    	}
    	return instance.messageService.getMessage(messageKey);
    }

    public static String getMessage(final String messageKey, final Locale locale) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey, locale);
    	}
    	return instance.messageService.getMessage(messageKey, locale);
    }

    public static String getMessage(String messageKey, Object... params) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey, params);
    	}
    	return instance.messageService.getMessage(messageKey, params);
    }

    public static String getMessage(String messageKey, Locale locale, Object... params) {
    	if (instance == null || instance.messageService == null) {
    		return I18NUtil.getMessage(messageKey, locale, params);
    	}
    	return instance.messageService.getMessage(messageKey, locale, params);
    }


}

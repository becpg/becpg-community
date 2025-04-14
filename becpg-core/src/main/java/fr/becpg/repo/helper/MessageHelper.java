package fr.becpg.repo.helper;

import java.util.Locale;

import org.alfresco.repo.i18n.MessageService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
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
    	return instance.messageService.getMessage(messageKey);
    }

    public static String getMessage(final String messageKey, final Locale locale) {
    	return instance.messageService.getMessage(messageKey, locale);
    }

    public static String getMessage(String messageKey, Object... params) {
    	return instance.messageService.getMessage(messageKey, params);
    }

    public static String getMessage(String messageKey, Locale locale, Object... params) {
    	return instance.messageService.getMessage(messageKey, locale, params);
    }


}

package fr.becpg.repo.designer.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.config.ConfigService;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Reload share configuration files
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class ReloadConfigWebscript  extends DeclarativeWebScript {
	

    private static final Log logger = LogFactory.getLog(DeclarativeWebScript.class);
    
	
	ConfigService configService;
	
	/**
	 * @param configService the configService to set
	 */
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	
	protected Map<String, Object> executeImpl(WebScriptRequest req,
	         Status status) {
	      Map<String, Object> model = new HashMap<String, Object>();
	      
	      logger.debug("Reload configService");
	      configService.reset();
	      
	      return model;
	   }

}

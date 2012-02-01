package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.cache.BeCPGCacheService;


/**
 * 
 * Allow to reset beCPG caches
 * @author matthieu
 * 
 */
public class CacheServiceWebscript extends AbstractWebScript  {

	private static Log _logger = LogFactory.getLog(CacheServiceWebscript.class);

	private BeCPGCacheService beCPGCacheService;

	


	/**
	 * @param beCPGCacheService the beCPGCacheService to set
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}




	/*
	 * (non-Javadoc)
	 * 
	 * @see org.alfresco.web.scripts.WebScript#execute(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.WebScriptResponse)
	 */
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException{

  		  _logger.info("Delete all cache");
  		  beCPGCacheService.clearAllCaches();
    }
	
	
}

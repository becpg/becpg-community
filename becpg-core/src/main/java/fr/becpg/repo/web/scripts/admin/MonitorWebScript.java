/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.service.cmr.repository.ContentService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 *
 * @author matthieu
 */
public class MonitorWebScript extends DeclarativeWebScript {

	private static final Log logger = LogFactory.getLog(MonitorWebScript.class);
	
	private ContentService contentService;
	
	private AbstractAuthenticationService authenticationService;

	public void setAuthenticationService(AbstractAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}
	
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		logger.debug("start admin webscript");
		
		Map<String, Object> ret = new HashMap<>();

		Set<String> users = new HashSet<>(authenticationService.getUsersWithTickets(true));

		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
		
			
		
		Runtime runtime = Runtime.getRuntime();

		ret.put("diskFreeSpace", contentService.getStoreFreeSpace());
		ret.put("totalMemory", runtime.totalMemory() / 1000000d);
		ret.put("freeMemory", runtime.freeMemory() / 1000000d);
		ret.put("maxMemory", runtime.maxMemory() / 1000000d);
		ret.put("nonHeapMemoryUsage", memoryMXBean.getNonHeapMemoryUsage().getUsed() / 1000000d);
		ret.put("connectedUsers", users.size());
		ret.put("status", "SUCCESS");
		return ret;

	}

}

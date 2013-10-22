/*
 * 
 */
package fr.becpg.repo.web.scripts.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.admin.InitVisitor;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.security.SecurityService;

/**
 * The Class AdminModuleWebScript.
 * 
 * @author querephi
 */
public class AdminModuleWebScript extends DeclarativeWebScript {

	private static Log logger = LogFactory.getLog(AdminModuleWebScript.class);

	private static final String PARAM_ACTION = "action";
	private static final String ACTION_INIT_REPO = "init-repo";
	private static final String ACTION_RELOAD_CACHE = "reload-cache";
	private static final String ACTION_RELOAD_ACL = "reload-acl";
	private static final String ACTION_RELOAD_MODEL = "reload-model";
	private static final String ACTION_GET_SYSTEM_ENTITIES = "system-entities";
	private static final String ACTION_GET_CONNECTED_USERS = "show-users";

	private InitVisitor initRepoVisitor;

	private Repository repository;

	private BeCPGCacheService beCPGCacheService;

	private SecurityService securityService;

	private DictionaryDAO dictionaryDAO;

	private EntitySystemService entitySystemService;
	
	private AbstractAuthenticationService authenticationService;
	
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setInitRepoVisitor(InitVisitor initRepoVisitor) {
		this.initRepoVisitor = initRepoVisitor;
	}

	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	public void setAuthenticationService(AbstractAuthenticationService authenticationService) {
		this.authenticationService = authenticationService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		logger.debug("start admin webscript");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String action = templateArgs.get(PARAM_ACTION);
		Map<String, Object> ret = new HashMap<String, Object>();
		

		// Check arg
		if (action == null || action.isEmpty()) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'action' argument cannot be null or empty");
		}
		
		// #378 : force to use server locale 
		I18NUtil.setLocale(Locale.getDefault());

		if (action.equals(ACTION_INIT_REPO)) {
			logger.debug("Init repository");
			initRepoVisitor.visitContainer(repository.getCompanyHome());
		} else if (action.equals(ACTION_RELOAD_CACHE)) {
			beCPGCacheService.printCacheInfos();
			logger.debug("Delete all cache");
			beCPGCacheService.clearAllCaches();
		} else if (action.equals(ACTION_RELOAD_ACL)) {
			logger.debug("Reload acls");
			securityService.refreshAcls();
		} else if (action.equals(ACTION_RELOAD_MODEL)) {
			logger.debug("Reload models");
			dictionaryDAO.reset();
		} else if (action.equals(ACTION_GET_SYSTEM_ENTITIES)) {
			logger.debug("Get system entities");
			ret.put("systemEntities", entitySystemService.getSystemEntities());
			ret.put("systemFolders", entitySystemService.getSystemFolders());
		} else if (action.equals(ACTION_GET_CONNECTED_USERS)) {
			logger.debug("Get connected users");
			ret.put("users", authenticationService.getUsersWithTickets(true));
		} else {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'action'. action = " + action);
		}

		
		//Add status
		
		ret.put("status", "SUCCESS");
		
		//Add system infos
		
		MemoryMXBean memoryMXBean=ManagementFactory.getMemoryMXBean();
		
		Runtime runtime = Runtime.getRuntime();
		
		ret.put("totalMemory", runtime.totalMemory()/ 1000000d);
		ret.put("freeMemory", runtime.freeMemory()/ 1000000d);
		ret.put("maxMemory", runtime.maxMemory()/ 1000000d);
		ret.put("nonHeapMemoryUsage",memoryMXBean.getNonHeapMemoryUsage().getUsed()/ 1000000d);
		ret.put("connectedUsers", authenticationService.getUsersWithTickets(true).size()-1);
		
		
		
		return ret;

	}
	

}

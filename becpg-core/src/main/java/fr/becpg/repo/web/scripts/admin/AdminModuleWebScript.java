/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.tenant.TenantAdminService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.admin.InitVisitorService;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntitySystemService;
import fr.becpg.repo.security.SecurityService;

/**
 * The Class AdminModuleWebScript.
 *
 * @author querephi
 */
public class AdminModuleWebScript extends DeclarativeWebScript {

	private static final Log logger = LogFactory.getLog(AdminModuleWebScript.class);

	private static final String PARAM_ACTION = "action";
	private static final String ACTION_INIT_REPO = "init-repo";
	private static final String ACTION_RELOAD_CACHE = "reload-cache";
	private static final String ACTION_RELOAD_ACL = "reload-acl";
	private static final String ACTION_RELOAD_MODEL = "reload-model";
	private static final String ACTION_GET_SYSTEM_ENTITIES = "system-entities";
	private static final String ACTION_GET_CONNECTED_USERS = "show-users";

	private InitVisitorService initVisitorService;

	private Repository repository;

	private BeCPGCacheService beCPGCacheService;

	private SecurityService securityService;

	private DictionaryDAO dictionaryDAO;

	private EntitySystemService entitySystemService;

	private AbstractAuthenticationService authenticationService;

	private TenantAdminService tenantAdminService;

	private String becpgSchema;

	public void setBecpgSchema(String becpgSchema) {
		this.becpgSchema = becpgSchema;
	}

	public void setTenantAdminService(TenantAdminService tenantAdminService) {
		this.tenantAdminService = tenantAdminService;
	}

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

	public void setInitVisitorService(InitVisitorService initVisitorService) {
		this.initVisitorService = initVisitorService;
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
		Map<String, Object> ret = new HashMap<>();

		// Check arg
		if ((action == null) || action.isEmpty()) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'action' argument cannot be null or empty");
		}

		Set<String> users = new HashSet<>(authenticationService.getUsersWithTickets(true));
		for (Iterator<String> iterator = users.iterator(); iterator.hasNext();) {
			String user = iterator.next();
			if ("guest".equals(user)) {
				iterator.remove();
			} else if (tenantAdminService.isEnabled() && !tenantAdminService.getCurrentUserDomain().equals(tenantAdminService.getUserDomain(user))) {
				iterator.remove();
			}
		}

		// #378 : force to use server locale
		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try {
			I18NUtil.setLocale(Locale.getDefault());
			I18NUtil.setContentLocale(null);
			switch (action) {
			case ACTION_INIT_REPO:
				logger.debug("Init repository");
				ret.put("sites",initVisitorService.run(repository.getCompanyHome()));
				break;
			case ACTION_RELOAD_CACHE:
				beCPGCacheService.printCacheInfos();
				logger.debug("Delete all cache");
				beCPGCacheService.clearAllCaches();
				break;
			case ACTION_RELOAD_ACL:
				logger.debug("Reload acls");
				securityService.refreshAcls();
				break;
			case ACTION_RELOAD_MODEL:
				logger.debug("Reload models");
				beCPGCacheService.clearCache(DynListConstraint.DYN_LIST_CACHE_NAME);
				dictionaryDAO.reset();
				break;
			case ACTION_GET_SYSTEM_ENTITIES:
				logger.debug("Get system entities");
				ret.put("systemEntities", entitySystemService.getSystemEntities());
				ret.put("systemFolders", entitySystemService.getSystemFolders());
				break;
			case ACTION_GET_CONNECTED_USERS:
				logger.debug("Get connected users");
				ret.put("users", users);
				break;
			default:
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Unsupported argument 'action'. action = " + action);
			}

		} finally {
			I18NUtil.setLocale(currentLocal);
			I18NUtil.setContentLocale(currentContentLocal);
		}

		// Add status

		ret.put("status", "SUCCESS");

		// Add system infos

		MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

		Runtime runtime = Runtime.getRuntime();

		ret.put("totalMemory", runtime.totalMemory() / 1000000d);
		ret.put("freeMemory", runtime.freeMemory() / 1000000d);
		ret.put("maxMemory", runtime.maxMemory() / 1000000d);
		ret.put("nonHeapMemoryUsage", memoryMXBean.getNonHeapMemoryUsage().getUsed() / 1000000d);
		ret.put("connectedUsers", users.size());
		ret.put("becpgSchema", becpgSchema);

		return ret;

	}

}

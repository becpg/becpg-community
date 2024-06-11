/*
 *
 */
package fr.becpg.repo.web.scripts.admin;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.model.Repository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.admin.InitVisitorService;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.dictionary.constraint.DynListConstraint;
import fr.becpg.repo.entity.EntitySystemService;

/**
 * The Class AdminModuleWebScript.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class AdminModuleWebScript extends MonitorWebScript {

	private static final Log logger = LogFactory.getLog(AdminModuleWebScript.class);

	private static final String PARAM_ACTION = "action";
	private static final String ACTION_INIT_REPO = "init-repo";
	private static final String ACTION_RELOAD_CACHE = "reload-cache";
	private static final String ACTION_RELOAD_MODEL = "reload-model";
	private static final String ACTION_GET_SYSTEM_ENTITIES = "system-entities";
	private static final String ACTION_GET_CONNECTED_USERS = "show-users";

	private InitVisitorService initVisitorService;

	private Repository repository;

	private BeCPGCacheService beCPGCacheService;

	private DictionaryDAO dictionaryDAO;

	private EntitySystemService entitySystemService;

	/**
	 * <p>Setter for the field <code>entitySystemService</code>.</p>
	 *
	 * @param entitySystemService a {@link fr.becpg.repo.entity.EntitySystemService} object.
	 */
	public void setEntitySystemService(EntitySystemService entitySystemService) {
		this.entitySystemService = entitySystemService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryDAO</code>.</p>
	 *
	 * @param dictionaryDAO a {@link org.alfresco.repo.dictionary.DictionaryDAO} object.
	 */
	public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}

	/**
	 * <p>Setter for the field <code>beCPGCacheService</code>.</p>
	 *
	 * @param beCPGCacheService a {@link fr.becpg.repo.cache.BeCPGCacheService} object.
	 */
	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	/**
	 * <p>Setter for the field <code>initVisitorService</code>.</p>
	 *
	 * @param initVisitorService a {@link fr.becpg.repo.admin.InitVisitorService} object.
	 */
	public void setInitVisitorService(InitVisitorService initVisitorService) {
		this.initVisitorService = initVisitorService;
	}

	/**
	 * <p>Setter for the field <code>repository</code>.</p>
	 *
	 * @param repository a {@link org.alfresco.repo.model.Repository} object.
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
	}

	/** {@inheritDoc} */
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
		
		Set<String> users = fillMonitoringInformation(ret, false);
		
		// #378 : force to use server locale
		Locale currentLocal = I18NUtil.getLocale();
		Locale currentContentLocal = I18NUtil.getContentLocale();
		try {
			I18NUtil.setLocale(Locale.getDefault());
			I18NUtil.setContentLocale(null);
			switch (action) {
			case ACTION_INIT_REPO:
				logger.debug("Init repository");
				ret.put("sites", initVisitorService.run(repository.getCompanyHome()));
				break;
			case ACTION_RELOAD_CACHE:
				beCPGCacheService.printCacheInfos();
				logger.debug("Delete all cache");
				beCPGCacheService.clearAllCaches();
				break;
			case ACTION_RELOAD_MODEL:
				logger.debug("Reload models");
				beCPGCacheService.clearCache(DynListConstraint.class.getName());
				//TODO Cluster cache ?
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

		ret.put("status", "SUCCESS");
		
		return ret;

	}

}

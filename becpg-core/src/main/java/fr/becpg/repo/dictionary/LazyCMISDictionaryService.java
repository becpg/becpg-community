package fr.becpg.repo.dictionary;

import org.alfresco.opencmis.dictionary.CMISStrictDictionaryService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationEvent;

/**
 * CMIS dictionary built on first use instead of at startup.
 * <p>
 * beCPG no longer queries through CMIS, but the Search subsystem still wires this service. Alfresco builds the CMIS
 * definitions of every model at each startup and again after each dictionary reload, about two seconds each time, for a
 * registry that is seldom read. The registry is now built by {@code getRegistry} the first time it is asked for.
 */
public class LazyCMISDictionaryService extends CMISStrictDictionaryService {

	private static final Log logger = LogFactory.getLog(LazyCMISDictionaryService.class);

	/**
	 * Listens to dictionary changes without building the registry.
	 *
	 * @param event the bootstrap event
	 */
	@Override
	protected void onBootstrap(ApplicationEvent event) {
		dictionaryDAO.registerListener(this);
	}

	/**
	 * Drops the registry of the current tenant, so that the next use rebuilds it from the reloaded dictionary.
	 */
	@Override
	public void afterDictionaryInit() {
		if (logger.isDebugEnabled()) {
			logger.debug("Dictionary reloaded, CMIS registry dropped until next use");
		}
		afterDictionaryDestroy();
	}
}

/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.formulation.FormulateException;

/**
 * @author querephi
 */
public class EntityTplWebScript extends AbstractWebScript {

	private static Log logger = LogFactory.getLog(EntityTplWebScript.class);

	private static final String ACTION_SYNCHRONIZE_ENTITIES = "synchronizeEntities";
	private static final String ACTION_FORMULATION_ENTITIES = "formulateEntities";
	
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private NodeService nodeService;
	
	private EntityTplService entityTplService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityTplService(EntityTplService entityTplService) {
		this.entityTplService = entityTplService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException {
		logger.debug("start entityTpl webscript");
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String action = templateArgs.get(PARAM_ACTION);

		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

		if (nodeService.exists(nodeRef)) {
			if (ACTION_SYNCHRONIZE_ENTITIES.equals(action)) {
				entityTplService.synchronizeEntities(nodeRef);
			} else if (ACTION_FORMULATION_ENTITIES.equals(action)) {
				try {
					entityTplService.formulateEntities(nodeRef);
				} catch (FormulateException e) {
					logger.error(e,e);
					throw new WebScriptException(e.getMessage());
				}
			} else {
				String error = "Unsupported action: " + action;
				logger.error(error);
				throw new WebScriptException(error);
			}
		}
	}
}

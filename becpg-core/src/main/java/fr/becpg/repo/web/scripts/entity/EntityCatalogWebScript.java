package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.JsonScoreHelper;

/**
 * Gathers product's missing fields info : which ones are missing, and what is
 * the score related to it.
 *
 * Requires PLM Module
 *
 * @author steven
 *
 */
public class EntityCatalogWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(EntityCatalogWebScript.class);

	private NodeService nodeService;

	private FormulationService<FormulatedEntity> formulationService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setFormulationService(FormulationService<FormulatedEntity> formulationService) {
		this.formulationService = formulationService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get("store_type");
		String storeId = templateArgs.get("store_id");
		String nodeId = templateArgs.get("id");

		NodeRef productNodeRef = new NodeRef(storeType, storeId, nodeId);

		if (!nodeService.exists(productNodeRef)) {
			throw new WebScriptException("Node " + productNodeRef + " does not exist");
		}

		try {

			if (formulationService.shouldFormulate(productNodeRef)) {
				formulationService.formulate(productNodeRef);
			}

			String scores = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ENTITY_SCORE);

			if ((scores != null) && !scores.isEmpty()) {

				JSONObject jsonObject = new JSONObject(scores);

				if (jsonObject.has(JsonScoreHelper.PROP_CATALOGS)) {

					res.setContentType("application/json");
					res.setContentEncoding("UTF-8");
					jsonObject.getJSONArray(JsonScoreHelper.PROP_CATALOGS).write(res.getWriter());
				}
			}

		} catch (JSONException e) {
			logger.error(e, e);
			throw new WebScriptException("Unable to serialize JSON", e);
		} catch (FormulateException e) {
			logger.error(e, e);
			throw new WebScriptException("Cannot formulate product", e);
		}

	}

}

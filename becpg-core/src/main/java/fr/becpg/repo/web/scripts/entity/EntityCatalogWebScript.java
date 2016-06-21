package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.JsonScoreHelper;
import fr.becpg.repo.repository.L2CacheSupport;

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

	private BehaviourFilter policyBehaviourFilter;

	private PermissionService permissionService;

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

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

			if (formulationService.shouldFormulate(productNodeRef)
					&& (permissionService.hasPermission(productNodeRef, PermissionService.WRITE) == AccessStatus.ALLOWED)) {

				try {
					policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

					L2CacheSupport.doInCacheContext(() -> {
						AuthenticationUtil.runAsSystem(() -> {
							formulationService.formulate(productNodeRef);
							return true;
						});

					}, false, true);

				} finally {
					policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				}

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
		} catch (Exception e) {
			logger.error(e, e);
			throw new WebScriptException("Cannot formulate product", e);
		}

	}

}

package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.rule.RuleService;
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
import fr.becpg.repo.entity.catalog.EntityCatalogService;
import fr.becpg.repo.entity.version.VersionHelper;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.repository.L2CacheSupport;

/**
 * Gathers product's missing fields info : which ones are missing, and what is
 * the score related to it.
 *
 * Requires PLM Module
 *
 * @author steven
 * @version $Id: $Id
 */
public class EntityCatalogWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(EntityCatalogWebScript.class);

	private NodeService nodeService;

	private FormulationService<FormulatedEntity> formulationService;

	private BehaviourFilter policyBehaviourFilter;

	private RuleService ruleService;

	private EntityCatalogService entityCatalogService;

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>formulationService</code>.</p>
	 *
	 * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object.
	 */
	public void setFormulationService(FormulationService<FormulatedEntity> formulationService) {
		this.formulationService = formulationService;
	}

	/**
	 * <p>Setter for the field <code>entityCatalogService</code>.</p>
	 *
	 * @param entityCatalogService a {@link fr.becpg.repo.entity.catalog.EntityCatalogService} object.
	 */
	public void setEntityCatalogService(EntityCatalogService entityCatalogService) {
		this.entityCatalogService = entityCatalogService;
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
			
			Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
			String storeType = templateArgs.get("store_type");
			String storeId = templateArgs.get("store_id");
			String nodeId = templateArgs.get("id");
			String catalogId = req.getParameter("catalogId");
			
			NodeRef productNodeRef = new NodeRef(storeType, storeId, nodeId);
			
			if (!nodeService.exists(productNodeRef)) {
				throw new WebScriptException("Node " + productNodeRef + " does not exist");
			}
			
			try {
				JSONObject jsonObject = new JSONObject();
				
				if (!VersionHelper.isVersion(productNodeRef)) {
					if (catalogId == null) {
						
						boolean formulated = false;
						if (formulationService.shouldFormulate(productNodeRef)) {
							
							try {
								policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
								ruleService.disableRules();
								
								L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> {
									formulationService.formulate(productNodeRef, FormulationService.FAST_FORMULATION_CHAINID);
									return true;
								}), false, true);
								
								formulated = true;
								
							} finally {
								ruleService.enableRules();
								policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
								policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
								policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
							}
							
						}
						
						String scores = (String) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_ENTITY_SCORE);
						
						if ((scores != null) && !scores.isEmpty()) {
							jsonObject = new JSONObject(scores);
						}
						
						jsonObject.put("formulated", formulated);
					} else {
						jsonObject = new JSONObject();
						
						jsonObject.put(EntityCatalogService.PROP_CATALOGS, entityCatalogService.formulateCatalog(catalogId, productNodeRef,
								(List<String>) nodeService.getProperty(productNodeRef, ReportModel.PROP_REPORT_LOCALES)));
					}
				}
				
				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				jsonObject.write(res.getWriter());
				
			} catch (JSONException e) {
				logger.error(e, e);
				throw new WebScriptException("Unable to serialize JSON", e);
			}

	}

}

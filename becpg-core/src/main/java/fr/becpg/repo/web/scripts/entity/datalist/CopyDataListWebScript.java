/*
 *
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.entity.EntityListDAO;

/**
 * Webscript that copy a datalist to another entity
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CopyDataListWebScript extends AbstractWebScript {

	/** Constant <code>PARAM_STORE_TYPE="store_type"</code> */
	protected static final String PARAM_STORE_TYPE = "store_type";
	/** Constant <code>PARAM_STORE_ID="store_id"</code> */
	protected static final String PARAM_STORE_ID = "store_id";
	/** Constant <code>PARAM_ID="id"</code> */
	protected static final String PARAM_ID = "id";

	private EntityListDAO entityListDAO;
	
	private EntityActivityService entityActivityService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	/**
	 * <p>Setter for the field <code>entityActivityService</code>.</p>
	 *
	 * @param entityActivityService a {@link fr.becpg.repo.activity.EntityActivityService} object
	 */
	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
	}
	
	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {

		try {

			JSONArray ret = new JSONArray();
			
			boolean isImport = "from".equals(req.getParameter("destination"));
			
			Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
			if (templateArgs != null) {
				String storeType = templateArgs.get(PARAM_STORE_TYPE);
				String storeId = templateArgs.get(PARAM_STORE_ID);
				String nodeId = templateArgs.get(PARAM_ID);
				if ((storeType != null) && (storeId != null) && (nodeId != null)) {
					NodeRef dataListNodeRef = new NodeRef(storeType, storeId, nodeId);
					NodeRef entityNodeRef = null;


					JSONObject json = (JSONObject) req.parseContent();
					String action = "Override";
					
					if ((json != null) && json.has("action")) {
						action = (String) json.get("action");
					}
					
					if ((json != null) && json.has("entity")) {
						entityNodeRef = new NodeRef((String) json.get("entity"));
						
						NodeRef targetEntityNodeRef = null;
						NodeRef sourceListNodeRef = null;
						
						if(isImport) {
							targetEntityNodeRef =  entityListDAO.getEntityFromList(dataListNodeRef);
							 NodeRef sourceListContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);
					         sourceListNodeRef = entityListDAO.findMatchingList(dataListNodeRef,sourceListContainerNodeRef);
						} else {
							sourceListNodeRef = dataListNodeRef;
							targetEntityNodeRef = entityNodeRef;
						}
						
						entityActivityService.postDataListCopyActivity(targetEntityNodeRef, entityNodeRef, sourceListNodeRef, action);
						
						policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
						
						if("Add".equals(action)) {
							entityListDAO.mergeDataList(sourceListNodeRef, targetEntityNodeRef, true);
						} else if ("Merge".equals(action)) {
							entityListDAO.mergeDataList(sourceListNodeRef, targetEntityNodeRef, false);
						} else {
							entityListDAO.copyDataList(sourceListNodeRef, targetEntityNodeRef, true);
						}

						ret.put(entityNodeRef);

					}

				}

			}

			resp.setContentType("application/json");
			resp.setContentEncoding("UTF-8");
			resp.getWriter().write(ret.toString(3));
		} catch (JSONException e) {
			throw new WebScriptException(e.getMessage());
		}

	}
}

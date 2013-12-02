package fr.becpg.repo.web.scripts.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.CopyService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;

/**
 * 
 * @author matthieu
 *
 */
public class SimulationWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PARAM_DATALISTITEMS = "dataListItems";

	private AssociationService associationService;

	private EntityListDAO entityListDAO;

	private NodeService nodeService;

	private CopyService copyService;
	
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setCopyService(CopyService copyService) {
		this.copyService = copyService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		String dataListItems = req.getParameter(PARAM_DATALISTITEMS);

		List<NodeRef> dataListItemsNodeRefs = new ArrayList<>();
		if (dataListItems != null && !dataListItems.isEmpty()) {
			for (String dataListItem : dataListItems.split(",")) {
				dataListItemsNodeRefs.add(new NodeRef(dataListItem));
			}
		}

		NodeRef simulationNodeRef = null;

		NodeRef entityNodeRef = null;
		if (entityNodeRefParam != null && !entityNodeRefParam.isEmpty()) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		}

		if (!dataListItemsNodeRefs.isEmpty()) {
			for (NodeRef dataListItem : dataListItemsNodeRefs) {
				
				simulationNodeRef = createSimulationNodeRef(associationService.getTargetAssoc(dataListItem, BeCPGModel.ASSOC_COMPOLIST_PRODUCT), nodeService.getPrimaryParent(entityListDAO.getEntity(dataListItem)).getParentRef());
				associationService.update(dataListItem, BeCPGModel.ASSOC_COMPOLIST_PRODUCT, simulationNodeRef);

			}
		} else if (entityNodeRef != null) {
			simulationNodeRef = createSimulationNodeRef(entityNodeRef, nodeService.getPrimaryParent(entityNodeRef).getParentRef());
		}

		try {
			JSONObject ret = new JSONObject();

			if (simulationNodeRef != null) {
				ret.put("persistedObject", simulationNodeRef);
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		}  catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} 

	}

	private NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef parentRef) {
		NodeRef simulationNodeRef = copyService.copyAndRename(entityNodeRef, parentRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);
		nodeService.setProperty(simulationNodeRef, BeCPGModel.PROP_PRODUCT_STATE, SystemState.Simulation);
		return simulationNodeRef;
	}

}

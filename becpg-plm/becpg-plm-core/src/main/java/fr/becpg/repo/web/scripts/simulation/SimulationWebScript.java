/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.simulation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.RepoService;

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

	private EntityService entityService;
	
	private RepoService repoService;
	
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setRepoService(RepoService repoService) {
		this.repoService = repoService;
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
				simulationNodeRef = createSimulationNodeRef(associationService.getTargetAssoc(dataListItem, PLMModel.ASSOC_COMPOLIST_PRODUCT), nodeService.getPrimaryParent(entityListDAO.getEntity(dataListItem)).getParentRef());
				associationService.update(dataListItem, PLMModel.ASSOC_COMPOLIST_PRODUCT, simulationNodeRef);
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
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} 

	}

	private NodeRef createSimulationNodeRef(NodeRef entityNodeRef, NodeRef parentRef) {
		String newEntityName = repoService.getAvailableName(parentRef, (String)nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
		NodeRef simulationNodeRef = entityService.createOrCopyFrom(entityNodeRef, parentRef, nodeService.getType(entityNodeRef), newEntityName);
		nodeService.setProperty(simulationNodeRef, PLMModel.PROP_PRODUCT_STATE, SystemState.Simulation);
		if (nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_VERSIONABLE)) {
			nodeService.setProperty(simulationNodeRef,BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL, nodeService.getProperty(entityNodeRef, ContentModel.PROP_VERSION_LABEL));
		} else {
			nodeService.setProperty(simulationNodeRef, BeCPGModel.PROP_BRANCH_FROM_VERSION_LABEL ,   RepoConsts.INITIAL_VERSION);
		}
		
		nodeService.setAssociations(simulationNodeRef, BeCPGModel.ASSOC_BRANCH_FROM_ENTITY, Arrays.asList(entityNodeRef));
		return simulationNodeRef;
	}

}

/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.simulation.SimulationService;

/**
 *
 * @author matthieu
 *
 */
public class SimulationWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PARAM_DATALISTITEMS = "dataListItems";

	private NodeService nodeService;
	
	private SimulationService simulationService;


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	
	public void setSimulationService(SimulationService simulationService) {
		this.simulationService = simulationService;
	}


	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		String dataListItems = req.getParameter(PARAM_DATALISTITEMS);

		List<NodeRef> dataListItemsNodeRefs = new ArrayList<>();
		if ((dataListItems != null) && !dataListItems.isEmpty()) {
			for (String dataListItem : dataListItems.split(",")) {
				dataListItemsNodeRefs.add(new NodeRef(dataListItem));
			}
		}

		NodeRef simulationNodeRef = null;

		NodeRef entityNodeRef = null;
		if ((entityNodeRefParam != null) && !entityNodeRefParam.isEmpty()) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		}

		if (!dataListItemsNodeRefs.isEmpty()) {

			simulationService.recurSimule(entityNodeRef, null, dataListItemsNodeRefs);

		} else if (entityNodeRef != null) {
			simulationNodeRef = simulationService.createSimulationNodeRef(entityNodeRef, nodeService.getPrimaryParent(entityNodeRef).getParentRef());
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


}

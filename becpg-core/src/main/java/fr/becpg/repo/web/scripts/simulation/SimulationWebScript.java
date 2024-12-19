/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
import java.util.Optional;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.simulation.EntitySimulationService;
import fr.becpg.repo.search.PaginatedSearchCache;

/**
 * <p>SimulationWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SimulationWebScript extends AbstractWebScript {

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private static final String PARAM_DATALISTITEMS = "dataListItems";

	private static final String PARAM_ALLPAGES = "allPages";

	private static final String PARAM_QUERY_EXECUTION_ID = "queryExecutionId";

	private static final String PARAM_NODEREFS = "nodeRefs";

	private static final String PARAM_MODE = "mode";

	private static final String PARAM_DEST_NODEREF = "destNodeRef";

	private static final String PARAM_BRANCH = "branch";

	private NodeService nodeService;
	
	private EntitySimulationService simulationService;
	

	private PaginatedSearchCache paginatedSearchCache;


	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	
	/**
	 * <p>Setter for the field <code>simulationService</code>.</p>
	 *
	 * @param simulationService a {@link fr.becpg.repo.entity.simulation.EntitySimulationService} object.
	 */
	public void setSimulationService(EntitySimulationService simulationService) {
		this.simulationService = simulationService;
	}

	
	

	/**
	 * <p>Setter for the field <code>paginatedSearchCache</code>.</p>
	 *
	 * @param paginatedSearchCache a {@link fr.becpg.repo.search.PaginatedSearchCache} object.
	 */
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}


	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		String destNodeRefParam = req.getParameter(PARAM_DEST_NODEREF);
		
		String dataListItems = req.getParameter(PARAM_DATALISTITEMS);
		String allPagesParam = req.getParameter(PARAM_ALLPAGES);
		String queryExecutionId  = req.getParameter(PARAM_QUERY_EXECUTION_ID);
		String nodeRefsParam = req.getParameter(PARAM_NODEREFS);
		String mode  = req.getParameter(PARAM_MODE);
		
		final boolean branch = Optional.ofNullable(req.getParameter(PARAM_BRANCH)).map(Boolean::valueOf)
				.orElse(true);
		
		
		List<NodeRef> nodeRefs = new ArrayList<>();
		
		if(allPagesParam!=null && "true".equalsIgnoreCase(allPagesParam) && queryExecutionId!=null){
			nodeRefs = paginatedSearchCache.getSearchResults(queryExecutionId);
    	} else if(nodeRefsParam!=null&& !nodeRefsParam.isEmpty()) {
			for (String nodeRefItem : nodeRefsParam.split(",")) {
				nodeRefs.add(new NodeRef(nodeRefItem));
			}
		}
		
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
		
		NodeRef destNodeRef = null;
		if ((destNodeRefParam != null) && !destNodeRefParam.isEmpty()) {
			destNodeRef = new NodeRef(destNodeRefParam);
		}
		
		if(destNodeRef == null && entityNodeRef!=null) {
			destNodeRef = nodeService.getPrimaryParent(entityNodeRef).getParentRef();
		}

		if(!nodeRefs.isEmpty()) {
			
			if(destNodeRef==null) {
				throw new IllegalStateException("destNodeRef is Mandatory");
			}
			
			if(mode==null) {
				throw new IllegalStateException("mode is Mandatory");
			}
			
			simulationNodeRef = simulationService.createSimulationNodeRefs(nodeRefs,destNodeRef, mode);
		} else if (!dataListItemsNodeRefs.isEmpty()) {
			simulationService.simuleDataListItems(entityNodeRef, dataListItemsNodeRefs, branch);

		} else if (entityNodeRef != null) {
			
			if(destNodeRef==null) {
				throw new IllegalStateException("destNodeRef is Mandatory");
			}
			
			simulationNodeRef = simulationService.createSimulationNodeRef(entityNodeRef,destNodeRef );
		} else {
			throw new IllegalStateException("Parameters are incorrects");
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

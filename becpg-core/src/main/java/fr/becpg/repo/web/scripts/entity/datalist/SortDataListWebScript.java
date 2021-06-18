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
package fr.becpg.repo.web.scripts.entity.datalist;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.datalist.DataListSortService;

/**
 * <p>SortDataListWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class SortDataListWebScript extends DeclarativeWebScript {

	private static final Log logger = LogFactory.getLog(SortDataListWebScript.class);

	private static final String PARAM_STORE_TYPE = "store_type";

	private static final String PARAM_STORE_ID = "store_id";

	/** Constant <code>PARAM_ID="id"</code> */
	protected static final String PARAM_ID = "id";

	private static final String PARAM_SELECTED_NODEREFS = "selectedNodeRefs";

	private static final String PARAM_DIR = "dir";
	
	private static final String SORT_DIR_UP = "up";

	private DataListSortService dataListSortService;

	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>dataListSortService</code>.</p>
	 *
	 * @param dataListSortService a {@link fr.becpg.repo.entity.datalist.DataListSortService} object.
	 */
	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		logger.debug("call Sort webscript");

		Map<String, Object> model = new HashMap<>();

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		String selectedNodeRefsArgs = req.getParameter(PARAM_SELECTED_NODEREFS);
		String dir = req.getParameter(PARAM_DIR);

		if (storeType != null && storeId != null && nodeId != null && selectedNodeRefsArgs != null) {
			NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);

			String[] selectedNodeRefs = selectedNodeRefsArgs.split(",");


				if (dir != null || selectedNodeRefs.length > 1) {
					
					if (!SORT_DIR_UP.equals(dir)) {
						ArrayUtils.reverse(selectedNodeRefs);
					}

					for (String selectedNodeRef : selectedNodeRefs) {
						dataListSortService.move(new NodeRef(selectedNodeRef), SORT_DIR_UP.equals(dir));
					}

				} else if (selectedNodeRefs.length == 1) {
					NodeRef toSortNodeRef = new NodeRef(selectedNodeRefs[0]);
					
					model.put("origSort", nodeService.getProperty(toSortNodeRef, BeCPGModel.PROP_SORT));
					dataListSortService.insertAfter(toSortNodeRef, nodeRef);
					model.put("destSort", nodeService.getProperty(toSortNodeRef, BeCPGModel.PROP_SORT));
				}

				return model;
			
		}
		throw new WebScriptException("Invalid argument ");
	}

}

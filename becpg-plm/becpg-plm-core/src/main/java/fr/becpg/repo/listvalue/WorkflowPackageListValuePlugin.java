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
package fr.becpg.repo.listvalue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.listvalue.impl.AbstractBaseListValuePlugin;
import fr.becpg.repo.listvalue.impl.NodeRefListValueExtractor;

public class WorkflowPackageListValuePlugin extends AbstractBaseListValuePlugin {

	private static final String SOURCE_TYPE_WF_PACKAGE = "workflow";

	private NodeService nodeService;

	private WorkflowService workflowService;

	private NamespaceService namespaceService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public String[] getHandleSourceTypes() {
		return new String[] { SOURCE_TYPE_WF_PACKAGE };
	}

	public ListValuePage suggest(String sourceType, String query, Integer pageNum, Integer pageSize, Map<String, Serializable> props) {

		String className = (String) props.get(ListValueService.PROP_CLASS_NAME);
	
		List<NodeRef> ret = new ArrayList<>();
		
		@SuppressWarnings("unchecked")
		Map<String, String> extras = (HashMap<String, String>) props.get(ListValueService.EXTRA_PARAM);
		if (extras != null) {
			if (extras.get("taskId") != null && extras.get("taskId").length()>0) {
				String taskId = (String)  extras.get("taskId");
				
				  ret = workflowService.getPackageContents(taskId);
			
					if (className != null) {
						QName type = QName.createQName(className, namespaceService);

						for (Iterator<NodeRef> iterator = ret.iterator(); iterator.hasNext();) {
							NodeRef nodeRef = (NodeRef) iterator.next();
							if (!type.equals(nodeService.getType(nodeRef))) {
								iterator.remove();
							}
						}
					}
			}
		}
		
		
		return new ListValuePage(ret, pageNum, pageSize, new NodeRefListValueExtractor(ContentModel.PROP_NAME, nodeService));
	}

}

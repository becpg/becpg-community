/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
package fr.becpg.repo.project.extractor;

import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.AttributeExtractorService.AttributeExtractorMode;

public class TaskListExtractorHelper {

	 
	public static void extractTaskListResources(NodeRef nodeRef, AttributeExtractorMode mode, QName itemType, Map<String, Object> ret, NodeService nodeService){
		if(ProjectModel.TYPE_TASK_LIST.equals(itemType)){
			if(mode.equals(AttributeExtractorMode.CSV) || mode.equals(AttributeExtractorMode.XLSX)){
				String resources = "";
				for(AssociationRef assocRef : nodeService.getTargetAssocs(nodeRef, ProjectModel.ASSOC_TL_RESOURCES)){
					QName displayedQname = ContentModel.PROP_USERNAME;
					NodeRef resourceRef = assocRef.getTargetRef();
					if(!resources.isEmpty()){
						resources += ",";
					}
					if(nodeService.getType(resourceRef).equals(ContentModel.TYPE_AUTHORITY_CONTAINER)){
						if(!nodeService.getChildAssocs(resourceRef).isEmpty()){
							resourceRef = nodeService.getChildAssocs(resourceRef).get(0).getChildRef();
							displayedQname = ContentModel.PROP_USERNAME;
						} else {
							displayedQname = ContentModel.PROP_AUTHORITY_DISPLAY_NAME;
						}
					}
					resources += (String) nodeService.getProperty(resourceRef, displayedQname);
				}
				ret.put("assoc_pjt_tlResources", resources);
				
			} else{
				List<Map<String,Object>> resources = (List<Map<String,Object>>) ret.get("assoc_pjt_tlResources");
				
				if(resources == null || resources.isEmpty()){
					return;
				}
				
				for(Map<String, Object>resource : resources){
					NodeRef resourceRef = new NodeRef((String) resource.get("value"));
					QName type = nodeService.getType(resourceRef);
					
					if (type.equals(ContentModel.TYPE_AUTHORITY_CONTAINER) && !nodeService.getChildAssocs(resourceRef).isEmpty()) {
						NodeRef personRef = nodeService.getChildAssocs(resourceRef).get(0).getChildRef();
						resource.put("value", personRef.toString());
						resource.put("metadata", (String) nodeService.getProperty(personRef, ContentModel.PROP_USERNAME));
						resource.put("displayValue", (String) nodeService.getProperty(personRef, ContentModel.PROP_USERNAME));
					}
				}
			}
		}
	}

	
	
}

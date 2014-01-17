/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

/**
 * Used to extract properties from nodeRef
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class NodeRefListValueExtractor implements ListValueExtractor<NodeRef> {

	private QName propName;
	
	private NodeService nodeService;
	
	

	public NodeRefListValueExtractor(QName propName,NodeService nodeService) {
		super();
		this.propName = propName;
		this.nodeService = nodeService;
	}


	@Override
	public List<ListValueEntry> extract(List<NodeRef> nodeRefs) {
		List<ListValueEntry> suggestions = new ArrayList<ListValueEntry>();
    	if(nodeRefs!=null){
    		for(NodeRef nodeRef : nodeRefs){
    			
    			String name = (String)nodeService.getProperty(nodeRef, propName);
    			suggestions.add(new ListValueEntry(nodeRef.toString(),name,nodeService.getType(nodeRef).getLocalName()));
    			
    		}
    	}
		return suggestions;
	}
	
}

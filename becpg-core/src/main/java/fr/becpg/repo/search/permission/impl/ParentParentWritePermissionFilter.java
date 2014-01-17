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
package fr.becpg.repo.search.permission.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.search.permission.BeCPGPermissionFilter;

/**
 * 
 * @author matthieu
 *
 */
public class ParentParentWritePermissionFilter implements BeCPGPermissionFilter{
	
	private NodeService nodeService;
	private static Log logger = LogFactory.getLog(ParentParentWritePermissionFilter.class);
	private HashMap<String, AccessStatus> cache = new HashMap<String, AccessStatus>();
	
	public ParentParentWritePermissionFilter(NodeService nodeService) {
		super();
		this.nodeService = nodeService;
	}

	public List<NodeRef> filter(List<NodeRef> toFilter,
			PermissionService permissionService) {
		
		return filter(toFilter, permissionService, -1);
		
	}

	public List<NodeRef> filter(List<NodeRef> toFilter,
			PermissionService permissionService, int searchLimit) {
		List<NodeRef>  ret =  new LinkedList<NodeRef>(); 
		
		if(toFilter!=null && !toFilter.isEmpty()){
			for (Iterator<NodeRef> iterator = toFilter.iterator(); iterator.hasNext();) {
				NodeRef nodeRef = (NodeRef) iterator.next();
				try {
				//&& child.isDocument && child.parent.parent.hasPermission("Write") && (child.properties["siged:statut"]==0 || q != "")
				  // Parent direct (RCE) 
				   AccessStatus as = AccessStatus.DENIED;
				  ChildAssociationRef rce =    nodeService.getPrimaryParent(nodeRef);
			      NodeRef rceref = rce.getParentRef();
			      if(!cache.containsKey(rceref.getId())){

				      // Parent du RCE
				      ChildAssociationRef parent =  nodeService.getPrimaryParent(rceref);
				      NodeRef parentref = parent.getParentRef();
				      
				       as = permissionService.hasPermission(parentref, PermissionService.WRITE);
				       cache.put(rceref.getId(),as);
			      } else {
			    	  as = cache.get(rceref.getId());
			      }
			      if (as == AccessStatus.ALLOWED) {
					ret.add(nodeRef);
					if(searchLimit!=-1 && searchLimit==ret.size()){
						return ret;
					}
		    	}
				}catch (Exception e) {
					logger.error(e,e);
				}
			}
		}
 
		return ret;
	}



}

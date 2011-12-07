package fr.becpg.repo.search.permission.impl;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;

import fr.becpg.repo.search.permission.BeCPGPermissionFilter;

public class ReadPermissionFilter implements BeCPGPermissionFilter{
	
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
				if(permissionService.hasPermission(nodeRef,PermissionService.READ) == AccessStatus.ALLOWED){
					ret.add(nodeRef);
					if(searchLimit!=-1 && searchLimit==ret.size()){
						return ret;
					}
		    	}
			}
		}
 
		return ret;
	}


}

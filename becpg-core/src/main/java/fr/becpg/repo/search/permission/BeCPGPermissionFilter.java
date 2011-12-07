package fr.becpg.repo.search.permission;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PermissionService;

/**
 * 
 * @author matthieu
 *
 */
public interface BeCPGPermissionFilter {

	List<NodeRef> filter(List<NodeRef> toFilter, PermissionService permissionService);

	List<NodeRef> filter(List<NodeRef> toFilter,
			PermissionService permissionService, int searchLimit);


}

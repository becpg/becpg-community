/*
 * 
 */
package fr.becpg.repo.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The Interface RepoService.
 *
 * @author querephi
 */
public interface RepoService {
	
	/**
	 * Creates the folder by paths.
	 */
	NodeRef getOrCreateFolderByPaths(NodeRef parentNodeRef, List<String> paths);
	
	/**
	 * Creates the folder by path.
	 */
	NodeRef getOrCreateFolderByPath(NodeRef parentNodeRef, String path, String name);
		
	/**
	 * Get the folder by path
	 */
	NodeRef getFolderByPath(NodeRef parentNodeRef, String path);
	
	/**
	 * Get the folder by path
	 * start from companyHome
	 */
	NodeRef getFolderByPath(String path);
	
	
	/**
	 * Move the node in the destination folder, rename the node with (1) if a node with same name already exists
	 */
	void moveNode(NodeRef nodeRefToMove, NodeRef destionationNodeRef);
	
	/**
	 * Calculate the name in order to get an available name ie: name (1)
	 */
	String getAvailableName(NodeRef folderNodeRef, String name, boolean forceRename);
	
}

/*
 * 
 */
package fr.becpg.repo.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

// TODO: Auto-generated Javadoc
/**
 * The Interface RepoService.
 *
 * @author querephi
 */
public interface RepoService {
	
	/**
	 * Creates the folder by paths.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param paths the paths
	 * @return the node ref
	 */
	NodeRef getOrCreateFolderByPaths(NodeRef parentNodeRef, List<String> paths);
	
	/**
	 * Creates the folder by path.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param path the path
	 * @param name the name
	 * @return the node ref
	 */
	NodeRef getOrCreateFolderByPath(NodeRef parentNodeRef, String path, String name);
		
	/**
	 * Get the folder by path
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	NodeRef getFolderByPath(NodeRef parentNodeRef, String path);
	
	/**
	 * Get the folder by path
	 * start from companyHome
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	NodeRef getFolderByPath(String path);
	
	
	/**
	 * Move the node in the destination folder, rename the node with (1) if a node with same name already exists
	 * @param nodeRefToMove
	 * @param destionationNodeRef
	 * @param name
	 */
	void moveNode(NodeRef nodeRefToMove, NodeRef destionationNodeRef);
	
	/**
	 * Calculate the name in order to get an available name ie: name (1)
	 * @param nodeRefToMove
	 * @param destionationNodeRef
	 * @param name
	 */
	String getAvailableName(NodeRef folderNodeRef, String name);
	
}

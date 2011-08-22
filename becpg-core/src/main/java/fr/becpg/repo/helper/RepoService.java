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
	public NodeRef createFolderByPaths(NodeRef parentNodeRef, List<String> paths);
	
	/**
	 * Creates the folder by path.
	 *
	 * @param parentNodeRef the parent node ref
	 * @param path the path
	 * @param name the name
	 * @return the node ref
	 */
	public NodeRef createFolderByPath(NodeRef parentNodeRef, String path, String name);
		
	/**
	 * Get the folder by path
	 * @param parentNodeRef
	 * @param path
	 * @return
	 */
	public NodeRef getFolderByPath(NodeRef parentNodeRef, String path);
	
}

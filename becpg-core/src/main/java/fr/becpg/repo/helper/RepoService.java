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
 * @version $Id: $Id
 */
public interface RepoService {
	
	/**
	 * Creates the folder by paths.
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param paths a {@link java.util.List} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOrCreateFolderByPaths(NodeRef parentNodeRef, List<String> paths);
	
	/**
	 * Creates the folder by path.
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param path a {@link java.lang.String} object.
	 * @param name a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOrCreateFolderByPath(NodeRef parentNodeRef, String path, String name);
		
	/**
	 * Get the folder by path
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getFolderByPath(NodeRef parentNodeRef, String path);
	
	/**
	 * Get the folder by path
	 * start from companyHome
	 *
	 * @param path a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getFolderByPath(String path);
	
	
	/**
	 * Move the node in the destination folder, rename the node with (1) if a node with same name already exists
	 *
	 * @param nodeRefToMove a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param destionationNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean
	 */
	boolean moveNode(NodeRef nodeRefToMove, NodeRef destionationNodeRef);
	
	/**
	 * Calculate the name in order to get an available name ie: name (1)
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @param forceRename a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	String getAvailableName(NodeRef folderNodeRef, String name, boolean forceRename);

	/**
	 * {@inheritDoc}
	 *
	 * @param folderNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param name a {@link java.lang.String} object
	 * @param forceRename a boolean
	 * @param keepExtension a boolean
	 * @return a {@link java.lang.String} object
	 */
	String getAvailableName(NodeRef folderNodeRef, String name, boolean forceRename, boolean keepExtension);
	
	/**
	 * <p>moveEntity.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param destinationFolder a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	boolean moveEntity(NodeRef entityNodeRef, NodeRef destinationFolder);
	
}

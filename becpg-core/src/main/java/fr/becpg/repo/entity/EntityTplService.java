package fr.becpg.repo.entity;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

@Service
public interface EntityTplService {

	/**
	 * Create the entity folderTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	public NodeRef createFolderTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<String> subFolders);
	
	/**
	 * Create the entityTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<QName> entityLists);
	
	/**
	 * Look for the entity folderTpl
	 * @param isContainer
	 * @param nodeType
	 * @return
	 */
	public NodeRef getFolderTpl(QName nodeType);
	
	/**
	 * Look for the entityTpl
	 * @param isContainer
	 * @param nodeType
	 * @return
	 */
	public NodeRef getEntityTpl(QName nodeType);
}

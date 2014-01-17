package fr.becpg.repo.entity;

import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.common.BeCPGException;

/**
 * Entity service
 * 
 * @author querephi
 * 
 */
public interface EntityService {

	NodeRef getImage(NodeRef entityNodeRef, String imgName) throws BeCPGException;

	List<NodeRef> getImages(NodeRef entityNodeRef) throws BeCPGException;

	NodeRef getEntityDefaultImage(NodeRef entityNodeRef) throws BeCPGException;

	void writeImages(NodeRef entityNodeRef, Map<String, byte[]> images) throws BeCPGException;
	
	NodeRef getImageFolder(NodeRef entityNodeRef) throws BeCPGException;

	byte[] getImage(NodeRef imgNodeRef);

	String getDefaultImageName(QName entityTypeQName);

	@Deprecated
	boolean hasAssociatedImages(QName type);

	NodeRef createOrCopyFrom(NodeRef sourceNodeRef, NodeRef parentNodeRef, QName entityType, String entityName);

	void copyFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	void moveFiles(NodeRef sourceNodeRef, NodeRef targetNodeRef);

	void deleteFiles(NodeRef entityNodeRef, boolean deleteArchivedNodes);

	void deleteDataLists(NodeRef entityNodeRef, boolean deleteArchivedNodes);

	NodeRef getOrCreateDocumentFolder(NodeRef entityNodeRef);

	

}

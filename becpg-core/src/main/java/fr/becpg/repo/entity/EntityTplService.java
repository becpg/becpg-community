package fr.becpg.repo.entity;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.formulation.FormulateException;


public interface EntityTplService {

	/**
	 * Create the entityTpl
	 * @param entityTplsNodeRef
	 * @param entityType
	 */
	public NodeRef createEntityTpl(NodeRef parentNodeRef, QName entityType, boolean enabled, Set<QName> entityLists, Set<String> subFolders);
	
	/**
	 * Look for the entityTpl
	 * @param isContainer
	 * @param nodeType
	 * @return
	 */
	public NodeRef getEntityTpl(QName nodeType);
	
	public void synchronizeEntities(NodeRef tplNodeRef);
	
	public void formulateEntities(NodeRef tplNodeRef) throws FormulateException;
}

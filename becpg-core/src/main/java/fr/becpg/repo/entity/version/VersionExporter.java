package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.AbstractExporter;
import org.alfresco.service.cmr.repository.AssociationExistsException;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;
import org.springframework.dao.ConcurrencyFailureException;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;

/**
 * <p>VersionExporter class.</p>
 *
 * @author matthieu
 */
public class VersionExporter extends AbstractExporter {

	private NodeService dbNodeService;
	
	private EntityDictionaryService entityDictionaryService;
	
	private NodeRef parentNodeRef = null;
	private NodeRef originalNodeRef = null;
	private NodeRef targetNode = null;

	/**
	 * <p>Constructor for VersionExporter.</p>
	 *
	 * @param originalNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param targetNode a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param dbNodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 */
	public VersionExporter(NodeRef originalNodeRef, NodeRef targetNode, NodeService dbNodeService, EntityDictionaryService entityDictionaryService) {
		this.originalNodeRef = originalNodeRef;
		this.targetNode = targetNode;
		this.dbNodeService = dbNodeService;
		this.entityDictionaryService = entityDictionaryService;
	}

	/** {@inheritDoc} */
	@Override
	public void startNode(NodeRef nodeRef) {
		QName nodeType = dbNodeService.getType(nodeRef);
		Map<QName, Serializable> props = dbNodeService.getProperties(nodeRef);
		
		if (nodeRef.equals(originalNodeRef)) {
			return;
		}
		
		if (entityDictionaryService.isSubClass(dbNodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITY_V2)) {
			throw new BeCPGException("The node contains the entity '" + dbNodeService.getProperty(nodeRef, ContentModel.PROP_NAME) + "' in its subfolders. Please remove it before");
		}
		
		NodeRef currentParent = dbNodeService.getPrimaryParent(nodeRef).getParentRef();
		
		if (currentParent.equals(originalNodeRef)) {
			parentNodeRef = targetNode;
		} else {
			String parentName = (String) dbNodeService.getProperty(currentParent, ContentModel.PROP_NAME);
			parentNodeRef = dbNodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, parentName);
		}

		String name = (String) props.get(ContentModel.PROP_NAME);
		
		NodeRef referenceNode = null;
		
		// look for a nodeRef for each of the first level childs (ie : Documents, Brief, Images)
		for (ChildAssociationRef childAssoc : dbNodeService.getChildAssocs(parentNodeRef)) {
			
			Serializable ref = dbNodeService.getProperty(childAssoc.getChildRef(), ContentModel.PROP_REFERENCE);
			
			if (ref != null && nodeRef.equals(ref)) {
				referenceNode = childAssoc.getChildRef();
				break;
			}
		}
		
		if (referenceNode == null) {
			referenceNode = dbNodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, name);
		}
		
		props.keySet().removeIf(e -> e.equals(ContentModel.PROP_NODE_UUID));
		
		if (referenceNode != null) {
			dbNodeService.setProperties(referenceNode, props);
		} else {
			// "copy" type and properties
			dbNodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), nodeType, props);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void endNode(NodeRef nodeRef) {
		
		if (nodeRef.equals(originalNodeRef)) {
			return;
		}
		
		parentNodeRef = dbNodeService.getPrimaryParent(parentNodeRef).getParentRef();
		
	}
	
	/** {@inheritDoc} */
	@Override
	public void end() {
		
        List<AssociationRef> originalAssocRefs = dbNodeService.getTargetAssocs(originalNodeRef, RegexQNamePattern.MATCH_ALL);
        
        List<AssociationRef> targetAssocRefs = dbNodeService.getTargetAssocs(targetNode, RegexQNamePattern.MATCH_ALL);

        for (AssociationRef nodeAssocRef : originalAssocRefs) {
        	
        	AssociationRef assoc = new AssociationRef(targetNode, nodeAssocRef.getTypeQName(), nodeAssocRef.getTargetRef());
        	
        	if (!targetAssocRefs.contains(assoc)) {
        		try {
        			dbNodeService.createAssociation(targetNode, nodeAssocRef.getTargetRef(), nodeAssocRef.getTypeQName());
        		} catch (AssociationExistsException e) {
                    // This will be rare, but it's not impossible.
                    // We have to retry the operation.
        			throw new ConcurrencyFailureException("Association already exists : " + assoc);
        		}
        	}
        }
	}
	
}

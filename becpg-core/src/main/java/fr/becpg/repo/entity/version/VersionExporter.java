package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.node.NodeExistsException;
import org.alfresco.repo.download.AbstractExporter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.namespace.RegexQNamePattern;

public class VersionExporter extends AbstractExporter {

	private NodeService dbNodeService;
	
	private NodeRef parentNodeRef = null;
	private NodeRef originalNodeRef = null;
	private NodeRef targetNode = null;

	public VersionExporter(NodeRef originalNodeRef, NodeRef targetNode, NodeService dbNodeService) {
		this.originalNodeRef = originalNodeRef;
		this.targetNode = targetNode;
		this.dbNodeService = dbNodeService;
	}

	@Override
	public void startNode(NodeRef nodeRef) {
		QName nodeType = dbNodeService.getType(nodeRef);
		Map<QName, Serializable> props = dbNodeService.getProperties(nodeRef);
		
		if (nodeRef.equals(originalNodeRef)) {
			return;
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
		
		props.keySet().removeIf(e -> e.equals(ContentModel.PROP_NODE_UUID));
		
		if (referenceNode != null) {
			dbNodeService.setProperties(referenceNode, props);
		} else {
			// "copy" type and properties
			dbNodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), nodeType, props);
		}

	}

	@Override
	public void endNode(NodeRef nodeRef) {
		
		if (nodeRef.equals(originalNodeRef)) {
			return;
		}
		
		parentNodeRef = dbNodeService.getPrimaryParent(parentNodeRef).getParentRef();
		
	}
	
	@Override
	public void end() {
		
        List<AssociationRef> nodeAssocRefs = dbNodeService.getTargetAssocs(originalNodeRef, RegexQNamePattern.MATCH_ALL);

        for (AssociationRef nodeAssocRef : nodeAssocRefs) {
        	dbNodeService.createAssociation(targetNode, nodeAssocRef.getTargetRef(), nodeAssocRef.getTypeQName());
        }
	}
	
}

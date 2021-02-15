package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.AbstractExporter;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.repo.version.VersionBaseModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

public class VersionExporter extends AbstractExporter {

	private NodeService dbNodeService;
	
	private NodeRef parentNodeRef = null;
	private NodeRef originalNodeRef = null;
	private NodeRef versionNode = null;

	public VersionExporter(NodeRef originalNodeRef, NodeRef versionNode, NodeService dbNodeService) {
		this.originalNodeRef = originalNodeRef;
		this.versionNode = versionNode;
		this.dbNodeService = dbNodeService;
	}

	@Override
	public void startNode(NodeRef nodeRef) {
		QName nodeType = dbNodeService.getType(nodeRef);
		Map<QName, Serializable> props = dbNodeService.getProperties(nodeRef);
		
		NodeRef currentParent = dbNodeService.getPrimaryParent(nodeRef).getParentRef();
		
		if (currentParent.equals(originalNodeRef)) {
			if (versionNode.getStoreRef().getProtocol().equals(VersionBaseModel.STORE_PROTOCOL)) {
				parentNodeRef = new NodeRef(StoreRef.PROTOCOL_WORKSPACE, Version2Model.STORE_ID, versionNode.getId());
			} else {
				parentNodeRef = versionNode;
			}
		} else {
			String parentName = (String) dbNodeService.getProperty(currentParent, ContentModel.PROP_NAME);
			parentNodeRef = dbNodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS, parentName);
		}
		
		//Si dossier alors parentNodeRef = currNodeRef
		// Si le noeud existe (Premier niveau ) alors mettre à jour les propriétés

		String name = (String) props.get(ContentModel.PROP_NAME);
		
		NodeRef currNodeRef = dbNodeService.getChildByName(parentNodeRef, ContentModel.ASSOC_CONTAINS,
				name);

		if (currNodeRef != null) {
			dbNodeService.setProperties(currNodeRef, props);
		} else {
			// "copy" type and properties
			dbNodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,  QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)), nodeType, props).getChildRef();
		}

	}

	@Override
	public void endNode(NodeRef nodeRef) {
		
		parentNodeRef = dbNodeService.getPrimaryParent(parentNodeRef).getParentRef();
		
	}

}

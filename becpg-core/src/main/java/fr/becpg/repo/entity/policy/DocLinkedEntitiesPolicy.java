/*
 * 
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * @author matthieu
 */
public class DocLinkedEntitiesPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy {

	private static final Log logger = LogFactory.getLog(DocLinkedEntitiesPolicy.class);

	// File extension to use for link nodes
	private static final String LINK_NODE_EXTENSION = ".url";

	private EntityService entityService;

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void doInit() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.ASPECT_DOC_LINKED_ENTITIES, new JavaBehaviour(this,
				"onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.ASPECT_DOC_LINKED_ENTITIES, new JavaBehaviour(this,
				"onDeleteAssociation"));
	}

	private void createLink(NodeRef nodeRef, NodeRef destRef) {
		String currentName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);

		if (!checkExists(currentName + LINK_NODE_EXTENSION, destRef)) {
			Map<QName, Serializable> props = new HashMap<>(2, 1.0f);

			logger.debug("Create link :" + currentName + " under " + destRef);
			String newName = currentName + LINK_NODE_EXTENSION;
			props.put(ContentModel.PROP_NAME, newName);
			props.put(ContentModel.PROP_LINK_DESTINATION, nodeRef);

			// create File Link node
			ChildAssociationRef childRef = nodeService.createNode(destRef, ContentModel.ASSOC_CONTAINS, QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName),
					ApplicationModel.TYPE_FILELINK, props);

			// apply the titled aspect - title and description
			Map<QName, Serializable> titledProps = new HashMap<>(2, 1.0f);
			titledProps.put(ContentModel.PROP_TITLE, currentName);
			titledProps.put(ContentModel.PROP_DESCRIPTION, currentName);
			nodeService.addAspect(childRef.getChildRef(), ContentModel.ASPECT_TITLED, titledProps);

		}

	}

	private void deleteLink(NodeRef nodeRef, NodeRef destRef) {
		String currentName = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		String newName = currentName + LINK_NODE_EXTENSION;
		NodeRef linkNodeRef = nodeService.getChildByName(destRef, ContentModel.ASSOC_CONTAINS, newName);
		if (linkNodeRef != null) {
			logger.debug("Delete link :" + linkNodeRef);
			nodeService.deleteNode(linkNodeRef);
		}

	}

	private boolean checkExists(String name, NodeRef destRef) {
		return nodeService.getChildByName(destRef, ContentModel.ASSOC_CONTAINS, name) != null;
	}

	@Override
	public void onDeleteAssociation(AssociationRef associationRef) {
		if (BeCPGModel.ASSOC_DOC_LINKED_ENTITIES.equals(associationRef.getTypeQName())) {
			NodeRef destRef = entityService.getOrCreateDocumentsFolder(associationRef.getTargetRef());

			deleteLink(associationRef.getSourceRef(), destRef);
		}

	}

	@Override
	public void onCreateAssociation(AssociationRef associationRef) {
		if (BeCPGModel.ASSOC_DOC_LINKED_ENTITIES.equals(associationRef.getTypeQName())) {
			NodeRef destRef = entityService.getOrCreateDocumentsFolder(associationRef.getTargetRef());

			createLink(associationRef.getSourceRef(), destRef);
		}

	}
}

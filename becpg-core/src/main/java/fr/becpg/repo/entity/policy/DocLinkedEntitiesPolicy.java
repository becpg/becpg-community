/*
 *
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ApplicationModel;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>DocLinkedEntitiesPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DocLinkedEntitiesPolicy extends AbstractBeCPGPolicy
		implements NodeServicePolicies.OnCreateAssociationPolicy, NodeServicePolicies.OnDeleteAssociationPolicy {

	private static final Log logger = LogFactory.getLog(DocLinkedEntitiesPolicy.class);

	// File extension to use for link nodes
	private static final String LINK_NODE_EXTENSION = ".url";

	private EntityService entityService;

	private AssociationService associationService;
	
	private MimetypeService mimetypeService;
	
	private ContentService contentService;
	
	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}
	
	/**
	 * <p>Setter for the field <code>mimetypeService</code>.</p>
	 *
	 * @param mimetypeService a {@link org.alfresco.service.cmr.repository.MimetypeService} object
	 */
	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object.
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object.
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, BeCPGModel.ASPECT_DOC_LINKED_ENTITIES,
				new JavaBehaviour(this, "onCreateAssociation"));
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnDeleteAssociationPolicy.QNAME, BeCPGModel.ASPECT_DOC_LINKED_ENTITIES,
				new JavaBehaviour(this, "onDeleteAssociation"));

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
			ChildAssociationRef childRef = nodeService.createNode(destRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, newName), ApplicationModel.TYPE_FILELINK, props);

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

	/** {@inheritDoc} */
	@Override
	public void onDeleteAssociation(AssociationRef associationRef) {
		if (BeCPGModel.ASSOC_DOC_LINKED_ENTITIES.equals(associationRef.getTypeQName())
				&& policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)
				&& policyBehaviourFilter.isEnabled(associationRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE)
				&& !isWorkingCopyOrVersion(associationRef.getSourceRef())
				&& !nodeService.hasAspect(associationRef.getSourceRef(), ContentModel.ASPECT_CHECKED_OUT)
				&& !nodeService.hasAspect(associationRef.getTargetRef(), ContentModel.ASPECT_PENDING_DELETE)) {
			
			String name = (String) nodeService.getProperty(associationRef.getSourceRef(), ContentModel.PROP_NAME);
			
			String mimeType = mimetypeService.guessMimetype(name, contentService.getReader(associationRef.getSourceRef(), ContentModel.PROP_CONTENT));
			
			boolean isImage = mimeType != null && mimeType.startsWith(MimetypeMap.PREFIX_IMAGE);
			
			NodeRef destRef = null;
			
			if (isImage) {
				destRef = entityService.getOrCreateImageFolder(associationRef.getTargetRef());
			} else {
				destRef = entityService.getOrCreateDocumentsFolder(associationRef.getTargetRef());
			}
			
			deleteLink(associationRef.getSourceRef(), destRef);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void onCreateAssociation(AssociationRef associationRef) {
		if (BeCPGModel.ASSOC_DOC_LINKED_ENTITIES.equals(associationRef.getTypeQName())
				&& policyBehaviourFilter.isEnabled(ContentModel.ASPECT_AUDITABLE)
				&& policyBehaviourFilter.isEnabled(associationRef.getSourceRef(), ContentModel.ASPECT_AUDITABLE)
				&& !nodeService.hasAspect(associationRef.getSourceRef(), ContentModel.ASPECT_CHECKED_OUT)
				&& !isWorkingCopyOrVersion(associationRef.getSourceRef())) {

			queueNode(associationRef.getSourceRef());

		}

	}

	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef entityNodeRef : pendingNodes) {
			if (nodeService.exists(entityNodeRef) && !isWorkingCopyOrVersion(entityNodeRef)) {
				
				String name = (String) nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME);
				
				String mimeType = mimetypeService.guessMimetype(name, contentService.getReader(entityNodeRef, ContentModel.PROP_CONTENT));
				
				boolean isImage = mimeType != null && mimeType.startsWith(MimetypeMap.PREFIX_IMAGE);

				List<NodeRef> linkedNodeRefs = associationService.getTargetAssocs(entityNodeRef, BeCPGModel.ASSOC_DOC_LINKED_ENTITIES);
				for (NodeRef linkedNodeRef : linkedNodeRefs) {
					NodeRef destRef = null;
					if (isImage) {
						destRef = entityService.getOrCreateImageFolder(linkedNodeRef);
					} else {
						destRef = entityService.getOrCreateDocumentsFolder(linkedNodeRef);
					}
					
					createLink(entityNodeRef, destRef);
				}
			}
		}
		return true;
	}

}

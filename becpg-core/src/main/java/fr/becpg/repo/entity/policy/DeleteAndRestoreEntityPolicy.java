package fr.becpg.repo.entity.policy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.NodeArchiveServicePolicies;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforePurgeNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeArchiveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.BeforeDeleteNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRestoreNodePolicy;
import org.alfresco.repo.node.db.NodeHierarchyWalker;
import org.alfresco.repo.node.db.NodeHierarchyWalker.VisitedNode;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantService;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.TypeDefinition;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.entity.remote.RemoteParams;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.PropertiesHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 * <p>DeleteAndRestoreEntityPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DeleteAndRestoreEntityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnRestoreNodePolicy,
		NodeServicePolicies.BeforeArchiveNodePolicy, NodeArchiveServicePolicies.BeforePurgeNodePolicy, NodeServicePolicies.BeforeDeleteNodePolicy {

	private static Log logger = LogFactory.getLog(DeleteAndRestoreEntityPolicy.class);

	private RemoteEntityService remoteEntityService;

	private ContentService contentService;

	private NodeDAO nodeDAO;

	private EntityDictionaryService entityDictionaryService;

	private DictionaryService dictionaryService;

	private AttributeExtractorService attributeExtractorService;

	private EntityListDAO entityListDAO;

	private TenantService tenantService;
	

	private static final String ENTITY_DELETED = "entity_deleted";

	/**
	 * <p>Setter for the field <code>tenantService</code>.</p>
	 *
	 * @param tenantService a {@link org.alfresco.repo.tenant.TenantService} object.
	 */
	public void setTenantService(TenantService tenantService) {
		this.tenantService = tenantService;
	}

	/**
	 * <p>Setter for the field <code>nodeDAO</code>.</p>
	 *
	 * @param nodeDAO a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	/**
	 * <p>Setter for the field <code>entityDictionaryService</code>.</p>
	 *
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object.
	 */
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}


	/**
	 * <p>Setter for the field <code>remoteEntityService</code>.</p>
	 *
	 * @param remoteEntityService a {@link fr.becpg.repo.entity.remote.RemoteEntityService} object.
	 */
	public void setRemoteEntityService(RemoteEntityService remoteEntityService) {
		this.remoteEntityService = remoteEntityService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>attributeExtractorService</code>.</p>
	 *
	 * @param attributeExtractorService a {@link fr.becpg.repo.helper.AttributeExtractorService} object.
	 */
	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}
	
	/** {@inheritDoc} */
	@Override
	public void doInit() {
		this.policyComponent.bindClassBehaviour(BeforePurgeNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2, new JavaBehaviour(this, "beforePurgeNode"));
		this.policyComponent.bindClassBehaviour(BeforeArchiveNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "beforeArchiveNode"));
		this.policyComponent.bindClassBehaviour(OnRestoreNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2, new JavaBehaviour(this, "onRestoreNode"));
		this.policyComponent.bindClassBehaviour(OnRestoreNodePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onRestoreNode"));

		this.policyComponent.bindClassBehaviour(BeforePurgeNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "beforePurgeNode"));
		this.policyComponent.bindClassBehaviour(BeforeArchiveNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM,
				new JavaBehaviour(this, "beforeArchiveNode"));
		this.policyComponent.bindClassBehaviour(OnRestoreNodePolicy.QNAME, BeCPGModel.TYPE_ENTITYLIST_ITEM, new JavaBehaviour(this, "onRestoreNode"));
		this.policyComponent.bindClassBehaviour(BeforeDeleteNodePolicy.QNAME, BeCPGModel.ASPECT_UNDELETABLE_ASPECT, new JavaBehaviour(this, "beforeDeleteNode"));

		// Add policies for cm:folder to handle all folder types
		this.policyComponent.bindClassBehaviour(BeforePurgeNodePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "beforePurgeNode"));
	}

	/** {@inheritDoc} */
	@Override
	public void onRestoreNode(ChildAssociationRef childAssocRef) {
		try {

			NodeRef childRef = childAssocRef.getChildRef();

			if (childRef == null) {
				return;
			}
			childRef = tenantService.getName(childRef);

			Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(childRef);

			if (nodePair != null) {

				// get the primary parent-child relationship before it is gone
				Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.getPrimaryParentAssoc(nodePair.getFirst());

				// Gather information about the hierarchy
				NodeHierarchyWalker walker = new NodeHierarchyWalker(nodeDAO);
				walker.walkHierarchy(nodePair, childAssocPair);

				for (VisitedNode visitedNode : walker.getNodes(true)) {

					NodeRef entityNodeRef = visitedNode.nodeRef;

					if (entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITY_V2)
							|| entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)) {

						NodeRef entityDeletedFileNodeRef = findEntityDeletedContentNodeRef(entityNodeRef);

						
						if (entityDeletedFileNodeRef != null) {

							if (logger.isDebugEnabled()) {
								logger.debug("Retrieving " + ENTITY_DELETED + "_" + entityNodeRef.getId() + " from archiveStore: "+ entityDeletedFileNodeRef);
							}

							ContentReader reader = contentService.getReader(entityDeletedFileNodeRef, ContentModel.PROP_CONTENT);
							if (reader != null) {
								try (InputStream in = reader.getContentInputStream()) {

									IntegrityChecker.setWarnInTransaction();
									remoteEntityService.createOrUpdateEntity(entityNodeRef, in, new RemoteParams(RemoteEntityFormat.xml), null);
								} catch (IOException e) {
									logger.error(e, e);
								}
							} else {
								logger.error("Cannot read content of " + ENTITY_DELETED + "_" + entityNodeRef.getId());
							}

							nodeService.deleteNode(entityDeletedFileNodeRef);
						} else if(!entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)){
							logger.error("Cannot find " + ENTITY_DELETED + "_" + entityNodeRef.getId() + " from archiveStore ");
						}
					}

				}
			}

		} catch (ContentIOException | BeCPGException e) {
			logger.error(e, e);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void beforeArchiveNode(NodeRef entityNodeRef) {


		AuthenticationUtil.runAsSystem(() -> {

			QName type = nodeService.getType(entityNodeRef);

			boolean isListItem = entityDictionaryService.isSubClass(type, BeCPGModel.TYPE_ENTITYLIST_ITEM);

			NodeRef entityParent = null;

			if (isListItem) {
				entityParent = entityListDAO.getEntity(entityNodeRef);
				if (isPendingDelete(entityParent) || !nodeService.exists(entityParent)) {
					return null;
				}
			}
			

			if (logger.isDebugEnabled()) {
				logger.debug("Creating " + ENTITY_DELETED + " for " + entityNodeRef);
			}

			NodeRef rootArchiveRef = nodeService.getStoreArchiveNode(entityNodeRef.getStoreRef());

			String entityDeletedId = entityDeletedContentId(entityNodeRef);
			String entityDeletedName = entityDeletedContentName(entityNodeRef);
			
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NODE_UUID, entityDeletedId);
			
			NodeRef entityDeletedFileNodeRef = nodeService
					.createNode(rootArchiveRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									QName.createValidLocalName(entityDeletedName)),
							ContentModel.TYPE_CONTENT, props)
					.getChildRef();

			nodeService.setProperty(entityDeletedFileNodeRef, ContentModel.PROP_NAME, entityDeletedName);
			nodeService.setProperty(entityDeletedFileNodeRef, ContentModel.PROP_ARCHIVED_DATE, new Date());

			ContentWriter writer = contentService.getWriter(entityDeletedFileNodeRef, ContentModel.PROP_CONTENT, true);

			writer.setMimetype(MimetypeMap.MIMETYPE_XML);

			try (OutputStream out = writer.getContentOutputStream()) {
				remoteEntityService.getEntity(entityNodeRef, out, new RemoteParams(RemoteEntityFormat.xml));
			} catch (ContentIOException | IOException | BeCPGException e) {
				logger.error(e, e);
			}

			if (isListItem) {

				String name = "";

				if (entityParent != null) {
					name += attributeExtractorService.extractPropName(entityParent) + " - ";
				}

				TypeDefinition typeDef = dictionaryService.getType(type);
				if ((typeDef != null) && (typeDef.getTitle(dictionaryService) != null)) {
					name += typeDef.getTitle(dictionaryService) + " - ";
				}

				name += attributeExtractorService.extractPropName(type, entityNodeRef) + "-" + UUID.randomUUID().toString();

				nodeService.setProperty(entityNodeRef, ContentModel.PROP_NAME, PropertiesHelper.cleanName(name));

			}

			return null;
		});

	}


	/** {@inheritDoc} */
	@Override
	public void beforePurgeNode(NodeRef nodeRef) {
		AuthenticationUtil.runAsSystem(() -> {
			
			if (logger.isDebugEnabled()) {
				logger.debug("beforePurgeNode called for: " + nodeRef.getId());
			}
			
			// Get the node from archive store
			Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(nodeRef);
			
			if (nodePair != null) {
				
				// Get the primary parent-child relationship
				Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.getPrimaryParentAssoc(nodePair.getFirst());
				
				// Walk the hierarchy to get all child nodes
				NodeHierarchyWalker walker = new NodeHierarchyWalker(nodeDAO);
				walker.walkHierarchy(nodePair, childAssocPair);
				
				// Process all nodes in the hierarchy (parent first, then children)
				for (VisitedNode visitedNode : walker.getNodes(true)) {
					
					NodeRef visitedNodeRef = visitedNode.nodeRef;
					QName visitedType = nodeService.getType(visitedNodeRef);
					
					// Delete archived content for entities and list items
					if (entityDictionaryService.isSubClass(visitedType, BeCPGModel.TYPE_ENTITY_V2)
							|| entityDictionaryService.isSubClass(visitedType, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
						
						if (logger.isDebugEnabled()) {
							logger.debug("Purging entity_deleted content for node: " + visitedNodeRef.getId());
						}
						
						deleteEntityDeletedContent(visitedNodeRef);
					}
				}
			} else {
				// Fallback: try to delete the archived content directly
				if (logger.isDebugEnabled()) {
					logger.debug("Node pair not found, attempting direct deletion for: " + nodeRef.getId());
				}
				deleteEntityDeletedContent(nodeRef);
			}
			
			return null;
		});
	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_UNDELETABLE_ASPECT)) {
			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			throw new IllegalStateException(I18NUtil.getMessage("message.element.undeletable", name));
		}
	}
	
	private void deleteEntityDeletedContent(NodeRef entityNodeRef) {
		
		if (logger.isDebugEnabled()) {
			logger.debug("Deleting entity_deleted content for: " + entityNodeRef.getId());
		}
		
		NodeRef entityDeletedContentNodeRef = findEntityDeletedContentNodeRef(entityNodeRef);
		
		if (entityDeletedContentNodeRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found and deleting entity_deleted: " + entityNodeRef.getId() + " from archiveStore");
			}
			nodeService.deleteNode(entityDeletedContentNodeRef);
		} else {
			if (logger.isDebugEnabled()) {
				logger.debug("No entity_deleted found for: " + entityNodeRef.getId());
			}
		}
	}

	private NodeRef findEntityDeletedContentNodeRef(NodeRef entityNodeRef) {
		NodeRef entityDeletedNodeRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, entityDeletedContentId(entityNodeRef));
		if (nodeService.exists(entityDeletedNodeRef)) {
			return entityDeletedNodeRef;
		}
		NodeRef rootArchiveRef = nodeService.getRootNode(entityNodeRef.getStoreRef());
		return nodeService.getChildByName(rootArchiveRef, ContentModel.ASSOC_CONTAINS, entityDeletedContentName(entityNodeRef));
	}

	private String entityDeletedContentId(NodeRef entityNodeRef) {
		return "X_" + entityNodeRef.getId().substring(2);
	}
	
	private String entityDeletedContentName(NodeRef entityNodeRef) {
		return ENTITY_DELETED + "_" + entityNodeRef.getId();
	}

}

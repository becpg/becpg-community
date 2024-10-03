package fr.becpg.repo.entity.policy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
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
	

	private static final String REMOTE_FILE_NAME = "entity_deleted";

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

						NodeRef rootArchiveRef = nodeService.getStoreArchiveNode(entityNodeRef.getStoreRef());


						NodeRef entityDeletedFileNodeRef = nodeService.getChildByName(rootArchiveRef, ContentModel.ASSOC_CONTAINS,
								REMOTE_FILE_NAME + "_" + entityNodeRef.getId());

						
						if (entityDeletedFileNodeRef != null) {

							if (logger.isDebugEnabled()) {
								logger.debug("Retrieving " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore: "+ entityDeletedFileNodeRef);
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
								logger.error("Cannot read content of " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId());
							}

							nodeService.deleteNode(entityDeletedFileNodeRef);
						} else if(!entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITYLIST_ITEM)){
							logger.error("Cannot find " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore ");
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
				logger.debug("Creating " + REMOTE_FILE_NAME + " for " + entityNodeRef);
			}

			NodeRef rootArchiveRef = nodeService.getStoreArchiveNode(entityNodeRef.getStoreRef());

			NodeRef entityDeletedFileNodeRef = nodeService
					.createNode(rootArchiveRef, ContentModel.ASSOC_CONTAINS,
							QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
									QName.createValidLocalName(REMOTE_FILE_NAME + "_" + entityNodeRef.getId())),
							ContentModel.TYPE_CONTENT)
					.getChildRef();

			nodeService.setProperty(entityDeletedFileNodeRef, ContentModel.PROP_NAME, REMOTE_FILE_NAME + "_" + entityNodeRef.getId());
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
	public void beforePurgeNode(NodeRef entityNodeRef) {
		NodeRef rootArchiveRef = nodeService.getRootNode(entityNodeRef.getStoreRef());

		if (logger.isDebugEnabled()) {
			logger.debug("Retrieving " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore ");
		}

		NodeRef entityDeletedFileNodeRef = nodeService.getChildByName(rootArchiveRef, ContentModel.ASSOC_CONTAINS,
				REMOTE_FILE_NAME + "_" + entityNodeRef.getId());

		if (entityDeletedFileNodeRef != null) {
			logger.debug("Deleting: " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore ");
			nodeService.deleteNode(entityDeletedFileNodeRef);
		} else {
			logger.error("Cannot find " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore ");
		}

	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_UNDELETABLE_ASPECT)) {
			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
			throw new IllegalStateException(I18NUtil.getMessage("message.element.undeletable", name));
		}
	}

}

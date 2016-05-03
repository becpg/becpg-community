package fr.becpg.repo.entity.policy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.MimetypeMap;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.node.NodeArchiveServicePolicies;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforePurgeNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies.BeforeArchiveNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies.OnRestoreNodePolicy;
import org.alfresco.repo.node.db.NodeHierarchyWalker;
import org.alfresco.repo.node.db.NodeHierarchyWalker.VisitedNode;
import org.alfresco.repo.policy.JavaBehaviour;
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

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;

/**
 *
 * @author matthieu
 *
 */
public class DeleteAndRestoreEntityPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnRestoreNodePolicy,
		NodeServicePolicies.BeforeArchiveNodePolicy, NodeArchiveServicePolicies.BeforePurgeNodePolicy {

	private static Log logger = LogFactory.getLog(DeleteAndRestoreEntityPolicy.class);

	private RemoteEntityService remoteEntityService;

	private ContentService contentService;

	private NodeDAO nodeDAO;

	private EntityDictionaryService entityDictionaryService;

	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	private final static String REMOTE_FILE_NAME = "entity_deleted";

	public void setRemoteEntityService(RemoteEntityService remoteEntityService) {
		this.remoteEntityService = remoteEntityService;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	@Override
	public void doInit() {
		this.policyComponent.bindClassBehaviour(BeforePurgeNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2, new JavaBehaviour(this, "beforePurgeNode"));
		this.policyComponent.bindClassBehaviour(BeforeArchiveNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2,
				new JavaBehaviour(this, "beforeArchiveNode"));
		this.policyComponent.bindClassBehaviour(OnRestoreNodePolicy.QNAME, BeCPGModel.TYPE_ENTITY_V2, new JavaBehaviour(this, "onRestoreNode"));
		this.policyComponent.bindClassBehaviour(OnRestoreNodePolicy.QNAME, ContentModel.TYPE_FOLDER, new JavaBehaviour(this, "onRestoreNode"));

	}

	@Override
	public void onRestoreNode(ChildAssociationRef childAssocRef) {
		try {
			

			Pair<Long, NodeRef> nodePair = nodeDAO.getNodePair(childAssocRef.getChildRef());

			// get the primary parent-child relationship before it is gone
			Pair<Long, ChildAssociationRef> childAssocPair = nodeDAO.getPrimaryParentAssoc(nodePair.getFirst());

			// Gather information about the hierarchy
			NodeHierarchyWalker walker = new NodeHierarchyWalker(nodeDAO);
			walker.walkHierarchy(nodePair, childAssocPair);

			for (VisitedNode visitedNode : walker.getNodes(true)) {

				NodeRef entityNodeRef = visitedNode.nodeRef;

				if (entityDictionaryService.isSubClass(nodeService.getType(entityNodeRef), BeCPGModel.TYPE_ENTITY_V2)) {

					NodeRef rootArchiveRef = nodeService.getStoreArchiveNode(entityNodeRef.getStoreRef());

					if (logger.isDebugEnabled()) {
						logger.debug("Retrieving " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore ");
					}

					NodeRef entityDeletedFileNodeRef = nodeService.getChildByName(rootArchiveRef, ContentModel.ASSOC_CONTAINS,
							REMOTE_FILE_NAME + "_" + entityNodeRef.getId());

					if (entityDeletedFileNodeRef != null) {

						ContentReader reader = contentService.getReader(entityDeletedFileNodeRef, ContentModel.PROP_CONTENT);
						if (reader != null) {
							try (InputStream in = reader.getContentInputStream()) {
								policyBehaviourFilter.disableBehaviour(entityNodeRef);
								
								remoteEntityService.createOrUpdateEntity(entityNodeRef, in, RemoteEntityFormat.xml, null);
							} catch (IOException e) {
								logger.error(e, e);
							} finally {
								policyBehaviourFilter.enableBehaviour(entityNodeRef);
							}
						} else {
							logger.error("Cannot read content of " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId());
						}

						nodeService.deleteNode(entityDeletedFileNodeRef);
					} else {
						logger.error("Cannot find " + REMOTE_FILE_NAME + "_" + entityNodeRef.getId() + " from archiveStore ");
					}
				}

			}

		} catch (ContentIOException | BeCPGException e) {
			logger.error(e, e);
		}

	}

	@Override
	public void beforeArchiveNode(NodeRef entityNodeRef) {

		if (logger.isDebugEnabled()) {
			logger.debug("Creating " + REMOTE_FILE_NAME + " for " + entityNodeRef);
		}

		NodeRef rootArchiveRef = nodeService.getStoreArchiveNode(entityNodeRef.getStoreRef());

		NodeRef entityDeletedFileNodeRef = nodeService.createNode(rootArchiveRef, ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(REMOTE_FILE_NAME + "_" + entityNodeRef.getId())),
				ContentModel.TYPE_CONTENT).getChildRef();

		nodeService.setProperty(entityDeletedFileNodeRef, ContentModel.PROP_NAME, REMOTE_FILE_NAME + "_" + entityNodeRef.getId());

		ContentWriter writer = contentService.getWriter(entityDeletedFileNodeRef, ContentModel.PROP_CONTENT, true);

		writer.setMimetype(MimetypeMap.MIMETYPE_XML);

		try (OutputStream out = writer.getContentOutputStream()) {
			remoteEntityService.getEntity(entityNodeRef, out, RemoteEntityFormat.xml);
		} catch (ContentIOException | IOException | BeCPGException e) {
			logger.error(e, e);
		}

	}

	@Override
	public void beforePurgeNode(NodeRef entityNodeRef) {
		NodeRef rootArchiveRef = nodeService.getStoreArchiveNode(entityNodeRef.getStoreRef());

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

}

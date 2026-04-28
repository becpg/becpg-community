package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;

/**
 * <p>EntityHistoryAspectPatch class.</p>
 *
 * @author matthieu
 */
public class EntityHistoryAspectPatch extends AbstractBeCPGPatch {

    private static final Log logger = LogFactory.getLog(EntityHistoryAspectPatch.class);
    private static final String MSG_SUCCESS = "patch.bcpg.entityHistoryAspectPatch.result";

    /** {@inheritDoc} */
    @Override
    protected String applyInternal() throws Exception {
        NodeRef rootNodeRef = nodeService.getRootNode(RepoConsts.SPACES_STORE);
        NodeRef entitiesHistoryNodeRef = nodeService.getChildByName(rootNodeRef, ContentModel.ASSOC_CONTAINS, RepoConsts.ENTITIES_HISTORY_NAME);
        if (entitiesHistoryNodeRef == null) {
            return I18NUtil.getMessage(MSG_SUCCESS);
        }

        List<NodeRef> nodes = new ArrayList<>();
        collectNodes(entitiesHistoryNodeRef, nodes);

        BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
            private int currentIndex = 0;

            @Override
            public int getTotalEstimatedWorkSize() {
                return nodes.size();
            }

            @Override
            public long getTotalEstimatedWorkSizeLong() {
                return nodes.size();
            }

            @Override
            public Collection<NodeRef> getNextWork() {
                List<NodeRef> batch = new ArrayList<>();
                while (currentIndex < nodes.size() && batch.size() < BATCH_SIZE) {
                    batch.add(nodes.get(currentIndex));
                    currentIndex++;
                }
                return batch;
            }
        };

        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("EntityHistoryAspectPatch", transactionService.getRetryingTransactionHelper(),
                workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

        BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

            @Override
            public void afterProcess() throws Throwable {
            }

            @Override
            public void beforeProcess() throws Throwable {
            }

            @Override
            public String getIdentifier(NodeRef entry) {
                return entry.toString();
            }

            @Override
            public void process(NodeRef nodeRef) throws Throwable {
                AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
                addEntityHistoryAspects(nodeRef);
            }
        };

        batchProcessor.processLong(worker, true);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

    private void collectNodes(NodeRef nodeRef, List<NodeRef> nodes) {
        if (nodeService.exists(nodeRef)) {
            nodes.add(nodeRef);
            for (ChildAssociationRef childAssocRef : nodeService.getChildAssocs(nodeRef)) {
                collectNodes(childAssocRef.getChildRef(), nodes);
            }
        }
    }

    private void addEntityHistoryAspects(NodeRef nodeRef) {
        if (nodeService.exists(nodeRef)) {
            if (!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_HISTORY)) {
                nodeService.addAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_HISTORY, null);
            }
            if (!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL)) {
                Map<QName, Serializable> aspectProperties = new HashMap<>(2);
                aspectProperties.put(ContentModel.PROP_IS_INDEXED, Boolean.FALSE);
                aspectProperties.put(ContentModel.PROP_IS_CONTENT_INDEXED, Boolean.FALSE);
                nodeService.addAspect(nodeRef, ContentModel.ASPECT_INDEX_CONTROL, aspectProperties);
            }
        }
    }
}

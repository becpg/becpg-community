package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;

public class RemoveDeprecatedPubAspectsPatch extends AbstractBeCPGPatch {

    private static final QName ASPECT_MAILING_LIST = QName.createQName(BeCPGModel.BECPG_URI, "MailingListChannelAspect");
    private static final QName ASPECT_CATALOGABLE = QName.createQName(BeCPGModel.BECPG_URI, "cataloguable");
    private static final QName ASPECT_PRODUCT_CATALOG = QName.createQName(BeCPGModel.BECPG_URI, "productCatalogChannelAspect");
    
    private static final QName TYPE_MAILING_LIST = QName.createQName(BeCPGModel.BECPG_URI, "MailingListChannel");
    private static final QName TYPE_PRODUCT_CATALOG = QName.createQName(BeCPGModel.BECPG_URI, "productCatalog");
    
    

    private static final Log logger = LogFactory.getLog(RemoveDeprecatedPubAspectsPatch.class);
    private static final String MSG_SUCCESS = "patch.bcpg.plm.RemoveDeprecatedPubAspectsPatch.result";
    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_THREADS = 4;
    private static final long INC = 1000L;

    private NodeDAO nodeDAO;
    private PatchDAO patchDAO;
    private QNameDAO qnameDAO;
    private BehaviourFilter policyBehaviourFilter;
    private RuleService ruleService;
    private IntegrityChecker integrityChecker;

    @Override
    protected String applyInternal() throws Exception {
        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
            final List<NodeRef> result = new ArrayList<>();
            final long maxNodeId = getNodeDAO().getMaxNodeId();
            long minSearchNodeId = 0;
            long maxSearchNodeId = INC;

            final Pair<Long, QName> mailingListVal = getQnameDAO().getQName(ASPECT_MAILING_LIST);
            final Pair<Long, QName> catalogableVal = getQnameDAO().getQName(ASPECT_CATALOGABLE);
            final Pair<Long, QName> productCatalogVal = getQnameDAO().getQName(ASPECT_PRODUCT_CATALOG);
             
            final Pair<Long, QName> mailingListTypeVal = getQnameDAO().getQName(TYPE_MAILING_LIST);
            final Pair<Long, QName> productCatalogTypeVal = getQnameDAO().getQName(TYPE_PRODUCT_CATALOG);

            @Override
            public int getTotalEstimatedWorkSize() {
                return result.size();
            }

            @Override
            public long getTotalEstimatedWorkSizeLong() {
                return getTotalEstimatedWorkSize();
            }

            @Override
            public Collection<NodeRef> getNextWork() {
                result.clear();

                while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
                    // Check each aspect type for nodes
                    processAspect(mailingListVal);
                    processAspect(catalogableVal);
                    processAspect(productCatalogVal);

                    processType(mailingListTypeVal);
                    processType(productCatalogTypeVal);

                    minSearchNodeId += INC;
                    maxSearchNodeId += INC;
                }

                return result;
            }

            private void processType(Pair<Long, QName> type) {
                if (type != null) {
                    Long typeQNameId = type.getFirst();
                    List<Long> nodeIds = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

                    for (Long nodeId : nodeIds) {
                        NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeId);
                        if (!status.isDeleted()) {
                            result.add(status.getNodeRef());
                        }
                    }
                }
            }

            private void processAspect(Pair<Long, QName> aspect) {
                if (aspect != null) {
                    Long typeQNameId = aspect.getFirst();
                    List<Long> nodeIds = getPatchDAO().getNodesByAspectQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);
                    
                    for (Long nodeId : nodeIds) {
                        NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeId);
                        if (!status.isDeleted()) {
                            result.add(status.getNodeRef());
                        }
                    }
                }
            }
        };

        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(
            "RemoveDeprecatedPubAspectsPatch",
            transactionService.getRetryingTransactionHelper(),
            workProvider,
            BATCH_THREADS,
            BATCH_SIZE,
            applicationEventPublisher,
            logger,
            1000
        );

        BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {
            @Override
            public void afterProcess() throws Throwable {
                // Do nothing
            }

            @Override
            public void beforeProcess() throws Throwable {
                // Do nothing
            }

            @Override
            public String getIdentifier(NodeRef entry) {
                return entry.toString();
            }

            @Override
            public void process(NodeRef nodeRef) throws Throwable {
                ruleService.disableRules();
                AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
                policyBehaviourFilter.disableBehaviour();
                integrityChecker.setEnabled(false);

                try {
                    if (nodeService.exists(nodeRef)) {
                        // Remove all three aspects if they exist
                        if (nodeService.hasAspect(nodeRef, ASPECT_MAILING_LIST)) {
                            nodeService.removeAspect(nodeRef, ASPECT_MAILING_LIST);
                        }
                        if (nodeService.hasAspect(nodeRef, ASPECT_CATALOGABLE)) {
                            nodeService.removeAspect(nodeRef, ASPECT_CATALOGABLE);
                        }
                        if (nodeService.hasAspect(nodeRef, ASPECT_PRODUCT_CATALOG)) {
                            nodeService.removeAspect(nodeRef, ASPECT_PRODUCT_CATALOG);
                        }

                        QName nodeType = nodeService.getType(nodeRef);
                        if (TYPE_MAILING_LIST.equals(nodeType) || TYPE_PRODUCT_CATALOG.equals(nodeType)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Removing node of deprecated type: " + nodeType + " - " + nodeRef);
                            }
                            nodeService.deleteNode(nodeRef);
                        }
                    } else {
                        logger.warn("Node doesn't exist: " + nodeRef);
                    }
                } finally {
                    integrityChecker.setEnabled(true);
                    policyBehaviourFilter.enableBehaviour();
                    ruleService.enableRules();
                }
            }
        };

        batchProcessor.processLong(worker, true);

        return I18NUtil.getMessage(MSG_SUCCESS);
    }

    // Getters and setters
    public void setIntegrityChecker(IntegrityChecker integrityChecker) {
        this.integrityChecker = integrityChecker;
    }

    public NodeDAO getNodeDAO() {
        return nodeDAO;
    }

    public void setNodeDAO(NodeDAO nodeDAO) {
        this.nodeDAO = nodeDAO;
    }

    public PatchDAO getPatchDAO() {
        return patchDAO;
    }

    public void setPatchDAO(PatchDAO patchDAO) {
        this.patchDAO = patchDAO;
    }

    public QNameDAO getQnameDAO() {
        return qnameDAO;
    }

    public void setQnameDAO(QNameDAO qnameDAO) {
        this.qnameDAO = qnameDAO;
    }

    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public RuleService getRuleService() {
        return ruleService;
    }

    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }
}
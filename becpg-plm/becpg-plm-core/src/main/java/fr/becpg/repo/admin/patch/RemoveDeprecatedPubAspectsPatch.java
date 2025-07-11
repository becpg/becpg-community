package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
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

import fr.becpg.model.PublicationModel;

/**
 * <p>RemoveDeprecatedPubAspectsPatch class.</p>
 *
 * @author matthieu
 */
public class RemoveDeprecatedPubAspectsPatch extends AbstractBeCPGPatch {

    private static final QName ASPECT_MAILING_LIST = QName.createQName(PublicationModel.PUBLICATION_URI, "MailingListChannelAspect");
    private static final QName ASPECT_CATALOGABLE = QName.createQName(PublicationModel.PUBLICATION_URI, "cataloguable");
    private static final QName ASPECT_PRODUCT_CATALOG = QName.createQName(PublicationModel.PUBLICATION_URI, "productCatalogChannelAspect");
    
    private static final QName TYPE_MAILING_LIST = QName.createQName(PublicationModel.PUBLICATION_URI, "MailingListChannel");
    private static final QName TYPE_PRODUCT_CATALOG = QName.createQName(PublicationModel.PUBLICATION_URI, "productCatalog");
    
    

    private static final Log logger = LogFactory.getLog(RemoveDeprecatedPubAspectsPatch.class);
    private static final String MSG_SUCCESS = "patch.bcpg.plm.RemoveDeprecatedPubAspectsPatch.result";
    private static final int BATCH_SIZE = 1000;
    private static final int BATCH_THREADS = 4;
    private static final long INC = 1000L;


    private BehaviourFilter policyBehaviourFilter;
    private RuleService ruleService;
    private IntegrityChecker integrityChecker;

    /** {@inheritDoc} */
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

    /**
     * <p>Setter for the field <code>integrityChecker</code>.</p>
     *
     * @param integrityChecker a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object
     */
    public void setIntegrityChecker(IntegrityChecker integrityChecker) {
        this.integrityChecker = integrityChecker;
    }

 

    /**
     * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
     *
     * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
     */
    public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    /**
     * <p>Getter for the field <code>ruleService</code>.</p>
     *
     * @return a {@link org.alfresco.service.cmr.rule.RuleService} object
     */
    public RuleService getRuleService() {
        return ruleService;
    }

    /**
     * <p>Setter for the field <code>ruleService</code>.</p>
     *
     * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }
}

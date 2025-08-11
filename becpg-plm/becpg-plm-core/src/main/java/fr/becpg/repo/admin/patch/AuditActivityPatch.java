package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.io.Serializable;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;

/**
 * <p>AuditActivityPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AuditActivityPatch extends AbstractBeCPGPatch {

    private static final Log logger = LogFactory.getLog(AuditActivityPatch.class);
    private static final String MSG_SUCCESS = "Success of AuditActivityPtach";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private LockService lockService;
	private BeCPGAuditService beCPGAuditService;
	private EntityService entityService;
	private AssociationService associationService;
	private BehaviourFilter policyBehaviourFilter;
	

    private static final int BATCH_THREADS = Integer.getInteger(
            "becpg.patch.auditActivity.threads", 6);
    private static final int BATCH_SIZE = Integer.getInteger(
            "becpg.patch.auditActivity.batchSize", 35);

	
	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/**
	 * <p>Setter for the field <code>entityService</code>.</p>
	 *
	 * @param entityService a {@link fr.becpg.repo.entity.EntityService} object
	 */
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	
	/**
	 * <p>Setter for the field <code>beCPGAuditService</code>.</p>
	 *
	 * @param beCPGAuditService a {@link fr.becpg.repo.audit.service.BeCPGAuditService} object
	 */
	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}

	/**
	 * <p>Getter for the field <code>nodeDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public NodeDAO getNodeDAO() {
		return nodeDAO;
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
	 * <p>Getter for the field <code>patchDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	/**
	 * <p>Setter for the field <code>patchDAO</code>.</p>
	 *
	 * @param patchDAO a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	/**
	 * <p>Getter for the field <code>qnameDAO</code>.</p>
	 *
	 * @return a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>Getter for the field <code>ruleService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public RuleService getRuleService() {
		return ruleService;
	}


    /**
     * <p>Setter for the field <code>ruleService</code>.</p>
     *
     * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
     */
    public void setRuleService(RuleService ruleService) {
        this.ruleService = ruleService;
    }

    /**
     * <p>Setter for the field <code>lockService</code>.</p>
     *
     * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object
     */
    public void setLockService(LockService lockService) {
        this.lockService = lockService;
    }

    /** {@inheritDoc} */
    @Override
    protected String applyInternal() throws Exception {

        AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

        applyPatch();

        return MSG_SUCCESS;
    }

    private void applyPatch() {
        if (logger.isInfoEnabled()) {
            logger.info("AuditActivityPatch config: threads=" + BATCH_THREADS + ", batchSize=" + BATCH_SIZE);
        }
        BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
            final List<NodeRef> result = new ArrayList<>();

            final long maxNodeId = getNodeDAO().getMaxNodeId();
            final long inc = BATCH_THREADS * BATCH_SIZE * 1L;

            long minSearchNodeId = 0;
            long maxSearchNodeId = inc;

            final Pair<Long, QName> val = getQnameDAO().getQName(BeCPGModel.TYPE_ACTIVITY_LIST);

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
                if (val != null) {
                    Long typeQNameId = val.getFirst();

                    result.clear();

                    while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {

                        List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

                        for (Long nodeid : nodeids) {
                            NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
                            if (!status.isDeleted()) {
                                result.add(status.getNodeRef());
                            }
                        }
                        minSearchNodeId = minSearchNodeId + inc;
                        maxSearchNodeId = maxSearchNodeId + inc;
                    }
                }

                return result;
            }
        };

        BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(AuditActivityPatch.class.getSimpleName(), transactionService.getRetryingTransactionHelper(),
                workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

        BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

            @Override
            public String getIdentifier(NodeRef entry) {
                return entry.toString();
            }

            @Override
            public void process(NodeRef activityNodeRef) throws Throwable {
                
                try {
                    ruleService.disableRules();
                    IntegrityChecker.setWarnInTransaction();
                    policyBehaviourFilter.disableBehaviour();
                    
                    AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
                    
                    if (nodeService.exists(activityNodeRef)) {
                        if (lockService.isLocked(activityNodeRef)) {
                            lockService.unlock(activityNodeRef);
                        }
                        
                        Map<QName, Serializable> props = nodeService.getProperties(activityNodeRef);
                        String created = ISO8601DateFormat.format((Date) props.get(ContentModel.PROP_CREATED));
                        String userId = (String) props.get(BeCPGModel.PROP_ACTIVITYLIST_USERID);
                        Serializable typeProp = props.get(BeCPGModel.PROP_ACTIVITYLIST_TYPE);
                        String activityListType = (typeProp != null) ? typeProp.toString() : null;
                        String activityListData = (String) props.get(BeCPGModel.PROP_ACTIVITYLIST_DATA);
                        
                        for (NodeRef entityNodeRef : associationService.getSourcesAssocs(activityNodeRef, ProjectModel.ASSOC_PROJECT_CUR_COMMENTS)) {
                            JSONObject activityJson = new JSONObject();
                            
                            activityJson.put("prop_cm_created", created);
                            activityJson.put("prop_bcpg_alUserId", userId);
                            activityJson.put("prop_bcpg_alType", activityListType);
                            activityJson.put("prop_bcpg_alData", activityListData);
                            
                            nodeService.setProperty(entityNodeRef, ProjectModel.PROP_PROJECT_CUR_COMMENT, activityJson.toString());
                        }
                        
                        NodeRef entityNodeRef = entityService.getEntityNodeRef(activityNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
                        
                        try (AuditScope auditScope = beCPGAuditService.startAudit(AuditType.ACTIVITY)) {
                            if (entityNodeRef != null) {
                                auditScope.putAttribute("entityNodeRef", entityNodeRef.toString());
                            }
                            
                            auditScope.putAttribute("prop_cm_created", created);
                            auditScope.putAttribute("prop_bcpg_alUserId", userId);
                            auditScope.putAttribute("prop_bcpg_alType", activityListType);
                            auditScope.putAttribute("prop_bcpg_alData", activityListData);
                        }
                        
                        nodeService.addAspect(activityNodeRef, ContentModel.ASPECT_TEMPORARY, null);
                        nodeService.deleteNode(activityNodeRef);
                        
                    } else {
                        logger.warn("activityNodeRef doesn't exist : " + activityNodeRef + " or is not in workspace store");
                    }
                    
                } finally {
                    ruleService.enableRules();
                    policyBehaviourFilter.enableBehaviour();
                }
            }

            @Override
            public void beforeProcess() throws Throwable {
                
            }

            @Override
            public void afterProcess() throws Throwable {
                
            }
        };
        
        batchProcessor.processLong(worker, true);
    
    }
    
}

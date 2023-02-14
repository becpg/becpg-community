package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.audit.model.AuditScope;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.EntityService;

public class AuditActivityPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(AuditActivityPatch.class);
	private static final String MSG_SUCCESS = "Success of AuditActivityPtach";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private LockService lockService;
	private IntegrityChecker integrityChecker;
	private BeCPGAuditService beCPGAuditService;
	private EntityService entityService;
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	
	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}

	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
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
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

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
						minSearchNodeId = minSearchNodeId + INC;
						maxSearchNodeId = maxSearchNodeId + INC;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(AuditActivityPatch.class.getSimpleName(), transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREAD, BATCH_SIZE, applicationEventPublisher, logger, INC);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				ruleService.enableRules();
				integrityChecker.setEnabled(true);
			}

			@Override
			public void beforeProcess() throws Throwable {
				ruleService.disableRules();
				integrityChecker.setEnabled(false);
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef activityNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				if (nodeService.exists(activityNodeRef)) {
					if (lockService.isLocked(activityNodeRef)) {
						lockService.unlock(activityNodeRef);
					}
					
					NodeRef entityNodeRef = entityService.getEntityNodeRef(activityNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
					
					try (AuditScope auditScope = beCPGAuditService.startAudit(AuditType.ACTIVITY)) {
						if (entityNodeRef != null) {
							auditScope.putAttribute("entityNodeRef", entityNodeRef.toString());
						}
						auditScope.putAttribute("prop_cm_created", ISO8601DateFormat.format((Date) nodeService.getProperty(activityNodeRef, ContentModel.PROP_CREATED)));
						auditScope.putAttribute("prop_bcpg_alUserId", nodeService.getProperty(activityNodeRef, BeCPGModel.PROP_ACTIVITYLIST_USERID));
						auditScope.putAttribute("prop_bcpg_alType", nodeService.getProperty(activityNodeRef, BeCPGModel.PROP_ACTIVITYLIST_TYPE).toString());
						auditScope.putAttribute("prop_bcpg_alData", nodeService.getProperty(activityNodeRef, BeCPGModel.PROP_ACTIVITYLIST_DATA));
					}
					
					nodeService.deleteNode(activityNodeRef);
					
				} else {
					logger.warn("activityNodeRef doesn't exist : " + activityNodeRef + " or is not in workspace store");
				}
			}
		};
		
		batchProcessor.processLong(worker, true);
	
	}
	
}

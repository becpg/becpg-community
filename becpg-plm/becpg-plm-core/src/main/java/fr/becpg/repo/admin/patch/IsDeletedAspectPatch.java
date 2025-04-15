package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.model.PublicationModel;

/**
 * Update NutListPatch
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class IsDeletedAspectPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(IsDeletedAspectPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.isDeletedAspectPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private IntegrityChecker integrityChecker;
	private RuleService ruleService;
	private LockService lockService;


	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		doApply(PublicationModel.TYPE_PUBLICATION_CHANNEL);
		doApply(PackModel.TYPE_LABEL);
		doApply(PackModel.TYPE_LABELING_TEMPLATE);
		doApply(GS1Model.TYPE_DUTY_FEE_TAX);
		doApply(GS1Model.TYPE_TARGET_MARKET);
		doApply(PLMModel.TYPE_PLANT);
		doApply(PLMModel.TYPE_COST);
		doApply(PLMModel.TYPE_SUBSIDIARY);
		doApply(PLMModel.TYPE_TRADEMARK);
		doApply(PLMModel.TYPE_CERTIFICATION);
		doApply(PLMModel.TYPE_CONTACTLIST);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doApply(final QName type) {
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(type);

			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && minSearchNodeId < maxNodeId) {

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("IsDeletedAspectPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorkerAdaptor<NodeRef>() {

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			public void process(NodeRef entityNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
				
				policyBehaviourFilter.disableBehaviour();
				ruleService.disableRules();
				integrityChecker.setEnabled(false);

				if (nodeService.exists(entityNodeRef) && entityNodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

					if (lockService.isLocked(entityNodeRef)) {
						lockService.unlock(entityNodeRef);
					}

					logger.info("Updating :" + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
					
					if (!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_DELETED)) {
						nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_DELETED, new HashMap<>());
					}
				} else {
					if (entityNodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
						logger.warn("entityNodeRef doesn't exist : " + entityNodeRef);
					}
				}

				policyBehaviourFilter.enableBehaviour();
				ruleService.enableRules();
				integrityChecker.setEnabled(true);
			}

		};

		batchProcessor.processLong(worker, true);

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
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>nodeDAO</code>.</p>
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
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>patchDAO</code>.</p>
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
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>qnameDAO</code>.</p>
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
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
	 * <p>Setter for the field <code>integrityChecker</code>.</p>
	 *
	 * @param integrityChecker a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object.
	 */
	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

}

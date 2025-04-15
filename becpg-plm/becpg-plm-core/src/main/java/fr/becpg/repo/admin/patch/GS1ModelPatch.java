package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.GS1Model;

/**
 * Update GS1ModelPatch
 *
 * @author Matthieu
 * @version $Id: $Id
 */
public class GS1ModelPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(GS1ModelPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.gs1ModelPatch.result";

	private static final QName ASPECT_GS1_DATES_ASPECT = QName.createQName(GS1Model.GS1_URI, "datesAspect");
	private static final QName ASPECT_GS1_INDICATORS_ASPECT = QName.createQName(GS1Model.GS1_URI, "indicatorsAspect");
	

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private LockService lockService;

	
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

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void applyPatch() {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(GS1Model.ASPECT_GS1_ASPECT);

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

						List<Long> nodeids = getPatchDAO().getNodesByAspectQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>(GS1ModelPatch.class.getSimpleName(), transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				//DO Nothing
			}

			@Override
			public void beforeProcess() throws Throwable {
				//DO Nothing
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef productNodeRef) throws Throwable {
				ruleService.disableRules();

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				if (nodeService.exists(productNodeRef)) {
					if (lockService.isLocked(productNodeRef)) {
						lockService.unlock(productNodeRef);
					}
					
					nodeService.removeAspect(productNodeRef, ASPECT_GS1_DATES_ASPECT);
					nodeService.removeAspect(productNodeRef, ASPECT_GS1_INDICATORS_ASPECT);
					nodeService.addAspect(productNodeRef, GS1Model.ASPECT_PACK_SORTING, new HashMap<>());
				} else {
					logger.warn("productNodeRef doesn't exist : " + productNodeRef + " or is not in workspace store");
				}
				ruleService.enableRules();
			}

		};
		
		batchProcessor.processLong(worker, true);
	

	}

	
}

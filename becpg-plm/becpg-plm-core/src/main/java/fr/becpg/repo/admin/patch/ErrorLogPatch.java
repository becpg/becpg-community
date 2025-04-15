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

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;

/**
 * ErrorLogPatch
 *
 * @author valentin
 * @version $Id: $Id
 */
public class ErrorLogPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ErrorLogPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.errorLogPatch.result";


	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;


	/** {@inheritDoc} */
	@SuppressWarnings("deprecation")
	@Override
	protected String applyInternal() throws Exception {
		apply(PLMModel.TYPE_COSTLIST, QName.createQName(BeCPGModel.BECPG_URI, "costListFormulaErrorLog"));
		apply(PLMModel.TYPE_LCALIST, QName.createQName(BeCPGModel.BECPG_URI, "lcaListFormulaErrorLog"));
		apply(PLMModel.TYPE_NUTLIST, PLMModel.PROP_NUTLIST_FORMULA_ERROR);
		apply(PLMModel.TYPE_PHYSICOCHEMLIST, PLMModel.PROP_PHYSICOCHEMFORMULA_ERROR);
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void apply(QName sourceType, QName prop) {
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(sourceType);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("ErrorLogPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {
				//Do Nothing
			}

			@Override
			public void beforeProcess() throws Throwable {
				//Do Nothing
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {
				
				ruleService.disableRules();
				IntegrityChecker.setWarnInTransaction();

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
				policyBehaviourFilter.disableBehaviour();

				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					nodeService.removeProperty(dataListNodeRef, prop);

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
				ruleService.enableRules();
			}

		};

		batchProcessor.processLong(worker, true);

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

}

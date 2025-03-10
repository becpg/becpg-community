package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
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
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;

/**
 * Add mandatory effectivity aspect
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class AddEffectivityAspectPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(AddEffectivityAspectPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.addEffectivityAspectPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		for (QName listQName : Arrays.asList(PLMModel.TYPE_COMPOLIST, PLMModel.TYPE_PACKAGINGLIST, MPMModel.TYPE_PROCESSLIST)) {

			BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
				final List<NodeRef> result = new ArrayList<>();

				final long maxNodeId = getNodeDAO().getMaxNodeId();

				long minSearchNodeId = 0;

				final Pair<Long, QName> val = getQnameDAO().getQName(listQName);

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
							List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, minSearchNodeId + INC);

							for (Long nodeid : nodeids) {
								NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
								if (!status.isDeleted()) {
									result.add(status.getNodeRef());
								}
							}
							minSearchNodeId = minSearchNodeId + INC;
						}
					}

					return result;
				}
			};

			BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("AddEffectivityAspectPatch",
					transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger,
					500);

			BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<>() {

				@Override
				public void afterProcess() throws Throwable {
					//Do nothing

				}

				@Override
				public void beforeProcess() throws Throwable {
					//Do nothing
				}

				@Override
				public String getIdentifier(NodeRef entry) {
					return entry.toString();
				}

				@Override
				public void process(NodeRef dataListNodeRef) throws Throwable {
					if (nodeService.exists(dataListNodeRef) && !nodeService.hasAspect(dataListNodeRef, BeCPGModel.ASPECT_EFFECTIVITY)) {
						ruleService.disableRules();
						AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
						policyBehaviourFilter.disableBehaviour();
						nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_EFFECTIVITY, new HashMap<>());
						ruleService.enableRules();
					}
				}

			};
			batchProcessor.processLong(worker, true);

		}

		return I18NUtil.getMessage(MSG_SUCCESS);

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
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

}

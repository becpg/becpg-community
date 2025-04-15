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
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ECMModel;
import fr.becpg.repo.ecm.data.ChangeOrderType;

/**
 * Change Automatic Eco Type
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ECOTypePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ECOTypePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.ecoTypePatch.result";

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

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = getQnameDAO().getQName(ECMModel.TYPE_ECO);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("ECOTypePatch", transactionService.getRetryingTransactionHelper(), workProvider,
				BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

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
				ruleService.disableRules();
				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					String oldType = (String) nodeService.getProperty(dataListNodeRef, ECMModel.PROP_ECO_TYPE);
					if ((oldType != null)
							&& !(ChangeOrderType.Replacement.toString().equals(oldType) || ChangeOrderType.Merge.toString().equals(oldType))) {
						nodeService.setProperty(dataListNodeRef, ECMModel.PROP_ECO_TYPE, ChangeOrderType.Replacement.toString());
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
				ruleService.enableRules();
			}

		};
		batchProcessor.processLong(worker, true);
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

}

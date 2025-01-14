package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
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
 * <p>ApprovalNumbersPatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ApprovalNumbersPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(ApprovalNumbersPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.approvalNumbersPatch.result";

	private QName ASSOC_PLANT_APPROVAL_NUMBERS = QName.createQName(BeCPGModel.BECPG_URI, "plantApprovalNumbers");

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;

	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {

			final List<NodeRef> result = new ArrayList<>();

			final Pair<Long, QName> val = qnameDAO.getQName(PLMModel.TYPE_PLANT);

			final long maxNodeId = nodeDAO.getMaxNodeId();
			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

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

						List<Long> nodeids = patchDAO.getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = nodeDAO.getNodeIdStatus(nodeid);
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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("ApprovalNumbersPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

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
				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
				policyBehaviourFilter.disableBehaviour();

				List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(dataListNodeRef, ASSOC_PLANT_APPROVAL_NUMBERS);

				if (!targetAssocs.isEmpty()) {

					String approvalNumberName = "";

					for (AssociationRef assoc : targetAssocs) {
						if (assoc.getTypeQName().equals(ASSOC_PLANT_APPROVAL_NUMBERS)) {
							NodeRef approvalNumberNodeRef = assoc.getTargetRef();
							approvalNumberName += (approvalNumberName.isEmpty() ? "" : ", ")
									+ (String) nodeService.getProperty(approvalNumberNodeRef, ContentModel.PROP_NAME);
						}
					}

					logger.debug("Setting approval number prop for node " + dataListNodeRef + " to \"" + approvalNumberName + "\"");
					nodeService.setProperty(dataListNodeRef, ASSOC_PLANT_APPROVAL_NUMBERS, approvalNumberName);
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

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

}

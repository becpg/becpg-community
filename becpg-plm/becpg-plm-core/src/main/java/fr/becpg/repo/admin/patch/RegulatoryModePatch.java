package fr.becpg.repo.admin.patch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.regulatory.decernis.RegulatoryMode;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RegulatoryModePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(RegulatoryModePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.regulatoryModePatch.result";
	public static final QName PROP_REGULATORY_MODE = QName.createQName(BeCPGModel.BECPG_URI, "regulatoryMode");

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private IntegrityChecker integrityChecker;

	@Override
	protected String applyInternal() throws Exception {
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			final Pair<Long, QName> val = getQnameDAO().getQName(PLMModel.ASPECT_REGULATORY);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("RegulatoryModePatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

		BatchProcessor.BatchProcessWorker<NodeRef> worker = new BatchProcessor.BatchProcessWorker<>() {

			@Override
			public void afterProcess() throws Throwable {/*no op*/}

			@Override
			public void beforeProcess() throws Throwable {/*no op*/}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef nodeRef) throws Throwable {
				ruleService.disableRules();

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
				policyBehaviourFilter.disableBehaviour();

				if (nodeService.exists(nodeRef)) {
					String result = (String) nodeService.getProperty(nodeRef, PROP_REGULATORY_MODE);

					if ("BECPG_ONLY".equals(result)) {
						nodeService.setProperty(nodeRef, PROP_REGULATORY_MODE, RegulatoryMode.DECERNIS_BECPG_ONLY);
					} else if ("BOTH".equals(result)) {
						nodeService.setProperty(nodeRef, PROP_REGULATORY_MODE, RegulatoryMode.DECERNIS_BOTH);
					}
				} else {
					logger.warn("nodeRef doesn't exist : " + nodeRef);
				}
				ruleService.enableRules();
			}
		};

		getIntegrityChecker().setEnabled(false);
		try {
			batchProcessor.processLong(worker, true);
		} finally {
			getIntegrityChecker().setEnabled(true);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	@Override
	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	@Override
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	@Override
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	@Override
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	@Override
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	@Override
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	public BehaviourFilter getPolicyBehaviourFilter() {
		return policyBehaviourFilter;
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

	public IntegrityChecker getIntegrityChecker() {
		return integrityChecker;
	}

	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}
}
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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.productList.DynamicCharactSynchronisableState;

/**
 * Remove isManual form dynCharact
 * 
 * @author matthieu
 * 
 */
public class DynCharactPatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(DynCharactPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.dynCharactPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;

	
	
	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<NodeRef>();

			long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 1;
			long maxSearchNodeId = count;

			Pair<Long, QName> val = getQnameDAO().getQName(PLMModel.TYPE_DYNAMICCHARACTLIST);

			public int getTotalEstimatedWorkSize() {
				return result.size();
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
						minSearchNodeId = minSearchNodeId + count;
						maxSearchNodeId = maxSearchNodeId + count;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>("DynCharactPatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			public void afterProcess() throws Throwable {
			}

			public void beforeProcess() throws Throwable {
			}

			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			public void process(NodeRef dataListNodeRef) throws Throwable {
				if (nodeService.exists(dataListNodeRef)) {
					try {
						policyBehaviourFilter.disableBehaviour(dataListNodeRef);
						ruleService.disableRules();
						Boolean isManual = (Boolean) nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM);
						if (Boolean.TRUE.equals(isManual)) {
							nodeService.setProperty(dataListNodeRef, PLMModel.PROP_DYNAMICCHARACT_SYNCHRONIZABLE_STATE,
									DynamicCharactSynchronisableState.Manual);
						} else {
							nodeService.setProperty(dataListNodeRef, PLMModel.PROP_DYNAMICCHARACT_SYNCHRONIZABLE_STATE,
									DynamicCharactSynchronisableState.Synchronized);
						}
						nodeService.removeAspect(dataListNodeRef, BeCPGModel.ASPECT_IS_MANUAL_LISTITEM);
					} finally {
						policyBehaviourFilter.enableBehaviour(dataListNodeRef);
						ruleService.enableRules();
					}
				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
			}

		};

		batchProcessor.process(worker, true);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
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
	
	

}

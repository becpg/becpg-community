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
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;

/**
 * Update NutListPatch
 * 
 * @author matthieu
 * 
 */
public class NutListValuePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(NutListValuePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.nutListValuePatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private IntegrityChecker integrityChecker;
	private RuleService ruleService;
	private EntityListDAO entityListDAO;

	private final int batchThreads = 4;
	private final int batchSize = 30;
	private final long count = batchThreads * batchSize;

	@Override
	protected String applyInternal() throws Exception {

		doApply(PLMModel.TYPE_FINISHEDPRODUCT);

		doApply(PLMModel.TYPE_SEMIFINISHEDPRODUCT);

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doApply(final QName type) {
		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 1;
			long maxSearchNodeId = count;

			final Pair<Long, QName> val = getQnameDAO().getQName(type);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("NutListValuePatch", transactionService.getRetryingTransactionHelper(), workProvider, batchThreads, batchSize,
				applicationEventPublisher, logger, 500);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			public void afterProcess() throws Throwable {
				ruleService.disableRules();
			}

			public void beforeProcess() throws Throwable {
				ruleService.enableRules();
			}

			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			public void process(NodeRef entityNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				if (nodeService.exists(entityNodeRef) &&  entityNodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)
						) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

					logger.debug("Updating :" + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));

					NodeRef entityContainer = entityListDAO.getListContainer(entityNodeRef);
					if (entityContainer != null) {
						NodeRef listNodeRef = entityListDAO.getList(entityContainer, PLMModel.TYPE_NUTLIST);
						if (listNodeRef != null) {
							List<NodeRef> nuts = entityListDAO.getListItems(listNodeRef, PLMModel.TYPE_NUTLIST);
							for (NodeRef dataListNodeRef : nuts) {

								if (!Boolean.TRUE.equals(nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM))) {

									policyBehaviourFilter.disableBehaviour();
									nodeService.setProperty(dataListNodeRef, PLMModel.PROP_NUTLIST_FORMULATED_VALUE, nodeService.getProperty(dataListNodeRef, PLMModel.PROP_NUTLIST_VALUE));
									nodeService.setProperty(dataListNodeRef, PLMModel.PROP_NUTLIST_VALUE, null);
									policyBehaviourFilter.enableBehaviour();

								}
							}
						}
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + entityNodeRef);
				}

			}

		};

		integrityChecker.setEnabled(false);
		try {
			batchProcessor.process(worker, true);
		} finally {
			integrityChecker.setEnabled(true);
		}

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

	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

}

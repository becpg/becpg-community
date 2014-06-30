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
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;

/**
 * Copy prop value of cm:name in bcpg:lvValue to support all char
 * 
 * @author quere
 * 
 */
public class ListValuePatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(ListValuePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.listValuePatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	
	

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;
	
	

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	@Override
	protected String applyInternal() throws Exception {

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<NodeRef>();

			long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 1;
			long maxSearchNodeId = count;

			Pair<Long, QName> val = getQnameDAO().getQName(BeCPGModel.TYPE_LIST_VALUE);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<NodeRef>("ListValuePatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			public void afterProcess() throws Throwable {
				ruleService.enableRules();
				
			}

			public void beforeProcess() throws Throwable {
				ruleService.disableRules();
			}

			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			public void process(NodeRef dataListNodeRef) throws Throwable {
				if (nodeService.exists(dataListNodeRef)) {
					AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
					policyBehaviourFilter.disableBehaviour();
					String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
					Boolean isDeleted = (Boolean) nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_IS_DELETED);
					if (name != null) {
						nodeService.setProperty(dataListNodeRef, ContentModel.PROP_NAME, name.replaceAll("\\?", ""));
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_LV_VALUE, name);
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_IS_DELETED, isDeleted != null ? isDeleted : false);
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}
			}

		};

		// Now set the batch processor to work

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

}

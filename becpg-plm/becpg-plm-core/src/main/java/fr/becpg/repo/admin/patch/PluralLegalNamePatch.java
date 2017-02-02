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
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.MLText;
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
 * Copy prop value of ingTypePluralLegalName in pluralLegalName to support mlText
 * 
 * @author matthieu
 * 
 */
public class PluralLegalNamePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(PluralLegalNamePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.pluralLegalNamePatch.result";
	
	QName PROP_ING_TYPE_LEGAL_NAME_PLURAL = QName.createQName(BeCPGModel.BECPG_URI, "ingTypePluralLegalName");

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
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = getQnameDAO().getQName(PLMModel.TYPE_ING_TYPE_ITEM);

			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && minSearchNodeId < maxNodeId) {
						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, minSearchNodeId + count);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + count;
					}
				}

				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("pluralLegalNamePatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 500);

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
					MLPropertyInterceptor.setMLAware(true);
					try {
						MLText ingTypePluralLegalName = (MLText) nodeService.getProperty(dataListNodeRef, PROP_ING_TYPE_LEGAL_NAME_PLURAL);
						nodeService.setProperty(dataListNodeRef, PLMModel.PROP_PLURAL_LEGAL_NAME, ingTypePluralLegalName);
						nodeService.removeProperty(dataListNodeRef, PROP_ING_TYPE_LEGAL_NAME_PLURAL);
					
					} finally {
						MLPropertyInterceptor.setMLAware(false);
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

}

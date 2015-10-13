package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;

/**
 * Copy prop value of cm:name in bcpg:charactName to support mlText
 * 
 * @author matthieu
 * 
 */
public class CharactNamePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(CharactNamePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.charactNamePatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private IntegrityChecker integrityChecker;
	private DictionaryService dictionaryService;
	
	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = batchThreads * batchSize;
	
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		for(QName toApplyType : dictionaryService.getSubTypes(BeCPGModel.TYPE_CHARACT, true)){			
			doApply(toApplyType);
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doApply(QName toApplyType) {
        logger.info("Applying patch for: "+toApplyType);

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = getQnameDAO().getQName(toApplyType);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("CharactNamePatch", transactionService.getRetryingTransactionHelper(),
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
					String name = (String) nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
					String charactName = (String) nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_CHARACT_NAME);
					Boolean isDeleted = (Boolean) nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_IS_DELETED);
					if (name != null && (charactName == null || charactName.isEmpty())) {
						
						nodeService.setProperty(dataListNodeRef, ContentModel.PROP_NAME, "patched-"+ name.replaceAll("\\?", "").replace(" ","_"));
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_CHARACT_NAME, name);
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_IS_DELETED, isDeleted != null ? isDeleted : false);
						
						if(!nodeService.hasAspect(dataListNodeRef, BeCPGModel.ASPECT_LEGAL_NAME)){
							nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_LEGAL_NAME, new HashMap<QName, Serializable>());
						}
					}

				} else {
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
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

}

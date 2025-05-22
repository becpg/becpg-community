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
 * @version $Id: $Id
 */
public class CharactNamePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(CharactNamePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.charactNamePatch.result";


	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private IntegrityChecker integrityChecker;
	private DictionaryService dictionaryService;
	
	
	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>integrityChecker</code>.</p>
	 *
	 * @param integrityChecker a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object.
	 */
	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

	/** {@inheritDoc} */
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

			final long maxNodeId = nodeDAO.getMaxNodeId();

			long minSearchNodeId = 0;

			final Pair<Long, QName> val = qnameDAO.getQName(toApplyType);

			public int getTotalEstimatedWorkSize() {
				return result.size();
			}
			
			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}

			public Collection<NodeRef> getNextWork() {
				if (val != null) {
					Long typeQNameId = val.getFirst();

					result.clear();

					while (result.isEmpty() && minSearchNodeId < maxNodeId) {
						List<Long> nodeids = patchDAO.getNodesByTypeQNameId(typeQNameId, minSearchNodeId, minSearchNodeId + INC);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = nodeDAO.getNodeIdStatus(nodeid);
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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("CharactNamePatch", transactionService.getRetryingTransactionHelper(),
				workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 500);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			public void afterProcess() throws Throwable {
				
				//Do Nothing
			}

			public void beforeProcess() throws Throwable {
				//Do Nothing
			}

			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			public void process(NodeRef dataListNodeRef) throws Throwable {
				ruleService.disableRules();
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
				ruleService.enableRules();
			}

		};
		integrityChecker.setEnabled(false);
		try {
			batchProcessor.processLong(worker, true);
		} finally {
			integrityChecker.setEnabled(true);
		}

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

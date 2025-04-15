package fr.becpg.repo.admin.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;

/**
 * Update CostParentLevel
 *
 * @author Philippe
 * @version $Id: $Id
 */
public class CostParentLevelPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(CostParentLevelPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.costParentLevelPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private BehaviourFilter policyBehaviourFilter;
	private RuleService ruleService;
	private IntegrityChecker integrityChecker;

	


	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

			AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

			BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
				final List<NodeRef> result = new ArrayList<>();

				final long maxNodeId = getNodeDAO().getMaxNodeId();

				long minSearchNodeId = 0;
				long maxSearchNodeId = INC;

				final Pair<Long, QName> val = getQnameDAO().getQName(PLMModel.TYPE_COSTLIST);

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
							
							
							List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

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

			BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("CostParentLevelPatch",
					transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS, BATCH_SIZE, applicationEventPublisher, logger, 1000);

			BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

				public void afterProcess() throws Throwable {
													
				}

				public void beforeProcess() throws Throwable {
										
				}

				public String getIdentifier(NodeRef entry) {
					return entry.toString();
				}

				public void process(NodeRef dataListNodeRef) throws Throwable {									
					ruleService.disableRules();
					if (nodeService.exists(dataListNodeRef)) {
						AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
						if(!nodeService.hasAspect(dataListNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)){
							try{
								policyBehaviourFilter.disableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
								Map<QName, Serializable> properties = new HashMap<>();
								properties.put(BeCPGModel.PROP_DEPTH_LEVEL, 1);
								if(nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_IS_MANUAL_LISTITEM)==null){
									properties.put(BeCPGModel.PROP_IS_MANUAL_LISTITEM,false);
								}
								
								nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL, properties);
							}
							finally{
								policyBehaviourFilter.enableBehaviour(BeCPGModel.ASPECT_DEPTH_LEVEL);
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

	/**
	 * <p>Getter for the field <code>ruleService</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public RuleService getRuleService() {
		return ruleService;
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

}

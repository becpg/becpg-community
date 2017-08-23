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
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.PLMModel;

public class ApprovalNumbersPatch extends AbstractBeCPGPatch {
	
	private static final Log logger = LogFactory.getLog(ApprovalNumbersPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.ingTypePatch.result";
	
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

			final List<NodeRef> result = new ArrayList<>();
			
			final Pair<Long, QName> val = qnameDAO.getQName(PLMModel.TYPE_PLANT);
			
			final long maxNodeId = nodeDAO.getMaxNodeId();
			long minSearchNodeId = 0;
			long maxSearchNodeId = count;
			
			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
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
						minSearchNodeId = minSearchNodeId + count;
						maxSearchNodeId = maxSearchNodeId + count;
					}
				}

				return result;
			}
			
		};
		
		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("IngTypePatch", transactionService.getRetryingTransactionHelper(),
				workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 500);
		
		BatchProcessWorker<NodeRef> worker = new BatchProcessWorker<NodeRef>() {

			@Override
			public void afterProcess() throws Throwable {
				ruleService.disableRules();
			}

			@Override
			public void beforeProcess() throws Throwable {
				ruleService.enableRules();
			}

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef dataListNodeRef) throws Throwable {

				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
				policyBehaviourFilter.disableBehaviour();

				List<AssociationRef> targetAssocs = nodeService.getTargetAssocs(dataListNodeRef, PLMModel.ASSOC_PLANT_APPROVAL_NUMBERS);
				
				if(!targetAssocs.isEmpty()){
					
					String approvalNumberName = ""; 
					
					for(AssociationRef assoc : targetAssocs){
						if(assoc.getTypeQName().equals(PLMModel.ASSOC_PLANT_APPROVAL_NUMBERS)){
							NodeRef approvalNumberNodeRef = assoc.getTargetRef();
							approvalNumberName += (approvalNumberName.isEmpty() ? "" : ", " ) + 
									(String) nodeService.getProperty(approvalNumberNodeRef, ContentModel.PROP_NAME);
						}
					}
					
					logger.debug("Setting approval number prop for node "+dataListNodeRef+" to \""+approvalNumberName+"\"");
					nodeService.setProperty(dataListNodeRef, PLMModel.ASSOC_PLANT_APPROVAL_NUMBERS, approvalNumberName);
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
	
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

}

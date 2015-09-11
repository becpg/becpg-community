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
import org.alfresco.repo.node.integrity.IntegrityChecker;
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

/**
 * Update CostParentLevel
 * 
 * @author Philippe
 * 
 */
public class FormulatedAspectPatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(FormulatedAspectPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.formulatedAspectPatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;

	private IntegrityChecker integrityChecker;

	private final int batchThreads = 3;
	private final int batchSize = 40;
	private final long count = 10000;

	@Override
	protected String applyInternal() throws Exception {

			AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();
			
			doForType(PLMModel.TYPE_PACKAGINGMATERIAL);
			doForType(PLMModel.TYPE_RAWMATERIAL);
			doForType(PLMModel.TYPE_RESOURCEPRODUCT);

		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

	private void doForType(final QName type) {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getPatchDAO().getMaxAdmNodeID();

			long minSearchNodeId = 0;
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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("FormulatedAspectPatch",
				transactionService.getRetryingTransactionHelper(), workProvider, batchThreads, batchSize, applicationEventPublisher, logger, 1000);

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

			public void process(NodeRef productNodeRef) throws Throwable {
				
				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();				
				
				if (nodeService.exists(productNodeRef) && productNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE) ) {
					if(!nodeService.hasAspect(productNodeRef, BeCPGModel.ASPECT_FORMULATED_ENTITY)){
						nodeService.addAspect(productNodeRef, BeCPGModel.ASPECT_FORMULATED_ENTITY, null);						
					}
					if(!nodeService.hasAspect(productNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF)){
						nodeService.addAspect(productNodeRef, BeCPGModel.ASPECT_ENTITY_TPL_REF, null);						
					}
					// nodeService.removeAspect(productNodeRef, QName.createQName(BeCPGModel.BECPG_URI, "entityVersionable"));
				} else {
					logger.warn("productNodeRef doesn't exist : " + productNodeRef +" or is not in workspace store");
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

	public RuleService getRuleService() {
		return ruleService;
	}

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}
	

	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}
	

}

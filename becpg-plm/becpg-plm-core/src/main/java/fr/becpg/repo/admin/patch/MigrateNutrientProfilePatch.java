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
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;

public class MigrateNutrientProfilePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(MigrateNutrientProfilePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.MigrateNutrientProfilePatch.result";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private BehaviourFilter policyBehaviourFilter;

	private static final int BATCH_THREADS = 3;
	private static final int BATCH_SIZE = 40;
	private static final long COUNT = BATCH_THREADS * (long) BATCH_SIZE;

	/**
	 * <p>
	 * Getter for the field <code>nodeDAO</code>.
	 * </p>
	 *
	 * @return a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public NodeDAO getNodeDAO() {
		return nodeDAO;
	}

	/**
	 * <p>
	 * Setter for the field <code>nodeDAO</code>.
	 * </p>
	 *
	 * @param nodeDAO a {@link org.alfresco.repo.domain.node.NodeDAO} object.
	 */
	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	/**
	 * <p>
	 * Getter for the field <code>patchDAO</code>.
	 * </p>
	 *
	 * @return a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public PatchDAO getPatchDAO() {
		return patchDAO;
	}

	/**
	 * <p>
	 * Setter for the field <code>patchDAO</code>.
	 * </p>
	 *
	 * @param patchDAO a {@link org.alfresco.repo.domain.patch.PatchDAO} object.
	 */
	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	/**
	 * <p>
	 * Getter for the field <code>qnameDAO</code>.
	 * </p>
	 *
	 * @return a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public QNameDAO getQnameDAO() {
		return qnameDAO;
	}

	/**
	 * <p>
	 * Setter for the field <code>qnameDAO</code>.
	 * </p>
	 *
	 * @param qnameDAO a {@link org.alfresco.repo.domain.qname.QNameDAO} object.
	 */
	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	/**
	 * <p>
	 * Setter for the field <code>ruleService</code>.
	 * </p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService}
	 *                    object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>
	 * Setter for the field <code>policyBehaviourFilter</code>.
	 * </p>
	 *
	 * @param policyBehaviourFilter a
	 *                              {@link org.alfresco.repo.policy.BehaviourFilter}
	 *                              object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = COUNT;

			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			public Collection<NodeRef> getNextWork() {

				result.clear();
				
				if (getQnameDAO().getQName(PLMModel.TYPE_NUTRIENT_PROFILE) != null) {
					while (result.isEmpty() && minSearchNodeId < maxNodeId) {

						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(getQnameDAO().getQName(PLMModel.TYPE_NUTRIENT_PROFILE).getFirst(), minSearchNodeId, maxSearchNodeId);

						for (Long nodeid : nodeids) {
							NodeRef.Status status = getNodeDAO().getNodeIdStatus(nodeid);
							if (!status.isDeleted()) {
								result.add(status.getNodeRef());
							}
						}
						minSearchNodeId = minSearchNodeId + COUNT;
						maxSearchNodeId = maxSearchNodeId + COUNT;
					}
				}
				
				return result;
			}
		};

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("MigrateNutrientProfilePatch",
				transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS, BATCH_SIZE,
				applicationEventPublisher, logger, 1000);

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

			public void process(NodeRef nutrientProfile) throws Throwable {
				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				policyBehaviourFilter.disableBehaviour();
				
				List<AssociationRef> sourceAssocs = nodeService.getSourceAssocs(nutrientProfile, PLMModel.ASSOC_NUTRIENT_PROFILE_REF);
				
				if (sourceAssocs != null) {
					
					String nutrientProfileClass = (String) nodeService.getProperty(nutrientProfile, BeCPGModel.PROP_CHARACT_NAME);
					
					for (AssociationRef sourceAssoc : sourceAssocs) {
						
						if (nutrientProfileClass.contains("Others")) {
							nodeService.setProperty(sourceAssoc.getSourceRef(), PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Others.toString());
						} else if (nutrientProfileClass.contains("Beverages")) {
							nodeService.setProperty(sourceAssoc.getSourceRef(), PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Beverages.toString());
						} else if (nutrientProfileClass.contains("Fats")) {
							nodeService.setProperty(sourceAssoc.getSourceRef(), PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Fats.toString());
						} else if (nutrientProfileClass.contains("Cheeses")) {
							nodeService.setProperty(sourceAssoc.getSourceRef(), PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Cheeses.toString());
						}
						
						nodeService.removeAssociation(sourceAssoc.getSourceRef(), nutrientProfile, PLMModel.ASSOC_NUTRIENT_PROFILE_REF);
					}
				}
				
				nodeService.deleteNode(nutrientProfile);
				
				policyBehaviourFilter.enableBehaviour();
			}

		};

		batchProcessor.process(worker, true);
		
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		
		NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
		
		NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
		
		NodeRef entityListNodeRef = repoService.getFolderByPath(charactsNodeRef, "bcpg:entityLists");;
		
		NodeRef nutrientProfilesCategoryNodeRef = repoService.getFolderByPath(entityListNodeRef, PlmRepoConsts.PATH_NUTRIENTPROFILES);

		if (nutrientProfilesCategoryNodeRef != null && nodeService.exists(nutrientProfilesCategoryNodeRef)) {
			nodeService.deleteNode(nutrientProfilesCategoryNodeRef);
		}

		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}

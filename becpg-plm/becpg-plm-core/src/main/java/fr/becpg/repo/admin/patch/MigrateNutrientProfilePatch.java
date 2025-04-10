package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.domain.node.NodeDAO;
import org.alfresco.repo.domain.patch.PatchDAO;
import org.alfresco.repo.domain.qname.QNameDAO;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.NutrientProfileCategory;
import fr.becpg.model.NutrientProfileVersion;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.AssociationService;

/**
 * <p>MigrateNutrientProfilePatch class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MigrateNutrientProfilePatch extends AbstractBeCPGPatch {

	private static final Log logger = LogFactory.getLog(MigrateNutrientProfilePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.plm.MigrateNutrientProfilePatch.result";
	
	private static final  QName TYPE_NUTRIENT_PROFILE = QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfile");
	
	private static final  QName ASSOC_NUTRIENT_PROFILE_REF = QName.createQName(BeCPGModel.BECPG_URI, "nutrientProfileRef");
	
	private static final String PATH_NUTRIENTPROFILES = "NutrientProfiles";

	private NodeDAO nodeDAO;
	private PatchDAO patchDAO;
	private QNameDAO qnameDAO;
	private RuleService ruleService;
	private BehaviourFilter policyBehaviourFilter;
	private IntegrityChecker integrityChecker;
	private AssociationService associationService;
	
	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}
	
	/**
	 * <p>Setter for the field <code>integrityChecker</code>.</p>
	 *
	 * @param integrityChecker a {@link org.alfresco.repo.node.integrity.IntegrityChecker} object
	 */
	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}

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
	 * {@inheritDoc}
	 *
	 * <p>
	 * Setter for the field <code>nodeDAO</code>.
	 * </p>
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
	 * {@inheritDoc}
	 *
	 * <p>
	 * Setter for the field <code>patchDAO</code>.
	 * </p>
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
	 * {@inheritDoc}
	 *
	 * <p>
	 * Setter for the field <code>qnameDAO</code>.
	 * </p>
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

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {

		AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<NodeRef>() {
			final List<NodeRef> result = new ArrayList<>();

			final long maxNodeId = getNodeDAO().getMaxNodeId();

			long minSearchNodeId = 0;
			long maxSearchNodeId = INC;

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}
			
			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}
			
			public Collection<NodeRef> getNextWork() {

				result.clear();
				
				if (getQnameDAO().getQName(TYPE_NUTRIENT_PROFILE) != null) {
					while (result.isEmpty() && minSearchNodeId < maxNodeId) {

						List<Long> nodeids = getPatchDAO().getNodesByTypeQNameId(getQnameDAO().getQName(TYPE_NUTRIENT_PROFILE).getFirst(), minSearchNodeId, maxSearchNodeId);

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

		BatchProcessor<NodeRef> batchProcessor = new BatchProcessor<>("MigrateNutrientProfilePatch",
				transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS, BATCH_SIZE,
				applicationEventPublisher, logger, 1000);

		BatchProcessWorker<NodeRef> worker = new BatchProcessWorkerAdaptor<NodeRef>() {

			@Override
			public String getIdentifier(NodeRef entry) {
				return entry.toString();
			}

			@Override
			public void process(NodeRef nutrientProfile) throws Throwable {
				
				AuthenticationUtil.setAdminUserAsFullyAuthenticatedUser();

				ruleService.disableRules();
				policyBehaviourFilter.disableBehaviour();
				
				List<NodeRef> sourceAssocs = associationService.getSourcesAssocs(nutrientProfile, ASSOC_NUTRIENT_PROFILE_REF);
				
				logger.info("identified nutrient profile to migrate : " + nutrientProfile);
				
				if (sourceAssocs != null) {
					
					String nutrientProfileClass = (String) nodeService.getProperty(nutrientProfile, BeCPGModel.PROP_CHARACT_NAME);
					
					boolean nutrientProfileClassKnown = nutrientProfileClass != null 
							&& (nutrientProfileClass.contains("Others") ||
									nutrientProfileClass.contains("Autres") 
									|| nutrientProfileClass.contains("Beverages") 
									|| nutrientProfileClass.contains("Boissons") 
									|| nutrientProfileClass.contains("Fats") || nutrientProfileClass.contains("Matières grasses")
									|| nutrientProfileClass.contains("Cheeses")
									|| nutrientProfileClass.contains("Fromages")
									|| nutrientProfileClass.contains("Red meats") || nutrientProfileClass.contains("Viandes rouges"));
					
					if (nutrientProfileClassKnown) {
						for (NodeRef sourceAssoc : sourceAssocs) {
							if (nutrientProfileClass.contains("Others") || nutrientProfileClass.contains("Autres")) {
								nodeService.setProperty(sourceAssoc, PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Others.toString());
							} else if (nutrientProfileClass.contains("Beverages") || nutrientProfileClass.contains("Boissons")) {
								nodeService.setProperty(sourceAssoc, PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Beverages.toString());
							} else if (nutrientProfileClass.contains("Fats") || nutrientProfileClass.contains("Matières grasses")) {
								nodeService.setProperty(sourceAssoc, PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Fats.toString());
							} else if (nutrientProfileClass.contains("Cheeses") || nutrientProfileClass.contains("Fromages")) {
								nodeService.setProperty(sourceAssoc, PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.Cheeses.toString());
							} else if (nutrientProfileClass.contains("Red meats") || nutrientProfileClass.contains("Viandes rouges")) {
								nodeService.setProperty(sourceAssoc, PLMModel.PROP_NUTRIENT_PROFILE_CATEGORY, NutrientProfileCategory.RedMeats.toString());
							}
							
							if (nutrientProfileClass.contains(NutrientProfileVersion.VERSION_2023.toString())) {
								nodeService.setProperty(sourceAssoc, PLMModel.PROP_NUTRIENT_PROFILE_VERSION, NutrientProfileVersion.VERSION_2023.toString());
							}
							
							nodeService.removeAssociation(sourceAssoc, nutrientProfile, ASSOC_NUTRIENT_PROFILE_REF);
						}
						nodeService.deleteNode(nutrientProfile);
					} else {
						logger.info("unknown nutrient profile class : " + nutrientProfileClass);
					}
				}
				
				policyBehaviourFilter.enableBehaviour();
				ruleService.enableRules();
			}

		};

		integrityChecker.setEnabled(false);
		try {
			batchProcessor.processLong(worker, true);
		} finally {
			integrityChecker.setEnabled(true);
		}
		
		NodeRef companyHomeNodeRef = repository.getCompanyHome();
		if (companyHomeNodeRef != null) {
			NodeRef systemNodeRef = repoService.getFolderByPath(companyHomeNodeRef, RepoConsts.PATH_SYSTEM);
			if (systemNodeRef != null) {
				NodeRef charactsNodeRef = repoService.getFolderByPath(systemNodeRef, RepoConsts.PATH_CHARACTS);
				if (charactsNodeRef != null) {
					NodeRef entityListNodeRef = repoService.getFolderByPath(charactsNodeRef, "bcpg:entityLists");
					if (entityListNodeRef != null) {
						NodeRef nutrientProfilesCategoryNodeRef = repoService.getFolderByPath(entityListNodeRef, PATH_NUTRIENTPROFILES);
						if (nutrientProfilesCategoryNodeRef != null && nodeService.exists(nutrientProfilesCategoryNodeRef)) {
							if (nodeService.getChildAssocs(nutrientProfilesCategoryNodeRef).isEmpty()) {
								nodeService.addAspect(nutrientProfilesCategoryNodeRef, ContentModel.ASPECT_TEMPORARY, null);
								nodeService.deleteNode(nutrientProfilesCategoryNodeRef);
								logger.info("deleting nutrient profile list: " + nutrientProfilesCategoryNodeRef);
							}
						}
					}
				}
			}
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}

package fr.becpg.repo.project.admin.patch;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorkerAdaptor;
import org.alfresco.repo.node.MLPropertyInterceptor;
import org.alfresco.repo.node.integrity.IntegrityChecker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.tenant.TenantUtil;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.ProjectRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.admin.patch.AbstractBeCPGPatch;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.impl.AbstractBeCPGQueryBuilder;
import fr.becpg.repo.survey.SurveyModel;

/**
 * <p>ScoreListPatch class.</p>
 *
 * @author matthieu
 */
public class ScoreListPatch extends AbstractBeCPGPatch {
	private static final Log logger = LogFactory.getLog(ScoreListPatch.class);

	/** Constant <code>QUESTION_CRITERION</code> */
	protected static final QName QUESTION_CRITERION = QName.createQName(SurveyModel.SURVEY_URI, "questionCriterion");

	private EntityListDAO entityListDAO;

	private BehaviourFilter policyBehaviourFilter;

	private RuleService ruleService;

	private LockService lockService;

	private AssociationService associationService;

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/** {@inheritDoc} */
	@Override
	protected String applyInternal() throws Exception {
		return migrateScoreList();
	}

	/**
	 * <p>migrateScoreList.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String migrateScoreList() {
		
		logger.info("Current tenant: " + TenantUtil.getCurrentDomain());

		String currentTenant = TenantUtil.getCurrentDomain();
		
		IntegrityChecker.setWarnInTransaction();
		return AuthenticationUtil.runAsSystem(() -> {
			NodeRef scoreCriteriaFolder = findScoreCriteriaFolder();
			if (scoreCriteriaFolder == null) {
				logger.warn("Score Criteria folder not found");
				return "No changes applied";
			}
			
			ruleService.disableRules();
			policyBehaviourFilter.disableBehaviour();
			IntegrityChecker.setWarnInTransaction();
			try {
				Map<String, NodeRef> scoreCriterionNodeRefs = processScoreCriteria(scoreCriteriaFolder);
				
				updateScoreLists(scoreCriterionNodeRefs, currentTenant);
				updateSurveyQuestion(scoreCriterionNodeRefs, currentTenant);
				
				return "Patch applied successfully";
			} finally {
				ruleService.enableRules();
				policyBehaviourFilter.enableBehaviour();
			}
		});
	}

	private NodeRef findScoreCriteriaFolder() {
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria/.");
	}

	private Map<String, NodeRef> processScoreCriteria(NodeRef scoreCriteriaFolder) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ruleService.disableRules();
			policyBehaviourFilter.disableBehaviour();
			IntegrityChecker.setWarnInTransaction();
			NodeRef criterionTypesNodeRef = createOrGetCriterionTypesFolder();
			Map<String, NodeRef> scoreCriterionNodeRefs = new HashMap<>();
			List<NodeRef> criteriaNodes = BeCPGQueryBuilder.createQuery().selectNodesByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
					"/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria/*");
			for (NodeRef nodeRef : criteriaNodes) {
				processSingleCriteria(nodeRef, criterionTypesNodeRef, scoreCriterionNodeRefs);
			}
			nodeService.setProperty(scoreCriteriaFolder, DataListModel.PROP_DATALISTITEMTYPE,
					ProjectModel.TYPE_SCORE_CRITERION.toPrefixString(namespaceService));
			
			return scoreCriterionNodeRefs;
		}, false, true);
	}

	private NodeRef createOrGetCriterionTypesFolder() {
		NodeRef entityListsFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/ProjectLists/bcpg:entityLists") + "/.");

		NodeRef scoreCriterionTypesFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/ProjectLists/bcpg:entityLists/ScoreCriterionTypes") + "/.");

		if (scoreCriterionTypesFolder == null) {
			MLText entityListTranslated = TranslateHelper.getTranslatedPathMLText(ProjectRepoConsts.PATH_SCORE_CRITERION_TYPES);
			scoreCriterionTypesFolder = entityListDAO.createList(entityListsFolder, ProjectRepoConsts.PATH_SCORE_CRITERION_TYPES,
					BeCPGModel.TYPE_LIST_VALUE);
			nodeService.setProperty(scoreCriterionTypesFolder, ContentModel.PROP_TITLE, entityListTranslated);
		}
		
		return scoreCriterionTypesFolder;
	}

	private void processSingleCriteria(NodeRef nodeRef, NodeRef criterionTypesNodeRef, Map<String, NodeRef> scoreCriterionNodeRefs) {
		if (!nodeService.exists(nodeRef) || !nodeService.getType(nodeRef).equals(BeCPGModel.TYPE_LIST_VALUE)) {
			logger.warn("Node does not exist or is not listValue: " + nodeRef);
			return;
		}

		nodeService.moveNode(nodeRef, criterionTypesNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
		
		if (Boolean.TRUE.equals(nodeService.getProperty(nodeRef, BeCPGModel.PROP_IS_DELETED))) {
			logger.warn("Node is manually deleted: " + nodeRef);
			return;
		}
		
		boolean isMLAware = MLPropertyInterceptor.setMLAware(true);
		try {
			MLText mlText = (MLText) nodeService.getProperty(nodeRef, BeCPGModel.PROP_LV_VALUE);
			if (mlText == null) {
				logger.warn("No lvValue found for: " + nodeRef);
				return;
			}

			String key = Optional.ofNullable(nodeService.getProperty(nodeRef, BeCPGModel.PROP_LV_CODE)).map(Object::toString)
					.filter(s -> !s.isBlank()).orElseGet(() -> MLTextHelper.getClosestValue(mlText, Locale.getDefault()));

			if (key == null || key.isEmpty()) {
				logger.warn("No key found for: " + mlText.toString());
				key = nodeRef.getId();
			}

			NodeRef scoreCriterion = createScoreCriterion(key, mlText);
			scoreCriterionNodeRefs.put(key, scoreCriterion);
		} finally {
			MLPropertyInterceptor.setMLAware(isMLAware);
		}
	}

	private void updateScoreLists(Map<String, NodeRef> scoreCriterionNodeRefs, String currentTenant) {
		BatchProcessor<NodeRef> batchProcessor = createBatchTypeProcessor(ProjectModel.TYPE_SCORE_LIST, true);
		batchProcessor.processLong(createScoreListWorker(scoreCriterionNodeRefs, currentTenant), true);
	}

	private void updateSurveyQuestion(Map<String, NodeRef> scoreCriterionNodeRefs, String currentTenant) {
		BatchProcessor<NodeRef> batchProcessor = createBatchTypeProcessor(SurveyModel.TYPE_SURVEY_QUESTION, true);
		batchProcessor.processLong(createSurveyQuestionWorker(scoreCriterionNodeRefs, currentTenant), true);
	}

	private BatchProcessWorker<NodeRef> createSurveyQuestionWorker(Map<String, NodeRef> scoreCriterionNodeRefs, String tenantDomain) {
		return new BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				AuthenticationUtil.runAs(() -> {
					try {
						policyBehaviourFilter.disableBehaviour();
						ruleService.disableRules();
						IntegrityChecker.setWarnInTransaction();
						
						if (!nodeService.exists(entityNodeRef) || !entityNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)
								|| entityNodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID)) {
							return null;
						}
						
						if (lockService.isLocked(entityNodeRef)) {
							lockService.unlock(entityNodeRef);
						}
						
						String criterion = (String) nodeService.getProperty(entityNodeRef, QUESTION_CRITERION);
						NodeRef criterionNodeRef = scoreCriterionNodeRefs.get(criterion);
						
						if (criterionNodeRef != null) {
							if (!nodeService.exists(criterionNodeRef)) {
								logger.error("node does not exist: " + criterionNodeRef + " in tenant: " + TenantUtil.getCurrentDomain());
							} else {
								associationService.update(entityNodeRef, SurveyModel.ASSOC_SCORE_CRITERION, criterionNodeRef);
							}
						}
					} finally {
						policyBehaviourFilter.enableBehaviour();
						ruleService.enableRules();
					}
					return null;
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));      
			}
		};
	}

	private BatchProcessWorker<NodeRef> createScoreListWorker(Map<String, NodeRef> scoreCriterionNodeRefs, String tenantDomain) {
		return new BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				AuthenticationUtil.runAs(() -> {
					try {
						policyBehaviourFilter.disableBehaviour();
						ruleService.disableRules();
						IntegrityChecker.setWarnInTransaction();
						
						if (!nodeService.exists(entityNodeRef) || !entityNodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)
								|| entityNodeRef.getStoreRef().getIdentifier().contains(Version2Model.STORE_ID)) {
							return null;
						}
						
						if (lockService.isLocked(entityNodeRef)) {
							lockService.unlock(entityNodeRef);
						}
						
						if (!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
							nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL, new HashMap<>());
						}
						
						String criterion = (String) nodeService.getProperty(entityNodeRef, ProjectModel.PROP_SL_CRITERION);
						NodeRef criterionNodeRef = scoreCriterionNodeRefs.get(criterion);
						
						if (criterionNodeRef != null) {
							if (!nodeService.exists(criterionNodeRef)) {
								logger.error("node does not exist: " + criterionNodeRef + " in tenant: " + TenantUtil.getCurrentDomain());
							} else {
								associationService.update(entityNodeRef, ProjectModel.ASSOC_SL_SCORE_CRITERION, criterionNodeRef);
							}
						}
					} finally {
						policyBehaviourFilter.enableBehaviour();
						ruleService.enableRules();
					}
					return null;
                }, tenantAdminService.getDomainUser(AuthenticationUtil.getSystemUserName(), tenantDomain));            
			}
		};
	}

	private NodeRef createScoreCriterion(String key, MLText mlText) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, mlText);
		properties.put(ProjectModel.PROP_SCORE_CRITERION_TYPE, key);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria", key.replace("/", ""),
				ProjectModel.TYPE_SCORE_CRITERION, properties);

	}

	private NodeRef getOrCreateNode(NodeService nodeService, String path, String nodeName, QName type, Map<QName, Serializable> properties) {
		NodeRef folder = BeCPGQueryBuilder.createQuery().selectNodeByPath(path);
		NodeRef node = nodeService.getChildByName(folder, ContentModel.ASSOC_CONTAINS, nodeName);
		properties.put(ContentModel.PROP_NAME, nodeName);

		if (node == null) {
			ChildAssociationRef childAssocRef = nodeService.createNode(folder, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, nodeName), type, properties);
			node = childAssocRef.getChildRef();
		}

		return node;
	}

}

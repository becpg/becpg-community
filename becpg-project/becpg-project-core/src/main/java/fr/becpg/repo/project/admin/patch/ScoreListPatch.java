package fr.becpg.repo.project.admin.patch;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

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
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.Pair;
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

public class ScoreListPatch extends AbstractBeCPGPatch {
	private static final Log logger = LogFactory.getLog(ScoreListPatch.class);

	private EntityListDAO entityListDAO;

	private NodeDAO nodeDAO;

	private PatchDAO patchDAO;

	private QNameDAO qnameDAO;

	private BehaviourFilter policyBehaviourFilter;

	private IntegrityChecker integrityChecker;

	private RuleService ruleService;

	private LockService lockService;

	private AssociationService associationService;

	public EntityListDAO getEntityListDAO() {
		return entityListDAO;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public NodeService getNodeService() {
		return nodeService;
	}


	public void setNodeDAO(NodeDAO nodeDAO) {
		this.nodeDAO = nodeDAO;
	}

	public void setPatchDAO(PatchDAO patchDAO) {
		this.patchDAO = patchDAO;
	}

	public void setQnameDAO(QNameDAO qnameDAO) {
		this.qnameDAO = qnameDAO;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setIntegrityChecker(IntegrityChecker integrityChecker) {
		this.integrityChecker = integrityChecker;
	}


	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}


	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	@Override
	protected String applyInternal() throws Exception {
		return AuthenticationUtil.runAsSystem(() -> {
			NodeRef scoreCriteriaFolder = findScoreCriteriaFolder();
			if (scoreCriteriaFolder == null) {
				logger.warn("Score Criteria folder not found");
				return "No changes applied";
			}

			Map<String, NodeRef> scoreCriterionNodeRefs = processScoreCriteria(scoreCriteriaFolder);
			updateScoreLists(scoreCriterionNodeRefs);

			return "Patch applied successfully";
		});
	}

	private NodeRef findScoreCriteriaFolder() {
		return BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/System/ProjectLists/bcpg:entityLists/ScoreCriteria");
	}

	private Map<String, NodeRef> processScoreCriteria(NodeRef scoreCriteriaFolder) {
		NodeRef criterionTypesNodeRef = createOrGetCriterionTypesFolder();
		Map<String, NodeRef> scoreCriterionNodeRefs = new HashMap<>();

		List<NodeRef> criteriaNodes = BeCPGQueryBuilder.createQuery().selectNodesByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/System/ProjectLists/bcpg:entityLists/ScoreCriteria/*");

		for (NodeRef nodeRef : criteriaNodes) {
			processSingleCriteria(nodeRef, criterionTypesNodeRef, scoreCriterionNodeRefs);
		}

		nodeService.setProperty(scoreCriteriaFolder, DataListModel.PROP_DATALISTITEMTYPE,
				ProjectModel.TYPE_SCORE_CRITERION.toPrefixString(namespaceService));

		return scoreCriterionNodeRefs;
	}

	private NodeRef createOrGetCriterionTypesFolder() {
		NodeRef entityListsFolder = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
				"/app:company_home/" + AbstractBeCPGQueryBuilder.encodePath("/System/ProjectLists/bcpg:entityLists/") + "/.");

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
			return;
		}

		MLText mlText = (MLText) nodeService.getProperty(nodeRef, BeCPGModel.PROP_LV_VALUE);
		if (mlText == null) {
			return;
		}

		String key = Optional.ofNullable((String) nodeService.getProperty(nodeRef, BeCPGModel.PROP_LV_CODE))
				.orElse(MLTextHelper.getClosestValue(mlText, Locale.getDefault()));

		NodeRef scoreCriterion = createScoreCriterion(key, mlText);
		scoreCriterionNodeRefs.put(key, scoreCriterion);

		nodeService.moveNode(nodeRef, criterionTypesNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CONTAINS);
	}

	private void updateScoreLists(Map<String, NodeRef> scoreCriterionNodeRefs) {
		BatchProcessor<NodeRef> batchProcessor = createBatchProcessor();
		batchProcessor.processLong(createScoreListWorker(scoreCriterionNodeRefs), true);
	}

	private BatchProcessor<NodeRef> createBatchProcessor() {
		BatchProcessWorkProvider<NodeRef> workProvider = new BatchProcessWorkProvider<>() {
			private final long maxNodeId = nodeDAO.getMaxNodeId();
			private final Pair<Long, QName> typeQNamePair = qnameDAO.getQName(ProjectModel.TYPE_SCORE_LIST);
			private final List<NodeRef> result = new ArrayList<>();
			private long minSearchNodeId = 0;
			private long maxSearchNodeId = BATCH_SIZE;

			@Override
			public Collection<NodeRef> getNextWork() {
				result.clear();

				if (typeQNamePair == null) {
					return result;
				}

				Long typeQNameId = typeQNamePair.getFirst();

				while (result.isEmpty() && (minSearchNodeId < maxNodeId)) {
					List<Long> nodeIds = patchDAO.getNodesByTypeQNameId(typeQNameId, minSearchNodeId, maxSearchNodeId);

					result.addAll(nodeIds.stream().map(nodeDAO::getNodeIdStatus).filter(status -> !status.isDeleted()).map(NodeRef.Status::getNodeRef)
							.toList());

					minSearchNodeId += BATCH_SIZE;
					maxSearchNodeId += BATCH_SIZE;
				}

				return result;
			}

			@Override
			public int getTotalEstimatedWorkSize() {
				return result.size();
			}

			@Override
			public long getTotalEstimatedWorkSizeLong() {
				return getTotalEstimatedWorkSize();
			}
		};

		return new BatchProcessor<>("ScoreListPatch", transactionService.getRetryingTransactionHelper(), workProvider, BATCH_THREADS, BATCH_SIZE,
				applicationEventPublisher, logger, 500);
	}

	private BatchProcessWorker<NodeRef> createScoreListWorker(Map<String, NodeRef> scoreCriterionNodeRefs) {
		return new BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				processScoreListNode(entityNodeRef, scoreCriterionNodeRefs);
			}
		};
	}

	private void processScoreListNode(NodeRef entityNodeRef, Map<String, NodeRef> scoreCriterionNodeRefs) {
		try {
			policyBehaviourFilter.disableBehaviour();
			ruleService.disableRules();
			integrityChecker.setEnabled(false);

			if (!nodeService.exists(entityNodeRef) || !entityNodeRef.getStoreRef().equals(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE)) {
				return;
			}

			if (lockService.isLocked(entityNodeRef)) {
				lockService.unlock(entityNodeRef);
			}

			String criterion = (String) nodeService.getProperty(entityNodeRef, ProjectModel.PROP_SL_CRITERION);
			NodeRef criterionNodeRef = scoreCriterionNodeRefs.get(criterion);

			if (criterionNodeRef != null) {
				if (!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL)) {
					nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_DEPTH_LEVEL, new HashMap<>());
				}

				associationService.update(entityNodeRef, ProjectModel.ASSOC_SL_SCORE_CRITERION, criterionNodeRef);
			}
		} finally {
			policyBehaviourFilter.enableBehaviour();
			ruleService.enableRules();
			integrityChecker.setEnabled(true);
		}
	}

	private NodeRef createScoreCriterion(String key, MLText mlText) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(BeCPGModel.PROP_CHARACT_NAME, mlText);
		properties.put(ProjectModel.PROP_SCORE_CRITERION_TYPE, key);

		return getOrCreateNode(nodeService, "/app:company_home/cm:System/cm:ProjectLists/bcpg:entityLists/cm:ScoreCriteria", key,
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
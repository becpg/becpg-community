package fr.becpg.repo.ecm.impl;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ECMModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.PlmRepoConsts;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.impl.BatchAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.ecm.AutomaticECOService;
import fr.becpg.repo.ecm.ECOService;
import fr.becpg.repo.ecm.ECOState;
import fr.becpg.repo.ecm.data.ChangeOrderData;
import fr.becpg.repo.ecm.data.ChangeOrderType;
import fr.becpg.repo.ecm.data.RevisionType;
import fr.becpg.repo.ecm.data.dataList.ReplacementListDataItem;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>AutomaticECOServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service("automaticECOService")
public class AutomaticECOServiceImpl implements AutomaticECOService {

	private static final String REFORMULATE_BATCH_ID = "reformulateChangedEntities";

	private static final String CURRENT_ECO_PREF = "fr.becpg.ecm.currentEcmNodeRef";

	private static final Log logger = LogFactory.getLog(AutomaticECOServiceImpl.class);

	@Autowired
	private RepoService repoService;

	@Autowired
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;
	
	@Autowired
	private BeCPGAuditService beCPGAuditService;

	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Autowired
	private EntityDictionaryService entityDictionaryService;

	@Autowired
	@Qualifier("namespaceService")
    private NamespacePrefixResolver namespacePrefixResolver;

	private Boolean shouldApplyAutomaticECO() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.eco.automatic.apply"));
	}

	private Boolean withoutRecord() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.eco.automatic.withoutRecord"));
	}

	private String automaticRevisionType() {
		return systemConfigurationService.confValue("beCPG.eco.automatic.revision.type");
	}

	private String statesToRegister() {
		return systemConfigurationService.confValue("beCPG.eco.automatic.states");
	}

	private Boolean deleteOnApply() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.eco.automatic.deleteOnApply"));
	}

	private Boolean isEnable() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.eco.automatic.enable"));
	}

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private ECOService ecoService;

	@Autowired
	private NodeService nodeService;

	@Autowired
	private PreferenceService preferenceService;

	@Autowired
	private FormulationService<FormulatedEntity> formulationService;

	@Autowired
	private BehaviourFilter policyBehaviourFilter;

	@Autowired
	private WUsedListService wUsedListService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private BatchQueueService batchQueueService;

	@Value("${becpg.batch.automaticECO.reformulateChangedEntities.workerThreads}")
	private Integer reformulateWorkerThreads;

	@Value("${becpg.batch.automaticECO.reformulateChangedEntities.batchSize}")
	private Integer reformulateBatchSize;
	
	@Value("${becpg.batch.automaticECO.autoMergeBranch.workerThreads}")
	private Integer autoMergeWorkerThreads;
	
	@Value("${becpg.batch.automaticECO.autoMergeBranch.batchSize}")
	private Integer autoMergeBatchSize;

	/** {@inheritDoc} */
	@Override
	public boolean addAutomaticChangeEntry(final NodeRef entityNodeRef, final ChangeOrderData currentUserChangeOrderData) {

		if ((Boolean.TRUE.equals(withoutRecord()) && (currentUserChangeOrderData == null)) || !accept(entityNodeRef)) {
			return false;
		}

		return AuthenticationUtil.runAsSystem(() -> {
			NodeRef parentNodeRef = getChangeOrderFolder();

			ChangeOrderData changeOrderData = currentUserChangeOrderData;

			if (changeOrderData == null) {
				changeOrderData = new ChangeOrderData(generateEcoName(null), ECOState.Automatic, ChangeOrderType.Replacement, null);

				NodeRef ret = getAutomaticECONoderef(parentNodeRef);

				if (ret != null) {
					changeOrderData = (ChangeOrderData) alfrescoRepository.findOne(ret);
				} else {
					if (logger.isDebugEnabled()) {
						logger.debug("Creating new automatic change order");
					}
					changeOrderData = (ChangeOrderData) alfrescoRepository.create(parentNodeRef, changeOrderData);
				}
			}

			List<ReplacementListDataItem> replacementList = changeOrderData.getReplacementList();

			if (replacementList == null) {
				replacementList = new ArrayList<>();
			}

			// avoid recreate same entry
			for (ReplacementListDataItem item : replacementList) {
				if (entityNodeRef.equals(item.getTargetItem())) {
					if (logger.isDebugEnabled()) {
						logger.debug("NodeRef " + entityNodeRef + " already present in automatic change order :" + changeOrderData.getName());
					}
					return false;
				}
			}

			replacementList.add(new ReplacementListDataItem(RevisionType.valueOf(automaticRevisionType()), Collections.singletonList(entityNodeRef),
					entityNodeRef, 100));

			if (logger.isDebugEnabled()) {
				logger.debug("Adding nodeRef " + entityNodeRef + " to automatic change order :" + changeOrderData.getName());
				logger.debug("Revision type : " + automaticRevisionType());
			}

			changeOrderData.setReplacementList(replacementList);

			alfrescoRepository.save(changeOrderData);

			return true;
		});

	}

	private boolean accept(NodeRef entityNodeRef) {
		if (nodeService.exists(entityNodeRef)) {

			String productState = (String) nodeService.getProperty(entityNodeRef, PLMModel.PROP_PRODUCT_STATE);

			if ((productState == null) || productState.isEmpty() || !statesToRegister().contains(productState)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping product state : " + productState);
				}
				return false;
			}

			QName nodeType = nodeService.getType(entityNodeRef);

			if (PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT.equals(nodeType)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Skipping local semi finished product");
				}
				return false;
			}

			return true;
		}
		return false;
	}

	private NodeRef getAutomaticECONoderef(NodeRef parentFolderNodeRef) {
		return BeCPGQueryBuilder.createQuery().parent(parentFolderNodeRef).ofType(ECMModel.TYPE_ECO)
				.andPropEquals(ECMModel.PROP_ECO_STATE, ECOState.Automatic.toString()).inDB().singleValue();
	}

	private String generateEcoName(String name) {
		if (name != null) {
			return name + "-" + I18NUtil.getMessage("plm.ecm.current.name", new Date());
		}
		return I18NUtil.getMessage("plm.ecm.automatic.name", new Date());
	}

	private NodeRef getChangeOrderFolder() {
		return repoService.getFolderByPath("/" + RepoConsts.PATH_SYSTEM + "/" + PlmRepoConsts.PATH_ECO);
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyAutomaticEco() {

		if (Boolean.TRUE.equals(isEnable())) {

			autoMergeBranch();

			if (Boolean.TRUE.equals(withoutRecord())) {
				return reformulateChangedEntities();
			} else if (Boolean.TRUE.equals(shouldApplyAutomaticECO())) {

				if (logger.isDebugEnabled()) {
					logger.debug("Try to apply automatic change order");
				}

				final NodeRef ecoNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
					NodeRef parentNodeRef = getChangeOrderFolder();
					return getAutomaticECONoderef(parentNodeRef);
				}, false, true);

				if (ecoNodeRef != null) {

					return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
						if (logger.isDebugEnabled()) {
							logger.debug("Found automatic change order to apply :" + ecoNodeRef);
						}
						try {
							transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
								return ecoService.apply(ecoNodeRef, deleteOnApply(), true, false);
							}, false, true);

						} catch (Exception e) {
							if (RetryingTransactionHelper.extractRetryCause(e) != null) {
								throw e;
							}
							logger.error(e, e);
							return false;
						}
						return true;
					}, false, true);
				}
			}
		}
		return false;
	}

	private boolean autoMergeBranch() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, +1);

		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		String dateRange = dateFormat.format(cal.getTime());

		String ftsQuery = String.format("@bcpg\\:autoMergeDate:[MIN TO %s]", dateRange);

		logger.debug("Start of auto merge entities for: " + ftsQuery);

		BatchInfo batchInfo = new BatchInfo("autoMergeBranch", "becpg.batch.automaticECO.autoMergeBranch");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(autoMergeWorkerThreads != null ? autoMergeWorkerThreads : 3);
		batchInfo.setBatchSize(autoMergeBatchSize != null ? autoMergeBatchSize : 1);
		batchInfo.setPriority(BatchPriority.HIGH);

		List<NodeRef> nodeRefs = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2)
						.withAspect(BeCPGModel.ASPECT_AUTO_MERGE_ASPECT).andFTSQuery(ftsQuery).maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).list(),
						false, true);

		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {

				if (logger.isDebugEnabled()) {
					logger.debug("Found product to merge: " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " ("
							+ entityNodeRef + ") ");
				}
				try {
					AuthenticationUtil.runAsSystem(() -> {

						Date newEffectivity = (Date) nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_AUTO_MERGE_DATE);

						NodeRef newEntityNodeRef = entityVersionService.mergeBranch(entityNodeRef, newEffectivity);

						if (newEntityNodeRef != null) {
							nodeService.removeAspect(newEntityNodeRef, BeCPGModel.ASPECT_AUTO_MERGE_ASPECT);
						}

						return true;
					});

				} catch (Exception e) {
					if (RetryingTransactionHelper.extractRetryCause(e) != null) {
						throw e;
					}
					logger.error("Cannot merge node:" + entityNodeRef, e);
				}

			}
		};

		batchQueueService.queueBatch(batchInfo, new EntityListBatchProcessWorkProvider<>(nodeRefs), processWorker, null);

		return true;
	}

	private boolean reformulateChangedEntities() {
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");

		String dateRange = dateFormat.format(getFromDate());

		BatchInfo batchInfo = new BatchInfo(REFORMULATE_BATCH_ID, "becpg.batch.automaticECO.reformulateChangedEntities");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(reformulateWorkerThreads != null ? reformulateWorkerThreads : 2);
		batchInfo.setBatchSize(reformulateBatchSize != null ? reformulateBatchSize : 1);
		batchInfo.setPriority(BatchPriority.LOW);
		
		List<NodeRef> nodeRefs = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGQueryBuilder.createQuery()
						.excludeArchivedEntities()
						.ofType(PLMModel.TYPE_PRODUCT)
						.orBetween(ContentModel.PROP_CREATED, dateRange, "MAX")
						.orBetween(ContentModel.PROP_MODIFIED, dateRange, "MAX")
						.inDBIfPossible()
						.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
						.list(), false, true);
		
		addACLProducts(dateRange, nodeRefs);
		
		logger.info("modified products size: " + nodeRefs.size());
		
		List<NodeRef> toReformulateEntities = new ArrayList<>();

		List<BatchStep<NodeRef>> steps = new ArrayList<>();

		BatchStep<NodeRef> wusedStep = new BatchStep<>();

		BatchStep<NodeRef> formulateStep = new BatchStep<>();

		wusedStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(nodeRefs));

		wusedStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {

			@Override
			public void process(NodeRef entry) throws Throwable {
				List<NodeRef> wused = extractWUsedToFormulate(entry);
				if (wused.isEmpty()) {
					wused.add(entry);
				}

				for (NodeRef wusedLeaf : wused) {
					if (!toReformulateEntities.contains(wusedLeaf)) {
						toReformulateEntities.add(wusedLeaf);
					}
				}
			}
		});

		wusedStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void afterStep() {
				formulateStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(toReformulateEntities));
				logger.info("reformulated entities size: " + toReformulateEntities.size());
			}
		});

		steps.add(wusedStep);

		ReformulateChangedEntitiesProcessWorker processWorker = new ReformulateChangedEntitiesProcessWorker();

		formulateStep.setProcessWorker(processWorker);
		steps.add(formulateStep);

		batchQueueService.queueBatch(batchInfo, steps);

		return true;
	}

	private void addACLProducts(String dateRange, List<NodeRef> nodeRefs) {
		List<NodeRef> modifiedACLs = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGQueryBuilder.createQuery()
						.excludeArchivedEntities()
						.ofType(SecurityModel.TYPE_ACL_GROUP)
						.orBetween(ContentModel.PROP_CREATED, dateRange, "MAX")
						.orBetween(ContentModel.PROP_MODIFIED, dateRange, "MAX")
						.inDBIfPossible()
						.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
						.list(), false, true);
		
		for (NodeRef modifiedACL : modifiedACLs) {
			ACLGroupData aclGroupData = (ACLGroupData) alfrescoRepository.findOne(modifiedACL);
			String nodeTypeString = aclGroupData.getNodeType();
			if (nodeTypeString != null && nodeTypeString.contains(":")) {
				QName nodeType = QName.createQName(nodeTypeString.split(":")[0], nodeTypeString.split(":")[1], namespacePrefixResolver);
				if (entityDictionaryService.isSubClass(nodeType, PLMModel.TYPE_PRODUCT) && isACLApplied(aclGroupData)) {
					List<NodeRef> aclProducts = transactionService.getRetryingTransactionHelper()
							.doInTransaction(() -> BeCPGQueryBuilder.createQuery()
									.excludeArchivedEntities().ofType(nodeType)
									.orBetween(BeCPGModel.PROP_FORMULATED_DATE, "MIN", dateRange)
									.inDBIfPossible()
									.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED)
									.list(), false, true);
					
					for (NodeRef aclProduct : aclProducts) {
						if (!nodeRefs.contains(aclProduct)) {
							nodeRefs.add(aclProduct);
						}
					}
				}
			}
		}
	}

	private boolean isACLApplied(ACLGroupData aclGroupData) {
		for (ACLEntryDataItem acl : aclGroupData.getAcls()) {
			String propNameString = acl.getPropName();
			if ("View-documents".equals(propNameString)) {
				return true;
			}
			if (propNameString != null && propNameString.contains(":") && enforceACL()) {
				QName propName = QName.createQName(propNameString.split(":")[0], propNameString.split(":")[1], namespacePrefixResolver);
				if (entityDictionaryService.isSubClass(propName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private boolean enforceACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.enforceACL"));
	}

	private Date getFromDate() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);

		Date dateFrom = cal.getTime();
		
		AuditQuery auditQuery = AuditQuery.createQuery().asc(false).dbAsc(false).sortBy(AuditPlugin.STARTED_AT)
				.filter(BatchAuditPlugin.BATCH_ID, REFORMULATE_BATCH_ID).maxResults(1);
		
		List<JSONObject> lastActivityResult = beCPGAuditService.listAuditEntries(AuditType.BATCH, auditQuery);
		
		if (!lastActivityResult.isEmpty()) {
			JSONObject lastActivity = lastActivityResult.get(0);
			if (lastActivity.has(AuditPlugin.STARTED_AT)) {
				
				Date lastBatchStartDate = null;
				
				Object startedAt = lastActivity.get(AuditPlugin.STARTED_AT);
				
				if (startedAt instanceof Date) {
					lastBatchStartDate = (Date) startedAt;
				} else {
					lastBatchStartDate = ISO8601DateFormat.parse(startedAt.toString());
				}
				
				if (lastBatchStartDate.before(dateFrom)) {
					dateFrom = lastBatchStartDate;
				}
			}
		}
		return dateFrom;
	}

	private class ReformulateChangedEntitiesProcessWorker extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> {

		@Override
		public void process(NodeRef toReformulate) throws Throwable {

			if (nodeService.exists(toReformulate) && alfrescoRepository.findOne(toReformulate) instanceof FormulatedEntity) {

				if (logger.isDebugEnabled()) {
					logger.debug("Reformulating product: " + nodeService.getProperty(toReformulate, ContentModel.PROP_NAME) + " (" + toReformulate
							+ ") ");
				}
				try {
					policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

					L2CacheSupport.doInCacheContext(
							() -> AuthenticationUtil.runAsSystem(
									() -> formulationService.formulate(toReformulate))
							, false, true, true);

				} catch (Throwable e) {
					if (RetryingTransactionHelper.extractRetryCause(e) != null) {
						logger.debug("Retrying the formulation due to exception "+e.getMessage());
						throw e;
					}
					logger.error("Cannot reformulate node:" + toReformulate, e);
				} finally {
					policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
					policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
					policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
				}
			}
		}
	}

	private QName evaluateWUsedAssociation(NodeRef targetAssocNodeRef) {

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_LOGISTICUNIT)) {
			return PLMModel.ASSOC_COMPOLIST_PRODUCT;
		} else if (nodeType.isMatch(PLMModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(PLMModel.TYPE_PACKAGINGKIT)) {
			return PLMModel.ASSOC_PACKAGINGLIST_PRODUCT;
		} else if (nodeType.isMatch(PLMModel.TYPE_RESOURCEPRODUCT)) {
			return MPMModel.ASSOC_PL_RESOURCE;
		}

		return null;
	}

	private List<NodeRef> extractWUsedToFormulate(NodeRef entityNodeRef) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			if (accept(entityNodeRef)) {

				if (logger.isDebugEnabled()) {
					logger.debug("Found modified product: " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME) + " (" + entityNodeRef
							+ ") ");
				}
				try {

					QName associationQName = evaluateWUsedAssociation(entityNodeRef);

					if (associationQName != null) {
						MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(Arrays.asList(entityNodeRef), WUsedOperator.AND,
								associationQName, RepoConsts.MAX_DEPTH_LEVEL);

						if (logger.isTraceEnabled()) {
							logger.trace("WUsed to apply:" + wUsedData.toString());
							logger.trace("Leaf size :" + wUsedData.getAllLeafs().size());

						}

						return wUsedData.getAllLeafs();
					}
				} catch (Exception e) {
					Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
					if (validCause != null) {
						throw (RuntimeException) validCause;
					}
					logger.error(e, e);
				}
			}
			return new ArrayList<>();

		}, false, true);
	}

	/** {@inheritDoc} */
	@Override
	public ChangeOrderData createAutomaticEcoForUser(String name) {
		NodeRef parentNodeRef = getChangeOrderFolder();

		ChangeOrderData changeOrderData = new ChangeOrderData(generateEcoName(name), ECOState.ToCalculateWUsed, ChangeOrderType.Replacement, null);

		changeOrderData = (ChangeOrderData) alfrescoRepository.create(parentNodeRef, changeOrderData);
		String curUserName = AuthenticationUtil.getFullyAuthenticatedUser();
		Map<String, Serializable> prefs = preferenceService.getPreferences(curUserName);
		prefs.put(CURRENT_ECO_PREF, changeOrderData.getNodeRef().toString());
		preferenceService.setPreferences(curUserName, prefs);

		return changeOrderData;
	}

	/** {@inheritDoc} */
	@Override
	public ChangeOrderData getCurrentUserChangeOrderData() {
		String curUserName = AuthenticationUtil.getFullyAuthenticatedUser();
		Map<String, Serializable> prefs = preferenceService.getPreferences(curUserName);

		String prefNodeRef = (String) prefs.get(CURRENT_ECO_PREF);
		if (prefNodeRef != null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Found pref nodeRef : " + prefNodeRef);
			}
			NodeRef currentUserNodeRef = new NodeRef(prefNodeRef);
			if (nodeService.exists(currentUserNodeRef)
					&& ECOState.ToCalculateWUsed.toString().equals(nodeService.getProperty(currentUserNodeRef, ECMModel.PROP_ECO_STATE))) {
				if (logger.isDebugEnabled()) {
					logger.debug("Found current automatic Eco for user :" + curUserName);
				}
				return (ChangeOrderData) alfrescoRepository.findOne(currentUserNodeRef);
			} else {
				logger.info("Removing invalid eco automatic noderef from user prefs : " + curUserName);
				logger.info("Node doesn't exist ? " + nodeService.exists(currentUserNodeRef));
				prefs.put(CURRENT_ECO_PREF, null);
				preferenceService.setPreferences(curUserName, prefs);
			}
		}
		return null;
	}

}

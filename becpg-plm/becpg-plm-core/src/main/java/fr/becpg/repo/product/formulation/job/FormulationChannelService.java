package fr.becpg.repo.product.formulation.job;

import java.lang.management.ManagementFactory;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.alfresco.error.ExceptionStackUtil;
import org.alfresco.model.ContentModel;
import org.alfresco.query.PagingRequest;
import org.alfresco.query.PagingResults;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AbstractAuthenticationService;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespacePrefixResolver;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.sun.management.OperatingSystemMXBean;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.MPMModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PublicationModel;
import fr.becpg.model.ReportModel;
import fr.becpg.model.SecurityModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.batch.WorkProviderFactory;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.WUsedListService;
import fr.becpg.repo.entity.datalist.WUsedListService.WUsedOperator;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.formulation.FormulationService;
import fr.becpg.repo.product.formulation.SecurityFormulationHandler;
import fr.becpg.repo.publication.PublicationChannelService;
import fr.becpg.repo.publication.PublicationChannelService.PublicationChannelStatus;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.L2CacheSupport;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.security.data.ACLGroupData;
import fr.becpg.repo.security.data.dataList.ACLEntryDataItem;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.util.BeCPGTransactionUtil;

/**
 * <p>FormulationChannelService class.</p>
 *
 * @author matthieu
 */
@Service("formulationChannelService")
public class FormulationChannelService {

	private static final Log logger = LogFactory.getLog(FormulationChannelService.class);

	/** Constant <code>FORMULATE_ENTITIES_CHANNEL_ID="formulate-entities"</code> */
	public static final String FORMULATE_ENTITIES_CHANNEL_ID = "formulate-entities";
	
	private static final String REFORMULATE_BATCH_ID = "reformulateChangedEntities";

	private BatchQueueService batchQueueService;

	private PublicationChannelService publicationChannelService;

	private SystemConfigurationService systemConfigurationService;
	
	private TransactionService transactionService;

	private NodeService nodeService;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private EntityDictionaryService entityDictionaryService;
	
	private WUsedListService wUsedListService;
	
	private FormulationService<FormulatedEntity> formulationService;
	
	private BehaviourFilter policyBehaviourFilter;

	private NamespacePrefixResolver namespacePrefixResolver;
	
	private AbstractAuthenticationService authenticationService;

	private Integer reformulateWorkerThreads;

	private Integer reformulateBatchSize;
	
	/**
	 * <p>Constructor for FormulationChannelService.</p>
	 *
	 * @param batchQueueService a {@link fr.becpg.repo.batch.BatchQueueService} object
	 * @param publicationChannelService a {@link fr.becpg.repo.publication.PublicationChannelService} object
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object
	 * @param entityDictionaryService a {@link fr.becpg.repo.entity.EntityDictionaryService} object
	 * @param wUsedListService a {@link fr.becpg.repo.entity.datalist.WUsedListService} object
	 * @param formulationService a {@link fr.becpg.repo.formulation.FormulationService} object
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object
	 * @param namespacePrefixResolver a {@link org.alfresco.service.namespace.NamespacePrefixResolver} object
	 * @param authenticationService a {@link org.alfresco.repo.security.authentication.AbstractAuthenticationService} object
	 * @param reformulateWorkerThreads a {@link java.lang.Integer} object
	 * @param reformulateBatchSize a {@link java.lang.Integer} object
	 */
	public FormulationChannelService(BatchQueueService batchQueueService, PublicationChannelService publicationChannelService,
			SystemConfigurationService systemConfigurationService, TransactionService transactionService,
			@Qualifier("nodeService") NodeService nodeService, AlfrescoRepository<RepositoryEntity> alfrescoRepository,
			EntityDictionaryService entityDictionaryService, WUsedListService wUsedListService,
			FormulationService<FormulatedEntity> formulationService, @Qualifier("policyBehaviourFilter") BehaviourFilter policyBehaviourFilter,
			@Qualifier("namespaceService") NamespacePrefixResolver namespacePrefixResolver,
			@Qualifier("authenticationService") AbstractAuthenticationService authenticationService,
			@Value("${becpg.batch.automaticECO.reformulateChangedEntities.workerThreads}") Integer reformulateWorkerThreads,
			@Value("${becpg.batch.automaticECO.reformulateChangedEntities.batchSize}") Integer reformulateBatchSize) {
		super();
		this.batchQueueService = batchQueueService;
		this.publicationChannelService = publicationChannelService;
		this.systemConfigurationService = systemConfigurationService;
		this.transactionService = transactionService;
		this.nodeService = nodeService;
		this.alfrescoRepository = alfrescoRepository;
		this.entityDictionaryService = entityDictionaryService;
		this.wUsedListService = wUsedListService;
		this.formulationService = formulationService;
		this.policyBehaviourFilter = policyBehaviourFilter;
		this.namespacePrefixResolver = namespacePrefixResolver;
		this.authenticationService = authenticationService;
		this.reformulateWorkerThreads = reformulateWorkerThreads;
		this.reformulateBatchSize = reformulateBatchSize;
	}

	private String statesToRegister() {
		return systemConfigurationService.confValue("beCPG.eco.automatic.states");
	}
	
	private Integer minHoursSinceModification() {
		return Integer.parseInt(systemConfigurationService.confValue("beCPG.formulation.channel.minHoursSinceModification"));
	}
	
	private Integer maxProductsToFormulate() {
		return Integer.parseInt(systemConfigurationService.confValue("beCPG.formulation.channel.maxProducts"));
	}
	
	private Double maxCpuUsage() {
		String configValue = systemConfigurationService.confValue("beCPG.formulation.channel.maxCpuUsage");
	    return Double.parseDouble(configValue) / 100;
	}
	
	private Integer maxActiveUsers() {
		return Integer.parseInt(systemConfigurationService.confValue("beCPG.formulation.channel.maxActiveUsers"));
	}
	
	private String excludedTimeSlot() {
		return systemConfigurationService.confValue("beCPG.formulation.channel.excludedTimeSlot");
	}
	
	private Boolean isEnabled() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.channel.enable"));
	}
	
	private boolean enforceACL() {
		return Boolean.parseBoolean(systemConfigurationService.confValue("beCPG.formulation.security.enforceACL"));
	}
	
	/**
	 * <p>formulateEntities.</p>
	 *
	 * @return a {@link fr.becpg.repo.batch.BatchInfo} object
	 */
	public BatchInfo formulateEntities() {
		if (Boolean.TRUE.equals(isEnabled())) {
			return reformulateEntities();
		}
		return null;
	}
	
	/**
	 * <p>reformulateEntities.</p>
	 *
	 * @return a {@link fr.becpg.repo.batch.BatchInfo} object
	 */
	public BatchInfo reformulateEntities() {
		
		// Check if system conditions allow batch to run
		if (!shouldRunBatch()) {
			if (logger.isDebugEnabled()) {
				logger.debug("Batch execution skipped due to system conditions");
			}
			return null;
		}
		
		BatchInfo batchInfo = new BatchInfo(REFORMULATE_BATCH_ID, "becpg.batch.formulation.channel.formulateEntities");
		batchInfo.setRunAsSystem(true);
		batchInfo.setWorkerThreads(reformulateWorkerThreads != null ? reformulateWorkerThreads : 1);
		batchInfo.setBatchSize(reformulateBatchSize != null ? reformulateBatchSize : 1);
		batchInfo.setPriority(BatchPriority.LOW);
		
		if (batchQueueService.isBatchInQueue(batchInfo)) {
			logger.info("reformulateChangedEntities batch is already in queue");
			return null;
		}
		
		NodeRef channelNodeRef = publicationChannelService.getChannelById(FORMULATE_ENTITIES_CHANNEL_ID);
		PagingResults<NodeRef> results = publicationChannelService.getEntitiesByChannel(channelNodeRef, new PagingRequest(maxProductsToFormulate()));
		List<NodeRef> channelProducts = results.getPage();
		logger.info("Channel products to scan: " + channelProducts.size());

		List<NodeRef> markedSecurityRules = new ArrayList<>();
		
		Set<NodeRef> impactedProducts = new HashSet<>();
		List<NodeRef> toFormulateProducts = new ArrayList<>();
		List<NodeRef> toPublishProducts = new ArrayList<>();
		for (NodeRef channelProduct : channelProducts) {
			Date referenceDate = extractReferenceDate(channelProduct);
			if (SecurityModel.TYPE_ACL_GROUP.equals(nodeService.getType(channelProduct))) {
				markedSecurityRules.add(channelProduct);
				impactedProducts.addAll(getSecurityRuleProducts(channelProduct, referenceDate));
			} else if (needsFormulation(channelProduct)) {
				if (!toFormulateProducts.contains(channelProduct)) {
					toFormulateProducts.add(channelProduct);
				}
				impactedProducts.addAll(getWhereUsedProducts(channelProduct, referenceDate));
			} else {
				if (!toPublishProducts.contains(channelProduct)) {
					toPublishProducts.add(channelProduct);
				}
			}
		}
		logger.info("Impacted products to mark: " + impactedProducts.size());
		
		List<BatchStep<NodeRef>> steps = new ArrayList<>();
		
		BatchStep<NodeRef> impactedProductsStep = new BatchStep<>();
		impactedProductsStep.setStepDescId("becpg.batch.formulation.channel.formulateEntities.impactedProducts");
		impactedProductsStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(new ArrayList<>(impactedProducts)));
		steps.add(impactedProductsStep);
		impactedProductsStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				NodeRef channelListItem = publicationChannelService.getOrCreateChannelListNodeRef(entityNodeRef, FORMULATE_ENTITIES_CHANNEL_ID);
				nodeService.setProperty(channelListItem, PublicationModel.PROP_PUBCHANNELLIST_MODIFIED_DATE, new Date());
			}
		});
		impactedProductsStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void afterStep() {
				if (!batchInfo.isCancelled()) {
					for (NodeRef markedSecurityRule : markedSecurityRules) {
						publishEntityChannel(markedSecurityRule);
					}
				}
			}
		});
		
		List<NodeRef> totalNodesToProcess = new ArrayList<>();
		totalNodesToProcess.addAll(toFormulateProducts);
		totalNodesToProcess.addAll(toPublishProducts);
		
		if (totalNodesToProcess.size() < maxProductsToFormulate()) {
			Iterator<NodeRef> it = impactedProducts.iterator();
			while (it.hasNext() && totalNodesToProcess.size() < maxProductsToFormulate()) {
				NodeRef next = it.next();
				if (!toFormulateProducts.contains(next)) {
					toFormulateProducts.add(next);
					totalNodesToProcess.add(next);
				}
			}
		}
		logger.info("Products to formulate: " + toFormulateProducts.size());
		logger.info("Products to publish: " + toPublishProducts.size());
		totalNodesToProcess.sort((node1, node2) -> {
			if (!nodeService.exists(node1)) {
				return 1;
			}
			if (!nodeService.exists(node2)) {
				return -1;
			}
			int priority1 = getTypePriority(nodeService.getType(node1));
			int priority2 = getTypePriority(nodeService.getType(node2));
			return Integer.compare(priority1, priority2);
		});
		
		ReformulateChangedEntitiesProcessWorker processWorker = new ReformulateChangedEntitiesProcessWorker(toPublishProducts);
		BatchStep<NodeRef> formulateStep = batchQueueService.createBatchStepWithErrorHandling(batchInfo, totalNodesToProcess, processWorker);
		formulateStep.setStepDescId("becpg.batch.formulation.channel.formulateEntities.formulation");
		steps.add(formulateStep);

		batchQueueService.queueBatch(batchInfo, steps);

		return batchInfo;
	}
	
	/**
	 * Checks if the batch should run based on system conditions:
	 * - System load average threshold
	 * - Number of connected users threshold
	 * - Skipped time slot configuration
	 * 
	 * @return true if batch should run, false otherwise
	 */
	private boolean shouldRunBatch() {
		try {
			// Check system load average
			OperatingSystemMXBean os = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
			double cpuUsage = os.getCpuLoad();
			
			if (cpuUsage > 0 && maxCpuUsage() > 0 && cpuUsage >= maxCpuUsage()) {
				if (logger.isDebugEnabled()) {
					logger.debug("System load average too high: " + cpuUsage + " >= " + maxCpuUsage());
				}
				return false;
			}
			
			// Check number of connected users
			Set<String> usersWithTickets = authenticationService.getUsersWithTickets(true);
			int connectedUsers = usersWithTickets != null ? usersWithTickets.size() : 0;
			
			if (connectedUsers > maxActiveUsers()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Too many users connected: " + connectedUsers + " > " + maxActiveUsers());
				}
				return false;
			}
			
			// Check if current time is in skipped time slot
			if (isInSkippedTimeSlot()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Current time is in skipped time slot: " + excludedTimeSlot());
				}
				return false;
			}
			
			return true;
			
		} catch (Exception e) {
			logger.warn("Error checking batch execution conditions, allowing batch to proceed", e);
			return true;
		}
	}
	
	/**
	 * Checks if the current time is within the configured skipped time slot.
	 * Expected format: "HH:mm-HH:mm" (e.g., "09:00-17:00")
	 * 
	 * @return true if current time is in skipped slot, false otherwise
	 */
	private boolean isInSkippedTimeSlot() {
		String timeSlot = excludedTimeSlot();
		if (timeSlot == null || timeSlot.isBlank()) {
			return false;
		}
		
		try {
			String[] parts = timeSlot.split("-");
			if (parts.length != 2) {
				logger.warn("Invalid skipped time slot format: " + timeSlot + ". Expected format: HH:mm-HH:mm");
				return false;
			}
			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
			LocalTime startTime = LocalTime.parse(parts[0].trim(), formatter);
			LocalTime endTime = LocalTime.parse(parts[1].trim(), formatter);
			LocalTime currentTime = LocalTime.now();
			
			// Handle time slots that span midnight
			if (endTime.isBefore(startTime)) {
				return !currentTime.isBefore(startTime) || !currentTime.isAfter(endTime);
			} else {
				return !currentTime.isBefore(startTime) && !currentTime.isAfter(endTime);
			}
			
		} catch (Exception e) {
			logger.warn("Error parsing skipped time slot: " + timeSlot, e);
			return false;
		}
	}

	private List<NodeRef> getWhereUsedProducts(NodeRef channelProduct, Date referenceDate) {
		try {
			QName associationQName = evaluateWUsedAssociation(channelProduct);
			if (associationQName != null) {
				MultiLevelListData wUsedData = wUsedListService.getWUsedEntity(Arrays.asList(channelProduct), WUsedOperator.AND, associationQName,
						RepoConsts.MAX_DEPTH_LEVEL);
				
				List<NodeRef> allWhereUsed = wUsedData.getAllChilds();
				if (logger.isTraceEnabled()) {
					logger.trace("WUsed to apply:" + wUsedData.toString());
					logger.trace("WUsed size :" + allWhereUsed.size());
					
				}
				
				List<NodeRef> wUsedList = new ArrayList<>(allWhereUsed);
				wUsedList.removeIf(w -> {
					Date formulatedDate = (Date) nodeService.getProperty(w, BeCPGModel.PROP_FORMULATED_DATE);
					return formulatedDate != null && formulatedDate.after(referenceDate);
				});
				
				wUsedList.removeIf(e -> !accept(e));
				return wUsedList;
			}
		} catch (Exception e) {
			Throwable validCause = ExceptionStackUtil.getCause(e, RetryingTransactionHelper.RETRY_EXCEPTIONS);
			if (validCause != null) {
				throw (RuntimeException) validCause;
			}
			logger.error(e, e);
		}
		return List.of();
	}

	private List<NodeRef> getSecurityRuleProducts(NodeRef channelProduct, Date referenceDate) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
		String dateRange = dateFormat.format(referenceDate);
		ACLGroupData aclGroupData = (ACLGroupData) alfrescoRepository.findOne(channelProduct);
		String nodeTypeString = aclGroupData.getNodeType();
		if ((nodeTypeString != null) && nodeTypeString.contains(":")) {
			QName nodeType = QName.createQName(nodeTypeString.split(":")[0], nodeTypeString.split(":")[1], namespacePrefixResolver);
			if (entityDictionaryService.isSubClass(nodeType, PLMModel.TYPE_PRODUCT) && isACLApplied(aclGroupData)) {
				return transactionService.getRetryingTransactionHelper()
						.doInTransaction(() -> {
							BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery()
									.excludeVersions()
									.excludeArchivedEntities()
									.ofType(nodeType)
									.orBetween(BeCPGModel.PROP_FORMULATED_DATE, "MIN", dateRange)
									.inDB().ftsLanguage();
							return WorkProviderFactory.fromQueryBuilder(queryBuilder).collect();
						}, false, true);
			}
		}
		return List.of();
	}
	
	@SuppressWarnings("unchecked")
	private boolean needsFormulation(NodeRef channelProduct) {
		
		List<String> channelIds = (List<String>) nodeService.getProperty(channelProduct, PublicationModel.PROP_CHANNELIDS);
		if (channelIds != null && channelIds.contains(FORMULATE_ENTITIES_CHANNEL_ID)) {
			return true;
		}
		
		Date formulatedDate = (Date) nodeService.getProperty(channelProduct, BeCPGModel.PROP_FORMULATED_DATE);
		if (formulatedDate == null) {
			return true;
		}
		
		Date modifiedDate = (Date) nodeService.getProperty(channelProduct, ContentModel.PROP_MODIFIED);
		if (modifiedDate != null && modifiedDate.after(formulatedDate)) {
			// Check if enough time has passed since modification
			if (!hasEnoughTimePassedSinceModification(modifiedDate)) {
				if (logger.isDebugEnabled()) {
					logger.debug("Product modified too recently: " + channelProduct);
				}
				return false;
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Checks if enough time has passed since the last modification based on
	 * the configured numberOfHoursBeforeFormulation.
	 * 
	 * @param modifiedDate the date of last modification
	 * @return true if enough time has passed, false otherwise
	 */
	private boolean hasEnoughTimePassedSinceModification(Date modifiedDate) {
		if (modifiedDate == null) {
			return true;
		}
		
		try {
			Integer minHoursSinceModification = minHoursSinceModification();
			if (minHoursSinceModification == null || minHoursSinceModification <= 0) {
				// If not configured or set to 0, no waiting period required
				return true;
			}
			
			Calendar cal = Calendar.getInstance();
			cal.setTime(modifiedDate);
			cal.add(Calendar.HOUR_OF_DAY, minHoursSinceModification);
			Date thresholdDate = cal.getTime();
			
			Date now = new Date();
			return now.after(thresholdDate);
			
		} catch (Exception e) {
			logger.warn("Error checking time since modification, allowing formulation to proceed", e);
			return true;
		}
	}

	private void publishEntityChannel(NodeRef entityNodeRef) {
		policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
		NodeRef channelListNodeRef = publicationChannelService.getOrCreateChannelListNodeRef(entityNodeRef, FORMULATE_ENTITIES_CHANNEL_ID);
		nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_STATUS, PublicationChannelStatus.COMPLETED);
		nodeService.setProperty(channelListNodeRef, PublicationModel.PROP_PUBCHANNELLIST_PUBLISHEDDATE, new Date());
	}
	
	private int getTypePriority(QName type) {
		// Priority 1: Base materials that are usually children
		if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_RAWMATERIAL)
				|| entityDictionaryService.isSubClass(type, PLMModel.TYPE_PACKAGINGMATERIAL)) {
			return 1;
		}
		// Priority 2: Intermediate products
		if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_SEMIFINISHEDPRODUCT)
				|| entityDictionaryService.isSubClass(type, PLMModel.TYPE_PACKAGINGKIT)) {
			return 2;
		}
		// Priority 3: Top-level products
		if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_FINISHEDPRODUCT)) {
			return 3;
		}
		// Priority 4: Other complex types
		if (entityDictionaryService.isSubClass(type, PLMModel.TYPE_LOGISTICUNIT)) {
			return 4;
		}

		// Default priority for anything else
		return 10;
	}
	
	private Date extractReferenceDate(NodeRef entityNodeRef) {
		Date referenceDate = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_MODIFIED);
		if (referenceDate == null) {
			referenceDate = (Date) nodeService.getProperty(entityNodeRef, ContentModel.PROP_CREATED);
		}
		return referenceDate;
	}
	
	private class ReformulateChangedEntitiesProcessWorker extends BatchProcessor.BatchProcessWorkerAdaptor<NodeRef> {

		private List<NodeRef> toPublishProducts;

		public ReformulateChangedEntitiesProcessWorker(List<NodeRef> toPublishProducts) {
			this.toPublishProducts = toPublishProducts;
		}

		@Override
		public void process(NodeRef toProcess) throws Throwable {
			// First check: Does the node still exist?
			if (!nodeService.exists(toProcess)) {
				return;
			}
			
			if (toPublishProducts.contains(toProcess)) {
				publishEntityChannel(toProcess);
				return;
			}
			
			// Get the entity data
			RepositoryEntity entity = alfrescoRepository.findOne(toProcess);
			if (!(entity instanceof FormulatedEntity)) {
				return;
			}

			// If the check passes, proceed with formulation
			if (logger.isDebugEnabled()) {
				logger.debug("Reformulating product: " + nodeService.getProperty(toProcess, ContentModel.PROP_NAME) + " (" + toProcess + ")");
			}

			try {
				policyBehaviourFilter.disableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.disableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);

				// Initialize stopwatch only in debug mode
				StopWatch stopWatch = null;
				String nodeName = null;
				if (logger.isDebugEnabled()) {
					stopWatch = new StopWatch("formulation");
					stopWatch.start("formulate");
					nodeName = nodeService.getProperty(toProcess, ContentModel.PROP_NAME).toString();
				}
				
				// Using L2CacheSupport is good practice.
				L2CacheSupport.doInCacheContext(() -> AuthenticationUtil.runAsSystem(() -> formulationService.formulate(toProcess)), false, true);
				
				// Log execution time only in debug mode
				if (logger.isDebugEnabled() && stopWatch != null) {
					stopWatch.stop();
					logger.debug("Formulation time for " + nodeName + " (" + toProcess + "): " + stopWatch.getTotalTimeMillis() + " ms");
				}

				BeCPGTransactionUtil.bindLateTransactionListener(new TransactionListenerAdapter() {
					@Override
					public void afterCommit() {
						transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
							publishEntityChannel(toProcess);
							return null;
						}, false, true);
					}
				});
				
			} finally {
				policyBehaviourFilter.enableBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
				policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
				policyBehaviourFilter.enableBehaviour(BeCPGModel.TYPE_ENTITYLIST_ITEM);
			}
		}
	}
	
	private boolean isACLApplied(ACLGroupData aclGroupData) {
		for (ACLEntryDataItem acl : aclGroupData.getAcls()) {
			String propNameString = acl.getPropName();
			if (SecurityFormulationHandler.VIEW_DOCUMENTS.equals(propNameString)) {
				return true;
			}
			if ((propNameString != null) && propNameString.contains(":") && enforceACL()) {
				QName propName = QName.createQName(propNameString.split(":")[0], propNameString.split(":")[1], namespacePrefixResolver);
				if (entityDictionaryService.isSubClass(propName, BeCPGModel.TYPE_ENTITYLIST_ITEM)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private QName evaluateWUsedAssociation(NodeRef targetAssocNodeRef) {

		QName nodeType = nodeService.getType(targetAssocNodeRef);

		if (nodeType.isMatch(PLMModel.TYPE_RAWMATERIAL) || nodeType.isMatch(PLMModel.TYPE_LOCALSEMIFINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_SEMIFINISHEDPRODUCT) || nodeType.isMatch(PLMModel.TYPE_FINISHEDPRODUCT)
				|| nodeType.isMatch(PLMModel.TYPE_LOGISTICUNIT)) {
			return PLMModel.ASSOC_COMPOLIST_PRODUCT;
		} else if (nodeType.isMatch(PLMModel.TYPE_PACKAGINGMATERIAL) || nodeType.isMatch(PLMModel.TYPE_PACKAGINGKIT)) {
			return PLMModel.ASSOC_PACKAGINGLIST_PRODUCT;
		} else if (nodeType.isMatch(PLMModel.TYPE_RESOURCEPRODUCT)) {
			return MPMModel.ASSOC_PL_RESOURCE;
		}

		return null;
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

}

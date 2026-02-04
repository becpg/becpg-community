package fr.becpg.repo.notification.impl;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.RepoService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.notification.NotificationRuleService;
import fr.becpg.repo.notification.data.NotificationRuleListDataItem;
import fr.becpg.repo.notification.data.ScriptMode;
import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.search.SearchRuleService;
import fr.becpg.repo.search.data.SearchRuleFilter;
import fr.becpg.repo.search.data.SearchRuleResult;
import fr.becpg.repo.search.data.VersionFilterType;

/**
 * <p>NotificationRuleServiceImpl class.</p>
 *
 * @author rabah
 * @version $Id: $Id
 */
@Service("notificationRuleService")
public class NotificationRuleServiceImpl implements NotificationRuleService {

	private static final Log logger = LogFactory.getLog(NotificationRuleServiceImpl.class);

	private static final String DATE_FIELD = "dateField";
	private static final String NODE_TYPE = "type";
	private static final String NODE = "node";
	private static final String ITEM = "item";
	private static final String ITEMS = "items";
	private static final String NOTIFICATION = "notification";
	private static final String TARGET_PATH = "targetPath";
	private static final String ENTITYV2_SUBTYPE = "isEntityV2SubType";
	private static final String DISPLAY_PATH = "displayPath";
	private static final String DISPLAY_NAME = "displayName";

	@Autowired
	private NodeService nodeService;

	@Autowired
	private BeCPGMailService mailService;

	@Autowired
	@Qualifier("ServiceRegistry")
	private ServiceRegistry serviceRegistry;

	@Autowired
	private NamespaceService namespaceService;

	@Autowired
	private AuthorityService authorityService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EntityDictionaryService dictionaryService;

	@Autowired
	private SearchRuleService searchRuleService;

	@Autowired
	private ScriptService scriptService;

	@Autowired
	private BatchQueueService batchQueueService;

	@Autowired
	private AlfrescoRepository<NotificationRuleListDataItem> alfrescoRepository;

	@Autowired
	private ReportTplService reportTplService;

	@Autowired
	private ExportSearchService exportSearchService;

	@Autowired
	private DownloadService downloadService;

	@Autowired
	private RepoService repoService;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private FileFolderService fileFolderService;

	@Autowired
	private PersonService personService;

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	/** {@inheritDoc} */
	@Override
	public void sendNotifications() {
		for (NodeRef notificationNodeRef : getAllNotificationRule()) {
			NotificationRuleListDataItem notification = alfrescoRepository.findOne(notificationNodeRef);
			if ((notification != null) && !shouldSkip(notification)) {

				initializeNotification(notification);

				SearchRuleFilter filter = buildSearchFilter(notification);
				SearchRuleResult searchResult = searchRuleService.search(filter);

				if (!isEmptyResult(notification, searchResult, filter)) {

					Map<String, Object> templateArgs = buildTemplateArgs(notification, filter);
					Map<NodeRef, Object> entities = buildEntities(searchResult.getResults(), notification);

					Consumer<NodeRef> mailSender = createMailSender(notification, templateArgs, entities, filter, searchResult);

					if (CollectionUtils.isNotEmpty(notification.getReportTpls())) {
						processReportTemplates(notification, searchResult, mailSender);
					} else {
						mailSender.accept(null);
					}

					executeScriptIfExists(notification, searchResult.getResults(), templateArgs);
				} else {
					logNoObjects(notification, filter);

				}
			}
		}
	}

	
	private boolean shouldSkip(NotificationRuleListDataItem notification) {
		boolean skip = (notification.getNodeType() == null) || (notification.getAuthorities() == null) || !isAllowed(notification)
				|| Boolean.TRUE.equals(notification.getDisabled());
		if (skip) {
			logger.debug("Skipping notification: " + notification.getSubject());
		}
		return skip;
	}

	private void initializeNotification(NotificationRuleListDataItem notification) {
		notification.setFrequencyStartDate(new Date());
		notification.setErrorLog(null);
		alfrescoRepository.save(notification);
	}

	private SearchRuleFilter buildSearchFilter(NotificationRuleListDataItem notification) {
		SearchRuleFilter filter = new SearchRuleFilter();
		if ((notification.getCondtions() != null) && !notification.getCondtions().isEmpty()) {
			filter.fromJsonObject(new JSONObject(notification.getCondtions()), namespaceService);
		}
		filter.setNodeType(QName.createQName(notification.getNodeType(), namespaceService));
		filter.setDateField(QName.createQName(notification.getDateField(), namespaceService));
		if ((notification.getTarget() != null) && nodeService.exists(notification.getTarget())) {
			filter.setNodePath(nodeService.getPath(notification.getTarget()));
		}
		filter.setDateFilterDelay(notification.getDays());
		filter.setVersionFilterType(notification.getVersionFilterType());
		filter.setDateFilterType(notification.getTimeType());
		filter.setMaxResults(RepoConsts.MAX_RESULTS_1000);
		return filter;
	}

	private boolean isEmptyResult(NotificationRuleListDataItem notification, SearchRuleResult result, SearchRuleFilter filter) {
		List<NodeRef> items = result.getResults();
		Map<NodeRef, Map<String, NodeRef>> itemVersions = result.getItemVersions();
		return !Boolean.TRUE.equals(notification.getEnforced())
				&& ((items == null) || items.isEmpty() || (!VersionFilterType.NONE.equals(filter.getVersionFilterType()) && itemVersions.isEmpty()));
	}

	private void logNoObjects(NotificationRuleListDataItem notification, SearchRuleFilter filter) {
		logger.debug("No objects found for notification: " + notification.getSubject());
		if (!VersionFilterType.NONE.equals(filter.getVersionFilterType())) {
			logger.debug(" - version filter did not match any objects");
		}
	}

	private Map<String, Object> buildTemplateArgs(NotificationRuleListDataItem notification, SearchRuleFilter filter) {
		Map<String, Object> args = new HashMap<>();
		QName nodeType = filter.getNodeType();

		args.put(NODE_TYPE, Objects.toString(dictionaryService.getType(nodeType).getTitle(dictionaryService), nodeType.toPrefixString()));
		args.put(DATE_FIELD, Objects.toString(dictionaryService.getTitle(dictionaryService.getProperty(filter.getDateField()), nodeType),
				filter.getDateField().toPrefixString()));

		if ((notification.getTarget() != null) && nodeService.exists(notification.getTarget())) {
			args.put(TARGET_PATH, filter.getNodePath().subPath(2, filter.getNodePath().size() - 1).toDisplayPath(nodeService, permissionService) + "/"
					+ nodeService.getProperty(notification.getTarget(), ContentModel.PROP_NAME));
		}

		args.put(NOTIFICATION, notification.getNodeRef());
		return args;
	}

	private Map<NodeRef, Object> buildEntities(List<NodeRef> items, NotificationRuleListDataItem notification) {
		Map<NodeRef, Object> entities = new HashMap<>();
		QName nodeType = QName.createQName(notification.getNodeType(), namespaceService);
		QName pivotAssoc = dictionaryService.isSubClass(nodeType, BeCPGModel.TYPE_ENTITYLIST_ITEM) ? dictionaryService.getDefaultPivotAssoc(nodeType)
				: null;

		for (NodeRef nodeRef : items) {
			Map<String, Object> entity = new HashMap<>();
			entity.put(NODE, nodeRef);
			entity.put(DISPLAY_PATH,
					SiteHelper.extractSiteDisplayPath(nodeService.getPath(nodeRef), permissionService, nodeService, namespaceService));

			if (pivotAssoc != null) {
				entity.put(DISPLAY_NAME, nodeService.getTargetAssocs(nodeRef, pivotAssoc).stream().findFirst().map(AssociationRef::getTargetRef)
						.map(attributeExtractorService::extractPropName).orElse(StringUtils.EMPTY));
			}

			entity.put(ENTITYV2_SUBTYPE, dictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITY_V2));
			entities.put(nodeRef, entity);
		}
		return entities;
	}

	private Consumer<NodeRef> createMailSender(NotificationRuleListDataItem notification, Map<String, Object> templateArgs,
			Map<NodeRef, Object> entities, SearchRuleFilter searchFilter, SearchRuleResult searchResult) {
		return downloadNode -> {
			Set<String> sentUsers = new HashSet<>();
			for (NodeRef authorityRef : notification.getAuthorities()) {
				for (String userName : extractAuthoritiesFromGroup(authorityRef)) {
					if (!sentUsers.add(userName) || emailAdminNotificationDisabled(userName)) {
						continue;
					}

					List<Object> userEntities = searchResult.getResults().stream()
							.filter(node -> Boolean.TRUE.equals(AuthenticationUtil.runAs(
									() -> permissionService.hasPermission(node, PermissionService.READ).equals(AccessStatus.ALLOWED), userName)))
							.map(entities::get).toList();

					if (!userEntities.isEmpty() || Boolean.TRUE.equals(notification.getEnforced())) {
						sendMail(notification, templateArgs, searchFilter, searchResult, downloadNode, userName, userEntities);
					}
				}
			}
		};
	}

	private void sendMail(NotificationRuleListDataItem notification, Map<String, Object> templateArgs, SearchRuleFilter searchFilter,
			SearchRuleResult searchResult, NodeRef downloadNode, String userName, List<Object> userEntities) {
		Map<String, Object> userTemplate = new HashMap<>(templateArgs);
		userTemplate.put("entities", userEntities);
		if (!VersionFilterType.NONE.equals(searchFilter.getVersionFilterType())) {
			userTemplate.put("versions", searchResult.getItemVersions());
		}

		Map<String, Object> model = new HashMap<>();
		model.put("args", userTemplate);
		if (downloadNode != null) {
			permissionService.setPermission(downloadNode, userName, PermissionService.READ, true);
			model.put("exportNodeRef", downloadNode.toString());
		}

		String emailTemplate = notification.getEmail() != null ? nodeService.getPath(notification.getEmail()).toPrefixString(namespaceService)
				: RepoConsts.EMAIL_NOTIF_RULE_LIST_TEMPLATE;

		mailService.sendMail(List.of(authorityService.getAuthorityNodeRef(userName)), notification.getSubject(), emailTemplate, model, false);
	}

	private void processReportTemplates(NotificationRuleListDataItem notification, SearchRuleResult searchResult, Consumer<NodeRef> mailSender) {
		final var exportPath = String.join(RepoConsts.PATH_SEPARATOR, RepoConsts.PATH_SYSTEM, RepoConsts.PATH_EXCHANGE, RepoConsts.PATH_EXPORT,
				RepoConsts.PATH_NOTIFICATIONS);
		final var exportFolder = repoService.getFolderByPath(exportPath);

		for (NodeRef reportTpl : notification.getReportTpls()) {
			NodeRef downloadNode = exportSearchService.createReport(QName.createQName(notification.getNodeType(), namespaceService), reportTpl,
					searchResult.getResults(), reportTplService.getReportFormat(reportTpl));

			final var batchId = "notificationReportDownload-" + notification.getNodeRef() + "-" + reportTpl.getId();
			BatchInfo batchInfo = new BatchInfo(batchId, "becpg.batch." + batchId);

			BatchStep<NodeRef> step = new BatchStep<>();
			step.setWorkProvider(new EntityListBatchProcessWorkProvider<>(List.of(downloadNode)));
			step.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<>() {
				@Override
				public void process(NodeRef node) throws Throwable {
					NodeRef exportNode = waitAndSaveDownload(node, notification, exportFolder, reportTpl);
					mailSender.accept(exportNode);
				}
			});

			step.setBatchStepListener(new BatchStepAdapter() {
				@Override
				public void onError(String lastErrorEntryId, String lastError) {
					notification.setErrorLog(lastError);
					alfrescoRepository.save(notification);
				}
			});

			batchQueueService.queueBatch(batchInfo, List.of(step));
		}
	}

	private NodeRef waitAndSaveDownload(NodeRef downloadNode, NotificationRuleListDataItem notification, NodeRef exportFolder, NodeRef reportTpl)
			throws InterruptedException {
		final long startTime = System.currentTimeMillis();
		DownloadStatus status;

		while ((status = transactionService.getRetryingTransactionHelper().doInTransaction(() -> downloadService.getDownloadStatus(downloadNode),
				true, true)).getStatus() != DownloadStatus.Status.DONE) {

			long elapsed = System.currentTimeMillis() - startTime;

			switch (status.getStatus()) {
			case CANCELLED, MAX_CONTENT_SIZE_EXCEEDED -> {
				return null;
			}
			case PENDING -> {
				if (elapsed > 30_000) {
					notification.setErrorLog("Export still pending after 30s");
					return null;
				}
			}
			case IN_PROGRESS -> {
				if (elapsed > 300_000) {
					notification.setErrorLog("Export exceeded 5min");
					return null;
				}
			}
			default -> logger.debug(StringUtils.capitalize(status.getStatus().name()));
			}
			Thread.sleep(5000);
		}

		String name = notification.getSubject() + "_" + nodeService.getProperty(reportTpl, ContentModel.PROP_NAME);
		NodeRef exportNode = nodeService.getChildByName(exportFolder, ContentModel.ASSOC_CONTAINS, name);
		if (exportNode == null) {
			exportNode = fileFolderService.create(exportFolder, name, ContentModel.TYPE_CONTENT).getNodeRef();
		}

		nodeService.setProperty(exportNode, ContentModel.PROP_NAME, name);
		fileFolderService.getWriter(exportNode).putContent(fileFolderService.getReader(downloadNode));

		return exportNode;
	}

	private void executeScriptIfExists(NotificationRuleListDataItem notification, List<NodeRef> items, Map<String, Object> templateArgs) {
		if ((notification.getScript() != null) && nodeService.exists(notification.getScript())) {
			executeScript(notification, items, templateArgs);
		}
	}

	private boolean emailAdminNotificationDisabled(String username) {
		NodeRef person = personService.getPerson(username);
		if (person != null) {
			Serializable emailAdminNotificationDisabled = nodeService.getProperty(person, BeCPGModel.PROP_EMAIL_ADMIN_NOTIFICATION_DISABLED);
			if (Boolean.TRUE.equals(emailAdminNotificationDisabled)) {
				if (logger.isDebugEnabled()) {
					logger.debug("emailAdminNotificationDisabled for " + username);
				}
				return true;
			}
		}
		return false;
	}

	private void executeScript(NotificationRuleListDataItem notification, List<NodeRef> items, Map<String, Object> templateArgs) {

		if (ScriptMode.EACH.equals(notification.getScriptMode())) {
			executeScriptEach(notification, items, templateArgs);
		} else if (ScriptMode.ALL.equals(notification.getScriptMode())) {
			try {
				executeScriptAll(notification, items, templateArgs);
			} catch (Throwable e) {
				if (RetryingTransactionHelper.extractRetryCause(e) != null) {
					if (logger.isDebugEnabled()) {
						logger.debug("Retrying the formulation due to exception " + e.getMessage());
					}
	                throw e;
	            }
				logger.error("Error while executing script from notification " + notification.getNodeRef(), e);
				notification.setErrorLog(e.getMessage());
				alfrescoRepository.save(notification);
			}
		}
	}

	private void executeScriptEach(NotificationRuleListDataItem notification, List<NodeRef> items, Map<String, Object> templateArgs) {
		BatchStep<NodeRef> batchStep = new BatchStep<>();
		batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(items));
		batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
			@Override
			public void process(NodeRef nodeRef) throws Throwable {
				Map<String, Object> model = buildBaseModel(notification, templateArgs);
				model.put(ITEM, new ScriptNode(nodeRef, serviceRegistry));
				scriptService.executeScript(notification.getScript(), ContentModel.PROP_CONTENT, model);
			}
		});
		batchStep.setBatchStepListener(new BatchStepAdapter() {
			@Override
			public void onError(String lastErrorEntryId, String lastError) {
				notification.setErrorLog(lastError);
				alfrescoRepository.save(notification);
			}
		});

		batchQueueService.queueBatch(new BatchInfo("notificationScript-" + notification.getNodeRef().getId(), "becpg.batch.notificationScript"),
				List.of(batchStep));
	}

	private void executeScriptAll(NotificationRuleListDataItem notification, List<NodeRef> items, Map<String, Object> templateArgs) {
		Map<String, Object> model = buildBaseModel(notification, templateArgs);
		model.put(ITEMS, items.stream().map(n -> new ScriptNode(n, serviceRegistry)).toArray());
		scriptService.executeScript(notification.getScript(), ContentModel.PROP_CONTENT, model);
	}

	private Map<String, Object> buildBaseModel(NotificationRuleListDataItem notification, Map<String, Object> templateArgs) {
		Map<String, Object> model = new HashMap<>();
		model.put(NODE_TYPE, templateArgs.get(NODE_TYPE));
		model.put(DATE_FIELD, templateArgs.get(DATE_FIELD));
		model.put(TARGET_PATH, templateArgs.get(TARGET_PATH));
		model.put(NOTIFICATION, new ScriptNode(notification.getNodeRef(), serviceRegistry));
		return model;
	}

	private Set<String> extractAuthoritiesFromGroup(NodeRef authority) {
		Set<String> ret = new HashSet<>();
		if (nodeService.getType(authority).equals(ContentModel.TYPE_AUTHORITY_CONTAINER)) {
			String authorityName = (String) nodeService.getProperty(authority, ContentModel.PROP_AUTHORITY_NAME);
			ret = authorityService.getContainedAuthorities(AuthorityType.USER, authorityName, false);
		} else {
			ret.add((String) nodeService.getProperty(authority, ContentModel.PROP_USERNAME));
		}

		return ret;
	}

	private List<NodeRef> getAllNotificationRule() {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_NOTIFICATIONRULELIST).inDB();
		return queryBuilder.list();
	}

	private boolean isAllowed(NotificationRuleListDataItem notification) {

		LocalDate now = LocalDate.now();

		int frequency = notification.getFrequency();

		Date date = notification.getFrequencyStartDate() != null ? notification.getFrequencyStartDate()
				: (Date) nodeService.getProperty(notification.getNodeRef(), ContentModel.PROP_CREATED);

		LocalDate lastDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		if (now.isBefore(lastDate) || (frequency < 1)) {
			return false;
		}

		lastDate = switch (notification.getRecurringTime()) {
		case Day -> lastDate.plusDays(frequency);
		case Week -> lastDate.plusWeeks(frequency);
		case Month -> lastDate.plusMonths(frequency);
		case Year -> lastDate.plusYears(frequency);
		};

		if (notification.getRecurringDay() != null) {
			return (now.equals(lastDate) || now.isAfter(lastDate)) && now.getDayOfWeek().equals(notification.getRecurringDay());
		}

		return now.equals(lastDate) || now.isAfter(lastDate);
	}

}

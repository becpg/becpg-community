package fr.becpg.repo.notification.impl;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.ScriptService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.AuthorityType;
import org.alfresco.service.cmr.security.PermissionService;
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
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.BatchStep;
import fr.becpg.repo.batch.BatchStepAdapter;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
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
 * @author matthieu
 * @version $Id: $Id
 */
@Service("notificationRuleService")
public class NotificationRuleServiceImpl implements NotificationRuleService {

	private static final Log logger = LogFactory.getLog(NotificationRuleServiceImpl.class);

	private static final String DATE_FIELD = "dateField";
	private static final String NODE_TYPE = "type";
	private static final String NODE = "node";
	private static final String NOTIFICATION = "notification";
	private static final String TARGET_PATH = "targetPath";
	private static final String ENTITYV2_SUBTYPE = "isEntityV2SubType";
	private static final String DISPLAY_PATH = "displayPath";

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
	private DictionaryService dictionaryService;
	
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

	/** {@inheritDoc} */
	@Override
	public void sendNotifications() {

		for (NodeRef notificationNodeRef : getAllNotificationRule()) {

			final Map<String, Object> templateArgs = new HashMap<>();
			final Map<NodeRef, Object> entities = new HashMap<>();

			final NotificationRuleListDataItem notification;
			
			try {
				notification = alfrescoRepository.findOne(notificationNodeRef);
			} catch (final Exception e) {
				logger.warn("Error while retrieving notification data: " + e.getMessage());
				continue;
			}
			try {
				if (notification.getNodeType() == null || notification.getTarget() == null || !nodeService.exists(notification.getTarget())
						|| notification.getAuthorities() == null || !isAllowed(notification)) {
					logger.debug("Skip notification : " + notification.getSubject());
					continue;
				}
				
				notification.setFrequencyStartDate(new Date());
				notification.setErrorLog(null);
				alfrescoRepository.save(notification);
				
				SearchRuleFilter filter = new SearchRuleFilter();
				if ((notification.getCondtions() != null) && !notification.getCondtions().isEmpty()) {
					filter.fromJsonObject(new JSONObject(notification.getCondtions()),namespaceService);
				}
				final QName nodeType = QName.createQName(notification.getNodeType(), namespaceService);
				filter.setNodeType(nodeType);
				filter.setDateField(QName.createQName(notification.getDateField(), namespaceService));
				filter.setNodePath(nodeService.getPath(notification.getTarget()));
				
				filter.setDateFilterDelay(notification.getDays());
				filter.setVersionFilterType(notification.getVersionFilterType());
				filter.setDateFilterType(notification.getTimeType());
				
				SearchRuleResult ret = searchRuleService.search(filter);
				
				List<NodeRef> items = ret.getResults();
				Map<NodeRef, Map<String, NodeRef>> itemVersions = ret.getItemVersions();
				
				if (!notification.isEnforced() && ( items == null  || items.isEmpty() || (!VersionFilterType.NONE.equals(filter.getVersionFilterType()) && itemVersions.isEmpty() )) ) {
					logger.debug("No object found for notification: " + notification.getSubject());
					if(!VersionFilterType.NONE.equals(filter.getVersionFilterType()) && itemVersions.isEmpty() ) {
						logger.debug(" - version filter doesn't match" );
					}
					continue;
				}
				
				templateArgs.put(NODE_TYPE, dictionaryService.getType(nodeType).getTitle(serviceRegistry.getDictionaryService()));
				templateArgs.put(DATE_FIELD, dictionaryService.getProperty(filter.getDateField()).getTitle(serviceRegistry.getDictionaryService()));
				templateArgs.put(TARGET_PATH,
						filter.getNodePath().subPath(2, filter.getNodePath().size() - 1).toDisplayPath(nodeService, permissionService) + "/"
								+ nodeService.getProperty(notification.getTarget(), ContentModel.PROP_NAME));
				templateArgs.put(NOTIFICATION, notification.getNodeRef());
				
				String emailTemplate = notification.getEmail() != null ? nodeService.getPath(notification.getEmail()).toPrefixString(namespaceService)
						: RepoConsts.EMAIL_NOTIF_RULE_LIST_TEMPLATE;
				
				for (NodeRef nodeRef : items) {
					Map<String, Object> item = new HashMap<>();
					item.put(NODE, nodeRef);
					item.put(DISPLAY_PATH,
							SiteHelper.extractSiteDisplayPath(nodeService.getPath(nodeRef), permissionService, nodeService, namespaceService));
					item.put(ENTITYV2_SUBTYPE, dictionaryService.isSubClass(nodeService.getType(nodeRef), BeCPGModel.TYPE_ENTITY_V2));
					entities.put(nodeRef, item);
				}
				final Consumer<NodeRef> mailSender = downloadNode -> {
					Set<String> authorities = new HashSet<>();
					for (NodeRef authorityRef : notification.getAuthorities()) {
						
						for (String userName : extractAuthoritiesFromGroup(authorityRef)) {
							
							if (authorities.contains(userName)) {
								continue;
							}
							
							authorities.add(userName);
							final List<Object> entitiesByUser = new ArrayList<>();
							
							for (NodeRef nodeRef : items) {
								if (Boolean.TRUE.equals(AuthenticationUtil.runAs(
										() -> permissionService.hasPermission(nodeRef, PermissionService.READ_PERMISSIONS).equals(AccessStatus.ALLOWED),
										userName))) {
									
									entitiesByUser.add(entities.get(nodeRef));
									
								}
							}
							if (!entitiesByUser.isEmpty() || notification.isEnforced()) {
								Map<String, Object> templateModel = new HashMap<>();
								HashMap<String, Object> userTemplateArgs = new HashMap<>(templateArgs);
								userTemplateArgs.put("entities", entitiesByUser);
								if (!VersionFilterType.NONE.equals(filter.getVersionFilterType())) {
									userTemplateArgs.put("versions", itemVersions);
								}
								templateModel.put("args", userTemplateArgs);
								if (downloadNode != null) {
									permissionService.setPermission(downloadNode, userName, PermissionService.READ, true);
									templateModel.put("exportNodeRef", downloadNode.toString());
								}
								logger.debug("sendMail");
								mailService.sendMail(Arrays.asList(authorityService.getAuthorityNodeRef(userName)), notification.getSubject(), emailTemplate,
										templateModel, false);
							}
						}
					}
				};
				if (CollectionUtils.isNotEmpty(notification.getReportTpls())) {
					final var exchangeExportNotificationsPath = String.join(RepoConsts.PATH_SEPARATOR, RepoConsts.PATH_SYSTEM,
							RepoConsts.PATH_EXCHANGE, RepoConsts.PATH_EXPORT, RepoConsts.PATH_NOTIFICATIONS);
					final var exchangeExportNotificationsNodeRef = repoService
							.getFolderByPath(exchangeExportNotificationsPath);
					for (final var reportTplNodeRef : notification.getReportTpls()) {
						final var downloadNode = exportSearchService.createReport(nodeType, reportTplNodeRef, items, reportTplService.getReportFormat(reportTplNodeRef));
						// TODO replace thread by batch
						new Thread(() -> {
							AuthenticationUtil.runAsSystem(() ->
								transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
									logger.debug("waiting for download to complete");
									DownloadStatus downloadStatus;
									final var startTime = new Date().getTime();
									statusChecking: while ((downloadStatus = transactionService
											.getRetryingTransactionHelper().doInTransaction(
													() -> downloadService.getDownloadStatus(downloadNode), true, true))
											.getStatus() != DownloadStatus.Status.DONE) {
										final var currentTime = new Date().getTime();
										switch (downloadStatus.getStatus()) {
										case CANCELLED:
										case MAX_CONTENT_SIZE_EXCEEDED:
											break statusChecking;
										case PENDING:
											if (currentTime - startTime > 30_000) {
												notification.setErrorLog("Export still pending after 30 seconds");
												logger.debug(notification.getErrorLog());
												return null;
											}
											break;
										case IN_PROGRESS:
											if (currentTime - startTime > 5 * 60_000) {
												notification.setErrorLog("Export download exceeded 5 minutes");
												logger.debug(notification.getErrorLog());
												return null;
											}
											break;
										default:
											logger.debug(StringUtils.capitalize(downloadStatus.getStatus().name()));
										}
										try {
											Thread.sleep(5_000);
										} catch (InterruptedException e) {
											throw new RuntimeException(e);
										}
									}
									logger.debug("download completed");
									final var name = notification.getName() + "_"
											+ nodeService.getProperty(reportTplNodeRef, ContentModel.PROP_NAME);
									var exportNodeRef = nodeService.getChildByName(exchangeExportNotificationsNodeRef, ContentModel.ASSOC_CONTAINS, name);
									if (exportNodeRef == null) {
										exportNodeRef = fileFolderService.create(exchangeExportNotificationsNodeRef, name, ReportModel.TYPE_REPORT).getNodeRef();
									}
									nodeService.setProperty(exportNodeRef, ContentModel.PROP_NAME, name);
									fileFolderService.getWriter(exportNodeRef).putContent(fileFolderService.getReader(downloadNode));
									mailSender.accept(exportNodeRef);
									return null;
							}, false, true));
						}).start();
					}
				} else 
					mailSender.accept(null);
				if (notification.getScript() != null && nodeService.exists(notification.getScript())) {
					executeScript(notification, items, templateArgs);
				}
			} catch (Exception e) {
				logger.warn("Error while sending notification: " + e.getMessage());
				notification.setErrorLog(e.getMessage());
				alfrescoRepository.save(notification);
			}
		}

	}
	private void executeScript(NotificationRuleListDataItem notification, List<NodeRef> items, Map<String, Object> templateArgs) {
		
		Map<String, Object> model = new HashMap<>();
		model.put(NODE_TYPE, templateArgs.get(NODE_TYPE));
		model.put(DATE_FIELD, templateArgs.get(DATE_FIELD));
		model.put(TARGET_PATH, templateArgs.get(TARGET_PATH));
		model.put(NOTIFICATION, new ScriptNode(notification.getNodeRef(), serviceRegistry));
		
		if (ScriptMode.EACH.equals(notification.getScriptMode())) {
			BatchStep<NodeRef> batchStep = new BatchStep<>();
			batchStep.setWorkProvider(new EntityListBatchProcessWorkProvider<>(items));
			batchStep.setProcessWorker(new BatchProcessor.BatchProcessWorkerAdaptor<NodeRef>() {
				@Override
				public void process(NodeRef nodeRef) throws Throwable {
					model.put("item", new ScriptNode(nodeRef, serviceRegistry));
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
			batchQueueService.queueBatch(new BatchInfo("notificationScript", "becpg.batch.notificationScript"), List.of(batchStep));
		} else if (ScriptMode.ALL.equals(notification.getScriptMode())) {
			model.put("items", items.stream().map(n -> new ScriptNode(n, serviceRegistry)).toArray());
			scriptService.executeScript(notification.getScript(), ContentModel.PROP_CONTENT, model);
		}
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

		if (now.isBefore(lastDate) || frequency < 1) {
			return false;
		}

		switch (notification.getRecurringTime()) {
		case Day:
			lastDate = lastDate.plusDays(frequency);
			break;
		case Week:
			lastDate = lastDate.plusWeeks(frequency);
			break;
		case Month:
			lastDate = lastDate.plusMonths(frequency);
			break;
		case Year:
			lastDate = lastDate.plusYears(frequency);
			break;
		}

		if (notification.getRecurringDay() != null) {
			return (now.equals(lastDate) || now.isAfter(lastDate)) && now.getDayOfWeek().equals(notification.getRecurringDay());
		}

		return now.equals(lastDate) || now.isAfter(lastDate);
	}

}

package fr.becpg.repo.activity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchPriority;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.WorkProviderFactory;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.helper.json.JsonData;
import fr.becpg.repo.helper.json.JsonException;
import fr.becpg.repo.helper.json.JsonHelper;
import fr.becpg.repo.search.BeCPGQueryBuilder;
import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>EntityActivityCleaner class.</p>
 *
 * @author matthieu
 */
@Service
public class EntityActivityCleaner {

    private static final Log logger = LogFactory.getLog(EntityActivityCleaner.class);

    private static final int MAX_PAGE = 50;

    @Autowired
    private BeCPGAuditService beCPGAuditService;

    @Autowired
    private BehaviourFilter policyBehaviourFilter;

    @Autowired
    private BatchQueueService batchQueueService;

    @Autowired
    private EntityListDAO entityListDAO;
    
    @Autowired
    private SystemConfigurationService systemConfigurationService;

    private int getConfInt(String key, int defaultValue) {
		String val = systemConfigurationService.confValue(key);
		if (val != null && !val.isEmpty()) {
			try {
				return Integer.parseInt(val);
			} catch (NumberFormatException e) {
				logger.warn("Invalid integer for config " + key + ": " + val);
			}
		}
		return defaultValue;
	}

	private BatchProcessWorkProvider<NodeRef> createActivityProcessWorkProvider() {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).excludeVersions().inDB().ftsLanguage();
		return WorkProviderFactory.fromQueryBuilder(queryBuilder).build();
	}

    /**
     * Scheduled job: merge and clean entity activities.
     *
     * @param priority a {@link fr.becpg.repo.batch.BatchPriority} object
     * @return a {@link fr.becpg.repo.batch.BatchInfo} object
     */
    public BatchInfo cleanActivities(BatchPriority priority) {

        BatchInfo batchInfo = new BatchInfo("cleanActivities", "becpg.batch.activity.cleanActivities");
        batchInfo.setRunAsSystem(true);
        batchInfo.setPriority(priority);
        
        int threshold = getConfInt("beCPG.activity.purge.threshold", 50);
        int retentionMonths = getConfInt("beCPG.activity.purge.retention.months", 12);
        int batchSize = getConfInt("beCPG.activity.purge.batchSize", 1000);

        BatchProcessWorkProvider<NodeRef> workProvider = createActivityProcessWorkProvider();

        BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

            @Override
            public void process(NodeRef entityNodeRef) throws Throwable {
                int totalDeleted = 0;
                try {
                    policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);

                    if (logger.isDebugEnabled()) {
                        logger.debug("group activities of entity : " + entityNodeRef);
                    }

                    NodeRef activityListNodeRef = getActivityList(entityNodeRef);
                    if (activityListNodeRef == null) {
                        return;
                    }

                    Set<String> users = new HashSet<>();
                    Date cronDate = new Date();

                    if (threshold < 0) {
                        return;
                    }

                    // 1. Calculate retention date
                    Calendar retentionCal = Calendar.getInstance();
                    retentionCal.setTime(cronDate);
                    retentionCal.add(Calendar.MONTH, -retentionMonths);
                    Date retentionDate = retentionCal.getTime();

                    // 2. Count recent activities (Newer than retention date) to determine how many old ones we must keep
                    // We only need to count up to the threshold to know if we are safe
                    AuditQuery recentQuery = AuditQuery.createQuery()
                            .asc(false).dbAsc(false)
                            .sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
                            .filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString())
                            .timeRange(retentionDate, new Date())
                            .maxResults(threshold);

                    List<JSONObject> recentResults = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, recentQuery);
                    int recentCount = recentResults.size();
                    
                    // If we have fewer recent activities than threshold, we must keep some old ones
                    int neededFromOld = Math.max(0, threshold - recentCount);

                    // 3. Fetch old activities to process (Older than retention date)
                    // We fetch DESC (newest first) so the first 'neededFromOld' items are the ones to preserve
                    AuditQuery oldQuery = AuditQuery.createQuery()
                            .asc(false).dbAsc(false)
                            .sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
                            .filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString())
                            .timeRange(null, retentionDate)
                            .maxResults(batchSize); 

                    List<ActivityListDataItem> sortedActivityList = new ArrayList<>();
                    List<JSONObject> oldResults = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, oldQuery);
                    
                    for (int i = 0; i < oldResults.size(); i++) {
                         // Skip the items we need to keep to satisfy the threshold
                         if (i < neededFromOld) {
                             continue;
                         }
                         sortedActivityList.add(AuditActivityHelper.parseActivity(oldResults.get(i)));
                    }

                    int nbrActivity = sortedActivityList.size();

                    if (logger.isDebugEnabled()) {
                        logger.debug("nbrActivity: " + nbrActivity);
                    }

                    if (nbrActivity > 0) {
                        Map<ActivityType, List<ActivityListDataItem>> activitiesByType = new EnumMap<>(ActivityType.class);
                        int activityInPage = 0;
                        boolean hasFormulation = false;
                        boolean hasReport = false;

                        // Pre-size set for efficiency (used for both content and export deduplication)
                        Set<String> contentSet = new HashSet<>(Math.max(16, sortedActivityList.size()));

                        for (ActivityListDataItem activity : sortedActivityList) {
                            if (activityInPage == MAX_PAGE) {
                                hasFormulation = false;
                                hasReport = false;
                                activityInPage = 0;
                            }
                            activityInPage++;

                            Date created = activity.getCreatedDate();
                            if (created.before(cronDate)) {
                                cronDate = created;
                            }

                            ActivityType activityType = activity.getActivityType();

                            boolean toDelete = false;

                            if ((activityType == ActivityType.Formulation && hasFormulation)
                                    || (activityType == ActivityType.Report && hasReport)) {
                                toDelete = true;
                            } else if (activityType == ActivityType.Content) {
                                String contentNodeRef = extractContentNode(activity.getActivityData());
                                if (contentNodeRef != null) {
                                    String dayKey = contentNodeRef + "|" + EntityActivityCleaner.this.toDayKey(created);
                                    if (!contentSet.add(dayKey)) {
                                        toDelete = true;
                                    }
                                }
                            } else if (activityType == ActivityType.Export) {
                                String exportTitle = extractExportTitle(activity.getActivityData());
                                if (exportTitle != null) {
                                    String dayKey = exportTitle + "|" + EntityActivityCleaner.this.toDayKey(created);
                                    if (!contentSet.add(dayKey)) {
                                        toDelete = true;
                                    }
                                }
                            } else if (activityType == ActivityType.Datalist) {
                                String datalistClassName = null;

                                try {
                                	JsonData data = JsonHelper.read(activity.getActivityData());
                                    datalistClassName = data.get(EntityActivityService.PROP_CLASSNAME).getString(null);
                                } catch (JsonException e) {
                                    logger.error("Problem parsing activity data", e);
                                }

                                if (datalistClassName != null) {
                                    String dayKey = datalistClassName + "|" + EntityActivityCleaner.this.toDayKey(created);
                                    if (!contentSet.add(dayKey)) {
                                        toDelete = true;
                                    }
                                }
                            } else {
                                activitiesByType.computeIfAbsent(activityType, k -> new ArrayList<>()).add(activity);

                                if (activityType == ActivityType.Formulation) {
                                    hasFormulation = true;
                                }
                                if (activityType == ActivityType.Report) {
                                    hasReport = true;
                                }
                                users.add(activity.getUserId());
                            }

                            if (toDelete) {
                                deleteAuditActivity(activity);
                                nbrActivity--;
                                totalDeleted++;
                            }
                        }

                        List<ActivityListDataItem> dlActivities = activitiesByType.get(ActivityType.Datalist);
                        if (dlActivities != null) {
                            // Group list by day/week/month/year
                            int[] groupTime = { Calendar.DAY_OF_YEAR, Calendar.WEEK_OF_YEAR, Calendar.MONTH, Calendar.YEAR };
                            for (int i = 0; (i < groupTime.length) && (nbrActivity > MAX_PAGE); i++) {
                                int initialSize = dlActivities.size();
                                dlActivities = group(dlActivities, users, groupTime[i], cronDate);
                                activitiesByType.put(ActivityType.Datalist, dlActivities);
                                nbrActivity = dlActivities.size();
                                totalDeleted += (initialSize - nbrActivity);
                            }
                        }
                    }
                    
                    if (totalDeleted > 0 && logger.isDebugEnabled()) {
                        logger.debug("Deleted " + totalDeleted + " activities for entity " + entityNodeRef);
                    }

                } finally {
                    policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
                }
            }
        };

        batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);

        return batchInfo;
    }

    // Group activities
    private List<ActivityListDataItem> group(List<ActivityListDataItem> activities,
                                             Set<String> users, int timePeriod, Date cronDate) {

        Calendar maxLimit = Calendar.getInstance();
        maxLimit.setTime(new Date());
        maxLimit.set(Calendar.HOUR_OF_DAY, 23);

        Calendar minLimit = Calendar.getInstance();
        minLimit.setTime(new Date());
        minLimit.set(Calendar.HOUR_OF_DAY, 0);

        switch (timePeriod) {
            case Calendar.WEEK_OF_YEAR:
                maxLimit.set(Calendar.DAY_OF_WEEK, maxLimit.getFirstDayOfWeek());
                minLimit.set(Calendar.DAY_OF_WEEK, minLimit.getFirstDayOfWeek());
                minLimit.add(timePeriod, -1);
                break;
            case Calendar.MONTH:
                maxLimit.set(Calendar.DAY_OF_MONTH, 1);
                minLimit.set(Calendar.DAY_OF_MONTH, 1);
                minLimit.add(timePeriod, -1);
                break;
            case Calendar.YEAR:
                maxLimit.add(timePeriod, -1);
                minLimit.add(timePeriod, -2);
                break;
            default:
                // DAY_OF_YEAR: no adjustment
                break;
        }

        while (maxLimit.getTime().after(cronDate)) {
            for (String userId : users) {
                if (activities == null) {
                    continue;
                }

                Map<NodeRef, Set<String>> activitiesByEntity = new HashMap<>(activities.size());
                Iterator<ActivityListDataItem> iter = activities.iterator();

                while (iter.hasNext()) {
                    ActivityListDataItem activity = iter.next();
                    Date createdDate = activity.getCreatedDate();

                    boolean insidePeriod = createdDate.after(minLimit.getTime()) && createdDate.before(maxLimit.getTime());
                    boolean sameUser = userId.equals(activity.getUserId());

                    if (insidePeriod && sameUser) {
                        NodeRef parentRef = activity.getParentNodeRef();
                        String datalistClassName = null;

                        try {
                        	JsonData data = JsonHelper.read(activity.getActivityData());
                            datalistClassName = data.get(EntityActivityService.PROP_CLASSNAME).getString(null);
                        } catch (JsonException e) {
                            logger.error("Problem parsing activity data", e);
                        }

                        if (datalistClassName != null) {
                            Set<String> classNames = activitiesByEntity.computeIfAbsent(parentRef, k -> new HashSet<>());
                            if (!classNames.add(datalistClassName)) {
                                iter.remove();
                                deleteAuditActivity(activity);
                            }
                        }
                    }
                }
            }

            // Move to previous period
            maxLimit.add(timePeriod, -1);
            minLimit.add(timePeriod, -1);
        }

        return activities;
    }

    private NodeRef getActivityList(NodeRef projectNodeRef) {
        NodeRef listNodeRef = null;
        NodeRef listContainerNodeRef = entityListDAO.getListContainer(projectNodeRef);
        if (listContainerNodeRef != null) {
            listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);
        }
        return listNodeRef;
    }

    private void deleteAuditActivity(ActivityListDataItem lastActivity) {
        beCPGAuditService.deleteAuditEntries(AuditType.ACTIVITY, lastActivity.getId(), lastActivity.getId() + 1);
    }

    private String toDayKey(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.get(Calendar.YEAR) + "-" + cal.get(Calendar.DAY_OF_YEAR);
    }

    private String extractContentNode(String alData) {
        try {
        	JsonData data = JsonHelper.read(alData);
            return data.get("contentNodeRef").getString(null);
        } catch (JsonException e) {
            logger.warn("Invalid content activity data: " + alData, e);
            return null;
        }
    }

    private String extractExportTitle(String alData) {
        try {
        	JsonData data = JsonHelper.read(alData);
            return data.get("title").getString(null);
        } catch (JsonException e) {
            logger.warn("Invalid export activity data: " + alData, e);
            return null;
        }
    }
}

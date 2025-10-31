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
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
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
import fr.becpg.repo.search.BeCPGQueryBuilder;

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

	private BatchProcessWorkProvider<NodeRef> createActivityProcessWorkProvider() {
		BeCPGQueryBuilder queryBuilder = BeCPGQueryBuilder.createQuery().ofType(BeCPGModel.TYPE_ENTITY_V2).excludeVersions().inDB().ftsLanguage();
		return WorkProviderFactory.fromQueryBuilder(queryBuilder).build();
	}

    /**
     * Scheduled job: merge and clean entity activities.
     */
    public BatchInfo cleanActivities(BatchPriority priority) {

        BatchInfo batchInfo = new BatchInfo("cleanActivities", "becpg.batch.activity.cleanActivities");
        batchInfo.setRunAsSystem(true);
        batchInfo.setPriority(priority);

        BatchProcessWorkProvider<NodeRef> workProvider = createActivityProcessWorkProvider();

        BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

            @Override
            public void process(NodeRef entityNodeRef) throws Throwable {
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

                    // Get activity list ordered by creation date
                    AuditQuery auditQuery = AuditQuery.createQuery()
                            .asc(false)
                            .dbAsc(false)
                            .sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
                            .filter(ActivityAuditPlugin.ENTITY_NODEREF, entityNodeRef.toString())
                            .maxResults(RepoConsts.MAX_RESULTS_1000000);

                    List<ActivityListDataItem> sortedActivityList = new ArrayList<>();
                    List<JSONObject> results = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);
                    for (JSONObject result : results) {
                        sortedActivityList.add(AuditActivityHelper.parseActivity(result));
                    }

                    int nbrActivity = sortedActivityList.size();

                    // Keep only activities beyond the first 50
                    if (nbrActivity > MAX_PAGE) {
                        sortedActivityList = new ArrayList<>(sortedActivityList.subList(MAX_PAGE, nbrActivity));
                    } else {
                        sortedActivityList = new ArrayList<>();
                    }

                    nbrActivity = sortedActivityList.size();

                    if (logger.isDebugEnabled()) {
                        logger.debug("nbrActivity: " + nbrActivity);
                    }

                    if (nbrActivity > 0) {
                        Map<ActivityType, List<ActivityListDataItem>> activitiesByType = new EnumMap<>(ActivityType.class);
                        int activityInPage = 0;
                        boolean hasFormulation = false;
                        boolean hasReport = false;

                        // Pre-size set for efficiency
                        Set<String> contentSet = new HashSet<>(Math.max(16, sortedActivityList.size()));

                        for (ActivityListDataItem activity : sortedActivityList) {
                            if (activityInPage == MAX_PAGE) {
                                hasFormulation = false;
                                hasReport = false;
                                activityInPage = 0;
                            }

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
                                if (contentNodeRef != null && !contentSet.add(contentNodeRef)) {
                                    toDelete = true;
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
                            }
                        }

                        List<ActivityListDataItem> dlActivities = activitiesByType.get(ActivityType.Datalist);
                        if (dlActivities != null) {
                            // Group list by day/week/month/year
                            int[] groupTime = { Calendar.DAY_OF_YEAR, Calendar.WEEK_OF_YEAR, Calendar.MONTH, Calendar.YEAR };
                            for (int i = 0; (i < groupTime.length) && (nbrActivity > MAX_PAGE); i++) {
                                dlActivities = group(dlActivities, users, groupTime[i], cronDate);
                                activitiesByType.put(ActivityType.Datalist, dlActivities);
                                nbrActivity = dlActivities.size();
                            }
                        }
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
                            JSONObject data = new JSONObject(activity.getActivityData());
                            datalistClassName = data.optString(EntityActivityService.PROP_CLASSNAME, null);
                        } catch (JSONException e) {
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

    private String extractContentNode(String alData) {
        try {
            JSONObject data = new JSONObject(alData);
            return data.optString("contentNodeRef", null);
        } catch (JSONException e) {
            logger.warn("Invalid content activity data: " + alData, e);
            return null;
        }
    }
}

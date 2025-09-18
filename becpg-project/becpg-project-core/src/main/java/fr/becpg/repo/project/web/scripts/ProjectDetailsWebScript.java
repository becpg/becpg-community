package fr.becpg.repo.project.web.scripts;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * <p>ProjectDetailsWebScript class.</p>
 *
 * @author rim
 * @version $Id: $Id
 */
public class ProjectDetailsWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ProjectDetailsWebScript.class);

	private static final String NODE_REF = "nodeRef";

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private ContentService contentService;

	private EntityListDAO entityListDAO;

	private NodeService nodeService;
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>alfrescoRepository</code>.</p>
	 *
	 * @param alfrescoRepository a {@link fr.becpg.repo.repository.AlfrescoRepository} object.
	 */
	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object.
	 */
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	


	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		String nodeRef = req.getParameter(NODE_REF);

		NodeRef pjtNodeRef = new NodeRef(nodeRef);

		try {

			ProjectData data = (ProjectData) alfrescoRepository.findOne(pjtNodeRef);

			JSONObject pjtObject = new JSONObject();
			JSONArray arrayTasks = new JSONArray();

			JSONObject lObj = new JSONObject();

			int milestoneSum = 0, milestoneReleased = 0, remainingDays = 0, userTaskSum = 0;

			double overdueTaskCompletionPerc = 0, userOverdueTaskCompletionPerc = 0, userTaskCompletionPerc = 0;

			List<TaskListDataItem> tasks = data.getTaskList();

			JSONArray commingTaskList = new JSONArray();
			JSONArray overdueTaskList = new JSONArray();

			JSONObject nextMilestoneObject = new JSONObject();
			TaskListDataItem nextMilestoneTask = null;

			Date today = new Date();
			for (int i = 0; i < data.getTaskList().size(); i++) {

				JSONObject taskObject = new JSONObject();
				TaskListDataItem task = tasks.get(i);
				boolean isAuthenticatedUser = isAuthenticatedUser(task.getResources());

				taskObject.put("taskName", nodeService.getProperty(task.getNodeRef(), ProjectModel.PROP_TL_TASK_NAME));
				taskObject.put("isAuthenticatedUser", isAuthenticatedUser);
				taskObject.put("taskCompletionPerc", task.getCompletionPercent());
				taskObject.put("taskduration", task.getDuration());
				taskObject.put("isMilestoneTask", task.getIsMilestone());
				taskObject.put("taskEnd", formatDate(task.getEnd()));
				taskObject.put("taskStart", formatDate(task.getStart()));
				taskObject.put("taskState", task.getState());

				// milestoneDetails
				if (Boolean.TRUE.equals(task.getIsMilestone())) {
					milestoneSum++;
					if (((task.getEnd() != null) && task.getEnd().after(today)) || ((task.getStart() != null) && task.getStart().after(today))) {
						if (nextMilestoneTask == null) {
							nextMilestoneTask = task;
							nextMilestoneObject = taskObject;
						}
						if ((task.getStart() != null) && task.getStart().before(nextMilestoneTask.getStart())) {
							nextMilestoneTask = task;
							nextMilestoneObject = taskObject;
						}
					}
					if ((task.getCompletionPercent() != null) && (task.getCompletionPercent() == 100)) {
						milestoneReleased++;
					}
				}

				// User's tasks percent
				if (isAuthenticatedUser) {
					userTaskSum++;
					double taskCompletionPerc = task.getCompletionPercent() != null ? task.getCompletionPercent() : 0;
					if (userTaskCompletionPerc == 0) {
						userTaskCompletionPerc = taskCompletionPerc;
					} else {
						userTaskCompletionPerc = (taskCompletionPerc + userTaskCompletionPerc) / 2;
					}
				}

				// commingTask && overdueTask && overdueWorkPercent
				if ((task.getEnd() != null) && task.getEnd().before(today)
						&& (task.isPlanned() || task.getState().equals(TaskState.InProgress.toString()))) {
					overdueTaskCompletionPerc += task.getCompletionPercent() != null ? 100 - task.getCompletionPercent() : 100;
					overdueTaskList.put(taskObject);

					if (isAuthenticatedUser) {
						userOverdueTaskCompletionPerc += task.getCompletionPercent() != null ? 100 - task.getCompletionPercent() : 0;
					}

				} else if ((task.getStart() != null) && task.getStart().after(today)
						&& (task.isPlanned() || task.getState().equals(TaskState.InProgress.toString()))) {
					commingTaskList.put(taskObject);
				}

				arrayTasks.put(i, taskObject);

			}

			if ((data.getDueDate() != null) && data.getDueDate().after(today)) {
				remainingDays = getDaysBetween(today, data.getDueDate());
			}

			JSONObject taskList = new JSONObject();
			taskList.put("commingTaskList", commingTaskList);
			taskList.put("overdueTaskList", overdueTaskList);

			lObj.put("projectName", data.getName());
			lObj.put("completionDate", data.getCompletionDate());
			lObj.put("completionPerc", data.getCompletionPercent());
			lObj.put("dueDate", formatDate(data.getDueDate()));
			lObj.put("overdueDays", data.getOverdue());
			lObj.put("projectState", data.getProjectState());
			lObj.put("startDate", formatDate(data.getStartDate()));
			lObj.put("state", data.getState());

			lObj.put("tasks", arrayTasks);
			lObj.put("taskList", taskList);
			if (arrayTasks.length() > 0) {
				lObj.put("overduePerc", (overdueTaskCompletionPerc) / arrayTasks.length());
			} else {
				lObj.put("overduePerc", (overdueTaskCompletionPerc));
			}
			if (userTaskSum > 0) {
				lObj.put("userOverdueTaskCompletionPerc", (userOverdueTaskCompletionPerc) / userTaskSum);
			} else {
				lObj.put("userOverdueTaskCompletionPerc", (userOverdueTaskCompletionPerc));
			}
			lObj.put("userTaskSum", userTaskSum);
			lObj.put("milestoneSum", milestoneSum);
			lObj.put("milestoneReleased", milestoneReleased);
			if (milestoneSum > 0) {
				lObj.put("milestoneReleasedPerc", (milestoneReleased / milestoneSum) * 100);
			} else {
				lObj.put("milestoneReleasedPerc", (milestoneReleased));
			}
			lObj.put("nextMilestoneTask", nextMilestoneObject);
			lObj.put("remainingDays", remainingDays);
			lObj.put("totalTaskNumber", tasks.size());

			List<ActivityListDataItem> activityListDataItems = loadActivityDataList(pjtNodeRef);

			lObj.put("graphData", getActivities(pjtNodeRef, activityListDataItems));
			lObj.put("comments", getComments(pjtNodeRef, activityListDataItems));

			lObj.put("userTaskCompletionPerc", userTaskCompletionPerc);

			pjtObject.put("projectDetails", lObj);

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(pjtObject.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}

	}

	private List<ActivityListDataItem> loadActivityDataList(NodeRef entityNodeRef) {

		NodeRef listContainerNodeRef = entityListDAO.getListContainer(entityNodeRef);

		if (listContainerNodeRef != null) {
			NodeRef dataListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ACTIVITY_LIST);

			if (dataListNodeRef != null) {

				return BeCPGQueryBuilder.createQuery().parent(dataListNodeRef).ofType(BeCPGModel.TYPE_ACTIVITY_LIST)
						.addSort(ContentModel.PROP_CREATED, true).list().stream().map(el -> {
							ActivityListDataItem ret = (ActivityListDataItem) alfrescoRepository.findOne(el);
							ret.setParentNodeRef(dataListNodeRef);
							return ret;
						}).toList();

			}
		}

		return new ArrayList<>();
	}

	private boolean isAuthenticatedUser(List<NodeRef> listNodeRef) {

		// TODO utiliser ProjectService extractResources

		String authenticatedUser = AuthenticationUtil.getFullyAuthenticatedUser();
		String ressource = "";
		for (NodeRef nodeRef : listNodeRef) {
			ressource = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_USERNAME);
			if ((ressource != null) && ressource.equals(authenticatedUser)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * <p>getDaysBetween.</p>
	 *
	 * @param start a {@link java.util.Date} object.
	 * @param end a {@link java.util.Date} object.
	 * @return a int.
	 */
	public int getDaysBetween(Date start, Date end) {
		LocalDate startDate = start.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate endDate = end.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		return (int) ChronoUnit.DAYS.between(startDate, endDate);
	}

	private JSONArray getActivities(NodeRef pjtNodeRef, List<ActivityListDataItem> activityLists) {

		JSONArray ret = new JSONArray();

		Map<String, Integer> temp = new HashMap<>();

		for (ActivityListDataItem activityListDataItem : activityLists) {

			Date activityDate = (Date) nodeService.getProperty(activityListDataItem.getNodeRef(), ContentModel.PROP_CREATED);

			String activityDateStr = formatDate(activityDate);

			if (temp.get(activityDateStr) == null) {
				temp.put(activityDateStr, 1);
			} else {
				temp.put(activityDateStr, temp.get(activityDateStr) + 1);
			}

		}

		for (Map.Entry<String, Integer> entry : temp.entrySet()) {
			Object[] array = new Object[2];
			array[0] = entry.getKey();
			array[1] = entry.getValue();
			ret.put(array);
		}

		return ret;
	}

	private JSONArray getComments(NodeRef pjtNodeRef, List<ActivityListDataItem> activityLists) {
		JSONArray ret = new JSONArray();
		for (ActivityListDataItem activityComment : activityLists) {

			if (activityComment.getActivityType().equals(ActivityType.Comment)) {
				try {
					JSONObject commentObj = new JSONObject();
					commentObj.put("commentCreator", activityComment.getUserId());
					commentObj.put("commentCreationDate",
							formatDate(nodeService.getProperty(activityComment.getNodeRef(), ContentModel.PROP_CREATED)));
					JSONObject activityDataObj = activityComment.getJSONData();

					NodeRef commentRef = new NodeRef((String) activityDataObj.get("commentNodeRef"));

					if (nodeService.exists(commentRef)) {
						ContentReader reader = contentService.getReader(commentRef, ContentModel.PROP_CONTENT);

						if (reader != null) {
							commentObj.put("commentContent", reader.getContentString());
							commentObj.put("commentDetails", activityDataObj);
							ret.put(commentObj);
						}
					}
				} catch (InvalidNodeRefException | JSONException e) {
					logger.error("Unable to serialize JSON", e);
				}
			}
		}
		return ret;
	}

	private String formatDate(Serializable date) {
		if (date != null) {
			return PropertyFormats.forMode(FormatMode.JSON, false).formatDate(date);
		}
		return null;
	}
}

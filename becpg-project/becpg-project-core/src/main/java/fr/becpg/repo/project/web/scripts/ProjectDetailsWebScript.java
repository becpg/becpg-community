package fr.becpg.repo.project.web.scripts;

import java.io.IOException;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

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
 * 
 * @author rim
 *
 */
public class ProjectDetailsWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(ProjectDetailsWebScript.class);

	private static final String NODE_REF = "nodeRef";

	private PropertyFormats format = new PropertyFormats(false);

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	private ContentService contentService;

	private EntityListDAO entityListDAO;

	private NodeService nodeService;

	private PersonService personService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setPersonService(PersonService personService) {
		this.personService = personService;
	}

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

			double overdueTaskCompletionPerc = 0, userOverdueTaskCompletionPerc = 0, userTaskCompletionPerc = -1;

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
				if (task.getIsMilestone()) {
					milestoneSum++;
					if (task.getEnd().after(today)) {
						if (nextMilestoneTask == null) {
							nextMilestoneTask = task;
							nextMilestoneObject = taskObject;
						}
						if (task.getStart().before(nextMilestoneTask.getStart())) {
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
					if (userTaskCompletionPerc < 0) {
						userTaskCompletionPerc = taskCompletionPerc;
					} else {
						userTaskCompletionPerc = (taskCompletionPerc + userTaskCompletionPerc) / 2;
					}
				}

				// commingTask && overdueTask && overdueWorkPercent
				if (task.getEnd().before(today) && !task.getState().equals(TaskState.Completed.toString())) {
					overdueTaskCompletionPerc += task.getCompletionPercent() != null ? 100 - task.getCompletionPercent() : 100;
					overdueTaskList.put(taskObject);

					if (isAuthenticatedUser) {
						userOverdueTaskCompletionPerc += task.getCompletionPercent() != null ? 100 - task.getCompletionPercent() : 0;
					}

				} else if (task.getStart().after(today)) {
					commingTaskList.put(taskObject);
				}

				arrayTasks.put(i, taskObject);

			}

			if (data.getDueDate()!=null && data.getDueDate().after(today)) {
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
			if(arrayTasks.length() > 0) {
				lObj.put("overduePerc", (overdueTaskCompletionPerc) / arrayTasks.length());
			} else {
				lObj.put("overduePerc", (overdueTaskCompletionPerc));
			}
			//TODO
			if(userTaskSum> 0) {
				lObj.put("userOverdueTaskCompletionPerc", (userOverdueTaskCompletionPerc) / userTaskSum);
			} else {
				lObj.put("userOverdueTaskCompletionPerc", (userOverdueTaskCompletionPerc) );
			}
			lObj.put("userTaskSum", userTaskSum);
			lObj.put("milestoneSum", milestoneSum);
			lObj.put("milestoneReleased", milestoneReleased);
			//TODO
			if(milestoneSum> 0) {
				lObj.put("milestoneReleasedPerc", (milestoneReleased / milestoneSum) * 100);
			} else {
				lObj.put("milestoneReleasedPerc", (milestoneReleased ));
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
						}).collect(Collectors.toList());

			}
		}

		return new LinkedList<>();
	}

	private boolean isAuthenticatedUser(List<NodeRef> listNodeRef) {
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
		if(date != null) {
			return format.formatDate(date);
		}
		return null;
	}

	@Deprecated
	String getAvatar(String userId) {
		String avatar = "slingshot/profile/avatar/System/thumbnail/avatar32";

		if (personService.personExists(userId)) {
			NodeRef person = personService.getPerson(userId);

			List<AssociationRef> assocRefs = nodeService.getTargetAssocs(person, ContentModel.ASSOC_AVATAR);
			if ((assocRefs != null) && !assocRefs.isEmpty()) {
				NodeRef avatarRef = assocRefs.get(0).getTargetRef();
				avatar = "api/node/" + avatarRef.toString().replace("://", "/") + "/content/thumbnails/avatar";
			}
		}

		return avatar;

	}
}

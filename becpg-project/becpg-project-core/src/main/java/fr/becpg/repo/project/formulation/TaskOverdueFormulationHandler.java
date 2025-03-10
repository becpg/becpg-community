package fr.becpg.repo.project.formulation;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.project.ProjectNotificationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;
import fr.becpg.repo.project.impl.ProjectHelper;

/**
 * <p>TaskOverdueFormulationHandler class.</p>
 *
 * @author rabah
 * @version $Id: $Id
 */
public class TaskOverdueFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static final Log logger = LogFactory.getLog(TaskOverdueFormulationHandler.class);

	private static final String MAIL_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:project/cm:task-overdue.ftl";
	private static final String MAIL_SUBJECT = "project.notification.overdue.subject";

	private BeCPGMailService beCPGMailService;
	private WorkflowService workflowService;
	private ProjectNotificationService projectNotificationService;
	private ProjectService projectService;
	private NodeService nodeService;
	
	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>beCPGMailService</code>.</p>
	 *
	 * @param beCPGMailService a {@link fr.becpg.repo.mail.BeCPGMailService} object.
	 */
	public void setBeCPGMailService(BeCPGMailService beCPGMailService) {
		this.beCPGMailService = beCPGMailService;
	}

	/**
	 * <p>Setter for the field <code>workflowService</code>.</p>
	 *
	 * @param workflowService a {@link org.alfresco.service.cmr.workflow.WorkflowService} object.
	 */
	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	/**
	 * <p>Setter for the field <code>projectNotificationService</code>.</p>
	 *
	 * @param projectNotificationService a {@link fr.becpg.repo.project.ProjectNotificationService} object.
	 */
	public void setProjectNotificationService(ProjectNotificationService projectNotificationService) {
		this.projectNotificationService = projectNotificationService;
	}

	/**
	 * <p>Setter for the field <code>projectService</code>.</p>
	 *
	 * @param projectService a {@link fr.becpg.repo.project.ProjectService} object.
	 */
	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Checks if notification emails should be sent for each node with notificationParamAspect enabled
	 */
	@Override
	public boolean process(ProjectData projectData) {

		if (ProjectState.InProgress.equals(projectData.getProjectState()) && !projectData.getAspects().contains(BeCPGModel.ASPECT_ENTITY_TPL)) {

			logger.debug("Processing tasks notifications");

			for (TaskListDataItem task : projectData.getTaskList()) {
				if (TaskState.InProgress.equals(task.getTaskState())) {
					Boolean notificationsAreEnabled = ((task.getInitialNotification() != null) && (task.getNotificationAuthorities() != null)
							&& !task.getNotificationAuthorities().isEmpty() && (task.getDue() != null));

					if (Boolean.TRUE.equals(notificationsAreEnabled)) {

						logger.debug("/*-- \tTask " + task.getTaskName() + "\t --*/");

						Date firstNotificationDate = calculateFirstNotificationDate(task);
						Date currentDate = ProjectHelper.removeTime(new Date());
						Date nextNotification = calculateNextNotificationDate(task, firstNotificationDate);
						if (nextNotification != null) {
							String workflowTaskId = extractWorkflowTask(task);
		
								
								if (logger.isDebugEnabled()) {
									logger.debug("First notification: " + firstNotificationDate);
									logger.debug("Last notification: " + task.getLastNotification());
									logger.debug("Next notification: " + nextNotification);
									logger.debug("workflowTaskId: " + workflowTaskId);
								}
	
								if (currentDate.after(nextNotification)) {
									task.setLastNotification(currentDate);
									if(logger.isDebugEnabled()) {
										logger.debug("authoritiesNR: " + task.getNotificationAuthorities());
									}
									if(workflowTaskId !=null) {
										sendTaskNotificationEmails(task.getNotificationAuthorities(), task, projectData, workflowTaskId);
									} else {
										logger.warn("No workflowTaskId for task :"+task.getTaskName()+ " "+task.getNodeRef()+" - skipping notification" );
									}
								}
				
						} else {
							logger.debug("No new notification scheduled, skipping..");
						}
					} else {
						logger.debug("Task " + task.getTaskName() + " does not have notifications enabled, continue..");

					}
				} else {
					logger.debug("Task " + task.getTaskName() + " is not in progress, skipping..");
				}

			}
		} else {
			logger.debug("Project " + projectData.getName() + " not in progress (status: " + projectData.getProjectState() + ")");
		}
		return true;
	}

	/**
	 * <p>calculateNextNotificationDate.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param firstNotificationDate a {@link java.util.Date} object.
	 * @return a {@link java.util.Date} object.
	 */
	public Date calculateNextNotificationDate(TaskListDataItem task, Date firstNotificationDate) {
		if ((task.getLastNotification() == null) || task.getLastNotification().before(firstNotificationDate)) {
			return firstNotificationDate;
		} else if ((task.getNotificationFrequency() != null) && (task.getNotificationFrequency() > 0)) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(task.getLastNotification());
			cal.add(Calendar.DAY_OF_MONTH, task.getNotificationFrequency());
			return ProjectHelper.removeTime(cal.getTime());
		} else {
			//no notif frequency set
			return null;
		}
	}

	// End 01/08 +5 -> 06/08
	/**
	 * <p>calculateFirstNotificationDate.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a {@link java.util.Date} object.
	 */
	public Date calculateFirstNotificationDate(TaskListDataItem task) {
		Calendar firstNotificationCal = Calendar.getInstance();
		firstNotificationCal.setTime(task.getDue());
		firstNotificationCal.add(Calendar.DAY_OF_MONTH, task.getInitialNotification());
		return ProjectHelper.removeTime(firstNotificationCal.getTime());
	}

	/**
	 * <p>sendTaskNotificationEmails.</p>
	 *
	 * @param authorities a {@link java.util.List} object.
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param workflowTaskId a {@link java.lang.String} object.
	 */
	public void sendTaskNotificationEmails(List<NodeRef> authorities, TaskListDataItem task, ProjectData project, String workflowTaskId) {
		Map<String, Object> templateArgs = new HashMap<>();
		templateArgs.put("date", new Date());
		templateArgs.put("task", task.getTaskName());
		templateArgs.put("project", project.getName());
		templateArgs.put("dueDate", task.getDue());
		templateArgs.put("taskId", workflowTaskId);

		authorities = projectService.extractResources(project.getNodeRef(), authorities);
		Map<String, Object> argsMap = new HashMap<>();
		argsMap.put("args", templateArgs);
		
		authorities.removeIf(this::emailProjectNotificationDisabled);
		
		beCPGMailService.sendMail(authorities,
				projectNotificationService.createSubject(project.getNodeRef(), task.getNodeRef(), I18NUtil.getMessage(MAIL_SUBJECT)), MAIL_TEMPLATE,
				argsMap, true);
	}
	
	private boolean emailProjectNotificationDisabled(NodeRef person) {
		if (person != null) {
			Serializable emailProjectNotificationDisabled = nodeService.getProperty(person, BeCPGModel.PROP_EMAIL_PROJECT_NOTIFICATION_DISABLED);
			if (Boolean.TRUE.equals(emailProjectNotificationDisabled)) {
				if (logger.isDebugEnabled()) {
					logger.debug("emailProjectNotificationDisabled for " + nodeService.getProperty(person, ContentModel.PROP_USERNAME));
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>extractWorkflowTask.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String extractWorkflowTask(TaskListDataItem task) {

		if (StringUtils.isEmpty(task.getWorkflowInstance())) {
			logger.debug("Workflow instance is empty");
			return null;
		}

		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(task.getWorkflowInstance());
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
		List<WorkflowTask> foundTasks = workflowService.queryTasks(taskQuery, false);

		if (foundTasks.isEmpty()) {
			logger.debug("found no tasks matching..");
			return null;
		}

		List<WorkflowTask> filteredTasks = foundTasks.stream()
				.filter(found -> task.getNodeRef().equals(found.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK))).collect(Collectors.toList());

		if (filteredTasks.isEmpty()) {
			return null;
		}

		return filteredTasks.get(0).getId();
	}
}

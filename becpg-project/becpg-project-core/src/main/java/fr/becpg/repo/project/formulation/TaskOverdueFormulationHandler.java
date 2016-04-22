package fr.becpg.repo.project.formulation;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.formulation.FormulationBaseHandler;
import fr.becpg.repo.mail.BeCPGMailService;
import fr.becpg.repo.project.ProjectNotificationService;
import fr.becpg.repo.project.ProjectService;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.ProjectState;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.project.data.projectList.TaskState;

public class TaskOverdueFormulationHandler extends FormulationBaseHandler<ProjectData> {

	private static final Log logger = LogFactory.getLog(TaskOverdueFormulationHandler.class);
	
	private static final String MAIL_TEMPLATE = "/app:company_home/app:dictionary/app:email_templates/cm:project/cm:task-overdue.ftl";
	private static final String MAIL_SUBJECT= "notification.emailSubject";	

	private BeCPGMailService beCPGMailService;
	private WorkflowService workflowService;
	private ProjectNotificationService projectNotificationService;
	private ProjectService projectService;

	public void setBeCPGMailService(BeCPGMailService beCPGMailService) {
		this.beCPGMailService = beCPGMailService;
	}

	public void setWorkflowService(WorkflowService workflowService) {
		this.workflowService = workflowService;
	}

	public void setProjectNotificationService(ProjectNotificationService projectNotificationService) {
		this.projectNotificationService = projectNotificationService;
	}

	public void setProjectService(ProjectService projectService) {
		this.projectService = projectService;
	}

	/**
	 * Checks if notification emails should be sent for each node with notificationParamAspect enabled
	 * @return 
	 */
	@Override
	public boolean process(ProjectData projectData) {
		logger.info("Processing tasks notifications");

		if(ProjectState.InProgress.equals(projectData.getProjectState())){

			for(TaskListDataItem task :  projectData.getTaskList()){
				Boolean notificationsAreEnabled = (task.getInitialNotification() != null 
						&& task.getNotificationAuthorities() != null 
						&& !task.getNotificationAuthorities().isEmpty());

				if(!notificationsAreEnabled){
					logger.debug("Task "+task.getTaskName()+" does not have notifications enabled, continue..");
					continue;
				}

				if(!TaskState.InProgress.equals(task.getTaskState())){
					logger.debug("Task "+task.getTaskName()+" is not in progress, skipping..");
					continue;
				}

				logger.debug("/*-- \tTask "+task.getTaskName()+"\t --*/");

				Date firstNotificationDate = calculateFirstNotificationDate(task);
				Date currentDate = new Date();
				Date nextNotification = calculateNextNotificationDate(task, firstNotificationDate);
				String workflowTaskId = extractWorkflowTask(task);
				logger.debug("First notification: "+firstNotificationDate);
				logger.debug("Last notification: "+task.getLastNotification());
				logger.debug("Next notification: "+nextNotification);
				logger.debug("workflowTaskId: "+workflowTaskId);
				if(nextNotification == null){
					logger.debug("No new notification scheduled, skipping..");
					continue;
				}

				if(currentDate.after(nextNotification)){
					task.setLastNotification(nextNotification);
					logger.debug("authoritiesNR: "+task.getNotificationAuthorities());
					sendTaskNotificationEmails(task.getNotificationAuthorities(), task, projectData, workflowTaskId);
				}
			}
		} else {
			logger.info("Project "+projectData.getName()+" not in progress (status: "+projectData.getProjectState()+")");
		}
		return true;
	}

	public Date calculateNextNotificationDate(TaskListDataItem task, Date firstNotificationDate){
		if(task.getLastNotification() == null){
			return firstNotificationDate;
		} else if(task.getNotificationFrequency() != null && task.getNotificationFrequency() > 0){
			Calendar cal = Calendar.getInstance();
			cal.setTime(task.getLastNotification());
			cal.add(Calendar.DAY_OF_MONTH, task.getNotificationFrequency());
			return cal.getTime();
		} else {
			//no notif frequency set
			return null;
		}
	}

	public Date calculateFirstNotificationDate(TaskListDataItem task){
		Calendar firstNotificationCal = Calendar.getInstance();
		firstNotificationCal.setTime(task.getEnd());
		firstNotificationCal.add(Calendar.DAY_OF_MONTH, task.getInitialNotification());
		return firstNotificationCal.getTime();
	}

	public void sendTaskNotificationEmails(List<NodeRef> authorities, TaskListDataItem task, ProjectData project, String workflowTaskId){
		Map<String, Object> templateArgs = new HashMap<>();
		templateArgs.put("date", new Date());
		templateArgs.put("task", task.getTaskName());
		templateArgs.put("project", project.getName());
		templateArgs.put("dueDate", task.getEnd());
		templateArgs.put("taskId", workflowTaskId);
		
		authorities = projectService.extractResources(project.getNodeRef(), authorities);
		Map<String, Object> argsMap = new HashMap<>();
		argsMap.put("args", templateArgs);
		beCPGMailService.sendMail(authorities, 
									projectNotificationService.createSubject(project.getNodeRef(), task.getNodeRef(), I18NUtil.getMessage(MAIL_SUBJECT)),
									MAIL_TEMPLATE, 
									argsMap, true);	 
	}


	public String extractWorkflowTask(TaskListDataItem task){
		
		if(StringUtils.isEmpty(task.getWorkflowInstance())){
			logger.debug("Workflow instance is empty");
			return null;
		}
		
		WorkflowTaskQuery taskQuery = new WorkflowTaskQuery();
		taskQuery.setProcessId(task.getWorkflowInstance());
		taskQuery.setTaskState(WorkflowTaskState.IN_PROGRESS);
		List<WorkflowTask> foundTasks = workflowService.queryTasks(taskQuery, false);

		if(foundTasks.isEmpty()){
			logger.debug("found no tasks matching..");
			return null;
		}

		List<WorkflowTask> filteredTasks = foundTasks.stream()
				.filter(found -> task.getNodeRef().equals(found.getProperties().get(ProjectModel.ASSOC_WORKFLOW_TASK)))
				.collect(Collectors.toList());

		if(filteredTasks.isEmpty()){
			return null;
		}

		return filteredTasks.get(0).getId();
	}
}

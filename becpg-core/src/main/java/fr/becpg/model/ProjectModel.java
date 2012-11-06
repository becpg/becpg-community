package fr.becpg.model;

import org.alfresco.service.namespace.QName;

public interface ProjectModel {

	//
	// Namespace
	//

	/** Project Model URI */
	public static final String PROJECT_URI = "http://www.bcpg.fr/model/project/1.0";

	/** Security Model Prefix */
	public static final String PROJECT_PREFIX = "pjt";
	
	public static final QName TYPE_TASK = QName.createQName(PROJECT_URI, "task");
	public static final QName TYPE_TASK_LEGEND = QName.createQName(PROJECT_URI, "taskLegend");
	public static final QName TYPE_PROJECT_TPL = QName.createQName(PROJECT_URI, "projectTpl");	
	
	public static final QName TYPE_PROJECT = QName.createQName(PROJECT_URI, "project");		
	public static final QName PROP_PROJECT_HIERARCHY1 = QName.createQName(PROJECT_URI, "projectHierarchy1");
	public static final QName PROP_PROJECT_START_DATE = QName.createQName(PROJECT_URI, "projectStartDate");
	public static final QName PROP_PROJECT_DUE_DATE = QName.createQName(PROJECT_URI, "projectDueDate");
	public static final QName PROP_PROJECT_COMPLETION_DATE = QName.createQName(PROJECT_URI, "projectCompletionDate");
	public static final QName PROP_PROJECT_PRIORITY = QName.createQName(PROJECT_URI, "projectPriority");	
	public static final QName ASSOC_PROJECT_TPL = QName.createQName(PROJECT_URI, "projectProjectTpl");
	public static final QName ASSOC_PROJECT_ENTITY = QName.createQName(PROJECT_URI, "projectEntity");
	
	public static final QName TYPE_TASK_LIST = QName.createQName(PROJECT_URI, "taskList");
	public static final QName PROP_TL_TASK_NAME = QName.createQName(PROJECT_URI, "tlTaskName");
	public static final QName PROP_TL_IS_MILESTONE = QName.createQName(PROJECT_URI, "tlIsMilestone");
	public static final QName PROP_TL_DURATION = QName.createQName(PROJECT_URI, "tlDuration");
	public static final QName PROP_TL_WORKFLOW_NAME = QName.createQName(PROJECT_URI, "tlWorkflowName");
	public static final QName PROP_TL_START = QName.createQName(PROJECT_URI, "tlStart");
	public static final QName PROP_TL_END = QName.createQName(PROJECT_URI, "tlEnd");
	public static final QName PROP_TL_STATE = QName.createQName(PROJECT_URI, "tlState");	
	public static final QName ASSOC_TL_PREV_TASKS = QName.createQName(PROJECT_URI, "tlPrevTasks");
	public static final QName ASSOC_TL_RESOURCES = QName.createQName(PROJECT_URI, "tlResources");
	public static final QName ASSOC_TL_TASKLEGEND = QName.createQName(PROJECT_URI, "tlTaskLegend");
	public static final QName PROP_TL_WORKFLOW_INSTANCE = QName.createQName(PROJECT_URI, "tlWorkflowInstance");
	
	public static final QName TYPE_DELIVERABLE_LIST = QName.createQName(PROJECT_URI, "deliverableList");
	public static final QName PROP_DL_STATE = QName.createQName(PROJECT_URI, "dlState");
	public static final QName PROP_DL_DESCRIPTION = QName.createQName(PROJECT_URI, "dlDescription");
	public static final QName ASSOC_DL_TASK = QName.createQName(PROJECT_URI, "dlTask");
	public static final QName ASSOC_DL_CONTENT = QName.createQName(PROJECT_URI, "dlContent");
	
	public static final QName ASPECT_COMPLETION_ASPECT = QName.createQName(PROJECT_URI, "completionAspect");
	public static final QName PROP_COMPLETION_PERCENT = QName.createQName(PROJECT_URI, "completionPercent");
	
	public static final QName ASPECT_WORKFLOW_ASPECT = QName.createQName(PROJECT_URI, "workflowAspect");
	public static final QName ASSOC_WORKFLOW_TASK = QName.createQName(PROJECT_URI, "workflowTask");
}

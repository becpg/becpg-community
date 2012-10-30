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
	public static final QName TYPE_TASK_SET = QName.createQName(PROJECT_URI, "taskSet");
	public static final QName TYPE_PROJECT_TPL = QName.createQName(PROJECT_URI, "projectTpl");
	public static final QName TYPE_PROJECT = QName.createQName(PROJECT_URI, "project");
	public static final QName ASSOC_PROJECT_TPL = QName.createQName(PROJECT_URI, "projectProjectTpl");
	
	public static final QName TYPE_TASK_LIST = QName.createQName(PROJECT_URI, "taskList");
	public static final QName PROP_TL_IS_MILESTONE = QName.createQName(PROJECT_URI, "tlIsMilestone");
	public static final QName PROP_TL_DURATION = QName.createQName(PROJECT_URI, "tlDuration");
	public static final QName PROP_TL_WORKFLOW_NAME = QName.createQName(PROJECT_URI, "tlWorkflowName");
	public static final QName ASSOC_TL_TASKSET = QName.createQName(PROJECT_URI, "tlTaskSet");
	public static final QName ASSOC_TL_TASK = QName.createQName(PROJECT_URI, "tlTask");
	public static final QName ASSOC_TL_PREV_TASKS = QName.createQName(PROJECT_URI, "tlPrevTasks");
	public static final QName ASSOC_TL_ASSIGNEES = QName.createQName(PROJECT_URI, "tlAssignees");
	
	public static final QName TYPE_TASK_HISTORY_LIST = QName.createQName(PROJECT_URI, "taskHistoryList");	
	public static final QName PROP_THL_START = QName.createQName(PROJECT_URI, "thlStart");
	public static final QName PROP_THL_END = QName.createQName(PROJECT_URI, "thlEnd");
	public static final QName PROP_THL_DURATION = QName.createQName(PROJECT_URI, "thlDuration");
	public static final QName PROP_THL_COMMENT = QName.createQName(PROJECT_URI, "thlComment");
	public static final QName PROP_THL_STATE = QName.createQName(PROJECT_URI, "thlState");
	public static final QName ASSOC_THL_TASKSET = QName.createQName(PROJECT_URI, "thlTaskSet");
	public static final QName ASSOC_THL_TASK = QName.createQName(PROJECT_URI, "thlTask");
	
	public static final QName TYPE_DELIVERABLE_LIST = QName.createQName(PROJECT_URI, "deliverableList");
	public static final QName PROP_DL_STATE = QName.createQName(PROJECT_URI, "dlState");
	public static final QName PROP_DL_DESCRIPTION = QName.createQName(PROJECT_URI, "dlDescription");
	public static final QName ASSOC_DL_TASK = QName.createQName(PROJECT_URI, "dlTask");
	
}

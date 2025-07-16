/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.model;

import org.alfresco.service.namespace.QName;

/**
 * <p>
 * ProjectModel interface.
 * </p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ProjectModel {

	/** Project Model URI */
	String PROJECT_URI = "http://www.bcpg.fr/model/project/1.0";

	/** Security Model Prefix */
	String PROJECT_PREFIX = "pjt";

	/** Constant <code>TYPE_TASK_LEGEND</code> */
	QName TYPE_TASK_LEGEND = QName.createQName(PROJECT_URI, "taskLegend");

	/** Constant <code>TYPE_PROJECT</code> */
	QName TYPE_PROJECT = QName.createQName(PROJECT_URI, "project");
	/** Constant <code>PROP_PROJECT_HIERARCHY1</code> */
	QName PROP_PROJECT_HIERARCHY1 = QName.createQName(PROJECT_URI, "projectHierarchy1");
	/** Constant <code>PROP_PROJECT_HIERARCHY2</code> */
	QName PROP_PROJECT_HIERARCHY2 = QName.createQName(PROJECT_URI, "projectHierarchy2");
	/** Constant <code>PROP_PROJECT_START_DATE</code> */
	QName PROP_PROJECT_START_DATE = QName.createQName(PROJECT_URI, "projectStartDate");
	/** Constant <code>PROP_PROJECT_DUE_DATE</code> */
	QName PROP_PROJECT_DUE_DATE = QName.createQName(PROJECT_URI, "projectDueDate");
	/** Constant <code>PROP_PROJECT_COMPLETION_DATE</code> */
	QName PROP_PROJECT_COMPLETION_DATE = QName.createQName(PROJECT_URI, "projectCompletionDate");
	/** Constant <code>PROP_PROJECT_PLANNING_MODE</code> */
	QName PROP_PROJECT_PLANNING_MODE = QName.createQName(PROJECT_URI, "projectPlanningMode");
	/** Constant <code>PROP_PROJECT_PRIORITY</code> */
	QName PROP_PROJECT_PRIORITY = QName.createQName(PROJECT_URI, "projectPriority");
	/** Constant <code>PROP_PROJECT_STATE</code> */
	QName PROP_PROJECT_STATE = QName.createQName(PROJECT_URI, "projectState");
	/** Constant <code>PROP_PROJECT_LEGENDS</code> */
	QName PROP_PROJECT_LEGENDS = QName.createQName(PROJECT_URI, "projectLegends");
	/** Constant <code>ASSOC_PROJECT_ENTITY</code> */
	QName ASSOC_PROJECT_ENTITY = QName.createQName(PROJECT_URI, "projectEntity");
	/** Constant <code>ASSOC_PROJECT_OBSERVERS</code> */
	QName ASSOC_PROJECT_OBSERVERS = QName.createQName(PROJECT_URI, "projectObservers");

	/** Constant <code>PROP_OBSERVERS_EVENTS</code> */
	QName PROP_OBSERVERS_EVENTS = QName.createQName(PROJECT_URI, "observersEvents");

	/** Constant <code>ASSOC_PROJECT_CUR_TASKS</code> */
	QName ASSOC_PROJECT_CUR_TASKS = QName.createQName(PROJECT_URI, "projectCurrentTasks");
	/** Constant <code>ASSOC_PROJECT_CUR_COMMENTS</code> */
	QName ASSOC_PROJECT_CUR_COMMENTS = QName.createQName(PROJECT_URI, "projectCurrentComments");

	/** Constant <code>PROP_PROJECT_CUR_COMMENT</code> */
	QName PROP_PROJECT_CUR_COMMENT = QName.createQName(PROJECT_URI, "projectCurrentComment");

	/** Constant <code>TYPE_TASK_LIST</code> */
	QName TYPE_TASK_LIST = QName.createQName(PROJECT_URI, "taskList");
	/** Constant <code>PROP_TL_TASK_NAME</code> */
	QName PROP_TL_TASK_NAME = QName.createQName(PROJECT_URI, "tlTaskName");
	/** Constant <code>PROP_TL_TASK_DESCRIPTION</code> */
	QName PROP_TL_TASK_DESCRIPTION = QName.createQName(PROJECT_URI, "tlTaskDescription");
	/** Constant <code>PROP_TL_TASK_COMMENT</code> */
	QName PROP_TL_TASK_COMMENT = QName.createQName(PROJECT_URI, "tlTaskComment");

	/** Constant <code>WORKFLOW_TRANSITION</code> */
	QName WORKFLOW_TRANSITION = QName.createQName(PROJECT_URI, "worflowTransition");

	/** Constant <code>PROP_TL_IS_MILESTONE</code> */
	QName PROP_TL_IS_MILESTONE = QName.createQName(PROJECT_URI, "tlIsMilestone");
	/** Constant <code>PROP_TL_IS_EXCLUDE_FROM_SEARCH</code> */
	QName PROP_TL_IS_EXCLUDE_FROM_SEARCH = QName.createQName(PROJECT_URI, "tlIsExcludeFromSearch");
	/** Constant <code>PROP_TL_DURATION</code> */
	QName PROP_TL_DURATION = QName.createQName(PROJECT_URI, "tlDuration");
	/** Constant <code>PROP_TL_CAPACITY</code> */
	QName PROP_TL_CAPACITY = QName.createQName(PROJECT_URI, "tlCapacity");
	/** Constant <code>PROP_TL_WORK</code> */
	QName PROP_TL_WORK = QName.createQName(PROJECT_URI, "tlWork");
	/** Constant <code>PROP_TL_WORKFLOW_NAME</code> */
	QName PROP_TL_WORKFLOW_NAME = QName.createQName(PROJECT_URI, "tlWorkflowName");
	/** Constant <code>PROP_TL_START</code> */
	QName PROP_TL_START = QName.createQName(PROJECT_URI, "tlStart");
	/** Constant <code>PROP_TL_END</code> */
	QName PROP_TL_END = QName.createQName(PROJECT_URI, "tlEnd");
	/** Constant <code>PROP_TL_STATE</code> */
	QName PROP_TL_STATE = QName.createQName(PROJECT_URI, "tlState");
	/** Constant <code>PROP_TL_PREVIOUS_STATE</code> */
	QName PROP_TL_PREVIOUS_STATE = QName.createQName(PROJECT_URI, "tlPreviousState");
	/** Constant <code>ASSOC_TL_PREV_TASKS</code> */
	QName ASSOC_TL_PREV_TASKS = QName.createQName(PROJECT_URI, "tlPrevTasks");
	/** Constant <code>ASSOC_TL_REFUSED_TASK_REF</code> */
	QName ASSOC_TL_REFUSED_TASK_REF = QName.createQName(PROJECT_URI, "tlRefusedTaskRef");
	/** Constant <code>ASSOC_TL_RESOURCES</code> */
	QName ASSOC_TL_RESOURCES = QName.createQName(PROJECT_URI, "tlResources");
	/** Constant <code>PROP_TL_RESOURCES_ASSOC_INDEX</code> */
	QName PROP_TL_RESOURCES_ASSOC_INDEX = QName.createQName(PROJECT_URI, "tlResourcesAssocIndex");
	/** Constant <code>ASSOC_TL_TASKLEGEND</code> */
	QName ASSOC_TL_TASKLEGEND = QName.createQName(PROJECT_URI, "tlTaskLegend");
	/** Constant <code>ASSOC_TL_OBSERVERS</code> */
	QName ASSOC_TL_OBSERVERS = QName.createQName(PROJECT_URI, "tlObservers");
	/** Constant <code>PROP_TL_WORKFLOW_INSTANCE</code> */
	QName PROP_TL_WORKFLOW_INSTANCE = QName.createQName(PROJECT_URI, "tlWorkflowInstance");
	/** Constant <code>PROP_TL_WORKFLOW_TASK_INSTANCE</code> */
	QName PROP_TL_WORKFLOW_TASK_INSTANCE = QName.createQName(PROJECT_URI, "tlWorkflowTaskInstance");
	/** Constant <code>PROP_TL_FIXED_COST</code> */
	QName PROP_TL_FIXED_COST = QName.createQName(PROJECT_URI, "tlFixedCost");
	/** Constant <code>PROP_TL_IS_REFUSED</code> */
	QName PROP_TL_IS_REFUSED = QName.createQName(PROJECT_URI, "tlIsRefused");
	/** Constant <code>PROP_TL_IS_GROUP</code> */
	QName PROP_TL_IS_GROUP = QName.createQName(PROJECT_URI, "tlIsGroup");

	/** Constant <code>ASSOC_SUB_PROJECT</code> */
	QName ASSOC_SUB_PROJECT = QName.createQName(PROJECT_URI, "subProjectRef");
	/** Constant <code>ASSOC_PARENT_PROJECT</code> */
	QName ASSOC_PARENT_PROJECT = QName.createQName(PROJECT_URI, "parentProjectRef");
	/** Constant <code>PROP_TL_MANUAL_DATE</code> */
	QName PROP_TL_MANUAL_DATE = QName.createQName(PROJECT_URI, "tlManualDate");

	/** Constant <code>PROP_TASK_LEGEND_SITES</code> */
	QName PROP_TASK_LEGEND_SITES = QName.createQName(PROJECT_URI, "taskLegendSites");

	/** Constant <code>ASSOC_TL_RESOURCE_COST</code> */
	QName ASSOC_TL_RESOURCE_COST = QName.createQName(PROJECT_URI, "tlResourceCost");

	/** Constant <code>TYPE_DELIVERABLE_LIST</code> */
	QName TYPE_DELIVERABLE_LIST = QName.createQName(PROJECT_URI, "deliverableList");
	/** Constant <code>PROP_DL_STATE</code> */
	QName PROP_DL_STATE = QName.createQName(PROJECT_URI, "dlState");
	/** Constant <code>PROP_DL_URL</code> */
	QName PROP_DL_URL = QName.createQName(PROJECT_URI, "dlUrl");
	/** Constant <code>PROP_DL_DESCRIPTION</code> */
	QName PROP_DL_DESCRIPTION = QName.createQName(PROJECT_URI, "dlDescription");
	/** Constant <code>ASSOC_DL_TASK</code> */
	QName ASSOC_DL_TASK = QName.createQName(PROJECT_URI, "dlTask");
	/** Constant <code>ASSOC_DL_CONTENT</code> */
	QName ASSOC_DL_CONTENT = QName.createQName(PROJECT_URI, "dlContent");

	/** Constant <code>TYPE_SCORE_LIST</code> */
	QName TYPE_SCORE_LIST = QName.createQName(PROJECT_URI, "scoreList");
	/** Constant <code>PROP_SL_SCORE</code> */
	QName PROP_SL_SCORE = QName.createQName(PROJECT_URI, "slScore");
	/** Constant <code>PROP_SL_WEIGHT</code> */
	QName PROP_SL_WEIGHT = QName.createQName(PROJECT_URI, "slWeight");

	/** Constant <code>ASPECT_COMPLETION_ASPECT</code> */
	QName ASPECT_COMPLETION_ASPECT = QName.createQName(PROJECT_URI, "completionAspect");
	/** Constant <code>PROP_COMPLETION_PERCENT</code> */
	QName PROP_COMPLETION_PERCENT = QName.createQName(PROJECT_URI, "completionPercent");

	/** Constant <code>ASPECT_WORKFLOW_ASPECT</code> */
	QName ASPECT_WORKFLOW_ASPECT = QName.createQName(PROJECT_URI, "workflowAspect");
	/** Constant <code>ASSOC_WORKFLOW_TASK</code> */
	QName ASSOC_WORKFLOW_TASK = QName.createQName(PROJECT_URI, "workflowTask");

	/** Constant <code>ASSOC_PROJECT_MANAGER</code> */
	QName ASSOC_PROJECT_MANAGER = QName.createQName(PROJECT_URI, "projectManager");

	/** Constant <code>TYPE_LOG_TIME_LIST</code> */
	QName TYPE_LOG_TIME_LIST = QName.createQName(PROJECT_URI, "logTimeList");
	/** Constant <code>PROP_LTL_TIME</code> */
	QName PROP_LTL_TIME = QName.createQName(PROJECT_URI, "ltlTime");
	/** Constant <code>ASSOC_LTL_TASK</code> */
	QName ASSOC_LTL_TASK = QName.createQName(PROJECT_URI, "ltlTask");

	/** Constant <code>TYPE_RESOURCE_COST</code> */
	QName TYPE_RESOURCE_COST = QName.createQName(PROJECT_URI, "resourceCost");
	/** Constant <code>PROP_RESOURCE_COST_VALUE</code> */
	QName PROP_RESOURCE_COST_VALUE = QName.createQName(PROJECT_URI, "resourceCostValue");
	/** Constant <code>PROP_RESOURCE_COST_BILL_RATE</code> */
	QName PROP_RESOURCE_COST_BILL_RATE = QName.createQName(PROJECT_URI, "resourceCostBillRate");

	// Budget List
	/** Constant <code>TYPE_BUDGET_LIST</code> */
	QName TYPE_BUDGET_LIST = QName.createQName(PROJECT_URI, "budgetList");
	/** Constant <code>PROP_BL_ITEM</code> */
	QName PROP_BL_ITEM = QName.createQName(PROJECT_URI, "blItem");
	/** Constant <code>PROP_BL_PROFIT</code> */
	QName PROP_BL_PROFIT = QName.createQName(PROJECT_URI, "blProfit");
	/** Constant <code>PROP_BL_BUDGEDTED_EXPENSE</code> */
	QName PROP_BL_BUDGEDTED_EXPENSE = QName.createQName(PROJECT_URI, "blBudgetedExpense");
	/** Constant <code>PROP_BL_BUDGEDTED_INVOICE</code> */
	QName PROP_BL_BUDGEDTED_INVOICE = QName.createQName(PROJECT_URI, "blBudgetedInvoice");

	// Invoice List
	/** Constant <code>TYPE_INVOICE_LIST</code> */
	QName TYPE_INVOICE_LIST = QName.createQName(PROJECT_URI, "invoiceList");
	/** Constant <code>ASSOC_IL_BUDGET_REF</code> */
	QName ASSOC_IL_BUDGET_REF = QName.createQName(PROJECT_URI, "ilBudgetRef");
	/** Constant <code>ASSOC_IL_TASK_REF</code> */
	QName ASSOC_IL_TASK_REF = QName.createQName(PROJECT_URI, "ilTaskRef");
	/** Constant <code>ASSOC_IL_DOC_REF</code> */
	QName ASSOC_IL_DOC_REF = QName.createQName(PROJECT_URI, "ilDocRef");

	// Expense List
	/** Constant <code>TYPE_EXPENSE_LIST</code> */
	QName TYPE_EXPENSE_LIST = QName.createQName(PROJECT_URI, "expenseList");
	/** Constant <code>ASSOC_EL_BUDGET_REF</code> */
	QName ASSOC_EL_BUDGET_REF = QName.createQName(PROJECT_URI, "elBudgetRef");
	/** Constant <code>ASSOC_EL_TASK_REF</code> */
	QName ASSOC_EL_TASK_REF = QName.createQName(PROJECT_URI, "elTaskRef");
	/** Constant <code>ASSOC_EL_DOC_REF</code> */
	QName ASSOC_EL_DOC_REF = QName.createQName(PROJECT_URI, "elDocRef");

	// Budget Aspect
	/** Constant <code>ASPECT_BUDGET</code> */
	QName ASPECT_BUDGET = QName.createQName(PROJECT_URI, "budgetAspect");
	/** Constant <code>PROP_BUDGET_INVOICE</code> */
	QName PROP_BUDGET_INVOICE = QName.createQName(PROJECT_URI, "invoice");
	/** Constant <code>PROP_BUDGET_EXPENSE</code> */
	QName PROP_BUDGET_EXPENSE = QName.createQName(PROJECT_URI, "expense");

	// delegation
	/** Constant <code>PROP_QNAME_DELEGATION_STATE</code> */
	QName PROP_QNAME_DELEGATION_STATE = QName.createQName(PROJECT_URI, "delegationActivated");
	/** Constant <code>PROP_QNAME_DELEGATION_START</code> */
	QName PROP_QNAME_DELEGATION_START = QName.createQName(PROJECT_URI, "delegationStartDate");
	/** Constant <code>PROP_QNAME_DELEGATION_END</code> */
	QName PROP_QNAME_DELEGATION_END = QName.createQName(PROJECT_URI, "delegationEndDate");
	/** Constant <code>PROP_QNAME_REASSIGN_TASK</code> */
	QName PROP_QNAME_REASSIGN_TASK = QName.createQName(PROJECT_URI, "reassignCurrentTasks");
	/** Constant <code>PROP_QNAME_REASSIGN_RESOURCE</code> */
	QName PROP_QNAME_REASSIGN_RESOURCE = QName.createQName(PROJECT_URI, "reassignTo");

	//notification

	/** Constant <code>PROP_NOTIFICATION_FREQUENCY_VALUE</code> */
	QName PROP_NOTIFICATION_FREQUENCY_VALUE = QName.createQName(PROJECT_URI, "notificationFrequencyValue");
	/** Constant <code>PROP_NOTIFICATION_INITIAL_VALUE</code> */
	QName PROP_NOTIFICATION_INITIAL_VALUE = QName.createQName(PROJECT_URI, "notificationInitialValue");
	/** Constant <code>ASSOC_NOTIFICATION_AUTHORITIES</code> */
	QName ASSOC_NOTIFICATION_AUTHORITIES = QName.createQName(PROJECT_URI, "notificationAuthorities");

	/** Constant <code>PROP_SL_CRITERION</code> */
	QName PROP_SL_CRITERION = QName.createQName(PROJECT_URI, "slCriterion");

	/** Constant <code>ASPECT_PROJECT_SCORE</code> */
	QName ASPECT_PROJECT_SCORE = QName.createQName(PROJECT_URI, "projectScoreAspect");

	/** Constant <code>TYPE_SCORE_CRITERION</code> */
	QName TYPE_SCORE_CRITERION = QName.createQName(PROJECT_URI, "scoreCriterion");

	/** Constant <code>PROP_SCORE_CRITERION_TYPE</code> */
	QName PROP_SCORE_CRITERION_TYPE =  QName.createQName(PROJECT_URI, "scoreCriterionType");

	/** Constant <code>ASSOC_SL_SCORE_CRITERION</code> */
	QName ASSOC_SL_SCORE_CRITERION = QName.createQName(PROJECT_URI, "slScoreCriterion");

	/** Constant <code>PROP_SCORE_CRITERION_WEIGHT</code> */
	QName PROP_SCORE_CRITERION_WEIGHT = QName.createQName(PROJECT_URI, "scoreCriterionWeight");
	
	/** Constant <code>PROP_SCORE_CRITERION_FORMULA</code> */
	QName PROP_SCORE_CRITERION_FORMULA = QName.createQName(PROJECT_URI, "scoreCriterionFormula");
	
	/** Constant <code>PROP_SCORE_CRITERION_FORMULA_DETAIL</code> */
	QName PROP_SCORE_CRITERION_FORMULA_DETAIL = QName.createQName(PROJECT_URI, "scoreCriterionDetailFormula");
	
	/** Constant <code>PROP_SCORE_CRITERION_RANGE_FORMULA</code> */
	QName PROP_SCORE_CRITERION_RANGE_FORMULA = QName.createQName(PROJECT_URI, "scoreCriterionRangeFormula");
	
	/** Constant <code>PROP_SCORE_CRITERION_RANGE</code> */
	QName PROP_SCORE_CRITERION_RANGE = QName.createQName(PROJECT_URI, "scoreCriterionRange");

	/** Constant <code>PROP_SCORE_CRITERION_FORMULATED</code> */
	QName PROP_SCORE_CRITERION_FORMULATED = QName.createQName(PROJECT_URI, "scoreCriterionFormulated");
	
	/** Constant <code>PROP_PROJECT_OWNERS</code> */
	QName PROP_PROJECT_OWNERS = QName.createQName(PROJECT_URI, "projectOwners");
	

}

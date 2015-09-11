/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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

public interface ProjectModel {

	//
	// Namespace
	//

	/** Project Model URI */
	String PROJECT_URI = "http://www.bcpg.fr/model/project/1.0";

	/** Security Model Prefix */
	String PROJECT_PREFIX = "pjt";
		
	QName TYPE_TASK_LEGEND = QName.createQName(PROJECT_URI, "taskLegend");
	
	QName TYPE_PROJECT = QName.createQName(PROJECT_URI, "project");
	QName PROP_PROJECT_HIERARCHY1 = QName.createQName(PROJECT_URI, "projectHierarchy1");
	QName PROP_PROJECT_HIERARCHY2 = QName.createQName(PROJECT_URI, "projectHierarchy2");
	QName PROP_PROJECT_START_DATE = QName.createQName(PROJECT_URI, "projectStartDate");
	QName PROP_PROJECT_DUE_DATE = QName.createQName(PROJECT_URI, "projectDueDate");
	QName PROP_PROJECT_COMPLETION_DATE = QName.createQName(PROJECT_URI, "projectCompletionDate");
	QName PROP_PROJECT_PLANNING_MODE = QName.createQName(PROJECT_URI, "projectPlanningMode");
	QName PROP_PROJECT_PRIORITY = QName.createQName(PROJECT_URI, "projectPriority");
	QName PROP_PROJECT_STATE = QName.createQName(PROJECT_URI, "projectState");
	QName PROP_PROJECT_LEGENDS = QName.createQName(PROJECT_URI, "projectLegends");
	QName ASSOC_PROJECT_ENTITY = QName.createQName(PROJECT_URI, "projectEntity");
	QName ASSOC_PROJECT_OBSERVERS = QName.createQName(PROJECT_URI, "projectObservers");
	
	QName ASSOC_PROJECT_CUR_TASKS = QName.createQName(PROJECT_URI, "projectCurrentTasks");
	QName ASSOC_PROJECT_CUR_COMMENTS = QName.createQName(PROJECT_URI, "projectCurrentComments");
	
	QName TYPE_TASK_LIST = QName.createQName(PROJECT_URI, "taskList");
	QName PROP_TL_TASK_NAME = QName.createQName(PROJECT_URI, "tlTaskName");
	QName PROP_TL_TASK_DESCRIPTION = QName.createQName(PROJECT_URI, "tlTaskDescription");

	QName PROP_TL_IS_MILESTONE = QName.createQName(PROJECT_URI, "tlIsMilestone");
	QName PROP_TL_DURATION = QName.createQName(PROJECT_URI, "tlDuration");
	QName PROP_TL_CAPACITY = QName.createQName(PROJECT_URI, "tlCapacity");
	QName PROP_TL_WORK = QName.createQName(PROJECT_URI, "tlWork");
	QName PROP_TL_WORKFLOW_NAME = QName.createQName(PROJECT_URI, "tlWorkflowName");
	QName PROP_TL_START = QName.createQName(PROJECT_URI, "tlStart");
	QName PROP_TL_END = QName.createQName(PROJECT_URI, "tlEnd");
	QName PROP_TL_STATE = QName.createQName(PROJECT_URI, "tlState");
	QName ASSOC_TL_PREV_TASKS = QName.createQName(PROJECT_URI, "tlPrevTasks");
	QName ASSOC_TL_REFUSED_TASK_REF = QName.createQName(PROJECT_URI, "tlRefusedTaskRef");
	QName ASSOC_TL_RESOURCES = QName.createQName(PROJECT_URI, "tlResources");
	QName ASSOC_TL_TASKLEGEND = QName.createQName(PROJECT_URI, "tlTaskLegend");
	QName ASSOC_TL_OBSERVERS = QName.createQName(PROJECT_URI, "tlObservers");
	QName PROP_TL_WORKFLOW_INSTANCE = QName.createQName(PROJECT_URI, "tlWorkflowInstance");
	QName PROP_TL_FIXED_COST = QName.createQName(PROJECT_URI, "tlFixedCost");

	QName ASSOC_TL_RESOURCE_COST = QName.createQName(PROJECT_URI, "tlResourceCost");
		
	QName TYPE_DELIVERABLE_LIST = QName.createQName(PROJECT_URI, "deliverableList");
	QName PROP_DL_STATE = QName.createQName(PROJECT_URI, "dlState");
	QName PROP_DL_URL = QName.createQName(PROJECT_URI, "dlURL");
	QName PROP_DL_DESCRIPTION = QName.createQName(PROJECT_URI, "dlDescription");
	QName ASSOC_DL_TASK = QName.createQName(PROJECT_URI, "dlTask");
	QName ASSOC_DL_CONTENT = QName.createQName(PROJECT_URI, "dlContent");
	
	QName TYPE_SCORE_LIST = QName.createQName(PROJECT_URI, "scoreList");
	QName PROP_SL_SCORE = QName.createQName(PROJECT_URI, "slScore");
	QName PROP_SL_WEIGHT = QName.createQName(PROJECT_URI, "slWeight");
	
	QName ASPECT_COMPLETION_ASPECT = QName.createQName(PROJECT_URI, "completionAspect");
	QName PROP_COMPLETION_PERCENT = QName.createQName(PROJECT_URI, "completionPercent");
	
	QName ASPECT_WORKFLOW_ASPECT = QName.createQName(PROJECT_URI, "workflowAspect");
	QName ASSOC_WORKFLOW_TASK = QName.createQName(PROJECT_URI, "workflowTask");

	QName TYPE_ACTIVITY_LIST  = QName.createQName(PROJECT_URI, "activityList");

	QName PROP_ACTIVITYLIST_USERID = QName.createQName(PROJECT_URI, "alUserId");

	QName PROP_ACTIVITYLIST_TYPE = QName.createQName(PROJECT_URI, "alType");
	
	QName PROP_ACTIVITYLIST_DATA = QName.createQName(PROJECT_URI, "alData");

	QName ASSOC_PROJECT_MANAGER = QName.createQName(PROJECT_URI, "projectManager");
	
	QName TYPE_LOG_TIME_LIST  = QName.createQName(PROJECT_URI, "logTimeList");
	QName PROP_LTL_TIME  = QName.createQName(PROJECT_URI, "ltlTime");
	QName ASSOC_LTL_TASK  = QName.createQName(PROJECT_URI, "ltlTask");
	
	QName TYPE_RESOURCE_COST = QName.createQName(PROJECT_URI, "resourceCost");
	QName PROP_RESOURCE_COST_VALUE = QName.createQName(PROJECT_URI, "resourceCostValue");
	QName PROP_RESOURCE_COST_BILL_RATE = QName.createQName(PROJECT_URI, "resourceCostBillRate");
	
	//Budget List
	QName TYPE_BUDGET_LIST  = QName.createQName(PROJECT_URI, "budgetList");
	QName PROP_BL_ITEM  = QName.createQName(PROJECT_URI, "blItem");
	QName PROP_BL_PROFIT  = QName.createQName(PROJECT_URI, "blProfit");
	QName PROP_BL_BUDGEDTED_EXPENSE = QName.createQName(PROJECT_URI, "blBudgetedExpense");
	QName PROP_BL_BUDGEDTED_INVOICE = QName.createQName(PROJECT_URI, "blBudgetedInvoice");
	
	
	//Invoice List
	QName TYPE_INVOICE_LIST  = QName.createQName(PROJECT_URI, "invoiceList");
	QName ASSOC_IL_BUDGET_REF  = QName.createQName(PROJECT_URI, "ilBudgetRef");
	QName ASSOC_IL_TASK_REF  = QName.createQName(PROJECT_URI, "ilTaskRef");
	QName ASSOC_IL_DOC_REF  = QName.createQName(PROJECT_URI, "ilDocRef");
	
	//Expense List
	QName TYPE_EXPENSE_LIST  = QName.createQName(PROJECT_URI, "expenseList");
	QName ASSOC_EL_BUDGET_REF  = QName.createQName(PROJECT_URI, "elBudgetRef");
	QName ASSOC_EL_TASK_REF  = QName.createQName(PROJECT_URI, "elTaskRef");
	QName ASSOC_EL_DOC_REF  = QName.createQName(PROJECT_URI, "elDocRef");

	//Budget Aspect
	QName ASPECT_BUDGET = QName.createQName(PROJECT_URI, "budgetAspect");
	QName PROP_BUDGET_INVOICE = QName.createQName(PROJECT_URI, "invoice");
	QName PROP_BUDGET_EXPENSE = QName.createQName(PROJECT_URI, "expense");
	
	


}

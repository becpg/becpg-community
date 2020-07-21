/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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
package fr.becpg.repo.project;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Class used to manage workflow
 *
 * @author quere
 * @version $Id: $Id
 */
public interface ProjectWorkflowService {

	/**
	 * <p>isWorkflowActive.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @return a boolean.
	 */
	boolean isWorkflowActive(TaskListDataItem task);
	/**
	 * <p>cancelWorkflow.</p>
	 *
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	void cancelWorkflow(TaskListDataItem task);
	/**
	 * <p>startWorkflow.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListDataItem a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param nextDeliverables a {@link java.util.List} object.
	 */
	void startWorkflow(ProjectData projectData, TaskListDataItem taskListDataItem,
					   List<DeliverableListDataItem> nextDeliverables);
	/**
	 * <p>checkWorkflowInstance.</p>
	 *
	 * @param projectData a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param taskListDataItem a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param nextDeliverables a {@link java.util.List} object.
	 */
	void checkWorkflowInstance(ProjectData projectData, TaskListDataItem taskListDataItem,
							   List<DeliverableListDataItem> nextDeliverables);
	/**
	 * <p>deleteWorkflowTask.</p>
	 *
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void deleteWorkflowTask(NodeRef taskListNodeRef);
	/**
	 * <p>deleteWorkflowById.</p>
	 *
	 * @param workflowInstanceId a {@link java.lang.String} object.
	 */
	void deleteWorkflowById(String workflowInstanceId);
	
}

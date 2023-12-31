/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Class used to manage a project
 * 
 * @author quere
 * 
 */
public interface ProjectService {

	/**
	 * Open deliverable in progress
	 * 
	 * @param deliverableNodeRef
	 */
	void openDeliverable(NodeRef deliverableNodeRef);
	
	/**
	 * Reopen a task in progress
	 * @param taskNodeRef
	 */
	void reopenTask(NodeRef taskNodeRef);

	/**
	 * 
	 * @param projectNodeRef
	 */
	List<NodeRef> getTaskLegendList();
	
	/**
	 * Get the number of inProgress project for this legend
	 * 
	 */
	Long getNbProjectsByLegend(NodeRef legendNodeRef, String siteId);

	/**
	 * Get the projects container
	 * 
	 * @param siteId
	 */
	NodeRef getProjectsContainer(String siteId);

	/**
	 * Formulate a project
	 * 
	 * @param projectNodeRef
	 * @throws FormulateException 
	 */
	void formulate(NodeRef projectNodeRef) throws FormulateException;
	/**
	 * 
	 * @param taskListNodeRef
	 */
	void deleteTask(NodeRef taskListNodeRef);
	/**
	 * 
	 * @param taskListNodeRef
	 * @param taskComment 
	 */
	void submitTask(NodeRef taskListNodeRef, String taskComment);
	
	/**
	 * @param projectNodeRef
	 * @param taskListNodeRef
	 * @param resourceNodeRef
	 * @param allow
	 */
	void updateProjectPermission(NodeRef projectNodeRef, NodeRef taskListNodeRef, NodeRef resourceNodeRef, boolean allow);
	

	/**
	 * Refused a project Task
	 * @param taskNodeRef
	 * @param taskComment 
	 */
	NodeRef refusedTask(NodeRef taskNodeRef, String taskComment);

	
	/**
	 * Run a deliverable script
	 * @param projectNodeRef
	 * @param taskNodeRef
	 * @param stringScript
	 */
	void runScript(ProjectData project, TaskListDataItem task, DeliverableListDataItem deliverable);

	/**
	 * 
	 * @param projectNodeRef
	 * @param resources
	 */
	List<NodeRef> extractResources(NodeRef projectNodeRef, List<NodeRef> resources);

	/**
	 * 
	 * @param resource
	 */
	NodeRef getReassignedResource(NodeRef resource, Set<NodeRef> reassignedCandidates);

	/**
	 * 
	 * @param taskNodeRef
	 * @param assignee
	 */
	NodeRef reassignTask(NodeRef taskNodeRef, String assignee);

	
         /**
	 * Refused a project Task
	 * @param taskNodeRef
	 */
     NodeRef refusedTask(NodeRef nodeRef);

     Set<NodeRef> updateProjectState(NodeRef projectNodeRef, String beforeState, String afterState);

	TaskListDataItem createNewTask(ProjectData project);

}

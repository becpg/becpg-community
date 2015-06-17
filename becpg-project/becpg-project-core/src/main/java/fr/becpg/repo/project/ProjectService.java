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
package fr.becpg.repo.project;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.project.data.ProjectData;
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
	 * Get the task legend list
	 * 
	 * @return
	 */
	List<NodeRef> getTaskLegendList();
	
	/**
	 * Get the number of inProgress project for this legend
	 * 
	 * @return
	 */
	Long getNbProjectsByLegend(NodeRef legendNodeRef);

	/**
	 * Get the projects container
	 * 
	 * @param siteId
	 * @return
	 */
	NodeRef getProjectsContainer(String siteId);

	/**
	 * Cancel a project
	 * 
	 * @param projectNodeRef
	 */
	void cancel(NodeRef projectNodeRef);

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
	 */
	void submitTask(NodeRef taskListNodeRef);
	
	/**
	 * @param projectNodeRef
	 * @param taskListNodeRef
	 * @param resourceNodeRef
	 * @param allow
	 */
	void updateProjectPermission(NodeRef projectNodeRef, NodeRef taskListNodeRef, NodeRef resourceNodeRef, boolean allow);
	
	
	/**
	 * 
	 * @param projectNodeRef
	 * @param taskNodeRefs
	 * @param resources
	 * @param updatePermissions
	 * @return 
	 */
	List<NodeRef> updateTaskResources(NodeRef projectNodeRef, NodeRef taskNodeRefs, List<NodeRef> resources, boolean updatePermissions);

	/**
	 * 
	 * @param projectNodeRef
	 * @param url
	 * @return
	 */
	String getDeliverableUrl(NodeRef projectNodeRef, String url);

	
	/**
	 * Refused a project Task
	 * @param taskNodeRef
	 * @return 
	 */
	NodeRef refusedTask(NodeRef taskNodeRef);

	
	/**
	 * Run a deliverable script
	 * @param projectNodeRef
	 * @param taskNodeRef
	 * @param stringScript
	 */
	void runScript(ProjectData project, TaskListDataItem task, NodeRef scriptNode);



	

}

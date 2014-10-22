/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
	public void openDeliverable(NodeRef deliverableNodeRef);
	
	/**
	 * Open task in progress
	 * @param taskNodeRef
	 */
	public void openTask(NodeRef taskNodeRef);

	
	/**
	 * Complete task
	 * @param taskNodeRef
	 */
	public void completeTask(NodeRef taskNodeRef);
	
	/**
	 * Get the task legend list
	 * 
	 * @return
	 */
	public List<NodeRef> getTaskLegendList();

	/**
	 * Get the projects container
	 * 
	 * @param siteId
	 * @return
	 */
	public NodeRef getProjectsContainer(String siteId);

	/**
	 * Cancel a project
	 * 
	 * @param projectNodeRef
	 */
	public void cancel(NodeRef projectNodeRef);

	/**
	 * Formulate a project
	 * 
	 * @param projectNodeRef
	 * @throws FormulateException 
	 */
	public void formulate(NodeRef projectNodeRef) throws FormulateException;
	/**
	 * 
	 * @param taskListNodeRef
	 */
	public void deleteTask(NodeRef taskListNodeRef);
	/**
	 * 
	 * @param taskListNodeRef
	 */
	public void submitTask(NodeRef taskListNodeRef);
	
	/**
	 * @param projectNodeRef
	 * @param taskListNodeRef
	 * @param resourceNodeRef
	 * @param allow
	 */
	public void updateProjectPermission(NodeRef projectNodeRef, NodeRef taskListNodeRef, NodeRef resourceNodeRef, boolean allow);
	
	
	/**
	 * 
	 * @param projectNodeRef
	 * @param taskNodeRefs
	 * @param resources
	 * @param updatePermissions
	 * @return 
	 */
	public List<NodeRef> updateTaskResources(NodeRef projectNodeRef, NodeRef taskNodeRefs, List<NodeRef> resources, boolean updatePermissions);

	/**
	 * 
	 * @param projectNodeRef
	 * @param url
	 * @return
	 */
	public String getDeliverableUrl(NodeRef projectNodeRef, String url);

	
	/**
	 * Refused a project Task
	 * @param taskNodeRef
	 * @return 
	 */
	public NodeRef refusedTask(NodeRef taskNodeRef);

	
	/**
	 * Run a deliverable script
	 * @param projectNodeRef
	 * @param taskNodeRef
	 * @param stringScript
	 */
	public void runScript(NodeRef projectNodeRef, NodeRef taskNodeRef, String stringScript);

	

}

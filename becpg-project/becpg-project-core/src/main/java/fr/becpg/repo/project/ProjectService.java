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

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.project.data.ProjectData;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;

/**
 * Class used to manage a project
 *
 * @author quere
 * @version $Id: $Id
 */
public interface ProjectService {

	/**
	 * Open deliverable in progress
	 *
	 * @param deliverableNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void openDeliverable(NodeRef deliverableNodeRef);

	/**
	 * Reopen a task in progress
	 *
	 * @param taskNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void reopenTask(NodeRef taskNodeRef);

	/**
	 * <p>getTaskLegendList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getTaskLegendList();

	/**
	 * Get the number of inProgress project for this legend
	 *
	 * @param legendNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param siteId a {@link java.lang.String} object.
	 * @return a {@link java.lang.Long} object.
	 */
	Long getNbProjectsByLegend(NodeRef legendNodeRef, String siteId);

	/**
	 * Get the projects container
	 *
	 * @param siteId a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getProjectsContainer(String siteId);

	/**
	 * Formulate a project
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	void formulate(NodeRef projectNodeRef) throws FormulateException;

	/**
	 * <p>deleteTask.</p>
	 *
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void deleteTask(NodeRef taskListNodeRef);

	/**
	 * <p>submitTask.</p>
	 *
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskComment a {@link java.lang.String} object.
	 */
	void submitTask(NodeRef taskListNodeRef, String taskComment);

	/**
	 * <p>updateProjectPermission.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskListNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param resourceNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param allow a boolean.
	 */
	void updateProjectPermission(NodeRef projectNodeRef, NodeRef taskListNodeRef, NodeRef resourceNodeRef, boolean allow);

	/**
	 * <p>getDeliverableUrl.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param url a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getDeliverableUrl(NodeRef projectNodeRef, String url);

	/**
	 * Refused a project Task
	 *
	 * @param taskNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskComment a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef refusedTask(NodeRef taskNodeRef, String taskComment);

	/**
	 * Run a deliverable script
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object.
	 * @param task a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 * @param scriptNode a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void runScript(ProjectData project, TaskListDataItem task, NodeRef scriptNode);

	/**
	 * <p>extractResources.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param resources a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> extractResources(NodeRef projectNodeRef, List<NodeRef> resources);

	/**
	 * <p>getReassignedResource.</p>
	 *
	 * @param resource a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getReassignedResource(NodeRef resource);

	/**
	 * <p>reassignTask.</p>
	 *
	 * @param taskNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param assignee a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef reassignTask(NodeRef taskNodeRef, String assignee);

	/**
	 * <p>refusedTask.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef refusedTask(NodeRef nodeRef);

}

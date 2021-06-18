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
package fr.becpg.repo.project;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.activity.data.ActivityEvent;

/**
 * Class used to manage notification
 *
 * @author quere
 * @version $Id: $Id
 */
public interface ProjectNotificationService {

	/**
	 * <p>notifyTaskStateChanged.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param beforeState a {@link java.lang.String} object.
	 * @param afterState a {@link java.lang.String} object.
	 */
	void notifyTaskStateChanged(NodeRef projectNodeRef, NodeRef taskNodeRef, String beforeState, String afterState);
	/**
	 * <p>notifyComment.</p>
	 *
	 * @param commentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param activityEvent a {@link fr.becpg.repo.activity.data.ActivityEvent} object.
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param deliverableNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void notifyComment(NodeRef commentNodeRef, ActivityEvent activityEvent, NodeRef projectNodeRef, NodeRef taskNodeRef, NodeRef deliverableNodeRef);
	/**
	 * <p>createSubject.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param afterStateMsg a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
	String createSubject(NodeRef projectNodeRef, NodeRef taskNodeRef, String afterStateMsg);
	
}

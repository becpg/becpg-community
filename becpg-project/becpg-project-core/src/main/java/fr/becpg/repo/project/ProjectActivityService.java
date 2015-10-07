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

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;

import fr.becpg.repo.project.data.projectList.ActivityEvent;
import fr.becpg.repo.project.data.projectList.ActivityType;

public interface ProjectActivityService {

	void postTaskStateChangeActivity(NodeRef taskNodeRef,String beforeState,String afterState);
	
	void postProjectStateChangeActivity(NodeRef projectNodeRef,String beforeState,String afterState);
	
	void postDeliverableStateChangeActivity(NodeRef deliverableNodeRef,String beforeState,String afterState);
	
	void postCommentActivity(NodeRef commentNodeRef, ActivityEvent activityAction);

	JSONObject postActivityLookUp(ActivityType activityType, String value);

	void postContentActivity(NodeRef actionedUponNodeRef, ActivityEvent activityAction);

	boolean isInProject(NodeRef nodeRef);

}

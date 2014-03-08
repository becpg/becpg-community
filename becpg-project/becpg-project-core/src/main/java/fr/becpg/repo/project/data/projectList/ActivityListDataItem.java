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
package fr.becpg.repo.project.data.projectList;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Activity list of project
 * 
 * @author matthieu
 * 
 */
@AlfType
@AlfQname(qname = "pjt:activityList")
public class ActivityListDataItem extends BeCPGDataObject {

	private NodeRef task;
	private NodeRef deliverable;
	private ActivityType activityType = ActivityType.State;
	private String activityData;
	private String userId;

	@AlfProp
	@AlfQname(qname = "pjt:alTaskId")
	public NodeRef getTask() {
		return task;
	}

	public void setTask(NodeRef task) {
		this.task = task;
	}

	
	@AlfProp
	@AlfQname(qname = "pjt:alDeliverableId")
	public NodeRef getDeliverable() {
		return deliverable;
	}

	public void setDeliverable(NodeRef deliverable) {
		this.deliverable = deliverable;
	}

	@AlfProp
	@AlfQname(qname = "pjt:alType")
	public ActivityType getActivityType() {
		return activityType;
	}

	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	@AlfProp
	@AlfQname(qname = "pjt:alData")
	public String getActivityData() {
		return activityData;
	}

	public void setActivityData(String activityData) {
		this.activityData = activityData;
	}

	@AlfProp
	@AlfQname(qname = "pjt:alUserId")
	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public ActivityListDataItem() {
		super();
		this.userId = AuthenticationUtil.getFullyAuthenticatedUser();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((activityData == null) ? 0 : activityData.hashCode());
		result = prime * result + ((activityType == null) ? 0 : activityType.hashCode());
		result = prime * result + ((deliverable == null) ? 0 : deliverable.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActivityListDataItem other = (ActivityListDataItem) obj;
		if (activityData == null) {
			if (other.activityData != null)
				return false;
		} else if (!activityData.equals(other.activityData))
			return false;
		if (activityType != other.activityType)
			return false;
		if (deliverable == null) {
			if (other.deliverable != null)
				return false;
		} else if (!deliverable.equals(other.deliverable))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActivityListDataItem [task=" + task + ", deliverable=" + deliverable + ", activityType=" + activityType + ", activityData="
				+ activityData + ", userId=" + userId + "]";
	}

	
	
}

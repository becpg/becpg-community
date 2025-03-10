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
package fr.becpg.repo.activity.data;

import java.util.Date;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Activity list of project
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:activityList")
public class ActivityListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -817711206754301661L;
	
	
	
	private ActivityType activityType = ActivityType.State;
	private String activityData;
	private String userId;
	private Date createdDate;
	private Long id;
	
	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.Long} object
	 */
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object
	 */
	public Long getId() {
		return id;
	}

	/**
	 * <p>setCreated.</p>
	 *
	 * @param created a {@link java.util.Date} object
	 */
	public void setCreated(Date created) {
		this.createdDate = created;
	}
	
	/**
	 * <p>Getter for the field <code>createdDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	public Date getCreatedDate() {
		return createdDate;
	}
	
	/**
	 * <p>Getter for the field <code>activityType</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.activity.data.ActivityType} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:alType")
	public ActivityType getActivityType() {
		return activityType;
	}

	/**
	 * <p>Setter for the field <code>activityType</code>.</p>
	 *
	 * @param activityType a {@link fr.becpg.repo.activity.data.ActivityType} object.
	 */
	public void setActivityType(ActivityType activityType) {
		this.activityType = activityType;
	}

	/**
	 * <p>Getter for the field <code>activityData</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:alData")
	public String getActivityData() {
		return activityData;
	}

	/**
	 * <p>Setter for the field <code>activityData</code>.</p>
	 *
	 * @param activityData a {@link java.lang.String} object.
	 */
	public void setActivityData(String activityData) {
		this.activityData = activityData;
	}
	
	/**
	 * <p>getJSONData.</p>
	 *
	 * @return a {@link org.json.JSONObject} object.
	 * @throws org.json.JSONException if any.
	 */
	public JSONObject getJSONData() throws JSONException {
		return new JSONObject(activityData);
	}

	/**
	 * <p>Getter for the field <code>userId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:alUserId")
	public String getUserId() {
		return userId;
	}

	/**
	 * <p>Setter for the field <code>userId</code>.</p>
	 *
	 * @param userId a {@link java.lang.String} object.
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * <p>Constructor for ActivityListDataItem.</p>
	 */
	public ActivityListDataItem() {
		super();
		this.userId = AuthenticationUtil.getFullyAuthenticatedUser();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((activityData == null) ? 0 : activityData.hashCode());
		result = prime * result + ((activityType == null) ? 0 : activityType.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		return result;
	}

	/** {@inheritDoc} */
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
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ActivityListDataItem [activityType=" + activityType + ", activityData=" + activityData + ", userId=" + userId + "]";
	}


	
	
}

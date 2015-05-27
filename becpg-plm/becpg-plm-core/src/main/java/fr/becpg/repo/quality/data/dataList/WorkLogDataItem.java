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
package fr.becpg.repo.quality.data.dataList;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;


@AlfType
@AlfQname(qname = "qa:workLog")
public class WorkLogDataItem extends BeCPGDataObject {

	private String state;
	private String comment;
	private String creator;
	private Date created;
	
	@AlfProp
	@AlfQname(qname = "qa:wlState")
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}	
	
	@AlfProp
	@AlfQname(qname = "qa:wlComment")
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}	
	
	@AlfProp
	@AlfQname(qname = "cm:creator")
	public String getCreator() {
		return creator;
	}
	public void setCreator(String creator) {
		this.creator = creator;
	}
	
	@AlfProp
	@AlfQname(qname = "cm:created")
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	
	
	public WorkLogDataItem() {
		super();
	}
	public WorkLogDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}
	public WorkLogDataItem(NodeRef nodeRef, String state, String comment, String creator, Date created){
		this.nodeRef = nodeRef;
		this.state = state;
		this.comment = comment;
		this.creator = creator;
		this.created = created;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
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
		WorkLogDataItem other = (WorkLogDataItem) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (creator == null) {
			if (other.creator != null)
				return false;
		} else if (!creator.equals(other.creator))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "WorkLogDataItem [state=" + state + ", comment=" + comment + ", creator=" + creator + ", created=" + created + "]";
	}
	
	
}

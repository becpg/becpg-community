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

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Deliverable list of project
 * 
 * @author quere
 * 
 */
@AlfType
@AlfQname(qname = "pjt:deliverableList")
public class DeliverableListDataItem extends BeCPGDataObject {

	private NodeRef task;
	private DeliverableState state = DeliverableState.Planned;
	private String description;
	private String url;
	private Integer completionPercent = 0;
	private NodeRef content;

	@AlfSingleAssoc
	@AlfQname(qname = "pjt:dlTask")
	public NodeRef getTask() {
		return task;
	}

	public void setTask(NodeRef task) {
		this.task = task;
	}

	
	@AlfProp
	@AlfQname(qname = "pjt:dlUrl")
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@AlfProp
	@AlfQname(qname = "pjt:dlState")
	public DeliverableState getState() {
		return state;
	}
	
	
	public void setState(DeliverableState state) {
		this.state = state;
	}

	@AlfProp
	@AlfQname(qname = "pjt:dlDescription")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@AlfProp
	@AlfQname(qname = "pjt:completionPercent")
	public Integer getCompletionPercent() {
		return completionPercent;
	}

	public void setCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "pjt:dlContent")
	public NodeRef getContent() {
		return content;
	}

	public void setContent(NodeRef content) {
		this.content = content;
	}

	public DeliverableListDataItem() {
		super();
	}

	public DeliverableListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	public DeliverableListDataItem(NodeRef nodeRef, NodeRef task, DeliverableState state, String description, Integer completionPercent, NodeRef content) {
		this.nodeRef = nodeRef;
		this.task = task;
		this.state = state!=null ? state : DeliverableState.Planned;
		this.description = description;
		this.completionPercent = completionPercent;
		this.content = content;
	}

	public DeliverableListDataItem(DeliverableListDataItem d) {
		super();
		this.nodeRef = d.getNodeRef();
		this.task = d.getTask();
		this.state = d.getState();
		this.description = d.getDescription();
		this.completionPercent = d.getCompletionPercent();
		this.content = d.getContent();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((task == null) ? 0 : task.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DeliverableListDataItem other = (DeliverableListDataItem) obj;
		if (content == null) {
			if (other.content != null)
				return false;
		} else if (!content.equals(other.content))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (task == null) {
			if (other.task != null)
				return false;
		} else if (!task.equals(other.task))
			return false;
		if (state != other.state)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "DeliverableListDataItem [nodeRef=" + nodeRef + ", task=" + task + ", state=" + state + ", description=" + description + ", content=" + content + "]";
	}
}

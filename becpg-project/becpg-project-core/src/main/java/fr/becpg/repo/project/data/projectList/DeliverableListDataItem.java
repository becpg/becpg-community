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
package fr.becpg.repo.project.data.projectList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Deliverable list of project
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:deliverableList")
public class DeliverableListDataItem extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6649109093908843793L;
	private List<NodeRef> tasks;
	private DeliverableState state = DeliverableState.Planned;
	private String description;
	private String url;
	private DeliverableScriptOrder scriptOrder = DeliverableScriptOrder.None;
	private Integer completionPercent = 0;
	private NodeRef content;

	/**
	 * <p>Getter for the field <code>tasks</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:dlTask")
	public List<NodeRef> getTasks() {
		return tasks;
	}

	/**
	 * <p>Setter for the field <code>tasks</code>.</p>
	 *
	 * @param tasks a {@link java.util.List} object.
	 */
	public void setTasks(List<NodeRef> tasks) {
		this.tasks = tasks;
	}

	/**
	 * <p>Getter for the field <code>url</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:dlUrl")
	public String getUrl() {
		return url;
	}

	/**
	 * <p>Setter for the field <code>url</code>.</p>
	 *
	 * @param url a {@link java.lang.String} object.
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * <p>Getter for the field <code>scriptOrder</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableScriptOrder} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:dlScriptExecOrder")
	public DeliverableScriptOrder getScriptOrder() {
		return scriptOrder;
	}

	/**
	 * <p>Setter for the field <code>scriptOrder</code>.</p>
	 *
	 * @param scriptOrder a {@link fr.becpg.repo.project.data.projectList.DeliverableScriptOrder} object.
	 */
	public void setScriptOrder(DeliverableScriptOrder scriptOrder) {
		this.scriptOrder = scriptOrder;
	}

	/**
	 * <p>Getter for the field <code>state</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableState} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:dlState")
	public DeliverableState getState() {
		return state;
	}

	/**
	 * <p>Setter for the field <code>state</code>.</p>
	 *
	 * @param state a {@link fr.becpg.repo.project.data.projectList.DeliverableState} object.
	 */
	public void setState(DeliverableState state) {
		this.state = state;
	}

	/**
	 * <p>Getter for the field <code>description</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:dlDescription")
	public String getDescription() {
		return description;
	}

	/**
	 * <p>Setter for the field <code>description</code>.</p>
	 *
	 * @param description a {@link java.lang.String} object.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * <p>Getter for the field <code>completionPercent</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:completionPercent")
	public Integer getCompletionPercent() {
		return completionPercent;
	}

	/**
	 * <p>Setter for the field <code>completionPercent</code>.</p>
	 *
	 * @param completionPercent a {@link java.lang.Integer} object.
	 */
	public void setCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
	}

	/**
	 * <p>Getter for the field <code>content</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:dlContent")
	public NodeRef getContent() {
		return content;
	}

	/**
	 * <p>Setter for the field <code>content</code>.</p>
	 *
	 * @param content a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setContent(NodeRef content) {
		this.content = content;
	}

	/**
	 * <p>Constructor for DeliverableListDataItem.</p>
	 */
	public DeliverableListDataItem() {
		super();
	}

	/**
	 * <p>Constructor for DeliverableListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public DeliverableListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public static DeliverableListDataItem build() {
		return new DeliverableListDataItem();
	}

	/**
	 * <p>withTasks.</p>
	 *
	 * @param tasks a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public DeliverableListDataItem withTasks(List<NodeRef> tasks) {
		this.tasks = tasks;
		return this;
	}

	/**
	 * <p>withState.</p>
	 *
	 * @param state a {@link fr.becpg.repo.project.data.projectList.DeliverableState} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public DeliverableListDataItem withState(DeliverableState state) {
		this.state = state;
		return this;
	}

	/**
	 * <p>withDescription.</p>
	 *
	 * @param description a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public DeliverableListDataItem withDescription(String description) {
		this.description = description;
		return this;
	}

	/**
	 * <p>withUrl.</p>
	 *
	 * @param url a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public DeliverableListDataItem withUrl(String url) {
		this.url = url;
		return this;
	}

	/**
	 * <p>withCompletionPercent.</p>
	 *
	 * @param completionPercent a {@link java.lang.Integer} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public DeliverableListDataItem withCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
		return this;
	}

	/**
	 * <p>withContent.</p>
	 *
	 * @param content a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object
	 */
	public DeliverableListDataItem withContent(NodeRef content) {
		this.content = content;
		return this;
	}

	/**
	 * <p>Constructor for DeliverableListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param tasks a {@link java.util.List} object.
	 * @param state a {@link fr.becpg.repo.project.data.projectList.DeliverableState} object.
	 * @param description a {@link java.lang.String} object.
	 * @param completionPercent a {@link java.lang.Integer} object.
	 * @param content a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public DeliverableListDataItem(NodeRef nodeRef, List<NodeRef> tasks, DeliverableState state, String description, Integer completionPercent,
			NodeRef content) {
		this.nodeRef = nodeRef;
		this.tasks = tasks;
		this.state = state != null ? state : DeliverableState.Planned;
		this.description = description;
		this.completionPercent = completionPercent;
		this.content = content;
	}

	/**
	 * <p>Constructor for DeliverableListDataItem.</p>
	 *
	 * @param d a {@link fr.becpg.repo.project.data.projectList.DeliverableListDataItem} object.
	 */
	public DeliverableListDataItem(DeliverableListDataItem d) {
		super();
		this.nodeRef = d.getNodeRef();
		this.tasks = d.getTasks();
		this.state = d.getState();
		this.description = d.getDescription();
		this.completionPercent = d.getCompletionPercent();
		this.content = d.getContent();
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((content == null) ? 0 : content.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((scriptOrder == null) ? 0 : scriptOrder.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((tasks == null) ? 0 : tasks.hashCode());
		result = prime * result + ((url == null) ? 0 : url.hashCode());
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
		DeliverableListDataItem other = (DeliverableListDataItem) obj;
		if (completionPercent == null) {
			if (other.completionPercent != null)
				return false;
		} else if (!completionPercent.equals(other.completionPercent))
			return false;
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
		if (scriptOrder != other.scriptOrder)
			return false;
		if (state != other.state)
			return false;
		if (tasks == null) {
			if (other.tasks != null)
				return false;
		} else if (!tasks.equals(other.tasks))
			return false;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DeliverableListDataItem [tasks=" + tasks + ", state=" + state + ", description=" + description + ", url=" + url + ", scriptOrder="
				+ scriptOrder + ", completionPercent=" + completionPercent + ", content=" + content + "]";
	}
}

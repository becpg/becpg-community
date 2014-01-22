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
package fr.becpg.repo.project.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.ScoreListDataItem;
import fr.becpg.repo.project.data.projectList.TaskListDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.AspectAwareDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * ProjectData used to manipulate project
 * 
 * @author quere
 * 
 */
@AlfType
@AlfQname(qname = "pjt:project")
public class ProjectData extends BeCPGDataObject implements AspectAwareDataItem, FormulatedEntity, HierarchicalEntity {

	
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private Date startDate;
	private Date dueDate;
	private Date completionDate;
	private Integer priority = 2;
	private ProjectState projectState;
	private NodeRef projectTpl;
	private Integer completionPercent = 0;
	private List<NodeRef> entities;
	private List<NodeRef> legends = new ArrayList<NodeRef>();
	private Integer overdue = 0;
	private Integer score = 0;
	private Date created;
	private Date modified;
	private String creator;
	private String modifier;
	private NodeRef projectManager;
	
	/*
	 * Formulation
	 */
	private Date formulatedDate;
	
	private List<TaskListDataItem> taskList;
	private List<DeliverableListDataItem> deliverableList;
	private List<ScoreListDataItem> scoreList;

	public ProjectData() {
		super();
	}

	public ProjectData(NodeRef nodeRef, String name, NodeRef hierarchy1, NodeRef hierarchy2, Date startDate, Date dueDate, Date completionDate, Integer priority,ProjectState projectState,
			NodeRef projectTpl, Integer completionPercent, List<NodeRef> entities) {
		super(nodeRef, name);
		this.hierarchy1 = hierarchy1;
		this.hierarchy2 = hierarchy2;
		this.startDate = startDate;
		this.dueDate = dueDate;
		this.completionDate = completionDate;
		this.priority = priority;
		this.projectState = projectState;
		this.projectTpl = projectTpl;
		this.completionPercent = completionPercent;
		this.entities = entities;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectHierarchy1")
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectHierarchy2")
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectStartDate")
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	

	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
	public Date getFormulatedDate() {
		return formulatedDate;
	}

	public void setFormulatedDate(Date formulatedDate) {
		this.formulatedDate = formulatedDate;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectDueDate")
	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectCompletionDate")
	public Date getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectPriority")
	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectState")
		public ProjectState getProjectState() {
		return projectState;
	}

	public void setProjectState(ProjectState projectState) {
		this.projectState = projectState;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:entityTplRef")
	public NodeRef getProjectTpl() {
		return projectTpl;
	}

	public void setProjectTpl(NodeRef projectTpl) {
		this.projectTpl = projectTpl;
	}

	@AlfProp
	@AlfQname(qname = "pjt:completionPercent")
	public Integer getCompletionPercent() {
		return completionPercent;
	}

	public void setCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "pjt:projectEntity")
	public List<NodeRef> getEntities() {
		return entities;
	}

	public void setEntities(List<NodeRef> entities) {
		this.entities = entities;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectLegends")
	public List<NodeRef> getLegends() {
		return legends;
	}

	public void setLegends(List<NodeRef> legends) {
		this.legends = legends;
	}
	
	@AlfProp
	@AlfQname(qname = "pjt:projectOverdue")
	public Integer getOverdue() {
		return overdue;
	}

	public void setOverdue(Integer overdue) {
		this.overdue = overdue;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectScore")
	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}
	
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:projectManager")
	public NodeRef getProjectManager() {
		return projectManager;
	}

	public void setProjectManager(NodeRef projectManager) {
		this.projectManager = projectManager;
	}
	
	@AlfProp
	@AlfQname(qname = "cm:created")
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	@AlfProp
	@AlfQname(qname = "cm:modified")
	public Date getModified() {
		return modified;
	}

	public void setModified(Date modified) {
		this.modified = modified;
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
	@AlfQname(qname = "cm:modifier")
	public String getModifier() {
		return modifier;
	}

	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	@DataList
	@AlfQname(qname="pjt:taskList")
	public List<TaskListDataItem> getTaskList() {
		return taskList;
	}
	
	@DataList
	@AlfQname(qname="pjt:deliverableList")
	public List<DeliverableListDataItem> getDeliverableList() {
		return deliverableList;
	}

	public void setDeliverableList(List<DeliverableListDataItem> deliverableList) {
		this.deliverableList = deliverableList;
	}

	public void setTaskList(List<TaskListDataItem> taskList) {
		this.taskList = taskList;
	}

	@DataList
	@AlfQname(qname="pjt:scoreList")
	public List<ScoreListDataItem> getScoreList() {
		return scoreList;
	}

	public void setScoreList(List<ScoreListDataItem> scoreList) {
		this.scoreList = scoreList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((completionDate == null) ? 0 : completionDate.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((legends == null) ? 0 : legends.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((projectState == null) ? 0 : projectState.hashCode());
		result = prime * result + ((projectTpl == null) ? 0 : projectTpl.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
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
		ProjectData other = (ProjectData) obj;
		if (completionDate == null) {
			if (other.completionDate != null)
				return false;
		} else if (!completionDate.equals(other.completionDate))
			return false;
		if (completionPercent == null) {
			if (other.completionPercent != null)
				return false;
		} else if (!completionPercent.equals(other.completionPercent))
			return false;
		if (dueDate == null) {
			if (other.dueDate != null)
				return false;
		} else if (!dueDate.equals(other.dueDate))
			return false;
		if (entities == null) {
			if (other.entities != null)
				return false;
		} else if (!entities.equals(other.entities))
			return false;
		if (hierarchy1 == null) {
			if (other.hierarchy1 != null)
				return false;
		} else if (!hierarchy1.equals(other.hierarchy1))
			return false;
		if (legends == null) {
			if (other.legends != null)
				return false;
		} else if (!legends.equals(other.legends))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (projectState != other.projectState)
			return false;
		if (projectTpl == null) {
			if (other.projectTpl != null)
				return false;
		} else if (!projectTpl.equals(other.projectTpl))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProjectData [hierarchy1=" + hierarchy1 + ", startDate=" + startDate + ", dueDate=" + dueDate
				+ ", completionDate=" + completionDate + ", priority=" + priority + ", projectState=" + projectState
				+ ", projectTpl=" + projectTpl + ", completionPercent=" + completionPercent + ", entities=" + entities
				+ ", legends=" + legends + "]";
	}

	
	
}

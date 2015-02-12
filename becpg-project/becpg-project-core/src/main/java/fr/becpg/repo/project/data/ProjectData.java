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
package fr.becpg.repo.project.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.LogTimeListDataItem;
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
import fr.becpg.repo.repository.model.StateableEntity;

/**
 * ProjectData used to manipulate project
 * 
 * @author quere
 * 
 */
@AlfType
@AlfQname(qname = "pjt:project")
public class ProjectData extends BeCPGDataObject implements AspectAwareDataItem, FormulatedEntity, HierarchicalEntity, StateableEntity {

	
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private Date startDate;
	private Date dueDate;
	private Date completionDate;
	private PlanningMode planningMode = PlanningMode.Planning;
	private Integer priority = 2;
	private ProjectState projectState = ProjectState.Planned;
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
	private Double budgetedCost = 0d;
	private Double work = 0d;
	private Double loggedTime = 0d;
	
	/*
	 * Formulation
	 */
	private Date formulatedDate;
	private Integer reformulateCount;
	
	private List<TaskListDataItem> taskList;
	private List<DeliverableListDataItem> deliverableList;
	private List<ScoreListDataItem> scoreList;
	private List<LogTimeListDataItem> logTimeList;

	public ProjectData() {
		super();
	}

	public ProjectData(NodeRef nodeRef, String name, NodeRef hierarchy1, NodeRef hierarchy2, Date startDate, Date dueDate, Date completionDate, 
			PlanningMode planningMode, Integer priority,ProjectState projectState,
			NodeRef projectTpl, Integer completionPercent, List<NodeRef> entities) {
		super(nodeRef, name);
		this.hierarchy1 = hierarchy1;
		this.hierarchy2 = hierarchy2;
		this.startDate = startDate;
		this.dueDate = dueDate;
		this.completionDate = completionDate;
		this.planningMode = planningMode;
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


	public Integer getReformulateCount() {
		return reformulateCount;
	}

	public void setReformulateCount(Integer reformulateCount) {
		this.reformulateCount = reformulateCount;
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
	@AlfQname(qname = "pjt:projectPlanningMode")	
	public PlanningMode getPlanningMode() {
		return planningMode;
	}

	public void setPlanningMode(PlanningMode planningMode) {
		this.planningMode = planningMode;
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
	
	public String getState() {
		return projectState!=null ? projectState.toString() : ProjectState.Planned.toString();
	}

	public void setState(String state) {
		this.projectState = ProjectState.valueOf(state);
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

	@AlfProp
	@AlfQname(qname = "pjt:projectBudgetedCost")
	public Double getBudgetedCost() {
		return budgetedCost;
	}

	public void setBudgetedCost(Double budgetedCost) {
		this.budgetedCost = budgetedCost;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectWork")
	public Double getWork() {
		return work;
	}

	public void setWork(Double work) {
		this.work = work;
	}

	@AlfProp
	@AlfQname(qname = "pjt:projectLoggedTime")
	public Double getLoggedTime() {
		return loggedTime;
	}

	public void setLoggedTime(Double loggedTime) {
		this.loggedTime = loggedTime;
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
	
	@DataList
	@AlfQname(qname="pjt:logTimeList")
	public List<LogTimeListDataItem> getLogTimeList() {
		return logTimeList;
	}

	public void setLogTimeList(List<LogTimeListDataItem> logTimeList) {
		this.logTimeList = logTimeList;
	}

	@Override
	public String getEntityState() {
		return projectState!=null ? projectState.toString() : null;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((budgetedCost == null) ? 0 : budgetedCost.hashCode());
		result = prime * result + ((completionDate == null) ? 0 : completionDate.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((deliverableList == null) ? 0 : deliverableList.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((formulatedDate == null) ? 0 : formulatedDate.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((hierarchy2 == null) ? 0 : hierarchy2.hashCode());
		result = prime * result + ((legends == null) ? 0 : legends.hashCode());
		result = prime * result + ((logTimeList == null) ? 0 : logTimeList.hashCode());
		result = prime * result + ((loggedTime == null) ? 0 : loggedTime.hashCode());
		result = prime * result + ((modified == null) ? 0 : modified.hashCode());
		result = prime * result + ((modifier == null) ? 0 : modifier.hashCode());
		result = prime * result + ((overdue == null) ? 0 : overdue.hashCode());
		result = prime * result + ((planningMode == null) ? 0 : planningMode.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((projectManager == null) ? 0 : projectManager.hashCode());
		result = prime * result + ((projectState == null) ? 0 : projectState.hashCode());
		result = prime * result + ((projectTpl == null) ? 0 : projectTpl.hashCode());
		result = prime * result + ((reformulateCount == null) ? 0 : reformulateCount.hashCode());
		result = prime * result + ((score == null) ? 0 : score.hashCode());
		result = prime * result + ((scoreList == null) ? 0 : scoreList.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((taskList == null) ? 0 : taskList.hashCode());
		result = prime * result + ((work == null) ? 0 : work.hashCode());
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
		if (budgetedCost == null) {
			if (other.budgetedCost != null)
				return false;
		} else if (!budgetedCost.equals(other.budgetedCost))
			return false;
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
		if (deliverableList == null) {
			if (other.deliverableList != null)
				return false;
		} else if (!deliverableList.equals(other.deliverableList))
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
		if (formulatedDate == null) {
			if (other.formulatedDate != null)
				return false;
		} else if (!formulatedDate.equals(other.formulatedDate))
			return false;
		if (hierarchy1 == null) {
			if (other.hierarchy1 != null)
				return false;
		} else if (!hierarchy1.equals(other.hierarchy1))
			return false;
		if (hierarchy2 == null) {
			if (other.hierarchy2 != null)
				return false;
		} else if (!hierarchy2.equals(other.hierarchy2))
			return false;
		if (legends == null) {
			if (other.legends != null)
				return false;
		} else if (!legends.equals(other.legends))
			return false;
		if (logTimeList == null) {
			if (other.logTimeList != null)
				return false;
		} else if (!logTimeList.equals(other.logTimeList))
			return false;
		if (loggedTime == null) {
			if (other.loggedTime != null)
				return false;
		} else if (!loggedTime.equals(other.loggedTime))
			return false;
		if (modified == null) {
			if (other.modified != null)
				return false;
		} else if (!modified.equals(other.modified))
			return false;
		if (modifier == null) {
			if (other.modifier != null)
				return false;
		} else if (!modifier.equals(other.modifier))
			return false;
		if (overdue == null) {
			if (other.overdue != null)
				return false;
		} else if (!overdue.equals(other.overdue))
			return false;
		if (planningMode != other.planningMode)
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
			return false;
		if (projectManager == null) {
			if (other.projectManager != null)
				return false;
		} else if (!projectManager.equals(other.projectManager))
			return false;
		if (projectState != other.projectState)
			return false;
		if (projectTpl == null) {
			if (other.projectTpl != null)
				return false;
		} else if (!projectTpl.equals(other.projectTpl))
			return false;
		if (reformulateCount == null) {
			if (other.reformulateCount != null)
				return false;
		} else if (!reformulateCount.equals(other.reformulateCount))
			return false;
		if (score == null) {
			if (other.score != null)
				return false;
		} else if (!score.equals(other.score))
			return false;
		if (scoreList == null) {
			if (other.scoreList != null)
				return false;
		} else if (!scoreList.equals(other.scoreList))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (taskList == null) {
			if (other.taskList != null)
				return false;
		} else if (!taskList.equals(other.taskList))
			return false;
		if (work == null) {
			if (other.work != null)
				return false;
		} else if (!work.equals(other.work))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProjectData [hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", startDate=" + startDate + ", dueDate=" + dueDate
				+ ", completionDate=" + completionDate + ", planningMode=" + planningMode + ", priority=" + priority + ", projectState="
				+ projectState + ", projectTpl=" + projectTpl + ", completionPercent=" + completionPercent + ", entities=" + entities + ", legends="
				+ legends + ", overdue=" + overdue + ", score=" + score + ", created=" + created + ", modified=" + modified + ", creator=" + creator
				+ ", modifier=" + modifier + ", projectManager=" + projectManager + ", budgetedCost=" + budgetedCost + ", work=" + work
				+ ", loggedTime=" + loggedTime + ", formulatedDate=" + formulatedDate + ", reformulateCount=" + reformulateCount + ", taskList="
				+ taskList + ", deliverableList=" + deliverableList + ", scoreList=" + scoreList + ", logTimeList=" + logTimeList + "]";
	}

	
	
}

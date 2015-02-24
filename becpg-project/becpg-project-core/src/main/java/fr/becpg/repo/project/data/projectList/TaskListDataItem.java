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

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Planning list (done or to do)
 * 
 * @author quere
 * 
 */
@AlfType
@AlfQname(qname = "pjt:taskList")
public class TaskListDataItem extends BeCPGDataObject implements CompositeDataItem<TaskListDataItem> {
	
	
	private String taskName;
	private Boolean isMilestone;
	private Boolean isGroup;
	private Integer duration;
	private Integer capacity;
	private Double work;
	private Double loggedTime;
	private Date start;
	private Date end;
	private TaskState taskState = TaskState.Planned;
	private Integer completionPercent = 0;
	private List<NodeRef> prevTasks;
	private List<NodeRef> resources;
	private List<NodeRef> observers;
	private NodeRef taskLegend;
	private String workflowName;
	private String workflowInstance;
	private TaskManualDate manualDate;
	private Integer depthLevel;
	private TaskListDataItem parent;
	private TaskListDataItem refusedTask;
	private Double fixedCost;
	private Double budgetedCost;
	private ResourceCost resourceCost; 

	@AlfProp
	@AlfQname(qname = "pjt:tlTaskName")
	public String getTaskName() {
		return taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlIsMilestone")
	public Boolean getIsMilestone() {
		return isMilestone;
	}

	public void setIsMilestone(Boolean isMilestone) {
		this.isMilestone = isMilestone;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlIsGroup")
	public Boolean getIsGroup() {
		return isGroup;
	}

	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlDuration")
	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlCapacity")
	public Integer getCapacity() {
		return capacity;
	}

	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlWork")
	public Double getWork() {
		return work;
	}

	public void setWork(Double work) {
		this.work = work;
	}
	@AlfProp
	@AlfQname(qname = "pjt:tlLoggedTime")
	public Double getLoggedTime() {
		return loggedTime;
	}

	public void setLoggedTime(Double loggedTime) {
		this.loggedTime = loggedTime;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlStart")
	public Date getStart() {
		return start;
	}

	public void setStart(Date start) {
		this.start = start;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlEnd")
	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlState")
	public TaskState getTaskState() {
		return taskState;
	}
	
	
	public void setTaskState(TaskState state) {
		this.taskState = state;
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
	@AlfQname(qname = "pjt:tlPrevTasks")
	public List<NodeRef> getPrevTasks() {
		return prevTasks;
	}

	public void setPrevTasks(List<NodeRef> prevTasks) {
		this.prevTasks = prevTasks;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "pjt:tlResources")
	public List<NodeRef> getResources() {
		return resources;
	}

	public void setResources(List<NodeRef> resources) {
		this.resources = resources;
	}

	@AlfMultiAssoc
	@AlfQname(qname = "pjt:tlObservers")
	public List<NodeRef> getObservers() {
		return observers;
	}

	public void setObservers(List<NodeRef> observers) {
		this.observers = observers;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "pjt:tlTaskLegend")
	public NodeRef getTaskLegend() {
		return taskLegend;
	}

	public void setTaskLegend(NodeRef taskLegend) {
		this.taskLegend = taskLegend;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlWorkflowName")
	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlWorkflowInstance")
	public String getWorkflowInstance() {
		return workflowInstance;
	}

	
	public void setWorkflowInstance(String workflowInstance) {
		this.workflowInstance = workflowInstance;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlManualDate")
	public TaskManualDate getManualDate() {
		return manualDate;
	}

	public void setManualDate(TaskManualDate manualDate) {
		this.manualDate = manualDate;
	}

	@Override
	@AlfProp
	@AlfQname(qname="bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	@Override
	@AlfProp
	@AlfQname(qname="bcpg:parentLevel")
	public TaskListDataItem getParent() {
		return this.parent;
	}

	
	@AlfSingleAssoc
	@AlfQname(qname="pjt:tlRefusedTaskRef")
	public TaskListDataItem getRefusedTask() {
		return refusedTask;
	}

	public void setRefusedTask(TaskListDataItem refusedTask) {
		this.refusedTask = refusedTask;
	}

	@Override
	public void setParent(TaskListDataItem parent) {
		this.parent = parent;		
	}
	
	@AlfProp
	@AlfQname(qname = "pjt:tlFixedCost")
	public Double getFixedCost() {
		return fixedCost;
	}

	public void setFixedCost(Double fixedCost) {
		this.fixedCost = fixedCost;
	}

	@AlfProp
	@AlfQname(qname = "pjt:tlBudgetedCost")
	public Double getBudgetedCost() {
		return budgetedCost;
	}

	public void setBudgetedCost(Double budgetedCost) {
		this.budgetedCost = budgetedCost;
	}

	@AlfSingleAssoc
	@AlfQname(qname = "pjt:tlResourceCost")
	public ResourceCost getResourceCost() {
		return resourceCost;
	}

	public void setResourceCost(ResourceCost resourceCost) {
		this.resourceCost = resourceCost;
	}

	public TaskListDataItem() {
		super();
	}

	public TaskListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	public TaskListDataItem(NodeRef nodeRef, String taskName, Boolean isMilestone, Integer duration, List<NodeRef> prevTasks, List<NodeRef> resources, NodeRef taskLegend,
			String workflowName) {
		super();
		this.nodeRef = nodeRef;
		this.taskName = taskName;
		this.isMilestone = isMilestone;
		this.duration = duration;
		this.prevTasks = prevTasks;
		this.resources = resources;
		this.taskLegend = taskLegend;
		this.workflowName = workflowName;
	}

	public TaskListDataItem(NodeRef nodeRef, String taskName, Boolean isMilestone, Integer duration, Date start, Date end, TaskState state, Integer completionPercent,
			List<NodeRef> prevTasks, List<NodeRef> resources, NodeRef taskLegend, String workflowName, String workflowInstance) {
		super();
		this.nodeRef = nodeRef;
		this.taskName = taskName;
		this.isMilestone = isMilestone;
		this.duration = duration;
		this.start = start;
		this.end = end;
		this.taskState = state!=null ? state : TaskState.Planned;
		this.completionPercent = completionPercent;
		this.prevTasks = prevTasks;
		this.resources = resources;
		this.taskLegend = taskLegend;
		this.workflowName = workflowName;
		this.workflowInstance = workflowInstance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((capacity == null) ? 0 : capacity.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((fixedCost == null) ? 0 : fixedCost.hashCode());
		result = prime * result + ((isGroup == null) ? 0 : isGroup.hashCode());
		result = prime * result + ((isMilestone == null) ? 0 : isMilestone.hashCode());
		result = prime * result + ((manualDate == null) ? 0 : manualDate.hashCode());
		result = prime * result + ((observers == null) ? 0 : observers.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((budgetedCost == null) ? 0 : budgetedCost.hashCode());
		result = prime * result + ((prevTasks == null) ? 0 : prevTasks.hashCode());
		result = prime * result + ((resourceCost == null) ? 0 : resourceCost.hashCode());
		result = prime * result + ((resources == null) ? 0 : resources.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((taskState == null) ? 0 : taskState.hashCode());
		result = prime * result + ((taskLegend == null) ? 0 : taskLegend.hashCode());
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + ((work == null) ? 0 : work.hashCode());
		result = prime * result + ((workflowInstance == null) ? 0 : workflowInstance.hashCode());
		result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
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
		TaskListDataItem other = (TaskListDataItem) obj;
		if (capacity == null) {
			if (other.capacity != null)
				return false;
		} else if (!capacity.equals(other.capacity))
			return false;
		if (completionPercent == null) {
			if (other.completionPercent != null)
				return false;
		} else if (!completionPercent.equals(other.completionPercent))
			return false;
		if (depthLevel == null) {
			if (other.depthLevel != null)
				return false;
		} else if (!depthLevel.equals(other.depthLevel))
			return false;
		if (duration == null) {
			if (other.duration != null)
				return false;
		} else if (!duration.equals(other.duration))
			return false;
		if (end == null) {
			if (other.end != null)
				return false;
		} else if (!end.equals(other.end))
			return false;
		if (fixedCost == null) {
			if (other.fixedCost != null)
				return false;
		} else if (!fixedCost.equals(other.fixedCost))
			return false;
		if (isGroup == null) {
			if (other.isGroup != null)
				return false;
		} else if (!isGroup.equals(other.isGroup))
			return false;
		if (isMilestone == null) {
			if (other.isMilestone != null)
				return false;
		} else if (!isMilestone.equals(other.isMilestone))
			return false;
		if (manualDate != other.manualDate)
			return false;
		if (observers == null) {
			if (other.observers != null)
				return false;
		} else if (!observers.equals(other.observers))
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		if (budgetedCost == null) {
			if (other.budgetedCost != null)
				return false;
		} else if (!budgetedCost.equals(other.budgetedCost))
			return false;
		if (prevTasks == null) {
			if (other.prevTasks != null)
				return false;
		} else if (!prevTasks.equals(other.prevTasks))
			return false;
		if (resourceCost == null) {
			if (other.resourceCost != null)
				return false;
		} else if (!resourceCost.equals(other.resourceCost))
			return false;
		if (resources == null) {
			if (other.resources != null)
				return false;
		} else if (!resources.equals(other.resources))
			return false;
		if (start == null) {
			if (other.start != null)
				return false;
		} else if (!start.equals(other.start))
			return false;
		if (taskState != other.taskState)
			return false;
		if (taskLegend == null) {
			if (other.taskLegend != null)
				return false;
		} else if (!taskLegend.equals(other.taskLegend))
			return false;
		if (taskName == null) {
			if (other.taskName != null)
				return false;
		} else if (!taskName.equals(other.taskName))
			return false;
		if (work == null) {
			if (other.work != null)
				return false;
		} else if (!work.equals(other.work))
			return false;
		if (workflowInstance == null) {
			if (other.workflowInstance != null)
				return false;
		} else if (!workflowInstance.equals(other.workflowInstance))
			return false;
		if (workflowName == null) {
			if (other.workflowName != null)
				return false;
		} else if (!workflowName.equals(other.workflowName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TaskListDataItem [taskName=" + taskName + ", isMilestone=" + isMilestone + ", isGroup=" + isGroup
				+ ", duration=" + duration + ", capacity=" + capacity + ", work=" + work + ", start=" + start
				+ ", end=" + end + ", state=" + taskState + ", completionPercent=" + completionPercent + ", prevTasks="
				+ prevTasks + ", resources=" + resources + ", observers=" + observers + ", taskLegend=" + taskLegend
				+ ", workflowName=" + workflowName + ", workflowInstance=" + workflowInstance + ", manualDate="
				+ manualDate + ", depthLevel=" + depthLevel + ", parent=" + parent + ", fixedCost=" + fixedCost
				+ ", budgetedCost=" + budgetedCost + ", resourceCost=" + resourceCost + "]";
	}

}

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

import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.data.hierarchicalList.CompositeDataItem;
import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfSingleAssoc;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
import fr.becpg.repo.repository.annotation.MultiLevelGroup;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * Planning list (done or to do)
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:taskList")
@MultiLevelDataList
public class TaskListDataItem extends BeCPGDataObject implements CompositeDataItem<TaskListDataItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 158129873096972078L;
	private String taskName;
	private Boolean isMilestone = false;
	private Boolean isGroup = false;
	private Boolean isExcludeFromSearch;
	private Boolean isRefused;
	private Boolean isCritical;
	private Integer duration;
	private Integer realDuration;
	private Integer capacity;
	private Double work;
	private Double loggedTime;
	private Date start;
	private Date end;
	private Date due;
	private Date targetStart;
	private Date targetEnd;
	
	private TaskState taskState = TaskState.Planned;
	private TaskState previousTaskState = null;
	private Integer completionPercent = 0;
	
	private List<NodeRef> refusedTasksToReopen;
	private List<NodeRef> prevTasks;
	private List<NodeRef> resources;
	private List<NodeRef> observers;
	private NodeRef taskLegend;
	private String workflowName;
	private String workflowInstance;
	private String workflowTaskInstance;
	private TaskManualDate manualDate;
	private Integer depthLevel;
	private TaskListDataItem parent;
	private TaskListDataItem refusedTask;
	private Double fixedCost;
	private Double expense;	
	private Double invoice;
	private ResourceCost resourceCost;
	//Notification
	private Integer notificationFrequency;
	private Integer initialNotification;
	private Date lastNotification;
	private List<NodeRef> notificationAuthorities;
	private String description;
	
	private NodeRef subProject;
	
	/**
	 * <p>Getter for the field <code>description</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlTaskDescription")
	public String getDescription() {
		return description;
	}
	
	/**
	 * <p>Setter for the field <code>description</code>.</p>
	 *
	 * @param description a {@link java.lang.String} object
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * <p>Getter for the field <code>subProject</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@DataListIdentifierAttr(isDefaultPivotAssoc=true)
	@AlfQname(qname="pjt:subProjectRef")
	public NodeRef getSubProject() {
		return subProject;
	}
	
	/**
	 * <p>Setter for the field <code>subProject</code>.</p>
	 *
	 * @param subProject a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setSubProject(NodeRef subProject) {
		this.subProject = subProject;
	}	
	

	/**
	 * <p>Getter for the field <code>taskName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@DataListIdentifierAttr(isDefaultPivotAssoc=false)
	@AlfQname(qname = "pjt:tlTaskName")
	public String getTaskName() {
		return taskName;
	}

	/**
	 * <p>Setter for the field <code>taskName</code>.</p>
	 *
	 * @param taskName a {@link java.lang.String} object.
	 */
	public void setTaskName(String taskName) {
		this.taskName = taskName;
	}

	/**
	 * <p>Getter for the field <code>isMilestone</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlIsMilestone")
	public Boolean getIsMilestone() {
		return isMilestone;
	}

	/**
	 * <p>Setter for the field <code>isMilestone</code>.</p>
	 *
	 * @param isMilestone a {@link java.lang.Boolean} object.
	 */
	public void setIsMilestone(Boolean isMilestone) {
		this.isMilestone = isMilestone;
	}
	
	

	/**
	 * <p>Getter for the field <code>isGroup</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlIsGroup")
	@MultiLevelGroup
	public Boolean getIsGroup() {
		return isGroup;
	}

	/**
	 * <p>Setter for the field <code>isGroup</code>.</p>
	 *
	 * @param isGroup a {@link java.lang.Boolean} object.
	 */
	public void setIsGroup(Boolean isGroup) {
		this.isGroup = isGroup;
	}
	
	
	/**
	 * <p>Getter for the field <code>isRefused</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlIsRefused")
	public Boolean getIsRefused() {
		return isRefused;
	}

	/**
	 * <p>Setter for the field <code>isRefused</code>.</p>
	 *
	 * @param isRefused a {@link java.lang.Boolean} object.
	 */
	public void setIsRefused(Boolean isRefused) {
		this.isRefused = isRefused;
	}

	/**
	 * <p>Getter for the field <code>isExcludeFromSearch</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlIsExcludeFromSearch")
	public Boolean getIsExcludeFromSearch() {
		return isExcludeFromSearch;
	}

	/**
	 * <p>Setter for the field <code>isExcludeFromSearch</code>.</p>
	 *
	 * @param isExcludeFromSearch a {@link java.lang.Boolean} object.
	 */
	public void setIsExcludeFromSearch(Boolean isExcludeFromSearch) {
		this.isExcludeFromSearch = isExcludeFromSearch;
	}

	
	
	/**
	 * <p>Getter for the field <code>isCritical</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlIsCritical")
	public Boolean getIsCritical() {
		return isCritical;
	}

	/**
	 * <p>Setter for the field <code>isCritical</code>.</p>
	 *
	 * @param isCritical a {@link java.lang.Boolean} object
	 */
	public void setIsCritical(Boolean isCritical) {
		this.isCritical = isCritical;
	}

	/**
	 * <p>Getter for the field <code>duration</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlDuration")
	public Integer getDuration() {
		return duration;
	}

	/**
	 * <p>Setter for the field <code>duration</code>.</p>
	 *
	 * @param duration a {@link java.lang.Integer} object.
	 */
	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	/**
	 * <p>Getter for the field <code>realDuration</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlRealDuration")
	public Integer getRealDuration() {
		return realDuration;
	}

	/**
	 * <p>Setter for the field <code>realDuration</code>.</p>
	 *
	 * @param realDuration a {@link java.lang.Integer} object.
	 */
	public void setRealDuration(Integer realDuration) {
		this.realDuration = realDuration;
	}

	/**
	 * <p>Getter for the field <code>capacity</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlCapacity")
	public Integer getCapacity() {
		return capacity;
	}

	/**
	 * <p>Setter for the field <code>capacity</code>.</p>
	 *
	 * @param capacity a {@link java.lang.Integer} object.
	 */
	public void setCapacity(Integer capacity) {
		this.capacity = capacity;
	}

	/**
	 * <p>Getter for the field <code>work</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlWork")
	public Double getWork() {
		return work;
	}

	/**
	 * <p>Setter for the field <code>work</code>.</p>
	 *
	 * @param work a {@link java.lang.Double} object.
	 */
	public void setWork(Double work) {
		this.work = work;
	}

	/**
	 * <p>Getter for the field <code>loggedTime</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlLoggedTime")
	public Double getLoggedTime() {
		return loggedTime;
	}

	/**
	 * <p>Setter for the field <code>loggedTime</code>.</p>
	 *
	 * @param loggedTime a {@link java.lang.Double} object.
	 */
	public void setLoggedTime(Double loggedTime) {
		this.loggedTime = loggedTime;
	}

	/**
	 * <p>Getter for the field <code>start</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlStart")
	public Date getStart() {
		return start;
	}

	/**
	 * <p>Setter for the field <code>start</code>.</p>
	 *
	 * @param start a {@link java.util.Date} object.
	 */
	public void setStart(Date start) {
		this.start = start;
	}

	/**
	 * <p>Getter for the field <code>end</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlEnd")
	public Date getEnd() {
		return end;
	}

	/**
	 * <p>Setter for the field <code>end</code>.</p>
	 *
	 * @param end a {@link java.util.Date} object.
	 */
	public void setEnd(Date end) {
		this.end = end;
	}

	
	
	
	/**
	 * <p>Getter for the field <code>due</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlDue")
	public Date getDue() {
		return due;
	}

	/**
	 * <p>Setter for the field <code>due</code>.</p>
	 *
	 * @param due a {@link java.util.Date} object
	 */
	public void setDue(Date due) {
		this.due = due;
	}

	/**
	 * <p>Getter for the field <code>targetStart</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlTargetStart")
	public Date getTargetStart() {
		return targetStart;
	}

	/**
	 * <p>Setter for the field <code>targetStart</code>.</p>
	 *
	 * @param targetStart a {@link java.util.Date} object
	 */
	public void setTargetStart(Date targetStart) {
		this.targetStart = targetStart;
	}

	/**
	 * <p>Getter for the field <code>targetEnd</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlTargetEnd")
	public Date getTargetEnd() {
		return targetEnd;
	}

	/**
	 * <p>Setter for the field <code>targetEnd</code>.</p>
	 *
	 * @param targetEnd a {@link java.util.Date} object
	 */
	public void setTargetEnd(Date targetEnd) {
		this.targetEnd = targetEnd;
	}

	/**
	 * <p>Getter for the field <code>taskState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskState} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlState")
	public TaskState getTaskState() {
		return taskState;
	}
	
	

	/**
	 * <p>Setter for the field <code>taskState</code>.</p>
	 *
	 * @param state a {@link fr.becpg.repo.project.data.projectList.TaskState} object.
	 */
	public void setTaskState(TaskState state) {
		this.taskState = state;
	}

	/**
	 * <p>Getter for the field <code>previousTaskState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskState} object
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlPreviousState")
	public TaskState getPreviousTaskState() {
		return previousTaskState;
	}
	
	/**
	 * <p>Setter for the field <code>taskState</code>.</p>
	 *
	 * @param previousTaskState a {@link fr.becpg.repo.project.data.projectList.TaskState} object
	 */
	public void setPreviousTaskState(TaskState previousTaskState) {
		this.previousTaskState = previousTaskState;
	}
	
	
	// Helper method for script and Spel
	/**
	 * <p>getState.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getState() {
		return taskState != null ? taskState.toString() : TaskState.Planned.toString();
	}

	/**
	 * <p>setState.</p>
	 *
	 * @param state a {@link java.lang.String} object.
	 */
	public void setState(String state) {
		this.taskState = TaskState.valueOf(state);
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
	 * <p>Getter for the field <code>prevTasks</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:tlPrevTasks")
	public List<NodeRef> getPrevTasks() {
		return prevTasks;
	}

	/**
	 * <p>Setter for the field <code>prevTasks</code>.</p>
	 *
	 * @param prevTasks a {@link java.util.List} object.
	 */
	public void setPrevTasks(List<NodeRef> prevTasks) {
		this.prevTasks = prevTasks;
	}
	
	
	/**
	 * <p>Getter for the field <code>refusedTasksToReopen</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:tlRefusedTasksToReopen")
	public List<NodeRef> getRefusedTasksToReopen() {
		return refusedTasksToReopen;
	}

	/**
	 * <p>Setter for the field <code>refusedTasksToReopen</code>.</p>
	 *
	 * @param refusedTasksToReopen a {@link java.util.List} object.
	 */
	public void setRefusedTasksToReopen(List<NodeRef> refusedTasksToReopen) {
		this.refusedTasksToReopen = refusedTasksToReopen;
	}

	/**
	 * <p>Getter for the field <code>resources</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:tlResources")
	public List<NodeRef> getResources() {
		return resources;
	}

	/**
	 * <p>Setter for the field <code>resources</code>.</p>
	 *
	 * @param resources a {@link java.util.List} object.
	 */
	public void setResources(List<NodeRef> resources) {
		this.resources = resources;
	}

	/**
	 * <p>Getter for the field <code>observers</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:tlObservers")
	public List<NodeRef> getObservers() {
		return observers;
	}

	/**
	 * <p>Setter for the field <code>observers</code>.</p>
	 *
	 * @param observers a {@link java.util.List} object.
	 */
	public void setObservers(List<NodeRef> observers) {
		this.observers = observers;
	}

	/**
	 * <p>Getter for the field <code>taskLegend</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:tlTaskLegend")
	public NodeRef getTaskLegend() {
		return taskLegend;
	}

	/**
	 * <p>Setter for the field <code>taskLegend</code>.</p>
	 *
	 * @param taskLegend a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setTaskLegend(NodeRef taskLegend) {
		this.taskLegend = taskLegend;
	}

	/**
	 * <p>Getter for the field <code>workflowName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlWorkflowName")
	public String getWorkflowName() {
		return workflowName;
	}

	/**
	 * <p>Setter for the field <code>workflowName</code>.</p>
	 *
	 * @param workflowName a {@link java.lang.String} object.
	 */
	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	/**
	 * <p>Getter for the field <code>workflowInstance</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlWorkflowInstance")
	public String getWorkflowInstance() {
		return workflowInstance;
	}

	/**
	 * <p>Setter for the field <code>workflowInstance</code>.</p>
	 *
	 * @param workflowInstance a {@link java.lang.String} object.
	 */
	public void setWorkflowInstance(String workflowInstance) {
		this.workflowInstance = workflowInstance;
	}

	
	
	/**
	 * <p>Getter for the field <code>workflowTaskInstance</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlWorkflowTaskInstance")
	public String getWorkflowTaskInstance() {
		return workflowTaskInstance;
	}

	/**
	 * <p>Setter for the field <code>workflowTaskInstance</code>.</p>
	 *
	 * @param workflowTaskInstance a {@link java.lang.String} object.
	 */
	public void setWorkflowTaskInstance(String workflowTaskInstance) {
		this.workflowTaskInstance = workflowTaskInstance;
	}

	/**
	 * <p>Getter for the field <code>manualDate</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskManualDate} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:tlManualDate")
	public TaskManualDate getManualDate() {
		return manualDate;
	}

	/**
	 * <p>Setter for the field <code>manualDate</code>.</p>
	 *
	 * @param manualDate a {@link fr.becpg.repo.project.data.projectList.TaskManualDate} object.
	 */
	public void setManualDate(TaskManualDate manualDate) {
		this.manualDate = manualDate;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:depthLevel")
	public Integer getDepthLevel() {
		return depthLevel;
	}

	/**
	 * <p>Setter for the field <code>depthLevel</code>.</p>
	 *
	 * @param depthLevel a {@link java.lang.Integer} object.
	 */
	public void setDepthLevel(Integer depthLevel) {
		this.depthLevel = depthLevel;
	}

	/** {@inheritDoc} */
	@Override
	@AlfProp
	@AlfQname(qname = "bcpg:parentLevel")
	@InternalField
	public TaskListDataItem getParent() {
		return this.parent;
	}

	/**
	 * <p>Getter for the field <code>refusedTask</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:tlRefusedTaskRef")
	public TaskListDataItem getRefusedTask() {
		return refusedTask;
	}

	/**
	 * <p>Setter for the field <code>refusedTask</code>.</p>
	 *
	 * @param refusedTask a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object.
	 */
	public void setRefusedTask(TaskListDataItem refusedTask) {
		this.refusedTask = refusedTask;
	}

	/** {@inheritDoc} */
	@Override
	public void setParent(TaskListDataItem parent) {
		this.parent = parent;
	}

	/**
	 * <p>Getter for the field <code>fixedCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@Deprecated
	@AlfProp
	@AlfQname(qname = "pjt:tlFixedCost")
	public Double getFixedCost() {
		return fixedCost;
	}

	/**
	 * <p>Setter for the field <code>fixedCost</code>.</p>
	 *
	 * @param fixedCost a {@link java.lang.Double} object.
	 */
	@Deprecated
	public void setFixedCost(Double fixedCost) {
		this.fixedCost = fixedCost;
	}

	/**
	 * <p>Getter for the field <code>expense</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:expense")
	public Double getExpense() {
		return expense;
	}
	
	/**
	 * <p>Setter for the field <code>expense</code>.</p>
	 *
	 * @param expense a {@link java.lang.Double} object.
	 */
	public void setExpense(Double expense) {
		this.expense = expense;
	}
	
	/**
	 * <p>Getter for the field <code>invoice</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:invoice")
	public Double getInvoice() {
		return invoice;
	}

	/**
	 * <p>Setter for the field <code>invoice</code>.</p>
	 *
	 * @param invoice a {@link java.lang.Double} object.
	 */
	public void setInvoice(Double invoice) {
		this.invoice = invoice;
	}

	/**
	 * <p>Getter for the field <code>resourceCost</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.ResourceCost} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:tlResourceCost")
	public ResourceCost getResourceCost() {
		return resourceCost;
	}

	/**
	 * <p>Setter for the field <code>resourceCost</code>.</p>
	 *
	 * @param resourceCost a {@link fr.becpg.repo.project.data.projectList.ResourceCost} object.
	 */
	public void setResourceCost(ResourceCost resourceCost) {
		this.resourceCost = resourceCost;
	}

	/**
	 * <p>Getter for the field <code>notificationFrequency</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:notificationFrequencyValue")
	public Integer getNotificationFrequency() {
		return notificationFrequency;
	}

	/**
	 * <p>Setter for the field <code>notificationFrequency</code>.</p>
	 *
	 * @param notificationFrequency a {@link java.lang.Integer} object.
	 */
	public void setNotificationFrequency(Integer notificationFrequency) {
		this.notificationFrequency = notificationFrequency;
	}

	/**
	 * <p>Getter for the field <code>initialNotification</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:notificationInitialValue")
	public Integer getInitialNotification() {
		return initialNotification;
	}

	/**
	 * <p>Setter for the field <code>initialNotification</code>.</p>
	 *
	 * @param initialNotification a {@link java.lang.Integer} object.
	 */
	public void setInitialNotification(Integer initialNotification) {
		this.initialNotification = initialNotification;
	}

	/**
	 * <p>Getter for the field <code>lastNotification</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:lastNotificationDate")
	public Date getLastNotification() {
		return lastNotification;
	}

	/**
	 * <p>Setter for the field <code>lastNotification</code>.</p>
	 *
	 * @param lastNotification a {@link java.util.Date} object.
	 */
	public void setLastNotification(Date lastNotification) {
		this.lastNotification = lastNotification;
	}

	/**
	 * <p>Getter for the field <code>notificationAuthorities</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:notificationAuthorities")
	public List<NodeRef> getNotificationAuthorities() {
		return notificationAuthorities;
	}

	/**
	 * <p>Setter for the field <code>notificationAuthorities</code>.</p>
	 *
	 * @param notificationAuthorities a {@link java.util.List} object.
	 */
	public void setNotificationAuthorities(List<NodeRef> notificationAuthorities) {
		this.notificationAuthorities = notificationAuthorities;
	}

	/**
	 * <p>Constructor for TaskListDataItem.</p>
	 */
	public TaskListDataItem() {
		super();
	}


	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
	 */
	public static TaskListDataItem build() {
		return new TaskListDataItem();
	}

    /**
     * <p>withNodeRef.</p>
     *
     * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withNodeRef(NodeRef nodeRef) {
        this.nodeRef = nodeRef;
        return this;
    }

    /**
     * <p>withTaskName.</p>
     *
     * @param taskName a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withTaskName(String taskName) {
        this.taskName = taskName;
        return this;
    }

    /**
     * <p>withIsMilestone.</p>
     *
     * @param isMilestone a {@link java.lang.Boolean} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withIsMilestone(Boolean isMilestone) {
        this.isMilestone = isMilestone;
        return this;
    }

    /**
     * <p>withDuration.</p>
     *
     * @param duration a {@link java.lang.Integer} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withDuration(Integer duration) {
        this.duration = duration;
        return this;
    }

    /**
     * <p>withStart.</p>
     *
     * @param start a {@link java.util.Date} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withStart(Date start) {
        this.start = start;
        return this;
    }

    /**
     * <p>withEnd.</p>
     *
     * @param end a {@link java.util.Date} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withEnd(Date end) {
        this.end = end;
        return this;
    }

    /**
     * <p>withTaskState.</p>
     *
     * @param taskState a {@link fr.becpg.repo.project.data.projectList.TaskState} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withTaskState(TaskState taskState) {
        this.taskState = taskState;
        return this;
    }

    /**
     * <p>withCompletionPercent.</p>
     *
     * @param completionPercent a {@link java.lang.Integer} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withCompletionPercent(Integer completionPercent) {
        this.completionPercent = completionPercent;
        return this;
    }

    /**
     * <p>withPrevTasks.</p>
     *
     * @param prevTasks a {@link java.util.List} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withPrevTasks(List<NodeRef> prevTasks) {
        this.prevTasks = prevTasks;
        return this;
    }

    /**
     * <p>withResources.</p>
     *
     * @param resources a {@link java.util.List} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withResources(List<NodeRef> resources) {
        this.resources = resources;
        return this;
    }

    /**
     * <p>withTaskLegend.</p>
     *
     * @param taskLegend a {@link org.alfresco.service.cmr.repository.NodeRef} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withTaskLegend(NodeRef taskLegend) {
        this.taskLegend = taskLegend;
        return this;
    }

    /**
     * <p>withWorkflowName.</p>
     *
     * @param workflowName a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withWorkflowName(String workflowName) {
        this.workflowName = workflowName;
        return this;
    }

    /**
     * <p>withWorkflowInstance.</p>
     *
     * @param workflowInstance a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withWorkflowInstance(String workflowInstance) {
        this.workflowInstance = workflowInstance;
        return this;
    }


    /**
     * <p>withExpense.</p>
     *
     * @param expense a {@link java.lang.Double} object
     * @return a {@link fr.becpg.repo.project.data.projectList.TaskListDataItem} object
     */
    public TaskListDataItem withExpense(Double expense) {
        this.expense = expense;
        return this;
    }
	
	
	
	/**
	 * <p>Constructor for TaskListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 */
	public TaskListDataItem(NodeRef nodeRef, String name) {
		super(nodeRef, name);
	}

	/**
	 * <p>Constructor for TaskListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskName a {@link java.lang.String} object.
	 * @param isMilestone a {@link java.lang.Boolean} object.
	 * @param duration a {@link java.lang.Integer} object.
	 * @param prevTasks a {@link java.util.List} object.
	 * @param resources a {@link java.util.List} object.
	 * @param taskLegend a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workflowName a {@link java.lang.String} object.
	 */
	@Deprecated
	public TaskListDataItem(NodeRef nodeRef, String taskName, Boolean isMilestone, Integer duration, List<NodeRef> prevTasks,
			List<NodeRef> resources, NodeRef taskLegend, String workflowName) {
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

	/**
	 * <p>Constructor for TaskListDataItem.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param taskName a {@link java.lang.String} object.
	 * @param isMilestone a {@link java.lang.Boolean} object.
	 * @param duration a {@link java.lang.Integer} object.
	 * @param start a {@link java.util.Date} object.
	 * @param end a {@link java.util.Date} object.
	 * @param state a {@link fr.becpg.repo.project.data.projectList.TaskState} object.
	 * @param completionPercent a {@link java.lang.Integer} object.
	 * @param prevTasks a {@link java.util.List} object.
	 * @param resources a {@link java.util.List} object.
	 * @param taskLegend a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workflowName a {@link java.lang.String} object.
	 * @param workflowInstance a {@link java.lang.String} object.
	 * @param expense a {@link java.lang.Double} object.
	 */
	@Deprecated
	public TaskListDataItem(NodeRef nodeRef, String taskName, Boolean isMilestone, Integer duration, Date start, Date end, TaskState state,
			Integer completionPercent, List<NodeRef> prevTasks, List<NodeRef> resources, NodeRef taskLegend, String workflowName,
			String workflowInstance , Double expense) {
		super();
		this.nodeRef = nodeRef;
		this.taskName = taskName;
		this.isMilestone = isMilestone;
		this.duration = duration;
		this.start = start;
		this.end = end;
		this.taskState = state != null ? state : TaskState.Planned;
		this.completionPercent = completionPercent;
		this.prevTasks = prevTasks;
		this.resources = resources;
		this.taskLegend = taskLegend;
		this.workflowName = workflowName;
		this.workflowInstance = workflowInstance;
		this.expense = expense;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(capacity, completionPercent, depthLevel, duration, end, expense, fixedCost,
				initialNotification, invoice, isExcludeFromSearch, isGroup, isMilestone, isRefused, lastNotification, loggedTime, manualDate,
				notificationAuthorities, notificationFrequency, observers, realDuration, resourceCost, resources, start, subProject, taskLegend,
				taskName, taskState, work, workflowInstance, workflowName, workflowTaskInstance);
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
		TaskListDataItem other = (TaskListDataItem) obj;
		return Objects.equals(capacity, other.capacity)
				&& Objects.equals(completionPercent, other.completionPercent) && Objects.equals(depthLevel, other.depthLevel)
				&& Objects.equals(duration, other.duration) && Objects.equals(end, other.end) && Objects.equals(expense, other.expense)
				&& Objects.equals(fixedCost, other.fixedCost) && Objects.equals(initialNotification, other.initialNotification)
				&& Objects.equals(invoice, other.invoice) && Objects.equals(isExcludeFromSearch, other.isExcludeFromSearch)
				&& Objects.equals(isGroup, other.isGroup) && Objects.equals(isMilestone, other.isMilestone)
				&& Objects.equals(isRefused, other.isRefused) && Objects.equals(lastNotification, other.lastNotification)
				&& Objects.equals(loggedTime, other.loggedTime) && manualDate == other.manualDate
				&& Objects.equals(notificationAuthorities, other.notificationAuthorities)
				&& Objects.equals(notificationFrequency, other.notificationFrequency) && Objects.equals(observers, other.observers)
				&& Objects.equals(realDuration, other.realDuration) && Objects.equals(resourceCost, other.resourceCost)
				&& Objects.equals(resources, other.resources) && Objects.equals(start, other.start) && Objects.equals(subProject, other.subProject)
				&& Objects.equals(taskLegend, other.taskLegend) && Objects.equals(taskName, other.taskName) && taskState == other.taskState
				&& Objects.equals(work, other.work) && Objects.equals(workflowInstance, other.workflowInstance)
				&& Objects.equals(workflowName, other.workflowName) && Objects.equals(workflowTaskInstance, other.workflowTaskInstance);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "TaskListDataItem [taskName=" + taskName + ", isMilestone=" + isMilestone + ", isGroup=" + isGroup
				+ ", duration=" + duration + ", realDuration=" + realDuration + ", capacity=" + capacity + ", work="
				+ work + ", loggedTime=" + loggedTime + ", start=" + start + ", end=" + end + ", taskState="
				+ taskState + ", completionPercent=" + completionPercent + ", prevTasks=" + prevTasks + ", resources="
				+ resources + ", observers=" + observers + ", taskLegend=" + taskLegend + ", workflowName="
				+ workflowName + ", workflowInstance=" + workflowInstance + ", manualDate=" + manualDate
				+ ", depthLevel=" + depthLevel + ", parent=" + parent + ", refusedTask=" + refusedTask + ", fixedCost="
				+ fixedCost + ", expense=" + expense + ", invoice=" + invoice
				+ ", resourceCost=" + resourceCost + "]";
	}

	/**
	 * <p>isPlanned.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPlanned() {
		return  TaskState.Planned.equals(taskState) || (TaskState.Refused.equals(taskState) && Boolean.TRUE.equals(isRefused));
	}

	/**
	 * <p>isRefused.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isRefused() {
		return  TaskState.Refused.equals(taskState) && !Boolean.TRUE.equals(isRefused);
	}


}

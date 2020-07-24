/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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
import fr.becpg.repo.repository.annotation.DataListIdentifierAttr;
import fr.becpg.repo.repository.annotation.MultiLevelDataList;
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
	private Integer duration;
	private Integer realDuration;
	private Integer capacity;
	private Double work;
	private Double loggedTime;
	private Date start;
	private Date end;
	private TaskState taskState = TaskState.Planned;
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
	private Double budgetedCost;
	private Double expense;	
	private Double invoice;
	private ResourceCost resourceCost;
	//Notification
	private Integer notificationFrequency;
	private Integer initialNotification;
	private Date lastNotification;
	private List<NodeRef> notificationAuthorities;
	
	private NodeRef subProject;
	
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
	 * <p>Getter for the field <code>budgetedCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@Deprecated
	@AlfProp
	@AlfQname(qname = "pjt:tlBudgetedCost")
	public Double getBudgetedCost() {
		return budgetedCost;
	}

	/**
	 * <p>Setter for the field <code>budgetedCost</code>.</p>
	 *
	 * @param budgetedCost a {@link java.lang.Double} object.
	 */
	@Deprecated
	public void setBudgetedCost(Double budgetedCost) {
		this.budgetedCost = budgetedCost;
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
	 * @param plannedExpense a {@link java.lang.Double} object.
	 * @param expense a {@link java.lang.Double} object.
	 */
	public TaskListDataItem(NodeRef nodeRef, String taskName, Boolean isMilestone, Integer duration, Date start, Date end, TaskState state,
			Integer completionPercent, List<NodeRef> prevTasks, List<NodeRef> resources, NodeRef taskLegend, String workflowName,
			String workflowInstance, Double plannedExpense , Double expense) {
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
		result = prime * result + ((budgetedCost == null) ? 0 : budgetedCost.hashCode());
		result = prime * result + ((capacity == null) ? 0 : capacity.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((depthLevel == null) ? 0 : depthLevel.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((expense == null) ? 0 : expense.hashCode());
		result = prime * result + ((fixedCost == null) ? 0 : fixedCost.hashCode());
		result = prime * result + ((initialNotification == null) ? 0 : initialNotification.hashCode());
		result = prime * result + ((invoice == null) ? 0 : invoice.hashCode());
		result = prime * result + ((isExcludeFromSearch == null) ? 0 : isExcludeFromSearch.hashCode());
		result = prime * result + ((isGroup == null) ? 0 : isGroup.hashCode());
		result = prime * result + ((isMilestone == null) ? 0 : isMilestone.hashCode());
		result = prime * result + ((isRefused == null) ? 0 : isRefused.hashCode());
		result = prime * result + ((lastNotification == null) ? 0 : lastNotification.hashCode());
		result = prime * result + ((loggedTime == null) ? 0 : loggedTime.hashCode());
		result = prime * result + ((manualDate == null) ? 0 : manualDate.hashCode());
		result = prime * result + ((notificationAuthorities == null) ? 0 : notificationAuthorities.hashCode());
		result = prime * result + ((notificationFrequency == null) ? 0 : notificationFrequency.hashCode());
		result = prime * result + ((observers == null) ? 0 : observers.hashCode());
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		result = prime * result + ((prevTasks == null) ? 0 : prevTasks.hashCode());
		result = prime * result + ((realDuration == null) ? 0 : realDuration.hashCode());
		result = prime * result + ((refusedTask == null) ? 0 : refusedTask.hashCode());
		result = prime * result + ((refusedTasksToReopen == null) ? 0 : refusedTasksToReopen.hashCode());
		result = prime * result + ((resourceCost == null) ? 0 : resourceCost.hashCode());
		result = prime * result + ((resources == null) ? 0 : resources.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((subProject == null) ? 0 : subProject.hashCode());
		result = prime * result + ((taskLegend == null) ? 0 : taskLegend.hashCode());
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + ((taskState == null) ? 0 : taskState.hashCode());
		result = prime * result + ((work == null) ? 0 : work.hashCode());
		result = prime * result + ((workflowInstance == null) ? 0 : workflowInstance.hashCode());
		result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
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
		if (budgetedCost == null) {
			if (other.budgetedCost != null)
				return false;
		} else if (!budgetedCost.equals(other.budgetedCost))
			return false;
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
		if (expense == null) {
			if (other.expense != null)
				return false;
		} else if (!expense.equals(other.expense))
			return false;
		if (fixedCost == null) {
			if (other.fixedCost != null)
				return false;
		} else if (!fixedCost.equals(other.fixedCost))
			return false;
		if (initialNotification == null) {
			if (other.initialNotification != null)
				return false;
		} else if (!initialNotification.equals(other.initialNotification))
			return false;
		if (invoice == null) {
			if (other.invoice != null)
				return false;
		} else if (!invoice.equals(other.invoice))
			return false;
		if (isExcludeFromSearch == null) {
			if (other.isExcludeFromSearch != null)
				return false;
		} else if (!isExcludeFromSearch.equals(other.isExcludeFromSearch))
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
		if (isRefused == null) {
			if (other.isRefused != null)
				return false;
		} else if (!isRefused.equals(other.isRefused))
			return false;
		if (lastNotification == null) {
			if (other.lastNotification != null)
				return false;
		} else if (!lastNotification.equals(other.lastNotification))
			return false;
		if (loggedTime == null) {
			if (other.loggedTime != null)
				return false;
		} else if (!loggedTime.equals(other.loggedTime))
			return false;
		if (manualDate != other.manualDate)
			return false;
		if (notificationAuthorities == null) {
			if (other.notificationAuthorities != null)
				return false;
		} else if (!notificationAuthorities.equals(other.notificationAuthorities))
			return false;
		if (notificationFrequency == null) {
			if (other.notificationFrequency != null)
				return false;
		} else if (!notificationFrequency.equals(other.notificationFrequency))
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
		if (prevTasks == null) {
			if (other.prevTasks != null)
				return false;
		} else if (!prevTasks.equals(other.prevTasks))
			return false;
		if (realDuration == null) {
			if (other.realDuration != null)
				return false;
		} else if (!realDuration.equals(other.realDuration))
			return false;
		if (refusedTask == null) {
			if (other.refusedTask != null)
				return false;
		} else if (!refusedTask.equals(other.refusedTask))
			return false;
		if (refusedTasksToReopen == null) {
			if (other.refusedTasksToReopen != null)
				return false;
		} else if (!refusedTasksToReopen.equals(other.refusedTasksToReopen))
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
		if (subProject == null) {
			if (other.subProject != null)
				return false;
		} else if (!subProject.equals(other.subProject))
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
		if (taskState != other.taskState)
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
				+ fixedCost + ", budgetedCost=" + budgetedCost + ", expense=" + expense + ", invoice=" + invoice
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

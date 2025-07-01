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
package fr.becpg.repo.project.data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.entity.catalog.CataloguableEntity;
import fr.becpg.repo.formulation.FormulatedEntity;
import fr.becpg.repo.hierarchy.HierarchicalEntity;
import fr.becpg.repo.project.data.projectList.BudgetListDataItem;
import fr.becpg.repo.project.data.projectList.DeliverableListDataItem;
import fr.becpg.repo.project.data.projectList.ExpenseListDataItem;
import fr.becpg.repo.project.data.projectList.InvoiceListDataItem;
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
import fr.becpg.repo.survey.data.SurveyListDataItem;
import fr.becpg.repo.survey.data.SurveyableEntity;

/**
 * ProjectData used to manipulate project
 *
 * @author quere
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "pjt:project")
public class ProjectData extends BeCPGDataObject
		implements CataloguableEntity, AspectAwareDataItem, FormulatedEntity, HierarchicalEntity, StateableEntity, SurveyableEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6572843222555593368L;
	private String code;
	private NodeRef hierarchy1;
	private NodeRef hierarchy2;
	private Date startDate;
	private Date targetStartDate;
	private Date dueDate;
	private Date completionDate;
	private PlanningMode planningMode = PlanningMode.Planning;
	private Integer priority = 2;
	private ProjectState projectState = ProjectState.Planned;
	private NodeRef projectTpl;
	private Integer completionPercent = 0;
	private List<NodeRef> entities;
	private List<NodeRef> projectOwners;
	private List<NodeRef> legends = new ArrayList<>();
	private Integer overdue = 0;
	private Integer score = 0;
	private Integer duration;
	private Integer realDuration;
	private Date created;
	private Date modified;
	private String creator;
	private String modifier;
	private NodeRef projectManager;
	private Double budgetedCost = 0d;
	private Double work = 0d;
	private Double loggedTime = 0d;
	
	private boolean dirtyTaskTree = false;
	
	public boolean isDirtyTaskTree() {
		return dirtyTaskTree;
	}
	
	public void setDirtyTaskTree(boolean dirtyTaskTree) {
		this.dirtyTaskTree = dirtyTaskTree;
	}
	
	/*
	 * Completion scores
	 */
	private String entityScore;
	private List<String> reportLocales;

	/*
	 * Formulation
	 */
	private Date formulatedDate;

	private Integer reformulateCount;
	private Integer currentReformulateCount;
	private String formulationChainId;
	private Boolean updateFormulatedDate = true;
	private String requirementChecksum;

	private List<TaskListDataItem> taskList;
	private List<DeliverableListDataItem> deliverableList;
	private List<LogTimeListDataItem> logTimeList;
	private List<BudgetListDataItem> budgetList;
	private List<InvoiceListDataItem> invoiceList;
	private List<ExpenseListDataItem> expenseList;
	private List<ScoreListDataItem> scoreList;
	private List<SurveyListDataItem> surveyList;

	private List<NodeRef> currTasks;

	/**
	 * <p>Constructor for ProjectData.</p>
	 */
	public ProjectData() {
		super();
	}
	

	/**
	 * <p>build.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.ProjectData} object
	 */
	public static ProjectData build() {
		return new ProjectData();
	}

	
	/**
	 * <p>withName.</p>
	 *
	 * @param name a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.project.data.ProjectData} object
	 */
	public ProjectData withName(String name) {
		setName(name);
		return this;
	}
	

	
	/**
	 * <p>withTaskList.</p>
	 *
	 * @param taskList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.project.data.ProjectData} object
	 */
	public ProjectData withTaskList(List<TaskListDataItem> taskList) {
		setTaskList(taskList);
		return this;
	}

	/**
	 * <p>withDeliverableList.</p>
	 *
	 * @param deliverableList a {@link java.util.List} object
	 * @return a {@link fr.becpg.repo.project.data.ProjectData} object
	 */
	public ProjectData withDeliverableList(List<DeliverableListDataItem> deliverableList) {
		setDeliverableList(deliverableList);
		return this;
	}
	
	/**
	 * <p>Constructor for ProjectData.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param name a {@link java.lang.String} object.
	 * @param hierarchy1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param hierarchy2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param startDate a {@link java.util.Date} object.
	 * @param dueDate a {@link java.util.Date} object.
	 * @param completionDate a {@link java.util.Date} object.
	 * @param planningMode a {@link fr.becpg.repo.project.data.PlanningMode} object.
	 * @param priority a {@link java.lang.Integer} object.
	 * @param projectState a {@link fr.becpg.repo.project.data.ProjectState} object.
	 * @param projectTpl a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param completionPercent a {@link java.lang.Integer} object.
	 * @param entities a {@link java.util.List} object.
	 */
	public ProjectData(NodeRef nodeRef, String name, NodeRef hierarchy1, NodeRef hierarchy2, Date startDate, Date dueDate, Date completionDate,
			PlanningMode planningMode, Integer priority, ProjectState projectState, NodeRef projectTpl, Integer completionPercent,
			List<NodeRef> entities) {
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

	/**
	 * <p>Getter for the field <code>code</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:code")
	public String getCode() {
		return code;
	}

	/**
	 * <p>Setter for the field <code>code</code>.</p>
	 *
	 * @param code a {@link java.lang.String} object.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * <p>Getter for the field <code>hierarchy1</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectHierarchy1")
	public NodeRef getHierarchy1() {
		return hierarchy1;
	}

	/**
	 * <p>Setter for the field <code>hierarchy1</code>.</p>
	 *
	 * @param hierarchy1 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy1(NodeRef hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	/**
	 * <p>Getter for the field <code>hierarchy2</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectHierarchy2")
	public NodeRef getHierarchy2() {
		return hierarchy2;
	}

	/**
	 * <p>Setter for the field <code>hierarchy2</code>.</p>
	 *
	 * @param hierarchy2 a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setHierarchy2(NodeRef hierarchy2) {
		this.hierarchy2 = hierarchy2;
	}

	/**
	 * <p>Getter for the field <code>startDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectStartDate")
	public Date getStartDate() {
		return startDate;
	}

	/**
	 * <p>Setter for the field <code>startDate</code>.</p>
	 *
	 * @param startDate a {@link java.util.Date} object.
	 */
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	/**
	 * <p>Getter for the field <code>targetStartDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object
	 */
	public Date getTargetStartDate() {
		return targetStartDate;
	}

	/**
	 * <p>Setter for the field <code>targetStartDate</code>.</p>
	 *
	 * @param targetStartDate a {@link java.util.Date} object
	 */
	public void setTargetStartDate(Date targetStartDate) {
		this.targetStartDate = targetStartDate;
	}
	
	/**
	 * <p>Getter for the field <code>entityScore</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:entityScore")
	public String getEntityScore() {
		return entityScore;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>entityScore</code>.</p>
	 */
	public void setEntityScore(String string) {
		this.entityScore = string;
	}

	/**
	 * <p>Getter for the field <code>reportLocales</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname = "rep:reportLocales")
	public List<String> getReportLocales() {
		return reportLocales;
	}

	/**
	 * <p>Setter for the field <code>reportLocales</code>.</p>
	 *
	 * @param reportLocales a {@link java.util.List} object.
	 */
	public void setReportLocales(List<String> reportLocales) {
		this.reportLocales = reportLocales;
	}


	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>formulatedDate</code>.</p>
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:formulatedDate")
	@Override
	public Date getFormulatedDate() {
		return formulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulatedDate(Date formulatedDate) {
		this.formulatedDate = formulatedDate;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>requirementChecksum</code>.</p>
	 */
	@AlfProp
	@AlfQname(qname = "bcpg:requirementChecksum")
	@Override
	public String getRequirementChecksum() {
		return requirementChecksum;
	}

	/**
	 * <p>Setter for the field <code>requirementChecksum</code>.</p>
	 *
	 * @param requirementChecksum a {@link java.lang.String} object.
	 */
	public void setRequirementChecksum(String requirementChecksum) {
		this.requirementChecksum = requirementChecksum;
	}

	/** {@inheritDoc} */
	@Override
	public boolean shouldUpdateFormulatedDate() {
		return updateFormulatedDate;
	}

	/** {@inheritDoc} */
	@Override
	public void setUpdateFormulatedDate(boolean updateFormulatedDate) {
		this.updateFormulatedDate = updateFormulatedDate;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>reformulateCount</code>.</p>
	 */
	@Override
	public Integer getReformulateCount() {
		return reformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setReformulateCount(Integer reformulateCount) {
		this.reformulateCount = reformulateCount;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>currentReformulateCount</code>.</p>
	 */
	@Override
	public Integer getCurrentReformulateCount() {
		return currentReformulateCount;
	}

	/** {@inheritDoc} */
	@Override
	public void setCurrentReformulateCount(Integer currentReformulateCount) {
		this.currentReformulateCount = currentReformulateCount;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>formulationChainId</code>.</p>
	 */
	@Override
	public String getFormulationChainId() {
		return formulationChainId;
	}

	/** {@inheritDoc} */
	@Override
	public void setFormulationChainId(String formulationChainId) {
		this.formulationChainId = formulationChainId;
	}

	/**
	 * <p>Getter for the field <code>dueDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectDueDate")
	public Date getDueDate() {
		return dueDate;
	}

	/**
	 * <p>Setter for the field <code>dueDate</code>.</p>
	 *
	 * @param dueDate a {@link java.util.Date} object.
	 */
	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	/**
	 * <p>Getter for the field <code>completionDate</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectCompletionDate")
	public Date getCompletionDate() {
		return completionDate;
	}

	/**
	 * <p>Setter for the field <code>completionDate</code>.</p>
	 *
	 * @param completionDate a {@link java.util.Date} object.
	 */
	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	/**
	 * <p>Getter for the field <code>planningMode</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.PlanningMode} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectPlanningMode")
	public PlanningMode getPlanningMode() {
		return planningMode;
	}

	/**
	 * <p>Setter for the field <code>planningMode</code>.</p>
	 *
	 * @param planningMode a {@link fr.becpg.repo.project.data.PlanningMode} object.
	 */
	public void setPlanningMode(PlanningMode planningMode) {
		this.planningMode = planningMode;
	}

	/**
	 * <p>Getter for the field <code>priority</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectPriority")
	public Integer getPriority() {
		return priority;
	}

	/**
	 * <p>Setter for the field <code>priority</code>.</p>
	 *
	 * @param priority a {@link java.lang.Integer} object.
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * <p>Getter for the field <code>projectState</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.project.data.ProjectState} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectState")
	public ProjectState getProjectState() {
		return projectState;
	}

	/**
	 * <p>Setter for the field <code>projectState</code>.</p>
	 *
	 * @param projectState a {@link fr.becpg.repo.project.data.ProjectState} object.
	 */
	public void setProjectState(ProjectState projectState) {
		this.projectState = projectState;
	}

	/**
	 * <p>getState.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getState() {
		return projectState != null ? projectState.toString() : ProjectState.Planned.toString();
	}

	/**
	 * <p>setState.</p>
	 *
	 * @param state a {@link java.lang.String} object.
	 */
	public void setState(String state) {
		this.projectState = ProjectState.valueOf(state);
	}

	/**
	 * <p>Getter for the field <code>projectTpl</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "bcpg:entityTplRef")
	public NodeRef getProjectTpl() {
		return projectTpl;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>getFormulatedEntityTpl.</p>
	 */
	@Override
	public NodeRef getFormulatedEntityTpl() {
		return projectTpl;
	}

	/**
	 * <p>Setter for the field <code>projectTpl</code>.</p>
	 *
	 * @param projectTpl a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProjectTpl(NodeRef projectTpl) {
		this.projectTpl = projectTpl;
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
	 * <p>Getter for the field <code>entities</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:projectEntity")
	public List<NodeRef> getEntities() {
		return entities;
	}
	
	@AlfProp
	@AlfQname(qname = "pjt:projectOwners")
	public List<NodeRef> getProjectOwners() {
		return projectOwners;
	}
	
	public void setProjectOwners(List<NodeRef> projectOwners) {
		this.projectOwners = projectOwners;
	}

	/**
	 * <p>Setter for the field <code>entities</code>.</p>
	 *
	 * @param entities a {@link java.util.List} object.
	 */
	public void setEntities(List<NodeRef> entities) {
		this.entities = entities;
	}

	/**
	 * <p>Getter for the field <code>legends</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectLegends")
	public List<NodeRef> getLegends() {
		return legends;
	}

	/**
	 * <p>Setter for the field <code>legends</code>.</p>
	 *
	 * @param legends a {@link java.util.List} object.
	 */
	public void setLegends(List<NodeRef> legends) {
		this.legends = legends;
	}

	/**
	 * <p>Getter for the field <code>overdue</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectOverdue")
	public Integer getOverdue() {
		return overdue;
	}

	/**
	 * <p>Setter for the field <code>overdue</code>.</p>
	 *
	 * @param overdue a {@link java.lang.Integer} object.
	 */
	public void setOverdue(Integer overdue) {
		this.overdue = overdue;
	}

	/**
	 * <p>Getter for the field <code>duration</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
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
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>score</code>.</p>
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectScore")
	@Override
	public Integer getScore() {
		return score;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>score</code>.</p>
	 */
	@Override
	public void setScore(Integer score) {
		this.score = score;
	}

	/**
	 * <p>Getter for the field <code>projectManager</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	@AlfSingleAssoc
	@AlfQname(qname = "pjt:projectManager")
	public NodeRef getProjectManager() {
		return projectManager;
	}

	/**
	 * <p>Setter for the field <code>projectManager</code>.</p>
	 *
	 * @param projectManager a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setProjectManager(NodeRef projectManager) {
		this.projectManager = projectManager;
	}

	/**
	 * <p>Getter for the field <code>created</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "cm:created")
	public Date getCreated() {
		return created;
	}

	/**
	 * <p>Setter for the field <code>created</code>.</p>
	 *
	 * @param created a {@link java.util.Date} object.
	 */
	public void setCreated(Date created) {
		this.created = created;
	}

	/**
	 * <p>Getter for the field <code>modified</code>.</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	@AlfProp
	@AlfQname(qname = "cm:modified")
	public Date getModified() {
		return modified;
	}

	/**
	 * <p>Setter for the field <code>modified</code>.</p>
	 *
	 * @param modified a {@link java.util.Date} object.
	 */
	public void setModified(Date modified) {
		this.modified = modified;
	}

	/**
	 * <p>Getter for the field <code>creator</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "cm:creator")
	public String getCreator() {
		return creator;
	}

	/**
	 * <p>Setter for the field <code>creator</code>.</p>
	 *
	 * @param creator a {@link java.lang.String} object.
	 */
	public void setCreator(String creator) {
		this.creator = creator;
	}

	/**
	 * <p>Getter for the field <code>modifier</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname = "cm:modifier")
	public String getModifier() {
		return modifier;
	}

	/**
	 * <p>Setter for the field <code>modifier</code>.</p>
	 *
	 * @param modifier a {@link java.lang.String} object.
	 */
	public void setModifier(String modifier) {
		this.modifier = modifier;
	}

	/**
	 * <p>Getter for the field <code>budgetedCost</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectBudgetedCost")
	public Double getBudgetedCost() {
		return budgetedCost;
	}

	/**
	 * <p>Setter for the field <code>budgetedCost</code>.</p>
	 *
	 * @param budgetedCost a {@link java.lang.Double} object.
	 */
	public void setBudgetedCost(Double budgetedCost) {
		this.budgetedCost = budgetedCost;
	}

	/**
	 * <p>Getter for the field <code>work</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectWork")
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
	 * <p>Getter for the field <code>currTasks</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname = "pjt:projectCurrentTasks")
	public List<NodeRef> getCurrTasks() {
		return currTasks;
	}

	/**
	 * <p>Setter for the field <code>currTasks</code>.</p>
	 *
	 * @param currTasks a {@link java.util.List} object.
	 */
	public void setCurrTasks(List<NodeRef> currTasks) {
		this.currTasks = currTasks;
	}

	/**
	 * <p>Getter for the field <code>loggedTime</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	@AlfProp
	@AlfQname(qname = "pjt:projectLoggedTime")
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
	 * <p>Getter for the field <code>taskList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pjt:taskList")
	public List<TaskListDataItem> getTaskList() {
		return taskList;
	}

	/**
	 * <p>Getter for the field <code>deliverableList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pjt:deliverableList")
	public List<DeliverableListDataItem> getDeliverableList() {
		return deliverableList;
	}

	/**
	 * <p>Setter for the field <code>deliverableList</code>.</p>
	 *
	 * @param deliverableList a {@link java.util.List} object.
	 */
	public void setDeliverableList(List<DeliverableListDataItem> deliverableList) {
		this.deliverableList = deliverableList;
	}

	/**
	 * <p>Setter for the field <code>taskList</code>.</p>
	 *
	 * @param taskList a {@link java.util.List} object.
	 */
	public void setTaskList(List<TaskListDataItem> taskList) {
		this.taskList = taskList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>scoreList</code>.</p>
	 */
	@DataList
	@AlfQname(qname = "pjt:scoreList")
	@Override
	public List<ScoreListDataItem> getScoreList() {
		return scoreList;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Setter for the field <code>scoreList</code>.</p>
	 */
	@Override
	public void setScoreList(List<ScoreListDataItem> scoreList) {
		this.scoreList = scoreList;
	}

	/**
	 * <p>Getter for the field <code>logTimeList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pjt:logTimeList")
	public List<LogTimeListDataItem> getLogTimeList() {
		return logTimeList;
	}

	/**
	 * <p>Setter for the field <code>logTimeList</code>.</p>
	 *
	 * @param logTimeList a {@link java.util.List} object.
	 */
	public void setLogTimeList(List<LogTimeListDataItem> logTimeList) {
		this.logTimeList = logTimeList;
	}

	/**
	 * <p>Getter for the field <code>budgetList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pjt:budgetList")
	public List<BudgetListDataItem> getBudgetList() {
		return budgetList;
	}

	/**
	 * <p>Setter for the field <code>budgetList</code>.</p>
	 *
	 * @param budgetList a {@link java.util.List} object.
	 */
	public void setBudgetList(List<BudgetListDataItem> budgetList) {
		this.budgetList = budgetList;
	}

	/**
	 * <p>Getter for the field <code>invoiceList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pjt:invoiceList")
	public List<InvoiceListDataItem> getInvoiceList() {
		return invoiceList;
	}

	/**
	 * <p>Setter for the field <code>invoiceList</code>.</p>
	 *
	 * @param invoiceList a {@link java.util.List} object.
	 */
	public void setInvoiceList(List<InvoiceListDataItem> invoiceList) {
		this.invoiceList = invoiceList;
	}

	/**
	 * <p>Getter for the field <code>expenseList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "pjt:expenseList")
	public List<ExpenseListDataItem> getExpenseList() {
		return expenseList;
	}

	/**
	 * <p>Setter for the field <code>expenseList</code>.</p>
	 *
	 * @param expenseList a {@link java.util.List} object.
	 */
	public void setExpenseList(List<ExpenseListDataItem> expenseList) {
		this.expenseList = expenseList;
	}

	/** {@inheritDoc} */
	@Override
	public String getEntityState() {
		return projectState != null ? projectState.toString() : null;
	}

	/**
	 * {@inheritDoc}
	 *
	 * <p>Getter for the field <code>surveyList</code>.</p>
	 */
	@DataList
	@AlfQname(qname = "survey:surveyList")
	@Override
	public List<SurveyListDataItem> getSurveyList() {
		return surveyList;
	}

	/** {@inheritDoc} */
	@Override
	public void setSurveyList(List<SurveyListDataItem> surveyList) {
		this.surveyList = surveyList;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((budgetList == null) ? 0 : budgetList.hashCode());
		result = prime * result + ((budgetedCost == null) ? 0 : budgetedCost.hashCode());
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((completionDate == null) ? 0 : completionDate.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((creator == null) ? 0 : creator.hashCode());
		result = prime * result + ((currTasks == null) ? 0 : currTasks.hashCode());
		result = prime * result + ((currentReformulateCount == null) ? 0 : currentReformulateCount.hashCode());
		result = prime * result + ((deliverableList == null) ? 0 : deliverableList.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result + ((entities == null) ? 0 : entities.hashCode());
		result = prime * result + ((expenseList == null) ? 0 : expenseList.hashCode());
		result = prime * result + ((formulatedDate == null) ? 0 : formulatedDate.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((hierarchy2 == null) ? 0 : hierarchy2.hashCode());
		result = prime * result + ((invoiceList == null) ? 0 : invoiceList.hashCode());
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

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProjectData other = (ProjectData) obj;
		if (budgetList == null) {
			if (other.budgetList != null)
				return false;
		} else if (!budgetList.equals(other.budgetList))
			return false;
		if (budgetedCost == null) {
			if (other.budgetedCost != null)
				return false;
		} else if (!budgetedCost.equals(other.budgetedCost))
			return false;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
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
		if (currTasks == null) {
			if (other.currTasks != null)
				return false;
		} else if (!currTasks.equals(other.currTasks))
			return false;
		if (currentReformulateCount == null) {
			if (other.currentReformulateCount != null)
				return false;
		} else if (!currentReformulateCount.equals(other.currentReformulateCount))
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
		if (expenseList == null) {
			if (other.expenseList != null)
				return false;
		} else if (!expenseList.equals(other.expenseList))
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
		if (invoiceList == null) {
			if (other.invoiceList != null)
				return false;
		} else if (!invoiceList.equals(other.invoiceList))
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ProjectData [hierarchy1=" + hierarchy1 + ", hierarchy2=" + hierarchy2 + ", startDate=" + startDate + ", dueDate=" + dueDate
				+ ", completionDate=" + completionDate + ", planningMode=" + planningMode + ", priority=" + priority + ", projectState="
				+ projectState + ", projectTpl=" + projectTpl + ", completionPercent=" + completionPercent + ", entities=" + entities + ", legends="
				+ legends + ", overdue=" + overdue + ", score=" + score + ", created=" + created + ", modified=" + modified + ", creator=" + creator
				+ ", modifier=" + modifier + ", projectManager=" + projectManager + ", budgetedCost=" + budgetedCost + ", work=" + work
				+ ", loggedTime=" + loggedTime + ", formulatedDate=" + formulatedDate + ", reformulateCount=" + reformulateCount + ", taskList="
				+ taskList + ", deliverableList=" + deliverableList + ", scoreList=" + scoreList + ", logTimeList=" + logTimeList + ", budgetList="
				+ budgetList + ", invoiceList=" + invoiceList + ", expenseList=" + expenseList + "]";
	}

}

package fr.becpg.repo.project.data;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;

/**
 * ProjectData used to manipulate project
 * 
 * @author quere
 * 
 */
public class ProjectData extends AbstractProjectData {

	private String hierarchy1;
	private Date startDate;
	private Date dueDate;
	private Date completionDate;
	private Integer priority = 2;
	private ProjectState projectState;
	private NodeRef projectTpl;
	private Integer completionPercent = 0;
	private NodeRef entity;

	public String getHierarchy1() {
		return hierarchy1;
	}

	public void setHierarchy1(String hierarchy1) {
		this.hierarchy1 = hierarchy1;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public Date getDueDate() {
		return dueDate;
	}

	public void setDueDate(Date dueDate) {
		this.dueDate = dueDate;
	}

	public Date getCompletionDate() {
		return completionDate;
	}

	public void setCompletionDate(Date completionDate) {
		this.completionDate = completionDate;
	}

	public Integer getPriority() {
		return priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	public ProjectState getProjectState() {
		return projectState;
	}

	public void setProjectState(ProjectState projectState) {
		this.projectState = projectState;
	}

	public NodeRef getProjectTpl() {
		return projectTpl;
	}

	public void setProjectTpl(NodeRef projectTpl) {
		this.projectTpl = projectTpl;
	}

	public Integer getCompletionPercent() {
		return completionPercent;
	}

	public void setCompletionPercent(Integer completionPercent) {
		this.completionPercent = completionPercent;
	}
	
	

	public ProjectData(NodeRef nodeRef, String name, String hierarchy1, Date startDate, Date dueDate, Date completionDate, Integer priority, ProjectState projectState, NodeRef projectTpl,
			Integer completionPercent, NodeRef entity) {
		super(nodeRef, name);
		this.hierarchy1 = hierarchy1;
		this.startDate = startDate;
		this.dueDate = dueDate;
		this.completionDate = completionDate;
		this.priority = priority;
		this.projectState = projectState;
		this.projectTpl = projectTpl;
		this.completionPercent = completionPercent;
		this.entity = entity;
	}

	public NodeRef getEntity() {
		return entity;
	}

	public void setEntity(NodeRef entity) {
		this.entity = entity;
	}

	@Override
	public Map<QName, Serializable> getProperties() {
		Map<QName, Serializable> properties = super.getProperties();
		properties.put(ProjectModel.PROP_PROJECT_HIERARCHY1, hierarchy1);
		properties.put(ProjectModel.PROP_PROJECT_START_DATE, startDate);
		properties.put(ProjectModel.PROP_PROJECT_DUE_DATE, dueDate);
		properties.put(ProjectModel.PROP_PROJECT_COMPLETION_DATE, completionDate);
		properties.put(ProjectModel.PROP_PROJECT_PRIORITY, priority);
		properties.put(ProjectModel.PROP_PROJECT_STATE, projectState.toString());
		properties.put(ProjectModel.PROP_COMPLETION_PERCENT, completionPercent);
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {
		Map<QName, NodeRef> singleAssociations = super.getSingleAssociations();
		singleAssociations.put(ProjectModel.ASSOC_PROJECT_TPL, projectTpl);
		singleAssociations.put(ProjectModel.ASSOC_PROJECT_ENTITY, entity);
		return singleAssociations;
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {
		Map<QName, List<NodeRef>> multipleAssociations = super.getMultipleAssociations();
		return multipleAssociations;
	}
}

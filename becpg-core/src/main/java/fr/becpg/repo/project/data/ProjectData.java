package fr.becpg.repo.project.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.project.data.projectList.TaskHistoryListDataItem;

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
	private NodeRef projectTpl;
	private Integer completionPercent = 0;
	
	private List<TaskHistoryListDataItem> taskHistoryList = new ArrayList<TaskHistoryListDataItem>();
	
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

	public List<TaskHistoryListDataItem> getTaskHistoryList() {
		return taskHistoryList;
	}

	public void setTaskHistoryList(List<TaskHistoryListDataItem> taskHistoryList) {
		this.taskHistoryList = taskHistoryList;
	}

	public ProjectData(NodeRef nodeRef, String name, NodeRef projectTpl) {
		super(nodeRef, name);
		setProjectTpl(projectTpl);
		setTaskHistoryList(new ArrayList<TaskHistoryListDataItem>());
	}

	@Override
	public Map<QName, Serializable> getProperties() {
		Map<QName, Serializable> properties = super.getProperties();		
		properties.put(ProjectModel.PROP_PROJECT_HIERARCHY1, hierarchy1);
		properties.put(ProjectModel.PROP_PROJECT_START_DATE, startDate);
		properties.put(ProjectModel.PROP_PROJECT_DUE_DATE, dueDate);
		properties.put(ProjectModel.PROP_PROJECT_COMPLETION_DATE, completionDate);
		properties.put(ProjectModel.PROP_PROJECT_PRIORITY, priority);
		properties.put(ProjectModel.PROP_COMPLETION_PERCENT, completionPercent);		
		return properties;
	}

	@Override
	public Map<QName, NodeRef> getSingleAssociations() {
		Map<QName, NodeRef> singleAssociations = super.getSingleAssociations();
		singleAssociations.put(ProjectModel.ASSOC_PROJECT_TPL, projectTpl);
		return singleAssociations;
	}

	@Override
	public Map<QName, List<NodeRef>> getMultipleAssociations() {
		Map<QName, List<NodeRef>> multipleAssociations = super.getMultipleAssociations();
		return multipleAssociations;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((completionDate == null) ? 0 : completionDate.hashCode());
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((dueDate == null) ? 0 : dueDate.hashCode());
		result = prime * result + ((hierarchy1 == null) ? 0 : hierarchy1.hashCode());
		result = prime * result + ((priority == null) ? 0 : priority.hashCode());
		result = prime * result + ((projectTpl == null) ? 0 : projectTpl.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((taskHistoryList == null) ? 0 : taskHistoryList.hashCode());
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
		if (hierarchy1 == null) {
			if (other.hierarchy1 != null)
				return false;
		} else if (!hierarchy1.equals(other.hierarchy1))
			return false;
		if (priority == null) {
			if (other.priority != null)
				return false;
		} else if (!priority.equals(other.priority))
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
		if (taskHistoryList == null) {
			if (other.taskHistoryList != null)
				return false;
		} else if (!taskHistoryList.equals(other.taskHistoryList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ProjectData [hierarchy1=" + hierarchy1 + ", startDate=" + startDate + ", dueDate=" + dueDate
				+ ", completionDate=" + completionDate + ", priority=" + priority + ", projectTpl=" + projectTpl
				+ ", completionPercent=" + completionPercent + ", taskHistoryList=" + taskHistoryList + "]";
	}
}

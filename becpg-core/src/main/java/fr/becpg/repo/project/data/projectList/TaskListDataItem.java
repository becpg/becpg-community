package fr.becpg.repo.project.data.projectList;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

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
public class TaskListDataItem extends BeCPGDataObject {
	
	
	private String taskName;
	private Boolean isMilestone;
	private Integer duration;
	private Date start;
	private Date end;
	private TaskState state = TaskState.Planned;
	private Integer completionPercent = 0;
	private List<NodeRef> prevTasks;
	private List<NodeRef> resources;
	private NodeRef taskLegend;
	private String workflowName;
	private String workflowInstance;

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
	@AlfQname(qname = "pjt:tlDuration")
	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
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
	public TaskState getState() {
		return state;
	}
	
	
	public void setState(TaskState state) {
		this.state = state;
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
		this.state = state!=null ? state : TaskState.Planned;
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
		int result = 1;
		result = prime * result + ((completionPercent == null) ? 0 : completionPercent.hashCode());
		result = prime * result + ((duration == null) ? 0 : duration.hashCode());
		result = prime * result + ((end == null) ? 0 : end.hashCode());
		result = prime * result + ((isMilestone == null) ? 0 : isMilestone.hashCode());
		result = prime * result + ((nodeRef == null) ? 0 : nodeRef.hashCode());
		result = prime * result + ((prevTasks == null) ? 0 : prevTasks.hashCode());
		result = prime * result + ((resources == null) ? 0 : resources.hashCode());
		result = prime * result + ((start == null) ? 0 : start.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((taskLegend == null) ? 0 : taskLegend.hashCode());
		result = prime * result + ((taskName == null) ? 0 : taskName.hashCode());
		result = prime * result + ((workflowInstance == null) ? 0 : workflowInstance.hashCode());
		result = prime * result + ((workflowName == null) ? 0 : workflowName.hashCode());
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
		TaskListDataItem other = (TaskListDataItem) obj;
		if (completionPercent == null) {
			if (other.completionPercent != null)
				return false;
		} else if (!completionPercent.equals(other.completionPercent))
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
		if (isMilestone == null) {
			if (other.isMilestone != null)
				return false;
		} else if (!isMilestone.equals(other.isMilestone))
			return false;
		if (nodeRef == null) {
			if (other.nodeRef != null)
				return false;
		} else if (!nodeRef.equals(other.nodeRef))
			return false;
		if (prevTasks == null) {
			if (other.prevTasks != null)
				return false;
		} else if (!prevTasks.equals(other.prevTasks))
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
		if (state != other.state)
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
		return "TaskListDataItem [nodeRef=" + nodeRef + ", taskName=" + taskName + ", isMilestone=" + isMilestone + ", duration=" + duration + ", start=" + start + ", end=" + end
				+ ", state=" + state + ", completionPercent=" + completionPercent + ", prevTasks=" + prevTasks + ", resources=" + resources + ", taskLegend=" + taskLegend
				+ ", workflowName=" + workflowName + ", workflowInstance=" + workflowInstance + "]";
	}

}

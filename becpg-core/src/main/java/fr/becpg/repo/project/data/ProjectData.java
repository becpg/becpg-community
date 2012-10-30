package fr.becpg.repo.project.data;

import java.util.ArrayList;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.projectList.TaskHistoryListDataItem;

/**
 * ProjectData used to manipulate project
 * 
 * @author quere
 * 
 */
public class ProjectData extends AbstractProjectData {

	public ProjectData(NodeRef nodeRef, String name, NodeRef projectTpl) {
		super(nodeRef, name);
		setProjectTpl(projectTpl);
		setTaskHistoryList(new ArrayList<TaskHistoryListDataItem>());
	}

}

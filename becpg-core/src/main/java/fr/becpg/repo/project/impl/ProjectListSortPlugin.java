package fr.becpg.repo.project.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.util.StopWatch;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.entity.datalist.AbstractDataListSortPlugin;
import fr.becpg.repo.hierarchy.HierarchyHelper;

public class ProjectListSortPlugin extends AbstractDataListSortPlugin {

	private static final String PLUGIN_ID = "ProjectList";
	
	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public String getPluginId() {

		return PLUGIN_ID;
	}

	public List<NodeRef> sort(List<NodeRef> projectList) {
		
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}

		Collections.sort(projectList, new Comparator<NodeRef>() {

			@Override
			public int compare(NodeRef n1, NodeRef n2) {

				int comp = compare(getHierarchy(n1, ProjectModel.PROP_PROJECT_HIERARCHY1), getHierarchy(n2, ProjectModel.PROP_PROJECT_HIERARCHY1));

				if (EQUAL == comp) {
					comp = compare(getHierarchy(n1, ProjectModel.PROP_PROJECT_HIERARCHY2), getHierarchy(n2, ProjectModel.PROP_PROJECT_HIERARCHY2));
					
					if (EQUAL == comp) {
						comp = compare((String)nodeService.getProperty(n1, ContentModel.PROP_NAME), (String)nodeService.getProperty(n2, ContentModel.PROP_NAME));
					}
				}

				return comp;
			}
			
			
			private String getHierarchy(NodeRef nodeRef, QName hierarchyQName){
			
				NodeRef hierarchyNodeRef = (NodeRef) nodeService.getProperty(nodeRef, hierarchyQName);
				return HierarchyHelper.getHierachyName(hierarchyNodeRef, nodeService);
			}

			private int compare(String n1, String n2) {

				if (n1 != null && n2 != null) {
					if (n1.equals(n2)) {
						return EQUAL;
					}

					return n1.compareTo(n2);

				} else if (n1 == null && n2 != null) {
					return AFTER;
				} else if (n2 == null && n1 != null) {
					return BEFORE;
				} else {
					return EQUAL;
				}
			}
		});

		if (logger.isDebugEnabled()) {
			watch.stop();
			logger.debug("Project List sorted in "
					+ watch.getTotalTimeSeconds() + " seconds - size results "
					+  projectList.size() );
		}
		
		return projectList;
	}
}

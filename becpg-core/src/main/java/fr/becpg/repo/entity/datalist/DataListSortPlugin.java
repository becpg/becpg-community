package fr.becpg.repo.entity.datalist;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface DataListSortPlugin {

	public String getPluginId();

	public List<NodeRef> sort(List<NodeRef> projectList);
}

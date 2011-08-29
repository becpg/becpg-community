package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.dataList.ControlListDataItem;

public class WorkItemAnalysisData {

	NodeRef nodeRef;
	String name;
	
	List<ControlListDataItem> controlList = new ArrayList<ControlListDataItem>();
	
	public NodeRef getNodeRef() {
		return nodeRef;
	}

	public void setNodeRef(NodeRef nodeRef) {
		this.nodeRef = nodeRef;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<ControlListDataItem> getControlList() {
		return controlList;
	}

	public void setControlList(List<ControlListDataItem> controlList) {
		this.controlList = controlList;
	}
	
	public WorkItemAnalysisData(){
		
	}
}

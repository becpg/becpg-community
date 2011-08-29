package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;

public class ControlPointData {

	NodeRef nodeRef;
	String name;
	
	List<ControlDefListDataItem> controlDefList = new ArrayList<ControlDefListDataItem>();

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

	public List<ControlDefListDataItem> getControlDefList() {
		return controlDefList;
	}

	public void setControlDefList(List<ControlDefListDataItem> controlDefList) {
		this.controlDefList = controlDefList;
	}
		
}

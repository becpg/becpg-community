package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;

public class ControlPlanData {

	NodeRef nodeRef;	
	String name;
	
	List<SamplingDefListDataItem> samplingDefList = new ArrayList<SamplingDefListDataItem>();

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

	public List<SamplingDefListDataItem> getSamplingDefList() {
		return samplingDefList;
	}

	public void setSamplingDefList(List<SamplingDefListDataItem> samplingDefList) {
		this.samplingDefList = samplingDefList;
	}
		
}

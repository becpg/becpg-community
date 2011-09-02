package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.quality.data.dataList.ControlListDataItem;

public class WorkItemAnalysisData extends BeCPGDataObject {

	
	List<ControlListDataItem> controlList = new ArrayList<ControlListDataItem>();
	

	public List<ControlListDataItem> getControlList() {
		return controlList;
	}

	public void setControlList(List<ControlListDataItem> controlList) {
		this.controlList = controlList;
	}
	
	public WorkItemAnalysisData(){
		
	}
}

package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.quality.data.dataList.ControlListDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;

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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlList == null) ? 0 : controlList.hashCode());
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
		WorkItemAnalysisData other = (WorkItemAnalysisData) obj;
		if (controlList == null) {
			if (other.controlList != null)
				return false;
		} else if (!controlList.equals(other.controlList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WorkItemAnalysisData [controlList=" + controlList + "]";
	}
	
	
}

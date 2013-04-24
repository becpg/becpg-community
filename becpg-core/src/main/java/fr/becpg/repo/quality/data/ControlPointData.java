package fr.becpg.repo.quality.data;

import java.util.LinkedList;
import java.util.List;

import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public class ControlPointData extends BeCPGDataObject{

	List<ControlDefListDataItem> controlDefList = new LinkedList<ControlDefListDataItem>();


	public List<ControlDefListDataItem> getControlDefList() {
		return controlDefList;
	}

	public void setControlDefList(List<ControlDefListDataItem> controlDefList) {
		this.controlDefList = controlDefList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlDefList == null) ? 0 : controlDefList.hashCode());
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
		ControlPointData other = (ControlPointData) obj;
		if (controlDefList == null) {
			if (other.controlDefList != null)
				return false;
		} else if (!controlDefList.equals(other.controlDefList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ControlPointData [controlDefList=" + controlDefList + "]";
	}
		
	
	
}

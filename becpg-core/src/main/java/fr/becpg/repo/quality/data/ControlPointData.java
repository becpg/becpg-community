package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;

public class ControlPointData extends BeCPGDataObject{

	List<ControlDefListDataItem> controlDefList = new ArrayList<ControlDefListDataItem>();


	public List<ControlDefListDataItem> getControlDefList() {
		return controlDefList;
	}

	public void setControlDefList(List<ControlDefListDataItem> controlDefList) {
		this.controlDefList = controlDefList;
	}
		
}

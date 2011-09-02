package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;

public class ControlPlanData extends BeCPGDataObject {

	List<SamplingDefListDataItem> samplingDefList = new ArrayList<SamplingDefListDataItem>();


	public List<SamplingDefListDataItem> getSamplingDefList() {
		return samplingDefList;
	}

	public void setSamplingDefList(List<SamplingDefListDataItem> samplingDefList) {
		this.samplingDefList = samplingDefList;
	}
		
}

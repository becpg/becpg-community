package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.repository.model.BeCPGDataObject;

public class ControlPlanData extends BeCPGDataObject {

	List<SamplingDefListDataItem> samplingDefList = new ArrayList<SamplingDefListDataItem>();


	public List<SamplingDefListDataItem> getSamplingDefList() {
		return samplingDefList;
	}

	public void setSamplingDefList(List<SamplingDefListDataItem> samplingDefList) {
		this.samplingDefList = samplingDefList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((samplingDefList == null) ? 0 : samplingDefList.hashCode());
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
		ControlPlanData other = (ControlPlanData) obj;
		if (samplingDefList == null) {
			if (other.samplingDefList != null)
				return false;
		} else if (!samplingDefList.equals(other.samplingDefList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ControlPlanData [samplingDefList=" + samplingDefList + "]";
	}
	
	
		
}

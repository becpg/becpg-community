package fr.becpg.repo.quality.data;

import java.util.ArrayList;
import java.util.List;

import fr.becpg.repo.BeCPGDataObject;
import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;

public class NonConformityData extends BeCPGDataObject {

	private String state;
	private String comment;	
	
	List<WorkLogDataItem> workLog = new ArrayList<WorkLogDataItem>();
	
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public List<WorkLogDataItem> getWorkLog() {
		return workLog;
	}

	public void setWorkLog(List<WorkLogDataItem> workLog) {
		this.workLog = workLog;
	}
	
	public NonConformityData(){
		
	}
}

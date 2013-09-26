package fr.becpg.repo.quality.data;

import java.util.LinkedList;
import java.util.List;

import fr.becpg.repo.quality.data.dataList.WorkLogDataItem;
import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "qa:nc")
public class NonConformityData extends BeCPGDataObject {

	private String state;
	private String comment;

	List<WorkLogDataItem> workLog = new LinkedList<WorkLogDataItem>();

	@AlfProp
	@AlfQname(qname = "qa:ncState")
	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	@AlfProp
	@AlfQname(qname = "qa:ncComment")
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@DataList
	@AlfQname(qname = "qa:workLog")
	public List<WorkLogDataItem> getWorkLog() {
		return workLog;
	}

	public void setWorkLog(List<WorkLogDataItem> workLog) {
		this.workLog = workLog;
	}

	public NonConformityData() {

	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((comment == null) ? 0 : comment.hashCode());
		result = prime * result + ((state == null) ? 0 : state.hashCode());
		result = prime * result + ((workLog == null) ? 0 : workLog.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		NonConformityData other = (NonConformityData) obj;
		if (comment == null) {
			if (other.comment != null)
				return false;
		} else if (!comment.equals(other.comment))
			return false;
		if (state == null) {
			if (other.state != null)
				return false;
		} else if (!state.equals(other.state))
			return false;
		if (workLog == null) {
			if (other.workLog != null)
				return false;
		} else if (!workLog.equals(other.workLog))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NonConformityData [state=" + state + ", comment=" + comment + ", workLog=" + workLog + "]";
	}

}

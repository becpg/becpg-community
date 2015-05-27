/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
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

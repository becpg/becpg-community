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

import fr.becpg.repo.quality.data.dataList.SamplingDefListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname = "qa:controlPlan")
public class ControlPlanData extends BeCPGDataObject {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1365842750141431401L;
	List<SamplingDefListDataItem> samplingDefList = new LinkedList<>();


	@DataList
	@AlfQname(qname = "qa:samplingDefList")
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

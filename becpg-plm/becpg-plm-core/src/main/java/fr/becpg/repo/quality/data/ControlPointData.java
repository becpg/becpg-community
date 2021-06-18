/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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

import fr.becpg.repo.quality.data.dataList.ControlDefListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>ControlPointData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "qa:controlPoint")
public class ControlPointData extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5543494940526679993L;
	List<ControlDefListDataItem> controlDefList = new LinkedList<>();

	/**
	 * <p>Getter for the field <code>controlDefList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "qa:controlDefList")
	public List<ControlDefListDataItem> getControlDefList() {
		return controlDefList;
	}

	/**
	 * <p>Setter for the field <code>controlDefList</code>.</p>
	 *
	 * @param controlDefList a {@link java.util.List} object.
	 */
	public void setControlDefList(List<ControlDefListDataItem> controlDefList) {
		this.controlDefList = controlDefList;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((controlDefList == null) ? 0 : controlDefList.hashCode());
		return result;
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ControlPointData [controlDefList=" + controlDefList + "]";
	}

}

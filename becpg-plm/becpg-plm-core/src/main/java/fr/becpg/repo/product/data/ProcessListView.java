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
package fr.becpg.repo.product.data;

import java.util.List;

import fr.becpg.repo.product.data.productList.ProcessListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * <p>ProcessListView class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ProcessListView extends AbstractProductDataView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7335139813241579530L;
	private List<ProcessListDataItem> processList;
	
	
	/**
	 * <p>Getter for the field <code>processList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname="mpm:processList")
	public List<ProcessListDataItem> getProcessList() {
		return processList;
	}

	
	/** {@inheritDoc} */
	@Override
	public List<? extends CompositionDataItem> getMainDataList() {
		return getProcessList();
	}
	
	/**
	 * <p>Setter for the field <code>processList</code>.</p>
	 *
	 * @param processList a {@link java.util.List} object.
	 */
	public void setProcessList(List<ProcessListDataItem> processList) {
		this.processList = processList;
	}

	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((processList == null) ? 0 : processList.hashCode());
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
		ProcessListView other = (ProcessListView) obj;
		if (processList == null) {
			if (other.processList != null)
				return false;
		} else if (!processList.equals(other.processList))
			return false;
		return true;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "ProcessListView [processList=" + processList + "]";
	}			
	

}

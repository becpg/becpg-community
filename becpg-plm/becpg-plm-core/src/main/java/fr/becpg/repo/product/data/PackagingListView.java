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

import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * <p>PackagingListView class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PackagingListView extends AbstractProductDataView {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3568794116098626931L;
	private List<PackagingListDataItem> packagingList;
	
	
	/**
	 * <p>Getter for the field <code>packagingList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname="bcpg:packagingList")
	public List<PackagingListDataItem> getPackagingList() {
		return packagingList;
	}
	
	/** {@inheritDoc} */
	@Override
	public List<? extends CompositionDataItem> getMainDataList() {
		return getPackagingList();
	}


	/**
	 * <p>Setter for the field <code>packagingList</code>.</p>
	 *
	 * @param packagingList a {@link java.util.List} object.
	 */
	public void setPackagingList(List<PackagingListDataItem> packagingList) {
		this.packagingList = packagingList;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((packagingList == null) ? 0 : packagingList.hashCode());
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
		PackagingListView other = (PackagingListView) obj;
		if (packagingList == null) {
			if (other.packagingList != null)
				return false;
		} else if (!packagingList.equals(other.packagingList))
			return false;
		return true;
	}


	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PackagingListView [packagingList=" + packagingList + "]";
	}
	
	
	
}

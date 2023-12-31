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

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * <p>CompoListView class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CompoListView extends AbstractProductDataView {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1646699257033977776L;
	private List<CompoListDataItem> compoList;
	
	/**
	 * <p>Getter for the field <code>compoList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname="bcpg:compoList")
	public List<CompoListDataItem> getCompoList() {
		return compoList;
	}

	/** {@inheritDoc} */
	@Override
	public List<? extends CompositionDataItem> getMainDataList() {
		return getCompoList();
	}

	/**
	 * <p>Setter for the field <code>compoList</code>.</p>
	 *
	 * @param compoList a {@link java.util.List} object.
	 */
	public void setCompoList(List<CompoListDataItem> compoList) {
		this.compoList = compoList;
	}

	

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((compoList == null) ? 0 : compoList.hashCode());
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
		CompoListView other = (CompoListView) obj;
		if (compoList == null) {
			if (other.compoList != null)
				return false;
		} else if (!compoList.equals(other.compoList))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "CompoListView [compoList=" + compoList + "]";
	}


	


	
}

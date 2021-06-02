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

import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BaseObject;

/**
 * <p>LabelingListView class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LabelingListView  extends BaseObject {


	/**
	 * 
	 */
	private static final long serialVersionUID = -4278325759139858561L;
	private List<IngLabelingListDataItem> ingLabelingList;
	private List<LabelingRuleListDataItem> labelingRuleList;
	
	
	/**
	 * <p>Getter for the field <code>ingLabelingList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:ingLabelingList")
	public List<IngLabelingListDataItem> getIngLabelingList() {
		return ingLabelingList;
	}

	/**
	 * <p>Setter for the field <code>ingLabelingList</code>.</p>
	 *
	 * @param ingLabelingList a {@link java.util.List} object.
	 */
	public void setIngLabelingList(List<IngLabelingListDataItem> ingLabelingList) {
		this.ingLabelingList = ingLabelingList;
	}
	

	/**
	 * <p>Getter for the field <code>labelingRuleList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname = "bcpg:labelingRuleList")
	public List<LabelingRuleListDataItem> getLabelingRuleList() {
		return labelingRuleList;
	}

	/**
	 * <p>Setter for the field <code>labelingRuleList</code>.</p>
	 *
	 * @param labelingRuleList a {@link java.util.List} object.
	 */
	public void setLabelingRuleList(List<LabelingRuleListDataItem> labelingRuleList) {
		this.labelingRuleList = labelingRuleList;
	}

	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ingLabelingList == null) ? 0 : ingLabelingList.hashCode());
		result = prime * result + ((labelingRuleList == null) ? 0 : labelingRuleList.hashCode());
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
		LabelingListView other = (LabelingListView) obj;
		if (ingLabelingList == null) {
			if (other.ingLabelingList != null)
				return false;
		} else if (!ingLabelingList.equals(other.ingLabelingList))
			return false;
		if (labelingRuleList == null) {
			if (other.labelingRuleList != null)
				return false;
		} else if (!labelingRuleList.equals(other.labelingRuleList))
			return false;
		return true;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "LabelingListView [ingLabelingList=" + ingLabelingList + ", labelingRuleList=" + labelingRuleList + "]";
	}
	
	
}

/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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

import fr.becpg.repo.product.data.productList.LabelListDataItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.DataList;

@AlfType
@AlfQname(qname = "bcpg:packagingMaterial")
public class PackagingMaterialData extends ProductData   {

	private List<LabelListDataItem> labelingList;

	
	@DataList
	@AlfQname(qname = "pack:labelingList")
	public List<LabelListDataItem> getLabelingList() {
		return labelingList;
	}

	public void setLabelingList(List<LabelListDataItem> labelingList) {
		this.labelingList = labelingList;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((labelingList == null) ? 0 : labelingList.hashCode());
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
		PackagingMaterialData other = (PackagingMaterialData) obj;
		if (labelingList == null) {
			if (other.labelingList != null)
				return false;
		} else if (!labelingList.equals(other.labelingList))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "PackagingMaterialData [labelingList=" + labelingList + "]";
	}
	
	
	
}

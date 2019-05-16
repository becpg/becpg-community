/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;

@AlfType
@AlfQname(qname = "bcpg:packagingMaterial")
@MultiLevelLeaf
public class PackagingMaterialData extends ProductData   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1386599003766479590L;


	private List<NodeRef> suppliers = new ArrayList<>();
	
	private List<NodeRef> packagingMaterials = new ArrayList<>();

	@AlfMultiAssoc
	@AlfQname(qname="bcpg:suppliers")
	public List<NodeRef> getSuppliers() {
		return suppliers;
	}

	public void setSuppliers(List<NodeRef> suppliers) {
		this.suppliers = suppliers;
	}
	
	@AlfMultiAssoc
	@AlfQname(qname="pack:pmMaterialRefs")
	public List<NodeRef> getPackagingMaterials() {
		return packagingMaterials;
	}

	public void setPackagingMaterials(List<NodeRef> packagingMaterials) {
		this.packagingMaterials = packagingMaterials;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((packagingMaterials == null) ? 0 : packagingMaterials.hashCode());
		result = prime * result + ((suppliers == null) ? 0 : suppliers.hashCode());
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
		if (packagingMaterials == null) {
			if (other.packagingMaterials != null)
				return false;
		} else if (!packagingMaterials.equals(other.packagingMaterials))
			return false;
		if (suppliers == null) {
			if (other.suppliers != null)
				return false;
		} else if (!suppliers.equals(other.suppliers))
			return false;
		return true;
	}


	
}

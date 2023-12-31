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

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfMultiAssoc;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.annotation.MultiLevelLeaf;

/**
 * <p>PackagingMaterialData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname = "bcpg:packagingMaterial")
@MultiLevelLeaf
public class PackagingMaterialData extends ProductData   {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1386599003766479590L;


	
	private List<NodeRef> packagingMaterials = new ArrayList<>();

	
	
	/**
	 * <p>Getter for the field <code>packagingMaterials</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfMultiAssoc
	@AlfQname(qname="pack:pmMaterialRefs")
	public List<NodeRef> getPackagingMaterials() {
		return packagingMaterials;
	}

	/**
	 * <p>Setter for the field <code>packagingMaterials</code>.</p>
	 *
	 * @param packagingMaterials a {@link java.util.List} object.
	 */
	public void setPackagingMaterials(List<NodeRef> packagingMaterials) {
		this.packagingMaterials = packagingMaterials;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((packagingMaterials == null) ? 0 : packagingMaterials.hashCode());
		return result;
	}

	/** {@inheritDoc} */
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
		return true;
	}


	
}

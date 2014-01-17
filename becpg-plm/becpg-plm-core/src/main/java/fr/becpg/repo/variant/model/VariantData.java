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
package fr.becpg.repo.variant.model;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

@AlfType
@AlfQname(qname="bcpg:variant")
public class VariantData extends BeCPGDataObject {

	protected Boolean isDefaultVariant;

	@AlfProp
	@AlfQname(qname="bcpg:isDefaultVariant")
	public Boolean getIsDefaultVariant() {
		return isDefaultVariant;
	}

	public void setIsDefaultVariant(Boolean isDefaultVariant) {
		this.isDefaultVariant = isDefaultVariant;
	}

	@Override
	public String toString() {
		return "VariantData [isDefaultVariant=" + isDefaultVariant + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((isDefaultVariant == null) ? 0 : isDefaultVariant.hashCode());
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
		VariantData other = (VariantData) obj;
		if (isDefaultVariant == null) {
			if (other.isDefaultVariant != null)
				return false;
		} else if (!isDefaultVariant.equals(other.isDefaultVariant))
			return false;
		return true;
	}
	
	

}

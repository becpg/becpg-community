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
package fr.becpg.repo.variant.model;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.AlfType;
import fr.becpg.repo.repository.model.BeCPGDataObject;

/**
 * <p>VariantData class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@AlfType
@AlfQname(qname="bcpg:variant")
public class VariantData extends BeCPGDataObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6714268508538918393L;
	protected Boolean isDefaultVariant;
	protected String variantColumn;
	private Double recipeQtyUsed = 0d;
	private Double recipeQtyUsedWithLossPerc = 0d;
	private Double recipeVolumeUsed = 0d;
	

	/**
	 * <p>reset.</p>
	 */
	public void reset() {
		recipeQtyUsed = 0d;
		recipeQtyUsedWithLossPerc = 0d;
		recipeVolumeUsed = 0d;
	}
	
	/**
	 * <p>Getter for the field <code>isDefaultVariant</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:isDefaultVariant")
	public Boolean getIsDefaultVariant() {
		return isDefaultVariant!=null ? isDefaultVariant : false;
	}

	/**
	 * <p>Setter for the field <code>isDefaultVariant</code>.</p>
	 *
	 * @param isDefaultVariant a {@link java.lang.Boolean} object.
	 */
	public void setIsDefaultVariant(Boolean isDefaultVariant) {
		this.isDefaultVariant = isDefaultVariant;
	}
	
	/**
	 * <p>Getter for the field <code>variantColumn</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@AlfProp
	@AlfQname(qname="bcpg:variantColumn")
	public String getVariantColumn() {
		return variantColumn;
	}

	/**
	 * <p>Setter for the field <code>variantColumn</code>.</p>
	 *
	 * @param variantColumn a {@link java.lang.String} object.
	 */
	public void setVariantColumn(String variantColumn) {
		this.variantColumn = variantColumn;
	}


	/**
	 * <p>Getter for the field <code>recipeQtyUsed</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getRecipeQtyUsed() {
		return recipeQtyUsed;
	}

	/**
	 * <p>Setter for the field <code>recipeQtyUsed</code>.</p>
	 *
	 * @param recipeQtyUsed a {@link java.lang.Double} object.
	 */
	public void setRecipeQtyUsed(Double recipeQtyUsed) {
		this.recipeQtyUsed = recipeQtyUsed;
	}

	/**
	 * <p>Getter for the field <code>recipeQtyUsedWithLossPerc</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getRecipeQtyUsedWithLossPerc() {
		return recipeQtyUsedWithLossPerc;
	}

	/**
	 * <p>Setter for the field <code>recipeQtyUsedWithLossPerc</code>.</p>
	 *
	 * @param recipeQtyUsedWithLossPerc a {@link java.lang.Double} object.
	 */
	public void setRecipeQtyUsedWithLossPerc(Double recipeQtyUsedWithLossPerc) {
		this.recipeQtyUsedWithLossPerc = recipeQtyUsedWithLossPerc;
	}

	/**
	 * <p>Getter for the field <code>recipeVolumeUsed</code>.</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getRecipeVolumeUsed() {
		return recipeVolumeUsed;
	}

	/**
	 * <p>Setter for the field <code>recipeVolumeUsed</code>.</p>
	 *
	 * @param recipeVolumeUsed a {@link java.lang.Double} object.
	 */
	public void setRecipeVolumeUsed(Double recipeVolumeUsed) {
		this.recipeVolumeUsed = recipeVolumeUsed;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "VariantData [isDefaultVariant=" + isDefaultVariant + "]";
	}
	
	

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((isDefaultVariant == null) ? 0 : isDefaultVariant.hashCode());
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
		VariantData other = (VariantData) obj;
		if (isDefaultVariant == null) {
			if (other.isDefaultVariant != null)
				return false;
		} else if (!isDefaultVariant.equals(other.isDefaultVariant))
			return false;
		return true;
	}

	
	

}

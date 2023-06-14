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
package fr.becpg.repo.product.data.productList;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.InternalField;
import fr.becpg.repo.repository.model.AbstractEffectiveDataItem;
import fr.becpg.repo.variant.model.VariantDataItem;

/**
 * <p>Abstract AbstractEffectiveVariantListDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractEffectiveVariantListDataItem extends AbstractEffectiveDataItem implements VariantDataItem {


	/**
	 * 
	 */
	private static final long serialVersionUID = -2500364866935247636L;
	private List<NodeRef> variants;
	
	

	public AbstractEffectiveVariantListDataItem() {
		super();
	}
	
	public AbstractEffectiveVariantListDataItem(AbstractEffectiveVariantListDataItem c) {
		super(c);
		this.variants = c.variants;
	}

	/**
	 * <p>Getter for the field <code>variants</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:variantIds")
	public List<NodeRef> getVariants() {
		return variants;
	}

	/** {@inheritDoc} */
	public void setVariants(List<NodeRef> variants) {
		this.variants = variants;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((variants == null) ? 0 : variants.hashCode());
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
		AbstractEffectiveVariantListDataItem other = (AbstractEffectiveVariantListDataItem) obj;
		if (variants == null) {
			if (other.variants != null)
				return false;
		} else if (!variants.equals(other.variants))
			return false;
		return true;
	}
	

	
}

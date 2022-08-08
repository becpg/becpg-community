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
package fr.becpg.repo.repository.model;

import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.variant.model.VariantData;

/**
 * <p>VariantAwareDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class VariantAwareDataItem extends AbstractManualDataItem {


	private static final long serialVersionUID = -2757971744559304500L;

	public static final String VARIANT_COLUMN_NAME = "bcpg_variantColumn";

	public static final int VARIANT_COLUMN_SIZE= 5;
	

	/**
	 * <p>Constructor for VariantAwareDataItem.</p>
	 */
	protected VariantAwareDataItem(){
		super();
	}

	/**
	 * <p>Constructor for VariantAwareDataItem.</p>
	 *
	 * @param a a {@link fr.becpg.repo.repository.model.VariantAwareDataItem} object.
	 */
	protected VariantAwareDataItem(VariantAwareDataItem a) {
		super(a);
	}


	public Double getValue(VariantData variant) {
		String variantColumn = variant.getVariantColumn();
		if (variantColumn != null && !variantColumn.isEmpty()) {
			return getValue(variantColumn);
		}
		return null;
	}
	
	public Double getValue(String variantColumn) {
		if (variantColumn != null && !variantColumn.isEmpty() && variantColumn.startsWith(VARIANT_COLUMN_NAME)) {
			QName variantColumnName= QName.createQName(BeCPGModel.BECPG_URI, variantColumn.replace("bcpg_", ""));
			return (Double) this.getExtraProperties().get(variantColumnName);
		}
		return null;
	}


	public void setValue(Double value, VariantData variant) {
		String variantColumn = variant.getVariantColumn();
		if (variantColumn != null && !variantColumn.isEmpty()) {
			setValue(value, variantColumn);
		}
	}

	public void setValue(Double value, String variantColumn) {
		QName variantColumnName= QName.createQName(BeCPGModel.BECPG_URI, variantColumn.replace("bcpg_", ""));
		this.getExtraProperties().put(variantColumnName, value);
	}

}

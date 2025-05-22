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

import java.util.Objects;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.InternalField;

/**
 * <p>Abstract AbstractManualDataItem class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractManualDataItem extends BeCPGDataObject implements ManualDataItem, SortableDataItem {

	
	private static final long serialVersionUID = 4027538577640263821L;
	
	/**
	 * Indicates whether this data item is manually managed.
	 */
	protected Boolean isManual = Boolean.FALSE;
	
	/**
	 * The sort order of this data item.
	 */
	protected Integer sort;

	/**
	 * <p>Constructor for AbstractManualDataItem.</p>
	 */
	protected AbstractManualDataItem(){
		super();
	}
	
	/**
	 * <p>Constructor for AbstractManualDataItem.</p>
	 *
	 * @param a a {@link fr.becpg.repo.repository.model.AbstractManualDataItem} object.
	 */
	protected AbstractManualDataItem(AbstractManualDataItem a) {
		super(a);
		this.isManual = a.isManual;
		this.sort = a.sort;
	}

	/**
	 * <p>Getter for the field <code>isManual</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:isManualListItem")
	public Boolean getIsManual() {
		return isManual!=null ? isManual : Boolean.FALSE;
	}

	/** {@inheritDoc} */
	public void setIsManual(Boolean isManual) {
		this.isManual = isManual;
	}

	/**
	 * <p>Getter for the field <code>sort</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object.
	 */
	@AlfProp
	@InternalField
	@AlfQname(qname="bcpg:sort")
	public Integer getSort() {
		return sort;
	}

	/** {@inheritDoc} */
	public void setSort(Integer sort) {
		this.sort = sort;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(isManual, sort);
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
		AbstractManualDataItem other = (AbstractManualDataItem) obj;
		return Objects.equals(isManual, other.isManual) && Objects.equals(sort, other.sort);
	}

	
	
	
}

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
package fr.becpg.repo.entity.datalist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>PaginatedExtractedItems class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PaginatedExtractedItems {
	

	protected List<Map<String,Object>> items;
	
	protected List<AttributeExtractorStructure> computedFields;
	
	protected int fullListSize;
	

	/**
	 * <p>Constructor for PaginatedExtractedItems.</p>
	 */
	protected PaginatedExtractedItems() {
		//Do Nothing
	}
	
	/**
	 * <p>Constructor for PaginatedExtractedItems.</p>
	 *
	 * @param pageSize a {@link java.lang.Integer} object.
	 */
	public PaginatedExtractedItems(Integer pageSize) {
		items = new ArrayList<>(pageSize);
	}

	
	/**
	 * <p>addItem.</p>
	 *
	 * @param item a {@link java.util.Map} object.
	 * @return a boolean.
	 */
	public boolean addItem(Map<String, Object> item){
		return items.add(item);
	}
	
	/**
	 * <p>getPageItems.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<Map<String, Object>> getPageItems() {
		return items;
	}


	/**
	 * <p>Getter for the field <code>fullListSize</code>.</p>
	 *
	 * @return a int.
	 */
	public int getFullListSize() {
		return fullListSize;
	}

	/**
	 * <p>Setter for the field <code>fullListSize</code>.</p>
	 *
	 * @param fullListSize a int.
	 */
	public void setFullListSize(int fullListSize) {
		this.fullListSize = fullListSize;
	}

	/**
	 * <p>Getter for the field <code>computedFields</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public List<AttributeExtractorStructure> getComputedFields() {
		return computedFields;
	}

	/**
	 * <p>Setter for the field <code>computedFields</code>.</p>
	 *
	 * @param computedFields a {@link java.util.List} object.
	 */
	public void setComputedFields(List<AttributeExtractorStructure> computedFields) {
		this.computedFields = computedFields;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(computedFields, fullListSize, items);
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
		PaginatedExtractedItems other = (PaginatedExtractedItems) obj;
		return Objects.equals(computedFields, other.computedFields) && fullListSize == other.fullListSize && Objects.equals(items, other.items);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "PaginatedExtractedItems [items=" + items + ", computedFields=" + computedFields + ", fullListSize=" + fullListSize + "]";
	}
	

}

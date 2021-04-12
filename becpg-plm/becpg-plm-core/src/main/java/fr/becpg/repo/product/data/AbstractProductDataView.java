/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG. 
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

import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.repository.annotation.AlfQname;
import fr.becpg.repo.repository.annotation.DataList;
import fr.becpg.repo.repository.model.BaseObject;
import fr.becpg.repo.repository.model.CompositionDataItem;

/**
 * <p>Abstract AbstractProductDataView class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractProductDataView extends BaseObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8864829069100366849L;

	private List<DynamicCharactListItem> dynamicCharactList;
	
	/**
	 * <p>getMainDataList.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	public abstract List<? extends CompositionDataItem> getMainDataList();
	
	
	/**
	 * <p>Getter for the field <code>dynamicCharactList</code>.</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@DataList
	@AlfQname(qname="bcpg:dynamicCharactList")
	public List<DynamicCharactListItem> getDynamicCharactList() {
		return dynamicCharactList;
	}

	/**
	 * <p>Setter for the field <code>dynamicCharactList</code>.</p>
	 *
	 * @param dynamicCharactList a {@link java.util.List} object.
	 */
	public void setDynamicCharactList(List<DynamicCharactListItem> dynamicCharactList) {
		this.dynamicCharactList = dynamicCharactList;
	}

	
}

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
package fr.becpg.repo.product.data.spel;

import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;

/**
 * <p>DeclarationFilterContext class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DeclarationFilterContext {

	CompoListDataItem compoListDataItem;
	IngListDataItem ingListDataItem;

	/**
	 * <p>Constructor for DeclarationFilterContext.</p>
	 *
	 * @param compoListDataItem a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 * @param ingListDataItem a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 */
	public DeclarationFilterContext(CompoListDataItem compoListDataItem, IngListDataItem ingListDataItem) {
		super();
		this.compoListDataItem = compoListDataItem;
		this.ingListDataItem = ingListDataItem;
	}

	/**
	 * <p>Constructor for DeclarationFilterContext.</p>
	 */
	public DeclarationFilterContext() {
		super();
	}

	/**
	 * <p>Getter for the field <code>compoListDataItem</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.CompoListDataItem} object.
	 */
	public CompoListDataItem getCompoListDataItem() {
		return compoListDataItem;
	}

	/**
	 * <p>Getter for the field <code>ingListDataItem</code>.</p>
	 *
	 * @return a {@link fr.becpg.repo.product.data.productList.IngListDataItem} object.
	 */
	public IngListDataItem getIngListDataItem() {
		return ingListDataItem;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "DeclarationFilterContext [compoListDataItem=" + compoListDataItem + ", ingListDataItem=" + ingListDataItem + "]";
	}

	
	
}
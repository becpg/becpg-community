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
package fr.becpg.repo.product;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.CharactDetails;

/**
 * <p>CharactDetailsVisitor interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface CharactDetailsVisitor {

	/**
	 * <p>visit.</p>
	 *
	 * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @param elements a {@link java.util.List} object.
	 * @param level a {@link java.lang.Integer} object.
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	CharactDetails visit(ProductData productData, List<NodeRef> elements, Integer level) throws FormulateException;

	/**
	 * <p>setDataListType.</p>
	 *
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object.
	 */
	void setDataListType(QName dataType);

}

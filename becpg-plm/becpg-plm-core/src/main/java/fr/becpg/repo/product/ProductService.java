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

import fr.becpg.repo.product.data.CharactDetails;
import fr.becpg.repo.product.data.ProductData;

/**
 * <p>ProductService interface.</p>
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface ProductService {
		   	       
	
	
    /**
     * Formulate the product (update DB)
     *
     * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
     * @throws fr.becpg.repo.formulation.FormulateException if any.
     */
    void formulate(NodeRef productNodeRef) ;
    
    /**
     * Use fast chain formulation handler if fast param is true
     *
     * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
     * @param chainId a string.
     * @throws fr.becpg.repo.formulation.FormulateException if any.
     */
    void formulate(NodeRef productNodeRef,  String chainId) ;
    
    /**
     * Formulate the product (don't update DB)
     *
     * @param productData a {@link fr.becpg.repo.product.data.ProductData} object.
     * @return a {@link fr.becpg.repo.product.data.ProductData} object.
     * @throws fr.becpg.repo.formulation.FormulateException if any.
     */
    ProductData formulate(ProductData productData);
    
    /**
     * <p>formulate.</p>
     *
     * @param productData a {@link fr.becpg.repo.product.data.ProductData} object
     * @param chainId a {@link java.lang.String} object
     * @return a {@link fr.becpg.repo.product.data.ProductData} object
     */
    ProductData formulate(ProductData productData, String chainId);
     
    
	/**
	 * <p>formulateDetails.</p>
	 *
	 * @param productNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param dataType a {@link org.alfresco.service.namespace.QName} object.
	 * @param dataListName a {@link java.lang.String} object.
	 * @param elements a {@link java.util.List} object.
	 * @param level a {@link java.lang.Integer} object.
	 * @return a {@link fr.becpg.repo.product.data.CharactDetails} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	CharactDetails formulateDetails(NodeRef productNodeRef, QName dataType, String dataListName, List<NodeRef> elements, Integer level);


	/**
	 * <p>shouldFormulate.</p>
	 *
	 * @param product a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	boolean shouldFormulate(NodeRef product);

	
	/**
	 * Formulate a text recipe in Memory
	 *
	 * @param recipe a {@link java.lang.String} object.
	 * @return a {@link fr.becpg.repo.product.data.ProductData} object.
	 * @throws fr.becpg.repo.formulation.FormulateException if any.
	 */
	ProductData formulateText(String recipe);


   
}

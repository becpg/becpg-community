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
package fr.becpg.repo.product;

import java.util.Date;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.CharactDetails;

/**
 * @author querephi
 */
@Service
public interface ProductService {
		   	       
    /**
     * Formulate the product (update DB)
     */
    public void formulate(NodeRef productNodeRef) throws FormulateException;
    
    /**
     * Use fast chain formulation handler if fast param is true
     */
	public void formulate(NodeRef productNodeRef, boolean fast)   throws FormulateException;
    
    /**
     * Formulate the product (don't update DB)
     */
    public ProductData formulate(ProductData productData) throws FormulateException;
     
    
	public CharactDetails formulateDetails(NodeRef productNodeRef, QName dataType, String dataListName, List<NodeRef> elements) throws FormulateException;


	public boolean shouldFormulate(NodeRef product);


   
}

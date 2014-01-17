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
package fr.becpg.repo.web.scripts.product;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.formulation.FormulateException;
import fr.becpg.repo.product.ProductService;

public abstract class AbstractProductWebscript extends AbstractWebScript
{	
	
		private static Log logger = LogFactory.getLog(AbstractProductWebscript.class);
		
		//request parameter names
		protected static final String PARAM_STORE_TYPE = "store_type";

		protected static final String PARAM_STORE_ID = "store_id";

		protected static final String PARAM_ID = "id";
		
		
		/** The product service. */
		protected ProductService productService;
		
		/**
		 * Sets the product service.
		 *
		 * @param productService the new product service
		 */
		public void setProductService(ProductService productService){
			this.productService = productService;
		}
		
		
		protected NodeRef getProductNodeRef(WebScriptRequest req) {
			Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
	    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
	    	
			return new NodeRef(storeType, storeId, nodeId);
		}
		
		
		protected void handleFormulationError(FormulateException e) {

			logger.error(e,e);
			throw new WebScriptException(e.getMessage());
			
		}
		
}

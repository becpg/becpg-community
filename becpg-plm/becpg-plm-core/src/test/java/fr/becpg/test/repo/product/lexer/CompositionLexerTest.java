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
package fr.becpg.test.repo.product.lexer;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.lexer.CompositionLexer;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class CompositionLexerTest extends AbstractFinishedProductTest {

	protected static Log logger = LogFactory.getLog(CompositionLexerTest.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testCompositionLexer() throws Exception {
		
		String recipe = "5 P Raw material 1\n 12,9 gr Raw material 2\n 25.5005 g Raw material 3\n50 Raw material 5";

	   List<CompoListDataItem> ret = CompositionLexer.lexMultiLine(recipe);
	   
	   int check=0;
	   for(CompoListDataItem item : ret){

		   if(item.getProduct().equals(rawMaterial1NodeRef) && item.getQtySubFormula().equals(5d)
				   && item.getCompoListUnit().equals(CompoListUnit.P)){
			   check++;
		   }
		   if(item.getProduct().equals(rawMaterial2NodeRef) && item.getQtySubFormula().equals(12.9d)
				   && item.getCompoListUnit().equals(CompoListUnit.g)){
			   check++;
		   }
		   if(item.getProduct().equals(rawMaterial3NodeRef) && item.getQtySubFormula().equals(25.5005d)
				   && item.getCompoListUnit().equals(CompoListUnit.g)){
			   check++;
		   }
		   if(item.getProduct().equals(rawMaterial5NodeRef) && item.getQtySubFormula().equals(50d)
				   && item.getCompoListUnit().equals(CompoListUnit.P)){
			   check++;
		   }
	   }
	   
	   org.junit.Assert.assertEquals( 4, check);

	}
	
	
	@Test
	public void testFormulateRecipe() throws Exception {
		
	String recipe = "5 P Raw material 1\n 12,9 gr Raw material 2\n 25.5005 g Raw material 3\n50 Raw material 5";

	   ProductData productData =  productService.formulateText(recipe, new FinishedProductData());

	   org.junit.Assert.assertNotNull(productData);

	}


}

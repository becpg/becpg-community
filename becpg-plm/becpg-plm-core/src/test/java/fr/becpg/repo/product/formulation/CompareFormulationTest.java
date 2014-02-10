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
package fr.becpg.repo.product.formulation;

import java.util.Arrays;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;

public class CompareFormulationTest extends FormulationFullTest {

	protected static Log logger = LogFactory.getLog(CompareFormulationTest.class);

	@Resource
	private AssociationService associationService;


	/**
	 * Test formulate product.
	 * 
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationFull() throws Exception {

		logger.info("testFormulationFull");

		final NodeRef finishedProductNodeRef1 = createFullProductNodeRef("Produit fini 1");
		final NodeRef finishedProductNodeRef2 = createFullProductNodeRef("Produit fini 2");
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
				public NodeRef execute() throws Throwable {

					 associationService.update(finishedProductNodeRef1, BeCPGModel.ASSOC_COMPARE_WITH_ENTITIES, Arrays.asList(finishedProductNodeRef2));
					
					/*-- Formulate product --*/
					logger.info("/*-- Formulate product --*/");
					productService.formulate(finishedProductNodeRef1);

					/*-- Verify formulation --*/
					logger.info("/*-- Verify formulation --*/");
					ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);
					
					checkProduct(formulatedProduct);
					
					
					return null;

				}
			}, false, true);
		

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				ProductData product2 = alfrescoRepository.findOne(finishedProductNodeRef2);
				
				product2.setQty(3d);
				
				alfrescoRepository.save(product2);
				
				/*-- Formulate product --*/
				logger.info("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef1);

				/*-- Verify formulation --*/
				logger.info("/*-- Verify formulation --*/");
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef1);
				
				
				for (DynamicCharactListItem dynamicCharactListItem : formulatedProduct.getCompoListView().getDynamicCharactList()) {
					String trace = "Dyn charact :" + dynamicCharactListItem.getName() + " value " + dynamicCharactListItem.getValue();
					logger.info(trace);
					assertFalse("#Error".equals(dynamicCharactListItem.getValue()));
				}


				assertEquals(formulatedProduct.getCompoListView().getDynamicCharactList().get(0).getValue(),"{\"comp\":[{\"itemType\":\"bcpg:finishedProduct\",\"name\":\"Produit fini 2\",\"value\":2,\"nodeRef\":\"workspace://SpacesStore/a028a96e-7ae0-40ed-ae6f-7c2d37cda6f0\",\"displayValue\":2},{\"itemType\":\"bcpg:finishedProduct\",\"name\":\"Produit fini 2\",\"value\":3,\"nodeRef\":\"workspace://SpacesStore/a028a96e-7ae0-40ed-ae6f-7c2d37cda6f0\",\"displayValue\":3}]}");

				return null;

			}
		}, false, true);
	}

}

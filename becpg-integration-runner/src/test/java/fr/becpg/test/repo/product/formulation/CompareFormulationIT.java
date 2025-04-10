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
package fr.becpg.test.repo.product.formulation;

import java.util.Collections;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.helper.JsonFormulaHelper;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;

public class CompareFormulationIT extends FormulationFullIT {

	protected static final Log logger = LogFactory.getLog(CompareFormulationIT.class);

	@Autowired
	private AssociationService associationService;

	/**
	 * Test formulate product.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testCompareFormulation() throws Exception {

		logger.info("testFormulationFull");

		final NodeRef finishedProductNodeRef1 = createFullProductNodeRef("Produit fini 1");
		final NodeRef finishedProductNodeRef2 = createFullProductNodeRef("Produit fini 2");
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			associationService.update(finishedProductNodeRef1, BeCPGModel.ASSOC_COMPARE_WITH_ENTITIES,
					Collections.singletonList(finishedProductNodeRef2));

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			checkProduct(formulatedProduct);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			ProductData product2 = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef2);

			product2.setQty(3d);

			alfrescoRepository.save(product2);

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			for (DynamicCharactListItem dynamicCharactListItem : formulatedProduct.getCompoListView().getDynamicCharactList()) {
				String trace = "Dyn charact :" + dynamicCharactListItem.getName() + " value " + dynamicCharactListItem.getValue();
				logger.info(trace);
				Assert.assertNotEquals("#Error",dynamicCharactListItem.getValue());
			}

			// assertEquals((String)formulatedProduct.getCompoListView().getDynamicCharactList().get(0).getValue(),
			// "{\"comp\":[{\"itemType\":\"bcpg:finishedProduct\",\"name\":\"Produit
			// fini 2\""
			// +
			// ",\"value\":2,\"nodeRef\":\""+finishedProductNodeRef2.toString()+"\","
			// +
			// "\"displayValue\":\"2\"},{\"itemType\":\"bcpg:finishedProduct\",\"name\":\"Produit
			// fini 2\",\"value\":3,"
			// +
			// "\"nodeRef\":\""+finishedProductNodeRef2.toString()+"\",\"displayValue\":\"3\"}]}");

			JSONTokener tokener = new JSONTokener((String) formulatedProduct.getCompoListView().getDynamicCharactList().get(0).getValue());
			JSONObject jsonObject = new JSONObject(tokener);
			JSONArray array = (JSONArray) jsonObject.get(JsonFormulaHelper.JSON_COMP_ITEMS);
			Assert.assertEquals(2, array.length());

			Assert.assertEquals(3, ((JSONObject) array.get(1)).get(JsonFormulaHelper.JSON_VALUE));
			Assert.assertEquals(finishedProductNodeRef2.toString(), ((JSONObject) array.get(1)).get(JsonFormulaHelper.JSON_NODEREF));

			return null;
		}, false, true);
	}

}

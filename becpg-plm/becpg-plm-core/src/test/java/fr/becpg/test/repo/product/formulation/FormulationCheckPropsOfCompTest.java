/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.SystemState;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationCheckPropsOfCompTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationCheckPropsOfCompTest.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testCheckPropertiesOfComponents() throws Exception {

		logger.info("testCheckPropertiesOfComponents");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Finished product 1");
			finishedProduct.setLegalName("Legal Finished product 1");
			finishedProduct.setQty(2d);
			finishedProduct.setUnit(ProductUnit.P);
			finishedProduct.setLegalName(new MLText(Locale.FRENCH, "Produit fini 1"));
			finishedProduct.setNetWeight(0d);
			finishedProduct.setState(SystemState.Valid);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 100d, CompoListUnit.P, 10d, DeclarationType.Detail, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 10d, CompoListUnit.mL, 10d, DeclarationType.Detail, rawMaterial5NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 10d, CompoListUnit.g, 10d, DeclarationType.Detail, rawMaterial6NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 10d, CompoListUnit.g, 10d, DeclarationType.Detail, rawMaterial7NodeRef));

			finishedProduct.getCompoListView().setCompoList(compoList);
			NodeRef finishedProductNodeRef1 = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef1);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			ProductData formulatedProduct1 = alfrescoRepository.findOne(finishedProductNodeRef1);

			assertNotNull(formulatedProduct1.getCompoListView().getReqCtrlList());
			logger.info("formulation raised " + formulatedProduct1.getCompoListView().getReqCtrlList().size() + " rclDataItems");
			int checks = 0;
			for (ReqCtrlListDataItem r : formulatedProduct1.getCompoListView().getReqCtrlList()) {

				logger.info("Product: " + nodeService.getProperty(r.getSources().get(0), ContentModel.PROP_NAME));
				logger.info("Msg: " + r.getReqMessage());
				logger.info("Sources (" + r.getSources().size() + "): " + r.getSources());

				if (!r.getReqMessage().startsWith("Impossible")) {
					for (NodeRef source : r.getSources()) {
						if (r.getReqMessage().equals("Composant non validé")) {
							// no compo is valid
							assertEquals(4, r.getSources().size());
							assertTrue(r.getSources().contains(rawMaterial1NodeRef) && r.getSources().contains(rawMaterial5NodeRef)
									&& r.getSources().contains(rawMaterial6NodeRef) && r.getSources().contains(rawMaterial7NodeRef));
						} else if (source.equals(finishedProductNodeRef1)) {
							assertTrue(r.getReqMessage().equals("Champ obligatoire 'Précautions d'emploi' manquant (catalogue 'EU 1169/2011 (INCO)')")
									|| r.getReqMessage()
											.equals("Champ obligatoire 'Conditions de conservation' manquant (catalogue 'EU 1169/2011 (INCO)')")
									|| r.getReqMessage()
											.equals("Champ obligatoire 'Origine géographique' manquant (catalogue 'EU 1169/2011 (INCO)')"));
						} else if (source.equals(rawMaterial5NodeRef)) {
							assertEquals("L'unité utilisée n'est pas la bonne.", r.getReqMessage());
							checks++;
						} else if (source.equals(rawMaterial6NodeRef)) {
							assertEquals("L'unité utilisée n'est pas la bonne.", r.getReqMessage());
							checks++;
						} else {
							// should not occur
							assertTrue(false);
						}
					}
				}
			}

			Assert.assertEquals(0, checks);

			return null;

		}, false, true);

	}

}

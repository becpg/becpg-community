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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.repo.variant.model.VariantData;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;



/**
 * Test variant columns calculation
 *
 *
 */
public class FormulationVariantColumnsIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationVariantColumnsIT.class);


	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test variant columns.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationCostsWithVariantColumns() throws Exception {

		final ProductData entityTpl = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// template
			FinishedProductData templateFinishedProduct = new FinishedProductData();
			templateFinishedProduct.setName("Template Produit fini");
			ProductData entityTpl1 = (ProductData) alfrescoRepository.create(getTestFolderNodeRef(), templateFinishedProduct);
			nodeService.addAspect(entityTpl1.getNodeRef(), BeCPGModel.ASPECT_ENTITY_TPL, null);
			nodeService.addAspect(entityTpl1.getNodeRef(), BeCPGModel.ASPECT_ENTITY_VARIANT, null);
			//add variants on template
			Map<QName, Serializable> props = new HashMap<>();
			props.put(ContentModel.PROP_NAME, "variantTpl");
			props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, true);
			props.put(BeCPGModel.PROP_VARIANT_COLUMN, "bcpg_variantColumn1");
			nodeService.createNode(entityTpl1.getNodeRef(), BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props);
			return entityTpl1;
		}, false, true);

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// product
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(1d);
			finishedProduct.setEntityTpl(entityTpl);
			finishedProduct = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);
			return finishedProduct.getNodeRef();
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			FinishedProductData finishedProduct = (FinishedProductData)alfrescoRepository.findOne(finishedProductNodeRef);
			//Variants
			for (int i=2; i<5; i++) {
				Map<QName, Serializable> props = new HashMap<>();
				props.put(ContentModel.PROP_NAME, "variant"+i);
				props.put(BeCPGModel.PROP_IS_DEFAULT_VARIANT, false);
				if (i<4) {
					props.put(BeCPGModel.PROP_VARIANT_COLUMN, "bcpg_variantColumn"+i);
				}
				nodeService.createNode(finishedProductNodeRef, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.ASSOC_VARIANTS, BeCPGModel.TYPE_VARIANT, props);
			}

			//CompoList

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial4NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial5NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			// CostList
			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			finishedProduct.setCostList(costList);
			assertNotNull("CostList is null", finishedProduct.getCostList());

			// NutList
			List<NutListDataItem> nutList = new LinkedList<>();
			nutList.add(NutListDataItem.build().withNut(nut1)
);
			nutList.add(NutListDataItem.build().withNut(nut2)
);
			nutList.add(NutListDataItem.build().withNut(nut3)
);
			nutList.add(NutListDataItem.build().withNut(nut4)
);
			finishedProduct.setNutList(nutList);
			assertNotNull("NutList is null", finishedProduct.getNutList());

			// PhysicoChemList
			List<PhysicoChemListDataItem> physicoChemList = new LinkedList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem4));
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem5));
			finishedProduct.setPhysicoChemList(physicoChemList);
			assertNotNull("PhysicoChemList is null", finishedProduct.getPhysicoChemList());

			alfrescoRepository.save(finishedProduct);
			return finishedProductNodeRef;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			int checks = 0;
			ProductData finishedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			List<VariantData> variants = finishedProduct.getVariants();
			
			assertNotNull(variants);
			assertEquals(4,variants.size());
			assertEquals(5,finishedProduct.getCompoListView().getCompoList().size());
			//Add variant to composition
			
			finishedProduct.getCompoListView().getCompoList().get(0).setVariants(Collections.singletonList(variants.get(variants.size()-1).getNodeRef()));
			finishedProduct.getCompoListView().getCompoList().get(1).setVariants(Collections.singletonList(variants.get(variants.size()-1).getNodeRef()));

			for (int i=2; i<5; i++) {
				finishedProduct.getCompoListView().getCompoList().get(i).setVariants(Collections.singletonList(finishedProduct.getVariants().get(i-2).getNodeRef()));
			}
			alfrescoRepository.save(finishedProduct);
			productService.formulate(finishedProductNodeRef);
			finishedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			//Check costList
			for (CostListDataItem costListDataItem : finishedProduct.getCostList()) {
				String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ costListDataItem.getValue() + " - previous value: " + costListDataItem.getPreviousValue() + " - future value: "
						+ costListDataItem.getFutureValue() + " - unit: " + costListDataItem.getUnit();
				logger.info(trace);
				if (costListDataItem.getCost().equals(cost1)) {
					assertEquals(5d, costListDataItem.getValue());
					assertEquals(2.5d, costListDataItem.getPreviousValue());
					assertEquals(10d, costListDataItem.getFutureValue());
					assertEquals("€/kg", costListDataItem.getUnit());
					assertEquals(5d, costListDataItem.getValuePerProduct());
					assertEquals(5d, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(3d, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn3")));
					assertEquals(null, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn4")));

					checks++;
				}
				if (costListDataItem.getCost().equals(cost2)) {
					assertEquals(6.0d, costListDataItem.getValue());
					assertEquals(3d, costListDataItem.getPreviousValue());
					assertEquals(12d, costListDataItem.getFutureValue());
					assertEquals("€/kg", costListDataItem.getUnit());
					assertEquals(6d, costListDataItem.getValuePerProduct());
					assertEquals(6d, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(6d, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn3")));
					assertEquals(null, nodeService.getProperty(costListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn4")));

					checks++;
				}
			}
			assertEquals(2, checks);
			//Check nutList
			checks = 0;
			for (NutListDataItem nutListDataItem : finishedProduct.getNutList()) {
				String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.info(trace);

				if (nutListDataItem.getNut().equals(nut1)) {
					assertEquals(3d, nutListDataItem.getValue());
					assertEquals(3d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(3d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn4")));
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals(6d, nutListDataItem.getValue());
					assertEquals(6d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(6d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn4")));
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut3)) {
					assertEquals(16d, nutListDataItem.getValue());
					assertEquals(16d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(12d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn4")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn5")));
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut4)) {
					assertEquals(3d, nutListDataItem.getValue());
					assertEquals(3d, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn4")));
					assertEquals(null, nodeService.getProperty(nutListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn5")));
					checks++;
				}
			}
			assertEquals(4, checks);

			//Check physicoChemList
			checks = 0;
			for (PhysicoChemListDataItem physicoChemListDataItem : finishedProduct.getPhysicoChemList()) {

				if (physicoChemListDataItem.getPhysicoChem().equals(physicoChem3)) {
					assertEquals(3d, physicoChemListDataItem.getValue());
					assertEquals(3d, nodeService.getProperty(physicoChemListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn1")));
					assertEquals(3d, nodeService.getProperty(physicoChemListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn2")));
					assertEquals(null, nodeService.getProperty(physicoChemListDataItem.getNodeRef(),QName.createQName(BeCPGModel.BECPG_URI, "variantColumn3")));
					checks++;
				}
			}
			assertEquals(1, checks);

			return null;
		}, false, true);
	}

}
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 * Test the formulation of a generic raw material
 *
 * @author quere
 *
 */
public class FormulationGenericRawMaterialIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationGenericRawMaterialIT.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testFormulationGenericRawMaterial() throws Exception {

		logger.info("testFormulationGenericRawMaterial");

		final NodeRef genRawMaterialNodeRef = inWriteTx(() -> {

			RawMaterialData genRawMaterial = new RawMaterialData();
			genRawMaterial.setName("Gen RM");
			genRawMaterial.setUnit(ProductUnit.kg);
			genRawMaterial.setQty(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial7NodeRef));
			genRawMaterial.getCompoListView().setCompoList(compoList);
			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			genRawMaterial.setCostList(costList);
			List<PhysicoChemListDataItem> physicoChemList = new LinkedList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem3));
			genRawMaterial.setPhysicoChemList(physicoChemList);

			return alfrescoRepository.create(getTestFolderNodeRef(), genRawMaterial).getNodeRef();

		});

		inWriteTx(() -> {
			productService.formulate(genRawMaterialNodeRef);

			RawMaterialData formulatedProduct = (RawMaterialData) alfrescoRepository.findOne(genRawMaterialNodeRef);

			assertSuppliers(formulatedProduct);

			assertCostList(formulatedProduct);

			assertIngList(formulatedProduct);

			assertPhysicoChemList(formulatedProduct);

			assertAllergenList(formulatedProduct);

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterial1NodeRef);
			Assert.assertNotNull(listContainerNodeRef);
			Assert.assertNotNull(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST));

			return null;
		});
	}
	

	@Test
	public void testFormulationRawMaterial() throws Exception {

		logger.info("testFormulationRawMaterial");

		final NodeRef rawMaterialNodeRef = inWriteTx(() -> {

			RawMaterialData genRawMaterial = new RawMaterialData();
			genRawMaterial.setName("Standard RM");
			genRawMaterial.setUnit(ProductUnit.kg);
			genRawMaterial.setQty(1d);
			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			genRawMaterial.setCostList(costList);
			List<PhysicoChemListDataItem> physicoChemList = new LinkedList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem3));
			genRawMaterial.setPhysicoChemList(physicoChemList);

			return alfrescoRepository.create(getTestFolderNodeRef(), genRawMaterial).getNodeRef();

		});

		inWriteTx(() -> {

			productService.formulate(rawMaterialNodeRef);
			return null;
		});

		inWriteTx(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);

			Assert.assertNotNull(listContainerNodeRef);
			Assert.assertNotNull(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST));
			Assert.assertNull(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COMPOLIST));

			return null;

		});
	}

	private void assertAllergenList(RawMaterialData formulatedProduct) {
		int checks = 0;

		assertNotNull("AllergenList is null", formulatedProduct.getAllergenList());
		for (AllergenListDataItem allergenListDataItem : formulatedProduct.getAllergenList()) {
			String voluntarySources = "";
			for (NodeRef part : allergenListDataItem.getVoluntarySources()) {
				voluntarySources += nodeService.getProperty(part, BeCPGModel.PROP_CHARACT_NAME) + ", ";
			}

			String inVoluntarySources = "";
			for (NodeRef part : allergenListDataItem.getInVoluntarySources()) {
				inVoluntarySources += nodeService.getProperty(part, BeCPGModel.PROP_CHARACT_NAME) + ", ";
			}

			String trace = "allergen: " + nodeService.getProperty(allergenListDataItem.getAllergen(), BeCPGModel.PROP_CHARACT_NAME) + " qty Perc:  "
					+ allergenListDataItem.getQtyPerc() + " - voluntary: " + allergenListDataItem.getVoluntary() + " - involuntary: "
					+ allergenListDataItem.getInVoluntary() + " - voluntary sources:" + voluntarySources + " - involuntary sources:"
					+ inVoluntarySources;
			logger.info(trace);

			// allergen1 - voluntary: true - involuntary: false -
			// voluntary sources:Raw material 1, Raw material 2 -
			// involuntary sources:
			if (allergenListDataItem.getAllergen().equals(allergen1)) {
				assertEquals("allergen1.getVoluntary() == true, actual values: " + trace, true,
						Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
				assertEquals("allergen1.getInVoluntary() == false, actual values: " + trace, false,
						Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
				assertEquals("allergen1.getVoluntarySources() contains Raw material 1, actual values: " + trace, true,
						allergenListDataItem.getVoluntarySources().contains(rawMaterial1NodeRef));
				assertEquals("allergen1.getVoluntarySources() contains Raw material 2, actual values: " + trace, true,
						allergenListDataItem.getVoluntarySources().contains(rawMaterial2NodeRef));
				assertEquals("allergen1.getInVoluntarySources() is empty, actual values: " + trace, 0,
						allergenListDataItem.getInVoluntarySources().size());
				//Max(20,10)
				assertEquals(20d, allergenListDataItem.getQtyPerc());
				checks++;
			}
			// allergen2 - voluntary: false - involuntary: true -
			// voluntary sources: - involuntary sources:Raw material 2,
			if (allergenListDataItem.getAllergen().equals(allergen2)) {
				assertEquals("allergen2.getVoluntary() == false, actual values: " + trace, false,
						Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
				assertEquals("allergen2.getInVoluntary() == true, actual values: " + trace, true,
						Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
				assertEquals("allergen2.getInVoluntarySources() contains Raw material 2, actual values: " + trace, true,
						allergenListDataItem.getInVoluntarySources().contains(rawMaterial2NodeRef));
				assertEquals("allergen2.getVoluntarySources() is empty, actual values: " + trace, 0,
						allergenListDataItem.getVoluntarySources().size());
				//Max(5,50)
				assertEquals(50d, allergenListDataItem.getQtyPerc());
				checks++;
			}
			// allergen: allergen3 - voluntary: true - involuntary: true
			// - voluntary sources:Raw material 3, - involuntary
			// sources:Raw material 3,
			if (allergenListDataItem.getAllergen().equals(allergen3)) {
				assertEquals("allergen3.getVoluntary() == true, actual values: " + trace, false,
						Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
				assertEquals("allergen3.getInVoluntary() == true, actual values: " + trace, false,
						Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
				assertEquals("allergen3.getVoluntarySources() is empty, actual values: " + trace, 0,
						allergenListDataItem.getVoluntarySources().size());
				assertEquals("allergen3.getInVoluntarySources() is empty, actual values: " + trace, 0,
						allergenListDataItem.getInVoluntarySources().size());
				assertEquals(null, allergenListDataItem.getQtyPerc());
				checks++;
			}
			// allergen4 - voluntary: false - involuntary: false -
			// voluntary sources: - involuntary sources:
			if (allergenListDataItem.getAllergen().equals(allergen4)) {
				assertEquals("allergen4.getVoluntary() == false, actual values: " + trace, false,
						Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
				assertEquals("allergen4.getInVoluntary() == false, actual values: " + trace, false,
						Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
				assertEquals("allergen4.getVoluntarySources() is empty, actual values: " + trace, 0,
						allergenListDataItem.getVoluntarySources().size());
				assertEquals("allergen4.getInVoluntarySources() is empty, actual values: " + trace, 0,
						allergenListDataItem.getInVoluntarySources().size());
				assertEquals(null, allergenListDataItem.getQtyPerc());
				checks++;
			}
		}
		assertEquals(4, checks);

	}

	private void assertPhysicoChemList(RawMaterialData formulatedProduct) {

		int checks = 0;
		for (PhysicoChemListDataItem p : formulatedProduct.getPhysicoChemList()) {
			logger.info("p " + nodeService.getProperty(p.getPhysicoChem(), BeCPGModel.PROP_CHARACT_NAME) + " value: " + p.getValue() + " mini: "
					+ p.getMini() + " maxi: " + p.getMaxi());
			if (p.getPhysicoChem().equals(physicoChem3)) {
				assertEquals(1d, p.getValue());
				assertEquals(0.8d, p.getMini());
				assertEquals(2.1d, p.getMaxi());
				checks++;
			}
		}
		assertEquals(1, checks);

	}

	private void assertIngList(RawMaterialData formulatedProduct) {
		int checks = 0;
		for (IngListDataItem ing : formulatedProduct.getIngList()) {
			if (ing.getIng().equals(ing1) && (ing.getMini() != null) && (ing.getMaxi() != null)) {
				logger.info("ing1 mini: " + ing.getMini() + " maxi: " + ing.getMaxi());
				assertEquals(ing.getMini(), 15.0);
				assertEquals(ing.getMaxi(), 90.0);
				checks++;
			} else if (ing.getIng().equals(ing2) && (ing.getMini() != null) && (ing.getMaxi() != null)) {
				logger.info("ing2 mini: " + ing.getMini() + " maxi: " + ing.getMaxi());
				assertEquals(ing.getMini(), 18.0);
				assertEquals(ing.getMaxi(), 86.0);
				checks++;
			}
		}
		assertEquals(2, checks);

	}

	private void assertCostList(RawMaterialData formulatedProduct) {

		int checks = 0;
		for (CostListDataItem c : formulatedProduct.getCostList()) {
			logger.info("c " + nodeService.getProperty(c.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " " + c.getValue());
			if (c.getCost().equals(cost1)) {
				assertEquals(2d, c.getValue());
				checks++;
			} else if (c.getCost().equals(cost2)) {
				assertEquals(2d, c.getValue());
				checks++;
			}
		}
		assertEquals(2, checks);
	}

	private void assertSuppliers(RawMaterialData formulatedProduct) {

		assertEquals(2, formulatedProduct.getSuppliers().size());
		assertTrue(formulatedProduct.getSuppliers().contains(supplier1));
		assertTrue(formulatedProduct.getSuppliers().contains(supplier2));

	}

}

/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
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

	@Autowired
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testFormulationGenericRawMaterial() throws Exception {

		logger.info("testFormulationGenericRawMaterial");

		final NodeRef genRawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			RawMaterialData genRawMaterial = new RawMaterialData();
			genRawMaterial.setName("Gen RM");
			genRawMaterial.setUnit(ProductUnit.kg);
			genRawMaterial.setQty(1d);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, ProductUnit.kg, 0d, DeclarationType.Detail, rawMaterial7NodeRef));
			genRawMaterial.getCompoListView().setCompoList(compoList);
			List<CostListDataItem> costList = new LinkedList<>();
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			genRawMaterial.setCostList(costList);
			List<PhysicoChemListDataItem> physicoChemList = new LinkedList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, null, null, null, null, physicoChem3));
			genRawMaterial.setPhysicoChemList(physicoChemList);

			return alfrescoRepository.create(getTestFolderNodeRef(), genRawMaterial).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			productService.formulate(genRawMaterialNodeRef);

			RawMaterialData formulatedProduct = (RawMaterialData) alfrescoRepository.findOne(genRawMaterialNodeRef);

			assertEquals(2, formulatedProduct.getSuppliers().size());
			assertTrue(formulatedProduct.getSuppliers().contains(supplier1));
			assertTrue(formulatedProduct.getSuppliers().contains(supplier2));

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

			checks = 0;
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

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterial1NodeRef);
			Assert.assertNotNull(listContainerNodeRef);
			Assert.assertNotNull(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST));

			return null;

		}, false, true);
	}

	@Test
	public void testFormulationRawMaterial() throws Exception {

		logger.info("testFormulationRawMaterial");

		final NodeRef rawMaterialNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

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

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			productService.formulate(rawMaterialNodeRef);
			return null;
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(rawMaterialNodeRef);

			Assert.assertNotNull(listContainerNodeRef);
			Assert.assertNotNull(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COSTLIST));
			Assert.assertNull(entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_COMPOLIST));

			return null;

		}, false, true);
	}
}

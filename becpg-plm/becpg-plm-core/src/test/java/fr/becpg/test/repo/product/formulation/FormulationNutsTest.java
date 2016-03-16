/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
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
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.formulation.FormulationHandler;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.AbstractSimpleListFormulationHandler;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationNutsTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationNutsTest.class);

	@Resource
	private AssociationService associationService;

	@Autowired
	@Qualifier("productFormulationChain")
	private FormulationChain<ProductData> formulationChain;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testNutsFormulation() throws Exception {

		formulate(false);
		formulate(true);

	}

	protected void checkProduct(ProductData formulatedProduct, boolean propagateMode) {
		int checks = 0;

		// nuts
		assertNotNull("NutList is null", formulatedProduct.getNutList());
		for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
			String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
					+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
			logger.info(trace);
			if (nutListDataItem.getNut().equals(nut1)) {
				assertNotSame("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
				assertEquals("nut1.getUnit() == kJ/100g, actual values: " + trace, "kJ/100g", nutListDataItem.getUnit());
				assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
				checks++;
			}
			if (nutListDataItem.getNut().equals(nut2)) {
				assertEquals("nut2.getValue() == 6, actual values: " + trace, 6d, nutListDataItem.getValue());
				assertEquals("nut2.getUnit() == kcal/100g, actual values: " + trace, "kcal/100g", nutListDataItem.getUnit());
				assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
				assertEquals((6d * 50d) / 100, nutListDataItem.getValuePerServing());
				assertEquals((100 * nutListDataItem.getValuePerServing()) / 2000d, nutListDataItem.getGdaPerc());
				checks++;
			}
			if (nutListDataItem.getNut().equals(nut3)) {
				assertEquals("nut3.getValue() == 14, actual values: " + trace, 14d, nutListDataItem.getValue());

				checks++;
			}
			if (nutListDataItem.getNut().equals(nut4)) {
				assertEquals("nut4.getValue() == 1.5d, actual values: " + trace, 1.5d, nutListDataItem.getValue());

				checks++;
			}
			assertEquals(NutsCalculatingFormulationHandler.NUT_FORMULATED, nutListDataItem.getMethod());
		}

		if (propagateMode) {
			assertEquals(4, checks);
		} else {
			assertEquals(3, checks);
		}

		// ReqCtrlList
		checks = 0;

		String message0 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut1, BeCPGModel.PROP_CHARACT_NAME));
		String message1 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut2, BeCPGModel.PROP_CHARACT_NAME));
		String message2 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));
		String message3 = I18NUtil.getMessage(AbstractSimpleListFormulationHandler.MESSAGE_UNDEFINED_CHARACT,
				nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));
		String message4 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_MAXIMAL_DAILY_VALUE,
				nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));
		String message5 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut4, BeCPGModel.PROP_CHARACT_NAME));

		logger.info(formulatedProduct.getCompoListView().getReqCtrlList().size());
		for (ReqCtrlListDataItem r : formulatedProduct.getCompoListView().getReqCtrlList()) {

			logger.info("reqCtrl " + r.getReqMessage() + r.getReqType() + r.getSources());

			if (message1.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message2.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message0.equals(r.getReqMessage())) {
				fail();
			} else if (message3.equals(r.getReqMessage())) {
				if (propagateMode) {
					fail();
				}

				assertEquals(1, r.getSources().size());
				assertEquals(rawMaterial4NodeRef, r.getSources().get(0));
				checks++;
			} else if (message4.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message5.equals(r.getReqMessage())) {
				// should not occur
				fail();
			}
		}
		if (propagateMode) {
			assertEquals(3, checks);
		} else {
			assertEquals(4, checks);
		}
	}

	protected NodeRef createFullProductNodeRef(final String name) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Nut 1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification1 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(new NutListDataItem(null, null, null, 3d, 4d, null, nut1, null));
			nutList.add(new NutListDataItem(null, null, null, 7d, null, null, nut2, null));
			nutList.add(new NutListDataItem(null, null, null, null, 10d, null, nut3, null));
			nutList.add(new NutListDataItem(null, null, null, 1.5d, 10d, null, nut4, null));
			productSpecification1.setNutList(nutList);

			properties.put(ContentModel.PROP_NAME, name + " Spec Nut 2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);
			List<NutListDataItem> nutList2 = new ArrayList<>();
			nutList2.add(new NutListDataItem(null, null, null, 0.3d, 1.5d, null, nut4, null));
			productSpecification2.setNutList(nutList2);

			alfrescoRepository.save(productSpecification1);
			alfrescoRepository.save(productSpecification2);

			properties.put(ContentModel.PROP_NAME, name + " Spec Nut Global");
			NodeRef globalProductSpecificationNodeRef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData globalProductSpecification = (ProductSpecificationData) alfrescoRepository
					.findOne(globalProductSpecificationNodeRef);
			List<ProductSpecificationData> specList = new ArrayList<>();
			specList.add(productSpecification1);
			specList.add(productSpecification2);
			globalProductSpecification.setProductSpecifications(specList);

			alfrescoRepository.save(globalProductSpecification);

			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName(name);
			finishedProduct.setLegalName("Legal " + name);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(22.4d);
			finishedProduct.setDensity(1d);
			finishedProduct.setServingSize(50d);// 50g

			finishedProduct.setProjectedQty(10000l);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF1NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(0), null, 1d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(0), null, 2d, CompoListUnit.kg, 0d, DeclarationType.Detail, rawMaterial2NodeRef));
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.kg, 0d, DeclarationType.Detail, localSF2NodeRef));
			compoList
					.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, compoList.get(3), null, 3d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<NutListDataItem> nutList3 = new ArrayList<>();
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut1, null));
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut2, null));
			nutList3.add(new NutListDataItem(null, null, null, null, null, null, nut3, null));
			finishedProduct.setNutList(nutList3);

			finishedProduct = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);

			nodeService.createAssociation(globalProductSpecificationNodeRef, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(globalProductSpecificationNodeRef, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(finishedProduct.getNodeRef(), globalProductSpecificationNodeRef, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			// ProductData ps1 =
			// alfrescoRepository.findOne(productSpecificationNodeRef1);
			// logger.info("PS1 has "+ps1.getNutList().size()+" nuts");
			//
			// ProductData ps2 =
			// alfrescoRepository.findOne(productSpecificationNodeRef2);
			// logger.info("PS2 has "+ps2.getNutList().size()+" nuts");
			return finishedProduct.getNodeRef();
		}, false, true);
	}

	public void formulate(boolean propagate) throws Exception {

		logger.info("testNutsFormulation propagate - " + propagate);

		try {

			if (propagate) {
				for (FormulationHandler<ProductData> handler : formulationChain.getHandlers()) {
					if (handler instanceof NutsCalculatingFormulationHandler) {
						((NutsCalculatingFormulationHandler) handler).setPropagateModeEnable(true);
					}
				}
			}

			final NodeRef finishedProductNodeRef = createFullProductNodeRef("Nut formulation test 1 - " + propagate);

			transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

				/*-- Formulate product --*/
				logger.info("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);

				/*-- Verify formulation --*/
				logger.info("/*-- Verify formulation --*/");
				ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

				checkProduct(formulatedProduct, propagate);

				return null;

			}, false, true);

		} finally {
			if (propagate) {
				for (FormulationHandler<ProductData> handler : formulationChain.getHandlers()) {
					if (handler instanceof NutsCalculatingFormulationHandler) {
						((NutsCalculatingFormulationHandler) handler).setPropagateModeEnable(false);
					}
				}
			}
		}

	}

}

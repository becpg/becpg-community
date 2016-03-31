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
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementDataType;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ForbiddenIngListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.AllergensCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.ScoreCalculatingFormulationHandler;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationWithIngRequirementsTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationWithIngRequirementsTest.class);

	@Resource
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	/**
	 * Test formulate product, that has ings requirements defined
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationWithIngRequirements() throws Exception {

		logger.info("testFormulationWithIngRequirements");

		final NodeRef finishedProductNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			SemiFinishedProductData semiFinishedProduct = new SemiFinishedProductData();
			semiFinishedProduct.setName("Semi fini 1");
			semiFinishedProduct.setUnit(ProductUnit.kg);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(new CompoListDataItem(null, null, null, 1d, CompoListUnit.L, null, null, rawMaterial6NodeRef));
			semiFinishedProduct.getCompoListView().setCompoList(compoList);
			NodeRef semiFinishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), semiFinishedProduct).getNodeRef();

			/*-- Create finished product --*/
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			// finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setDensity(1d);

			compoList = new ArrayList<>();

			CompoListDataItem parent1 = new CompoListDataItem(null, null, null, 2d, CompoListUnit.kg, 10d, DeclarationType.Detail, localSF1NodeRef);

			compoList.add(parent1);
			CompoListDataItem parent12 = new CompoListDataItem(null, parent1, null, 1d, CompoListUnit.kg, 10d, DeclarationType.Detail,
					localSF2NodeRef);
			compoList.add(parent12);

			compoList.add(new CompoListDataItem(null, parent12, null, 0.80d, CompoListUnit.kg, 5d, DeclarationType.Declare, rawMaterial1NodeRef));
			compoList.add(new CompoListDataItem(null, parent12, null, 0.30d, CompoListUnit.kg, 10d, DeclarationType.Detail, rawMaterial2NodeRef));

			CompoListDataItem parent22 = new CompoListDataItem(null, parent1, null, 2d, CompoListUnit.kg, 20d, DeclarationType.Detail,
					localSF3NodeRef);

			compoList.add(parent22);
			compoList.add(new CompoListDataItem(null, parent22, null, 0.170d, CompoListUnit.kg, 0d, DeclarationType.Declare, rawMaterial3NodeRef));
			compoList.add(new CompoListDataItem(null, parent22, null, 0.40d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial4NodeRef));
			compoList.add(new CompoListDataItem(null, parent22, null, 1d, CompoListUnit.P, 0d, DeclarationType.Declare, rawMaterial5NodeRef));

			compoList.add(new CompoListDataItem(null, null, null, 2d, CompoListUnit.kg, null, null, semiFinishedProductNodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			// specification1
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Spec1");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification1 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);

			List<NodeRef> ings = new ArrayList<>();
			List<NodeRef> geoOrigins = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			List<NodeRef> voluntary = new ArrayList<>();
			List<NodeRef> inVoluntary = new ArrayList<>();

			List<ForbiddenIngListDataItem> forbiddenIngList1 = new ArrayList<>();
			forbiddenIngList1.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "OGM interdit", null, Boolean.TRUE, null, ings,
					geoOrigins, bioOrigins));
			forbiddenIngList1.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ionisation interdite", null, null, Boolean.TRUE,
					ings, geoOrigins, bioOrigins));

			ings = new ArrayList<>();
			geoOrigins = new ArrayList<>();
			ings.add(ing3);
			geoOrigins.add(geoOrigin1);
			forbiddenIngList1.add(new ForbiddenIngListDataItem(null, RequirementType.Tolerated, "Ing3 geoOrigin1 toléré", null, null, null, ings,
					geoOrigins, bioOrigins));

			ings = new ArrayList<>();
			geoOrigins = new ArrayList<>();
			ings.add(ing3);
			forbiddenIngList1
					.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ing3 < 40%", 0.4d, null, null, ings, geoOrigins, bioOrigins));

			ings = new ArrayList<>();
			geoOrigins = new ArrayList<>();
			ings.add(ing1);
			ings.add(ing4);
			geoOrigins.clear();
			forbiddenIngList1.add(new ForbiddenIngListDataItem(null, RequirementType.Forbidden, "Ing1 et ing4 interdits", null, null, null, ings,
					geoOrigins, bioOrigins));

			ings = new ArrayList<>();
			geoOrigins = new ArrayList<>();
			ings.add(ing3);
			geoOrigins.add(geoOrigin1);

			ForbiddenIngListDataItem forbiddenIngListDataItem = new ForbiddenIngListDataItem(null, RequirementType.Forbidden,
					"Ing3 geoOrigin1 obligatoire", null, null, null, ings, new ArrayList<NodeRef>(), bioOrigins);
			forbiddenIngListDataItem.setRequiredGeoOrigins(geoOrigins);
			forbiddenIngList1.add(forbiddenIngListDataItem);
			productSpecification1.setForbiddenIngList(forbiddenIngList1);

			// allergens
			ArrayList<AllergenListDataItem> allergenList = new ArrayList<>();
			voluntary.add(rawMaterial5NodeRef);
			inVoluntary.add(rawMaterial2NodeRef);
			allergenList.add(new AllergenListDataItem(null, null, false, false, voluntary, inVoluntary, allergen1, false));
			productSpecification1.setAllergenList(allergenList);

			alfrescoRepository.save(productSpecification1);

			// specification2
			properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "Spec2");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);
			List<ForbiddenIngListDataItem> forbiddenIngList2 = new ArrayList<>();

			ings = new ArrayList<>();
			geoOrigins = new ArrayList<>();
			ings.add(ing2);
			geoOrigins.add(geoOrigin2);
			forbiddenIngList2.add(new ForbiddenIngListDataItem(null, RequirementType.Info, "Ing2 geoOrigin2 interdit sur charcuterie", null, null,
					null, ings, geoOrigins, bioOrigins));

			productSpecification2.setForbiddenIngList(forbiddenIngList2);
			alfrescoRepository.save(productSpecification2);

			// create association
			nodeService.createAssociation(finishedProductNodeRef, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(finishedProductNodeRef, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			int checks = 0;

			logger.info("/*-- Formulation raised " + formulatedProduct.getCompoListView().getReqCtrlList().size() + " rclDataItems --*/");
			for (ReqCtrlListDataItem reqCtrlList : formulatedProduct.getCompoListView().getReqCtrlList()) {
				logger.info("/*-- Verify reqCtrlList : " + reqCtrlList.getReqMessage() + " --*/");
				logger.info("/*-- This item has " + reqCtrlList.getSources().size() + " sources --*/");

				/*
				 * #1909 added non validation rclDataItem
				 */
				if (I18NUtil.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_NON_VALIDATED_STATE).equals(reqCtrlList.getReqMessage())) {
					assertEquals(RequirementType.Tolerated, reqCtrlList.getReqType());
					assertEquals(RequirementDataType.Validation, reqCtrlList.getReqDataType());
					assertEquals(10, reqCtrlList.getSources().size());

					checks++;
				} else if (reqCtrlList.getReqMessage().equals("OGM interdit")) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(5, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial3NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial4NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial5NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial6NodeRef));
					checks++;
				} else if (reqCtrlList.getReqMessage().equals("Ionisation interdite")) {
					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(5, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial3NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial4NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial5NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial6NodeRef));
					checks++;
				} else if (reqCtrlList.getReqMessage().equals("Ing3 geoOrigin1 toléré")) {

					// should not occur
					assertTrue(false);
					assertEquals(RequirementType.Tolerated, reqCtrlList.getReqType());
				} else if (reqCtrlList.getReqMessage().equals("Ing3 geoOrigin1 obligatoire")) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(3, reqCtrlList.getSources().size());
					checks++;
				} else if (reqCtrlList.getReqMessage().equals("Ing3 < 40%")) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(0, reqCtrlList.getSources().size());
					checks++;
				} else if (reqCtrlList.getReqMessage().equals("Ing1 et ing4 interdits")) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(3, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(rawMaterial1NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial6NodeRef));
					checks++;
				} else if (reqCtrlList.getReqMessage().equals("Ing2 geoOrigin2 interdit sur charcuterie")) {

					assertEquals(RequirementType.Info, reqCtrlList.getReqType());
					assertEquals(3, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(rawMaterial1NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial2NodeRef));
					assertTrue(reqCtrlList.getSources().contains(rawMaterial6NodeRef));
					checks++;
				} else if (reqCtrlList.getReqMessage().equals(I18NUtil.getMessage(AllergensCalculatingFormulationHandler.MESSAGE_FORBIDDEN_ALLERGEN,
						nodeService.getProperty(allergen1, BeCPGModel.PROP_CHARACT_NAME)))) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(0, reqCtrlList.getSources().size());
					checks++;

				} else if (I18NUtil
						.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Libellé légal", "EU 1169/2011 (INCO)")
						.equals(reqCtrlList.getReqMessage())) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(1, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(formulatedProduct.getNodeRef()));
					checks++;

				} else if (I18NUtil
						.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Précautions d'emploi", "EU 1169/2011 (INCO)")
						.equals(reqCtrlList.getReqMessage())) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(1, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(formulatedProduct.getNodeRef()));
					checks++;

				} else if (I18NUtil.getMessage(ScoreCalculatingFormulationHandler.MESSAGE_MANDATORY_FIELD_MISSING, "Conditions de conservation",
						"EU 1169/2011 (INCO)").equals(reqCtrlList.getReqMessage())) {

					assertEquals(RequirementType.Forbidden, reqCtrlList.getReqType());
					assertEquals(1, reqCtrlList.getSources().size());
					assertTrue(reqCtrlList.getSources().contains(formulatedProduct.getNodeRef()));
					checks++;

				} else {
					logger.info("Unexpected rclDataItem: " + reqCtrlList.getReqMessage());
					fail();
				}
			}

			logger.info("/*-- Done checking, checks=" + checks + " (should be 11) --*/");
			assertEquals(11, checks);

			/*
			 * #257: check reqCtrlList is clear if all req are respected (we
			 * remove specification to get everything OK)
			 */
			nodeService.removeAssociation(finishedProductNodeRef, productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.removeAssociation(finishedProductNodeRef, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			/*-- Formulate product --*/
			logger.debug("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.debug("/*-- Verify formulation --*/");
			formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			/*
			 * 4 rclDataItem remains : one for non validated product, and three
			 * for INCO missing fields: legal name, conservation conditions, and
			 * precautions for use
			 */

			logger.debug("After removing specs, " + formulatedProduct.getCompoListView().getReqCtrlList().size() + " remain");
			assertEquals(4, formulatedProduct.getCompoListView().getReqCtrlList().size());

			return null;

		}, false, true);

	}
}

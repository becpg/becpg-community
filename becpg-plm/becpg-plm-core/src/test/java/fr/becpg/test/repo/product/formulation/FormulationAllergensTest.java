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
import java.util.Locale;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
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
import fr.becpg.model.SystemState;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.CompoListUnit;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.RequirementType;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.AllergensCalculatingFormulationHandler;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationAllergensTest extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationAllergensTest.class);

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
	public void testAllergensFormulation() throws Exception {

		formulate();

	}

	protected void checkProduct(ProductData formulatedProduct) {
		int checks = 0;

		// ReqCtrlList
		int checkMissingFields = 0;
		for (ReqCtrlListDataItem r : formulatedProduct.getCompoListView().getReqCtrlList()) {

			if ((r.getReqMessage() != null) && r.getReqMessage().contains("Champ obligatoire")) {
				assertEquals(RequirementType.Forbidden, r.getReqType());
				checkMissingFields++;
			} else if (I18NUtil.getMessage(AllergensCalculatingFormulationHandler.MESSAGE_FORBIDDEN_ALLERGEN,
					nodeService.getProperty(allergen2, BeCPGModel.PROP_CHARACT_NAME)).equals(r.getReqMessage())) {

				assertEquals(0, r.getSources().size());
				assertEquals(RequirementType.Forbidden, r.getReqType());
				checks++;
			} else if (I18NUtil.getMessage(AllergensCalculatingFormulationHandler.MESSAGE_FORBIDDEN_ALLERGEN,
					nodeService.getProperty(allergen1, BeCPGModel.PROP_CHARACT_NAME)).equals(r.getReqMessage())) {

				// should not happen
				assertTrue(false);
				assertEquals(0, r.getSources().size());
				assertEquals(RequirementType.Forbidden, r.getReqType());
				checks++;
			} else {
				logger.info("Unexpected rclDataItem: " + r.getReqMessage());
				assertTrue(false);
			}
		}
		assertEquals(2, checkMissingFields);
		assertEquals(1, checks);

	}

	protected NodeRef createFullProductNodeRef(final String name) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, name + " Spec Allergen globale");
			NodeRef productSpecificationNodeRef1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef1);

			// two catalogs
			/*-- Spec allergen 1 : Allergen 1 is allowed in RM1&3 if voluntary, or 11 is involuntary--*/
			properties.clear();
			properties.put(ContentModel.PROP_NAME, name + " Spec Allergen 1");
			NodeRef productSpecificationNodeRef2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();

			ProductSpecificationData productSpecification2 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef2);

			List<AllergenListDataItem> allergens = new ArrayList<>();

			allergens.add(new AllergenListDataItem(null, null, false, true, null, null, allergen1, false));
			allergens.add(new AllergenListDataItem(null, null, false, false, null, null, allergen2, false));
			productSpecification2.setAllergenList(allergens);
			alfrescoRepository.save(productSpecification2);

			/*-- Spec allergen 2 : allergen 1 is allowed in RM1&3 if voluntary--*/
			properties.clear();
			properties.put(ContentModel.PROP_NAME, name + " Spec Allergen 2");
			NodeRef productSpecificationNodeRef3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_PRODUCT_SPECIFICATION, properties).getChildRef();
			ProductSpecificationData productSpecification3 = (ProductSpecificationData) alfrescoRepository.findOne(productSpecificationNodeRef3);

			allergens.clear();
			allergens.add(new AllergenListDataItem(null, null, true, false, null, null, allergen1, false));
			productSpecification3.setAllergenList(allergens);
			alfrescoRepository.save(productSpecification3);

			alfrescoRepository.save(productSpecification);

			/*-- Creating FP --*/
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName(name);
			finishedProduct.setLegalName(new MLText(Locale.FRENCH, "Legal " + name));
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(22.4d);
			finishedProduct.setDensity(1d);
			finishedProduct.setServingSize(50d); // 50g
			finishedProduct.setNetWeight(1d);
			finishedProduct.setProductSpecifications(Collections.singletonList(productSpecification));

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

			// avoids rclDataItems due to invalid status
			for (CompoListDataItem compo : compoList) {
				ProductData product = alfrescoRepository.findOne(compo.getProduct());
				product.setState(SystemState.Valid);
				alfrescoRepository.save(product);

			}
			finishedProduct.getCompoListView().setCompoList(compoList);

			finishedProduct = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);

			// putting allergens in RMs
			ProductData rm1 = alfrescoRepository.findOne(rawMaterial1NodeRef);
			if (rm1.getAllergenList() != null) {
				rm1.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen1, false));
			}

			ProductData rm2 = alfrescoRepository.findOne(rawMaterial2NodeRef);
			if (rm2.getAllergenList() != null) {
				rm2.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen1, false));
				rm2.getAllergenList().add(new AllergenListDataItem(null, null, false, true, new ArrayList<>(), new ArrayList<>(), allergen2, false));
			}

			ProductData rm3 = alfrescoRepository.findOne(rawMaterial3NodeRef);
			if (rm3.getAllergenList() != null) {
				rm3.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen1, false));
				rm3.getAllergenList().add(new AllergenListDataItem(null, null, true, true, new ArrayList<>(), new ArrayList<>(), allergen2, false));
			}

			ProductData rm4 = alfrescoRepository.findOne(rawMaterial4NodeRef);
			if (rm4.getAllergenList() != null) {
				rm4.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen2, false));
			}

			ProductData rm11 = alfrescoRepository.findOne(rawMaterial11NodeRef);
			if (rm11.getAllergenList() != null) {
				rm11.getAllergenList().add(new AllergenListDataItem(null, null, true, false, new ArrayList<>(), new ArrayList<>(), allergen1, false));
			}

			// Binding specifications
			nodeService.createAssociation(productSpecificationNodeRef1, productSpecificationNodeRef3, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(productSpecificationNodeRef1, productSpecificationNodeRef2, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);
			nodeService.createAssociation(finishedProduct.getNodeRef(), productSpecificationNodeRef1, PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			return finishedProduct.getNodeRef();
		}, false, true);
	}

	public void formulate() {

		logger.info("testAllergensFormulation");

		final NodeRef finishedProductNodeRef = createFullProductNodeRef("Allergen formulation test 1");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = alfrescoRepository.findOne(finishedProductNodeRef);

			checkProduct(formulatedProduct);

			return null;

		}, false, true);

	}

}

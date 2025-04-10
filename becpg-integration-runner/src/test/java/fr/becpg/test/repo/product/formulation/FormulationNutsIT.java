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
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.formulation.FormulationChain;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.NutRequirementType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.ReqCtrlListDataItem;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;
import fr.becpg.repo.product.formulation.nutrient.NutrientCode;
import fr.becpg.repo.product.formulation.nutrient.RegulationFormulationHelper;
import fr.becpg.repo.product.requirement.NutsRequirementScanner;
import fr.becpg.repo.product.requirement.SimpleListRequirementScanner;
import fr.becpg.repo.system.SystemConfigurationService;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationNutsIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationNutsIT.class);

	@Autowired
	@Qualifier("productFormulationChain")
	private FormulationChain<ProductData> formulationChain;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	@Test
	public void testNutsFormulation() {

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

				assertEquals(RegulationFormulationHelper.extractValuePerServing(nutListDataItem.getRoundedValue(), Locale.FRENCH),
						(6d * 50d) / 100);

				RegulationFormulationHelper.extractRoundedValue(formulatedProduct, NutrientCode.Fat, nutListDataItem);
				assertEquals(4d, RegulationFormulationHelper.extractGDAPerc(nutListDataItem.getRoundedValue(), "ID"));
				assertEquals(4d, RegulationFormulationHelper.extractGDAPerc(nutListDataItem.getRoundedValue(), "MX"));

				checks++;
			}
			if (nutListDataItem.getNut().equals(nut3)) {
				assertEquals("nut3.getValue() == 14, actual values: " + trace, 14d, nutListDataItem.getValue());
				checks++;
			}
			if (nutListDataItem.getNut().equals(nut4)) {
				assertEquals("nut4.getValue() == 1.5d, actual values: " + trace, 1.5d, nutListDataItem.getValue());

				assertEquals(RegulationFormulationHelper.extractValue(nutListDataItem.getRoundedValue(), Locale.FRENCH), 2d);
				assertEquals(RegulationFormulationHelper.extractValue(nutListDataItem.getRoundedValue(), Locale.US), 2d);
				assertEquals(RegulationFormulationHelper.extractMini(nutListDataItem.getRoundedValue(), Locale.FRENCH), 0d);
				assertEquals(RegulationFormulationHelper.extractMaxi(nutListDataItem.getRoundedValue(), Locale.FRENCH), 1d);

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

		String message0 = I18NUtil.getMessage(NutsRequirementScanner.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut1, BeCPGModel.PROP_CHARACT_NAME), "3", "7<= ", "");
		String message1 = I18NUtil.getMessage(NutsRequirementScanner.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut2, BeCPGModel.PROP_CHARACT_NAME), "6", "7<= ", "");
		String message2 = I18NUtil.getMessage(NutsRequirementScanner.MESSAGE_NUT_NOT_IN_RANGE,
				nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME), "14", "", " <=10");
		String message4 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_MAXIMAL_DAILY_VALUE,
				nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));

		String message5 = I18NUtil.getMessage(NutsRequirementScanner.MESSAGE_NUT_NOT_IN_RANGE + ".AsPrepared",
				nodeService.getProperty(nut4, BeCPGModel.PROP_CHARACT_NAME),
				MLTextHelper.getI18NMessage(SimpleListRequirementScanner.MESSAGE_UNDEFINED_VALUE), "1,5<=", " <=10");
		String message6 = I18NUtil.getMessage(NutsRequirementScanner.MESSAGE_NUT_NOT_IN_RANGE + ".Serving",
				nodeService.getProperty(nut4, BeCPGModel.PROP_CHARACT_NAME), "14", "1,5<=", " <=10");
		String message7 = I18NUtil.getMessage(NutsRequirementScanner.MESSAGE_NUT_NOT_IN_RANGE + ".GdaPerc",
				nodeService.getProperty(nut4, BeCPGModel.PROP_CHARACT_NAME), "0.75", "1,5<=", " <=10");

		logger.info("Formulation raised " + formulatedProduct.getReqCtrlList().size() + " rclDataItems");
		for (ReqCtrlListDataItem r : formulatedProduct.getReqCtrlList()) {

			logger.info("reqCtrl " + r.getReqMessage() + r.getReqType() + r.getSources());

			if (message1.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message2.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message0.equals(r.getReqMessage())) {
				fail();
			} else if (message4.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message5.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message6.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			} else if (message7.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			}

		}

		assertEquals(3, checks);
	}

	protected NodeRef createFullProductNodeRef(final String name) {
		return inWriteTx(() -> {

			ProductSpecificationData productSpecification = ProductSpecificationData.build().withName(name + " Spec Nut 1")
					.withNutList(List.of(NutListDataItem.build().withMini(3d).withMaxi(4d).withNut(nut1),
							NutListDataItem.build().withMini(7d).withNut(nut2), NutListDataItem.build().withMaxi(10d).withNut(nut3),
							NutListDataItem.build().withMini(1.5d).withMaxi(10d).withNut(nut4),
							NutListDataItem.build().withMini(1.5d).withMaxi(10d).withNutRequirementType(NutRequirementType.AsPrepared).withNut(nut4),
							NutListDataItem.build().withMini(1.5d).withMaxi(10d).withNutRequirementType(NutRequirementType.Serving).withNut(nut4),
							NutListDataItem.build().withMini(1.5d).withMaxi(10d).withNutRequirementType(NutRequirementType.GdaPerc).withNut(nut4)

					));

			productSpecification = (ProductSpecificationData) alfrescoRepository.create(getTestFolderNodeRef(), productSpecification);

			List<CompoListDataItem> compoList = new ArrayList<>();

			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
					.withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial1NodeRef).withParent(compoList.get(0)));
			compoList.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
					.withProduct(rawMaterial2NodeRef).withParent(compoList.get(0)));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail)
					.withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare)
					.withProduct(rawMaterial3NodeRef).withParent(compoList.get(3)));
			compoList.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Omit)
					.withProduct(rawMaterial4NodeRef).withParent(compoList.get(3)));

			FinishedProductData finishedProduct = FinishedProductData.build().withName(name).withLegalName("Legal " + name).withUnit(ProductUnit.kg)
					.withQty(2d).withUnitPrice(22.4d).withDensity(1d).withServingSize(50d).withProjectedQty(10000L).withCompoList(compoList)
					.withNutList(List.of(NutListDataItem.build().withNut(nut1), NutListDataItem.build().withNut(nut2),
							NutListDataItem.build().withNut(nut3)));

			finishedProduct = (FinishedProductData) alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct);

			nodeService.createAssociation(finishedProduct.getNodeRef(), productSpecification.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS);

			return finishedProduct.getNodeRef();
		});
	}

	public void formulate(boolean propagate) {

		logger.info("testNutsFormulation propagate - " + propagate);

		try {

			if (propagate) {
				inWriteTx(() -> {
					systemConfigurationService.updateConfValue("beCPG.formulation.nutList.propagateUpEnable", "true");
					return null;
				});
			}

			final NodeRef finishedProductNodeRef = createFullProductNodeRef("Nut formulation test 1 - " + propagate);

			inWriteTx(() -> {

				/*-- Formulate product --*/
				logger.info("/*-- Formulate product --*/");
				productService.formulate(finishedProductNodeRef);

				/*-- Verify formulation --*/
				logger.info("/*-- Verify formulation --*/");
				ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

				checkProduct(formulatedProduct, propagate);

				return null;

			});

		} finally {
			if (propagate) {
				inWriteTx(() -> {
					systemConfigurationService.resetConfValue("beCPG.formulation.nutList.propagateUpEnable");
					return null;
				});
			}
		}

	}

}

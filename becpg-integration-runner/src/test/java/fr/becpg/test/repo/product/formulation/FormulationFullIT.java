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

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.DynamicCharactListItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelClaimListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.formulation.NutsCalculatingFormulationHandler;
import fr.becpg.repo.regulatory.RequirementListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

public class FormulationFullIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(FormulationFullIT.class);

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
	}

	protected NodeRef createFullProductNodeRef(final String name) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.info("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName(toTestName(name));
			finishedProduct.setLegalName("Legal " + name);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setUnitPrice(22.4d);
			finishedProduct.setDensity(1d);
			finishedProduct.setServingSize(50d);// 50g
			finishedProduct.setProjectedQty(10000l);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(0)).withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Detail).withProduct(localSF2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			compoList.add(CompoListDataItem.build().withParent(compoList.get(3)).withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d)
					.withDeclarationType(DeclarationType.Omit).withProduct(rawMaterial4NodeRef));
			finishedProduct.getCompoListView().setCompoList(compoList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 4000d, "€", null, fixedCost, true));
			costList.add(new CostListDataItem(null, null, null, null, cost1, null));
			costList.add(new CostListDataItem(null, null, null, null, cost2, null));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNut(nut1));
			nutList.add(NutListDataItem.build().withNut(nut2));
			nutList.add(NutListDataItem.build().withNut(nut3));
			finishedProduct.setNutList(nutList);

			List<DynamicCharactListItem> dynamicCharactListItems = new ArrayList<>();
			// Product
			dynamicCharactListItems.add(new DynamicCharactListItem("Product qty 1", "qty"));
			// Literal formula
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 1", "'Hello World'"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 2", "6.0221415E+23"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 3", "1+1+10-(4/100)"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 4", "0x7dFFFFFF"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 5", "true"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Literal 6", "null"));
			// Properties formulae
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  1", "costList[0].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  1Bis", "costList[1].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  2", "costList[0].unit"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  3", "costList[0].value / costList[1].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Property  4", "profitability"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Collection Selection  1", "costList.?[value == 4.0][0].unit"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Collection Selection  2", "costList.?[value < 5.0][0].value"));
			dynamicCharactListItems.add(new DynamicCharactListItem("Collection Projection  1", "costList.![value]"));
			// Variables
			dynamicCharactListItems
					.add(new DynamicCharactListItem("Variable  1", "compoListView.dynamicCharactList.?[title == 'Property  1' ][0].value"));
			// Template need Template Context
			// dynamicCharactListItems.add(new
			// DynamicCharactListItem("Template 1","Cost1/Cost2 :
			// #{costList[1].value / costList[2].value}% Profitability :
			// #{profitability}"
			// ));
			// Elvis
			dynamicCharactListItems.add(new DynamicCharactListItem("Elvis  1", "null?:'Unknown'"));
			// Boolean
			dynamicCharactListItems.add(new DynamicCharactListItem("Boolean  1", "costList[1].value > 1"));
			// Assignment
			dynamicCharactListItems.add(new DynamicCharactListItem("Assignement  1", "nutList.?[nut.toString() == '" + nut1 + "' ][0].value = 4d"));

			// Spel method
			dynamicCharactListItems.add(new DynamicCharactListItem(" beCPG findOne", "@beCPG.findOne(nodeRef).qty"));

			dynamicCharactListItems.add(new DynamicCharactListItem(" beCPG propValue", "@beCPG.propValue(nodeRef,'bcpg:bestBeforeDate')\n"));

			dynamicCharactListItems.add(new DynamicCharactListItem(" beCPG SUM ",
					"@beCPG.sum(compoListView.compoList.?[parent == null],\"entity.costList[0].value + dataListItem.qty\")"));

			dynamicCharactListItems.add(new DynamicCharactListItem(" beCPG SUM++ ",
					"@beCPG.sum(compoListView.compoList.?[parent == null],\"@beCPG.propValue(dataListItem.nodeRef,'bcpg:compoListQtyPercForSF')\")"));

			// Static fonction
			dynamicCharactListItems.add(new DynamicCharactListItem("Autonum V1 ",
					"T(fr.becpg.repo.helper.AutoNumHelper).getAutoNumValue(\"bcpg:finishedProduct\",\"bcpg:eanCode\")"));
			dynamicCharactListItems.add(
					new DynamicCharactListItem("Autonum V2 ", "T(fr.becpg.repo.helper.AutoNumHelper).getOrCreateCode(nodeRef,\"bcpg:eanCode\")"));

			// Formulate twice
			dynamicCharactListItems.add(new DynamicCharactListItem("Formulate twice", "reformulateCount=1"));

			// Multi Lines
			dynamicCharactListItems.add(new DynamicCharactListItem("MTLine1", "var abc=10+30;\n#abc + 5;"));
			dynamicCharactListItems.add(new DynamicCharactListItem("MTLine2", "var b = costList[1].value;\n#abc + #b;"));
			dynamicCharactListItems.add(new DynamicCharactListItem("MTLine3", "#abc - #b;\n\n"));

			// DynamicColumn

			DynamicCharactListItem dynCol = new DynamicCharactListItem("Col Dyn 1", "entity.costList[0].value + dataListItem.qty");
			dynCol.setColumnName("bcpg_dynamicCharactColumn1");
			dynamicCharactListItems.add(dynCol);

			dynCol = new DynamicCharactListItem("Col Dyn 2",
					"dataListItem.parent!=null ? entity.costList[0].value + dataListItem.qty : sum(entity.compoListView.compoList.?[parent == #root.dataListItem],\"entity.costList[0].value + dataListItem.qty\" )");


			dynCol.setColumnName("bcpg_dynamicCharactColumn2");
			dynamicCharactListItems.add(dynCol);

			dynCol = new DynamicCharactListItem("Col Dyn 3", "entity.costList[0].value + dataListItem.qty");
			dynCol.setColumnName("bcpg_dynamicCharactColumn3");
			dynCol.setMultiLevelFormula(true);
			dynamicCharactListItems.add(dynCol);

			finishedProduct.getCompoListView().setDynamicCharactList(dynamicCharactListItems);

			// Claim List

			List<LabelClaimListDataItem> labelClaimListDataItems = new ArrayList<>();

			nodeService.setProperty(labelClaims.get(0), PLMModel.PROP_LABEL_CLAIM_FORMULA, "((nutList.?[nut.toString() == '" + nut1
					+ "'][0].value < 40 and unit != T(fr.becpg.repo.product.data.constraints.ProductUnit).L and unit != T(fr.becpg.repo.product.data.constraints.ProductUnit).mL )"
					+ " or (nutList.?[nut.toString() == '" + nut1
					+ "'][0].value < 20 and (unit == T(fr.becpg.repo.product.data.constraints.ProductUnit).L or unit== T(fr.becpg.repo.product.data.constraints.ProductUnit).mL )))"
					+ " and (nutList.?[nut.toString() == '" + nut1 + "'][0].value > 4 )");
			nodeService.setProperty(labelClaims.get(1), PLMModel.PROP_LABEL_CLAIM_FORMULA,
					"nutList.?[nut.toString() == '" + nut1 + "'][0].value <= 4");

			labelClaimListDataItems.add(new LabelClaimListDataItem(labelClaims.get(0), "Nutritionnelle", false));
			labelClaimListDataItems.add(new LabelClaimListDataItem(labelClaims.get(1), "Nutritionnelle", false));

			finishedProduct.setLabelClaimList(labelClaimListDataItems);

			List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

			labelingRuleList
					.add(LabelingRuleListDataItem.build().withName("Test").withFormula("render()").withLabelingRuleType(LabelingRuleType.Render));

			LabelingRuleListDataItem percRule = LabelingRuleListDataItem.build().withName("%").withFormula("{0} {1,number,0.#%}")
					.withLabelingRuleType(LabelingRuleType.Format);
			percRule.setComponents(Arrays.asList(ing2, ing3, ing4));
			labelingRuleList.add(percRule);
			labelingRuleList.add(LabelingRuleListDataItem.build().withName("Param1")
					.withFormula("detailsDefaultFormat = \"{0} {1,number,0.#%} ({2})\"").withLabelingRuleType(LabelingRuleType.Prefs));

			labelingRuleList
					.add(LabelingRuleListDataItem.build().withName("Langue").withFormula("fr,en").withLabelingRuleType(LabelingRuleType.Locale));

			finishedProduct.getLabelingListView().setLabelingRuleList(labelingRuleList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

		}, false, true);
	}

	/**
	 * Test formulate product.
	 *
	 * @throws Exception
	 *             the exception
	 */
	@Test
	public void testFormulationFull() throws Exception {

		logger.info("testFormulationFull");

		final NodeRef finishedProductNodeRef = createFullProductNodeRef("Produit fini 1");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			checkProduct(formulatedProduct);

			return null;

		}, false, true);

	}

	protected void checkProduct(ProductData formulatedProduct) {
		// costs
		int checks = 0;
		assertNotNull("CostList is null", formulatedProduct.getCostList());
		for (CostListDataItem costListDataItem : formulatedProduct.getCostList()) {
			String trace = "cost: " + nodeService.getProperty(costListDataItem.getCost(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
					+ costListDataItem.getValue() + " - unit: " + costListDataItem.getUnit();
			logger.info(trace);
			if (costListDataItem.getCost().equals(cost1)) {
				assertEquals("cost1.getValue() == 4.0, actual values: " + trace, 4.0d, costListDataItem.getValue());
				assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
				checks++;
			}
			if (costListDataItem.getCost().equals(cost2)) {
				assertEquals("cost1.getValue() == 6.0, actual values: " + trace, 6.0d, costListDataItem.getValue());
				assertEquals("cost1.getUnit() == €/kg, actual values: " + trace, "€/kg", costListDataItem.getUnit());
				checks++;
			}
			if (costListDataItem.getCost().equals(fixedCost)) {
				assertEquals("fixedCost.getValue() == 4000d, actual values: " + trace, 4000d, costListDataItem.getValue());
				assertEquals("fixedCost.getUnit() == €/kg, actual values: " + trace, "€", costListDataItem.getUnit());
				checks++;
			}
		}
		assertEquals(3, checks);

		// dynamicCharact
		assertNotNull("DynamicCharact is null", formulatedProduct.getCompoListView().getDynamicCharactList());
		for (DynamicCharactListItem dynamicCharactListItem : formulatedProduct.getCompoListView().getDynamicCharactList()) {
			String trace = "Dyn charact :" + dynamicCharactListItem.getTitle() + " value " + dynamicCharactListItem.getValue() + " "
					+ dynamicCharactListItem.getErrorLog();
			logger.info(trace);
			assertFalse("#Error".equals(dynamicCharactListItem.getValue()));

			if ("MTLine1".equals(dynamicCharactListItem.getTitle())) {
				if (dynamicCharactListItem.getValue() instanceof Integer) {
					assertEquals(((Integer) dynamicCharactListItem.getValue()).intValue(),45);
				}
			}

		}

		// profitability
		DecimalFormat df = new DecimalFormat("0.00");
		assertEquals("check unitPrice", 22.4d, formulatedProduct.getUnitPrice());
		assertEquals("check unitTotalCost", 20.4d, formulatedProduct.getUnitTotalCost());
		assertEquals("check profitability", df.format(8.9285d), df.format(formulatedProduct.getProfitability()));
		assertEquals("check breakEven", (Long) 2000L, formulatedProduct.getBreakEven());

		// nuts
		checks = 0;
		assertNotNull("NutList is null", formulatedProduct.getNutList());
		for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
			String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
					+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
			logger.info(trace);
			if (nutListDataItem.getNut().equals(nut1)) {
				assertNotSame("nut1.getValue() == 3, actual values: " + trace, 3d, nutListDataItem.getValue());
				assertEquals("nut1.getValue() == 4 (Formula), actual values: " + trace, 4d, nutListDataItem.getValue());
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
			assertEquals(NutsCalculatingFormulationHandler.NUT_FORMULATED, nutListDataItem.getMethod());
		}
		assertEquals(3, checks);

		// allergens
		checks = 0;
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
				// (1×20+2×10+3×15)÷2
				assertEquals(42.5d, allergenListDataItem.getQtyPerc());
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
				// (1×5+2×50+3×15)÷2
				assertEquals(75d, allergenListDataItem.getQtyPerc());
				checks++;
			}
			// allergen: allergen3 - voluntary: true - involuntary: true
			// - voluntary sources:Raw material 3, - involuntary
			// sources:Raw material 3,
			if (allergenListDataItem.getAllergen().equals(allergen3)) {
				assertEquals("allergen3.getVoluntary() == true, actual values: " + trace, true,
						Boolean.TRUE.equals(allergenListDataItem.getVoluntary()));
				assertEquals("allergen3.getInVoluntary() == true, actual values: " + trace, true,
						Boolean.TRUE.equals(allergenListDataItem.getInVoluntary()));
				assertEquals("allergen3.getVoluntarySources() contains Raw material 3, actual values: " + trace, true,
						allergenListDataItem.getVoluntarySources().contains(rawMaterial3NodeRef));
				assertEquals("allergen3.getInVoluntarySources() contains Raw material 3, actual values: " + trace, true,
						allergenListDataItem.getInVoluntarySources().contains(rawMaterial3NodeRef));
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

		// verify IngList
		// 1 * RM1 , ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ;
		// geo1|geo2 //
		// 2 * RM2 , ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ;
		// geo1|geo2 //
		// 3 * RM3 , ingList : // // 4 ing3 ; bio1|bio2 ; geo2
		// 3 * RM4 [OMIT] , ingList : // // 4 ing3 ; bio1|bio2 ; geo2
		checks = 0;
		assertNotNull("IngList is null", formulatedProduct.getIngList());
		for (IngListDataItem ingListDataItem : formulatedProduct.getIngList()) {

			String geoOriginsText = "";
			for (NodeRef geoOrigin : ingListDataItem.getGeoOrigin()) {
				geoOriginsText += nodeService.getProperty(geoOrigin, BeCPGModel.PROP_CHARACT_NAME) + ", ";
			}

			String bioOriginsText = "";
			for (NodeRef bioOrigin : ingListDataItem.getBioOrigin()) {
				bioOriginsText += nodeService.getProperty(bioOrigin, BeCPGModel.PROP_CHARACT_NAME) + ", ";
			}

			String trace = "ing: " + nodeService.getProperty(ingListDataItem.getIng(), BeCPGModel.PROP_CHARACT_NAME) + " - qty: "
					+ ingListDataItem.getQtyPerc() + " - geo origins: " + geoOriginsText + " - bio origins: " + bioOriginsText + " is gmo: "
					+ ingListDataItem.getIsGMO() + " is ionized: " + ingListDataItem.getIsIonized();
			logger.debug(trace);

			df = new DecimalFormat("0.000000");

			// ing: ing1 - qty: 13.88888888888889 - geo origins:
			// geoOrigin1, - bio origins: bioOrigin1, is gmo: true
			if (ingListDataItem.getIng().equals(ing1)) {
				assertEquals("ing1.getQtyPerc() == 13.88888888888889, actual values: " + trace, df.format(13.88888888888889),
						df.format(ingListDataItem.getQtyPerc()));
				assertEquals("ing1.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
				assertEquals("ing1.getGeoOrigin() doesn't contain geo2, actual values: " + trace, false,
						ingListDataItem.getGeoOrigin().contains(geoOrigin2));
				assertEquals("ing1.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));
				assertEquals("ing1.getBioOrigin() doesn't contain bio2, actual values: " + trace, false,
						ingListDataItem.getBioOrigin().contains(bioOrigin2));
				assertEquals("ing1.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.getIsGMO().booleanValue());
				assertEquals("ing1.getIsIonized().booleanValue() is false, actual values: " + trace, true,
						ingListDataItem.getIsIonized().booleanValue());
				checks++;
			}
			// ing2 - qty: 36.111111111111114 - geo origins: geoOrigin1,
			// geoOrigin2, - bio origins: bioOrigin1, bioOrigin2, is
			// gmo: false
			if (ingListDataItem.getIng().equals(ing2)) {
				assertEquals("ing2.getQtyPerc() == 36.111111111111114, actual values: " + trace, df.format(36.111111111111114),
						df.format(ingListDataItem.getQtyPerc()));
				assertEquals("ing2.getGeoOrigin() contains geo1, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin1));
				assertEquals("ing2.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
				assertEquals("ing2.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));
				assertEquals("ing2.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
				assertEquals(1, ingListDataItem.getGeoTransfo().size());
				assertTrue(ingListDataItem.getGeoTransfo().contains(geoOrigin2));
				assertEquals("ing2.getIsGMO() is false, actual values: " + trace, false, ingListDataItem.getIsGMO().booleanValue());
				assertEquals("ing2.getIsIonized().booleanValue() is false, actual values: " + trace, false,
						ingListDataItem.getIsIonized().booleanValue());
				checks++;
			}
			// ing3 - qty: 50 - geo origins: geoOrigin2, - bio origins:
			// bioOrigin1, bioOrigin2, is gmo: true
			if (ingListDataItem.getIng().equals(ing3)) {
				assertEquals("ing3.getQtyPerc() == 50, actual values: " + trace, df.format(50), df.format(ingListDataItem.getQtyPerc()));
				assertEquals("ing3.getGeoOrigin() doesn't contain geo1, actual values: " + trace, false,
						ingListDataItem.getGeoOrigin().contains(geoOrigin1));
				assertEquals("ing3.getGeoOrigin() contains geo2, actual values: " + trace, true, ingListDataItem.getGeoOrigin().contains(geoOrigin2));
				assertEquals("ing3.getBioOrigin() contains bio1, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin1));
				assertEquals("ing3.getBioOrigin() contains bio2, actual values: " + trace, true, ingListDataItem.getBioOrigin().contains(bioOrigin2));
				assertEquals("ing3.getIsGMO() is false, actual values: " + trace, true, ingListDataItem.getIsGMO().booleanValue());
				assertEquals("ing3.getIsIonized().booleanValue() is false, actual values: " + trace, true,
						ingListDataItem.getIsIonized().booleanValue());
				checks++;
			}
		}
		assertEquals(3, checks);

		// verify IngLabelingList
		checks = 0;
		assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());

		for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {

			logger.info("grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.FRENCH));
			logger.info("grp: " + illDataItem.getGrp() + " - labeling: " + illDataItem.getValue().getValue(Locale.ENGLISH));

			// Pâte 50 % (Legal Raw material 2 66,67 % (ing2 75,00 %,
			// ing1 25,00 %), ing2 22,22 %, ing1 11,11 %), Garniture 50
			// % (ing3 50,00 %)

			checkILL("Garniture french 50% (ing3 french 50%)",
					"Pâte french 50% (Legal Raw material 2 33,3% (ing2 french 25%, ing1 french), ing2 french 11,1%, ing1 french)",
					illDataItem.getValue().getValue(Locale.FRENCH));

			checkILL("Garniture english 50% (ing3 english 50%)",
					"Pâte english 50% (Legal Raw material 2 33.3% (ing2 english 25%, ing1 english), ing2 english 11.1%, ing1 english)",
					illDataItem.getValue().getValue(Locale.ENGLISH));

			checks++;

		}
		assertEquals(1, checks);

		// ReqCtrlList
		checks = 0;
		String message2 = I18NUtil.getMessage(NutsCalculatingFormulationHandler.MESSAGE_MAXIMAL_DAILY_VALUE,
				nodeService.getProperty(nut3, BeCPGModel.PROP_CHARACT_NAME));
		logger.info(formulatedProduct.getReqCtrlList().size());
		for (RequirementListDataItem r : formulatedProduct.getReqCtrlList()) {

			logger.info("reqCtrl " + r.getReqMessage() + r.getReqType() + r.getSources());

			if (message2.equals(r.getReqMessage())) {
				assertEquals(0, r.getSources().size());
				checks++;
			}
		}
		assertEquals(1, checks);

		assertEquals(nut3, formulatedProduct.getNutList().get(2).getNut());

		// Claim label list
		checks = 0;
		for (LabelClaimListDataItem labelClaimListDataItem : formulatedProduct.getLabelClaimList()) {

			logger.info(nodeService.getProperty(labelClaimListDataItem.getLabelClaim(), BeCPGModel.PROP_CHARACT_NAME) + " - Evaluate to : "
					+ labelClaimListDataItem.getIsClaimed());

			if (labelClaimListDataItem.getLabelClaim().equals(labelClaims.get(0)) && !labelClaimListDataItem.getIsClaimed()) {
				checks++;
			}
			if (labelClaimListDataItem.getLabelClaim().equals(labelClaims.get(1)) && labelClaimListDataItem.getIsClaimed()) {
				checks++;
			}
		}

		assertEquals(2, checks);

	}

	@Test
	public void testFormulationInML() throws Exception {

		logger.info("testFormulationInML");

		final NodeRef finishedProductNodeRef = createFullProductNodeRef("Produit fini 2");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			nodeService.setProperty(finishedProductNodeRef, PLMModel.PROP_PRODUCT_NET_VOLUME, 2.5d);
			nodeService.setProperty(finishedProductNodeRef, PLMModel.PROP_PRODUCT_SERVING_SIZE, 62.5);// 62,5mL
			nodeService.setProperty(finishedProductNodeRef, PLMModel.PROP_PRODUCT_SERVING_SIZE_UNIT, "mL");

			/*-- Formulate product --*/
			logger.info("/*-- Formulate product --*/");
			productService.formulate(finishedProductNodeRef);

			/*-- Verify formulation --*/
			logger.info("/*-- Verify formulation --*/");
			ProductData formulatedProduct = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);

			// nuts
			int checks = 0;
			DecimalFormat df = new DecimalFormat("0.00");
			assertNotNull("NutList is null", formulatedProduct.getNutList());
			for (NutListDataItem nutListDataItem : formulatedProduct.getNutList()) {
				String trace = "nut: " + nodeService.getProperty(nutListDataItem.getNut(), BeCPGModel.PROP_CHARACT_NAME) + " - value: "
						+ nutListDataItem.getValue() + " - unit: " + nutListDataItem.getUnit();
				logger.info(trace);
				if (nutListDataItem.getNut().equals(nut1)) {
					assertNotSame("nut1.getValue() == 2.4, actual values: " + trace, df.format(2.4), df.format(nutListDataItem.getValue()));
					assertEquals("nut1.getValue() == 4 (Formula), actual values: ", df.format(4.0), df.format(nutListDataItem.getValue()));
					assertEquals("nut1.getUnit() == kJ/100mL, actual values: " + trace, "kJ/100mL", nutListDataItem.getUnit());
					assertEquals("must be group1", GROUP1, nutListDataItem.getGroup());
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut2)) {
					assertEquals("nut2.getValue() == 4.8, actual values: ", df.format(4.8), df.format(nutListDataItem.getValue()));
					assertEquals("nut2.getUnit() == kcal/100mL, actual values: " + trace, "kcal/100mL", nutListDataItem.getUnit());
					assertEquals("must be group2", GROUP2, nutListDataItem.getGroup());
					assertEquals(df.format((4.8 * 62.5) / 100), df.format(nutListDataItem.getValuePerServing()));
					assertEquals(df.format((100 * nutListDataItem.getValuePerServing()) / 2000d), df.format(nutListDataItem.getGdaPerc()));
					checks++;
				}
				if (nutListDataItem.getNut().equals(nut3)) {
					assertEquals("nut3.getValue() == 11.2, actual values: " + trace, df.format(11.2), df.format(nutListDataItem.getValue()));

					checks++;
				}
				assertEquals(NutsCalculatingFormulationHandler.NUT_FORMULATED, nutListDataItem.getMethod());
			}
			assertEquals(3, checks);

			return null;

		}, false, true);

	}

}

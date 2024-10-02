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
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.GS1Model;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.meat.MeatContentData;
import fr.becpg.repo.product.data.meat.MeatType;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.test.repo.product.AbstractFinishedProductTest;

/**
 *
 * @author matthieu
 *
 */
public class MeatContentFormulationIT extends AbstractFinishedProductTest {

	protected static final Log logger = LogFactory.getLog(MeatContentFormulationIT.class);

	protected NodeRef mpBeef1;
	protected NodeRef mpPorc2;

	protected NodeRef protein;
	protected NodeRef collagen;
	protected NodeRef fat;

	protected NodeRef beef;
	protected NodeRef porc;
	protected NodeRef beefFat;
	protected NodeRef porcFat;

	/**
	 * Inits the parts.
	 */
	protected void initParts2() {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- characteristics --*/
			Map<QName, Serializable> properties = new HashMap<>();

			// Nuts
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "protein");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, "Protein legalName");
			properties.put(PLMModel.PROP_NUTUNIT, "g");
			properties.put(GS1Model.PROP_NUTRIENT_TYPE_CODE, "PRO-");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP1);
			protein = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "fat");
			properties.put(PLMModel.PROP_NUTUNIT, "g");
			properties.put(GS1Model.PROP_NUTRIENT_TYPE_CODE, "FAT");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			fat = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "collagen");
			properties.put(PLMModel.PROP_NUTUNIT, "mg");
			properties.put(GS1Model.PROP_NUTRIENT_TYPE_CODE, "COLG");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			collagen = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();

			// Ings
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "beef");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "beef default");
			mlName.addValue(Locale.ENGLISH, "beef english");
			mlName.addValue(Locale.FRENCH, "beef french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			beef = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "beefFat");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "beefFat default");
			mlName.addValue(Locale.ENGLISH, "beefFat english");
			mlName.addValue(Locale.FRENCH, "beefFat french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);

			beefFat = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();

			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "porc");
			mlName.addValue(Locale.ENGLISH, "porc english");
			mlName.addValue(Locale.FRENCH, "porc french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);

			porc = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();

			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "porc fat");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);

			porcFat = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();

			/*-- Create raw materials --*/
			logger.debug("/*-- Create raw materials --*/");
			/*-- Raw material 1 --*/
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("MP Beef 1");
			rawMaterial1.setDensity(1d);
			rawMaterial1.setMeatType(MeatType.Mammals.toString());
			MLText legalName = new MLText("Legal MP Beef 1");
			legalName.addValue(Locale.FRENCH, "Legal MP Beef 1");
			legalName.addValue(Locale.ENGLISH, "Legal MP Beef 1");
			rawMaterial1.setLegalName(legalName);

			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();

			nutList.add(NutListDataItem.build().withValue(90d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(fat).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(10d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(protein).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(0.5d).withUnit("mg/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(collagen).withIsManual(false)
);

			rawMaterial1.setNutList(nutList);

			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, false, false, false, beef, false));

			rawMaterial1.setIngList(ingList);

			mpBeef1 = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			/*-- Create raw materials --*/
			logger.debug("/*-- Create raw materials --*/");
			/*-- Raw material 2 --*/
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("MP Porc 2");
			rawMaterial2.setMeatType(MeatType.Porcines.toString());
			rawMaterial2.setDensity(1d);
			legalName = new MLText("Legal MP Porc 2");
			legalName.addValue(Locale.FRENCH, "Legal MP Porc 2");
			legalName.addValue(Locale.ENGLISH, "Legal MP Porc 2");
			rawMaterial2.setLegalName(legalName);

			// nutList
			nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(3.90d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(fat).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(20.270d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(protein).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(105d).withUnit("mg/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(collagen).withIsManual(false)
);

			rawMaterial2.setNutList(nutList);

			ingList = new ArrayList<>();
			ingList.add(new IngListDataItem(null, 100d, new ArrayList<>(), new ArrayList<>(), false, false, false, porc, false));

			rawMaterial2.setIngList(ingList);

			mpPorc2 = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

			return null;

		}, false, true);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		// create RM and lSF
		initParts();
		initParts2();
	}

	private NodeRef createTestProduct(final List<LabelingRuleListDataItem> labelingRuleList) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			SemiFinishedProductData semifinishedProduct1 = new SemiFinishedProductData();
			semifinishedProduct1.setName("Semi Finished product " + Calendar.getInstance().getTimeInMillis());

			List<CompoListDataItem> compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(100d).withUnit(ProductUnit.Perc).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(mpPorc2));
			semifinishedProduct1.getCompoListView().setCompoList(compoList1);

			/**
			 * Finished product 1
			 */
			logger.debug("/*************************************/");
			logger.debug("/*-- Create Finished product 1--*/");
			logger.debug("/*************************************/");
			FinishedProductData finishedProduct1 = new FinishedProductData();
			finishedProduct1.setName("Finished product " + Calendar.getInstance().getTimeInMillis());
			finishedProduct1.setLegalName("legal Finished product 1");
			finishedProduct1.setQty(5d);
			finishedProduct1.setUnit(ProductUnit.kg);
			finishedProduct1.setDensity(1d);
			compoList1 = new ArrayList<>();
			compoList1.add(CompoListDataItem.build().withQtyUsed(2d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(mpBeef1));
			compoList1.add(CompoListDataItem.build().withQtyUsed(3d).withUnit(ProductUnit.kg).withLossPerc(0d).withDeclarationType(DeclarationType.Declare).withProduct(alfrescoRepository.create(getTestFolderNodeRef(), semifinishedProduct1).getNodeRef()));

			finishedProduct1.getCompoListView().setCompoList(compoList1);

			finishedProduct1.getLabelingListView().setLabelingRuleList(labelingRuleList);

			return alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct1).getNodeRef();
		}, false, true);
	}

	@Test
	public void testMeatContentLabeling() throws Exception {

		final NodeRef finishedProductNodeRef1 = createTestProduct(null);

		// Declare
		List<LabelingRuleListDataItem> labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "", LabelingRuleType.ShowPerc, null, null));

		checkILL(finishedProductNodeRef1, labelingRuleList, "porc french 60%, beef french 40%", Locale.FRENCH);
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FinishedProductData finishedProductData = (FinishedProductData) alfrescoRepository.findOne(finishedProductNodeRef1);

			productService.formulate(finishedProductData);

			Assert.assertTrue(finishedProductData.getMeatContents().size() == 2);

			MeatContentData meatContentData = finishedProductData.meatContentByType(MeatType.Mammals.toString());

			Assert.assertTrue(meatContentData.getFatPerc() == 90d);
			Assert.assertTrue(meatContentData.getProteinPerc() == 10d);
			Assert.assertTrue(meatContentData.getCollagenPerc() == (0.5 / 1000d));

			return null;
		}, false, true);

		labelingRuleList = new ArrayList<>();

		labelingRuleList.add(new LabelingRuleListDataItem("Rendu", "render()", LabelingRuleType.Render));
		labelingRuleList.add(new LabelingRuleListDataItem("%", "", LabelingRuleType.ShowPerc, null, null));
		labelingRuleList.add(new LabelingRuleListDataItem("QUID Boeuf", MeatType.Mammals.toString(), LabelingRuleType.Group, Arrays.asList(beef),
				Arrays.asList(beefFat)));
		labelingRuleList.add(new LabelingRuleListDataItem("QUID porc", MeatType.Porcines.toString(), LabelingRuleType.Group, Arrays.asList(porc),
				Arrays.asList(porcFat)));

		checkILL(finishedProductNodeRef1, labelingRuleList, "porc french 60%, beefFat french 34,7%, beef french 5,3%", Locale.FRENCH);

	}

}

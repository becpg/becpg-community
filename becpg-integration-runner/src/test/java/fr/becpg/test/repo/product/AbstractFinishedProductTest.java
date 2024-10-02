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
package fr.becpg.test.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.PackagingKitData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.constraints.LabelingRuleType;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.constraints.TareUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.IngLabelingListDataItem;
import fr.becpg.repo.product.data.productList.IngListDataItem;
import fr.becpg.repo.product.data.productList.LabelingRuleListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PhysicoChemListDataItem;
import fr.becpg.test.PLMBaseTestCase;

public abstract class AbstractFinishedProductTest extends PLMBaseTestCase {

	protected static final Log logger = LogFactory.getLog(AbstractFinishedProductTest.class);

	/** The product service. */
	@Autowired
	protected ProductService productService;

	/** The GROU p1. */
	protected static final String GROUP1 = "Groupe 1";

	/** The GROU p2. */
	protected static final String GROUP2 = "Groupe 2";

	/** The GROUPOTHER. */
	protected static String GROUPOTHER = "Autre";

	protected static String PACKAGING_PRIMAIRE = "Primaire";
	protected static String PACKAGING_TERTIAIRE = "Tertiaire";

	protected static final String FLOAT_FORMAT = "0.0000";

	/** The local s f1 node ref. */
	private NodeRef localSF1NodeRef;

	/** The raw material1 node ref. */
	private NodeRef rawMaterial1NodeRef;

	/** The raw material2 node ref. */
	private NodeRef rawMaterial2NodeRef;

	/** The local s f2 node ref. */
	private NodeRef localSF2NodeRef;

	private NodeRef localSF3NodeRef;

	/** The raw material3 node ref. */
	private NodeRef rawMaterial3NodeRef;

	/** The raw material4 node ref. */
	private NodeRef rawMaterial4NodeRef;

	/** The raw material5 node ref. */
	private NodeRef rawMaterial5NodeRef;

	private NodeRef rawMaterial6NodeRef;

	private NodeRef rawMaterial7NodeRef;

	private NodeRef rawMaterial8NodeRef;

	private NodeRef localSF11NodeRef;

	private NodeRef rawMaterial11NodeRef;

	private NodeRef rawMaterial12NodeRef;
	private NodeRef localSF12NodeRef;
	private NodeRef rawMaterial13NodeRef;
	private NodeRef rawMaterial14NodeRef;
	private NodeRef rawMaterial15NodeRef;
	private NodeRef rawMaterial16NodeRef;

	private NodeRef rawMaterialWaterNodeRef;

	private NodeRef packagingMaterial1NodeRef;
	private NodeRef packagingMaterial2NodeRef;
	private NodeRef packagingMaterial3NodeRef;
	private NodeRef packagingMaterial4NodeRef;
	private NodeRef packagingMaterial5NodeRef;
	private NodeRef packagingMaterial6NodeRef;

	private NodeRef packagingKit1NodeRef;

	/** The cost1. */
	private NodeRef cost1;

	/** The cost2. */
	private NodeRef cost2;

	private NodeRef cost3;

	private NodeRef cost4;

	private NodeRef cost5;

	private NodeRef parentCost;

	private NodeRef fixedCost;

	private NodeRef pkgCost1;

	private NodeRef pkgCost2;

	/** The nut1. */
	private NodeRef nut1;

	/** The nut2. */
	private NodeRef nut2;

	/** The nut3. */
	private NodeRef nut3;

	/** The nut4. */
	private NodeRef nut4;

	/** The allergen1. */
	private NodeRef allergen1;

	/** The allergen2. */
	private NodeRef allergen2;

	/** The allergen3. */
	private NodeRef allergen3;

	/** The allergen4. */
	private NodeRef allergen4;

	/** The ing1. */
	private NodeRef ing1;

	/** The ing2. */
	private NodeRef ing2;

	/** The ing3. */
	private NodeRef ing3;

	/** The ing4. */
	private NodeRef ing4;

	private NodeRef ing5;

	private NodeRef ing6;

	private NodeRef ingWater;

	private NodeRef ingType1;

	private NodeRef ingType2;

	/** The bio origin1. */
	private NodeRef bioOrigin1;

	/** The bio origin2. */
	private NodeRef bioOrigin2;

	/** The geo origin1. */
	private NodeRef geoOrigin1;

	/** The geo origin2. */
	private NodeRef geoOrigin2;

	private NodeRef physicoChem1;

	private NodeRef physicoChem2;

	private NodeRef physicoChem3;

	private NodeRef physicoChem4;

	private NodeRef physicoChem5;

	private NodeRef physicoChem6;

	private NodeRef physicoChem7;

	private NodeRef physicoChem8;

	private NodeRef supplier1;

	private NodeRef supplier2;

	private NodeRef plant1;

	private NodeRef plant2;

	protected void initParts() {

	}

	protected void checkILL(String expectedStr1, String expectedStr2, String actualStr) {

		String expectedStr;

		if (actualStr.startsWith(expectedStr1)) {
			expectedStr = expectedStr1 + ", " + expectedStr2;
		} else {
			expectedStr = expectedStr2 + ", " + expectedStr1;
		}
		try {
			Assert.assertEquals("Incorrect label :" + expectedStr + "\n   - compare to " + actualStr, expectedStr, actualStr);
		} catch (Throwable e) {

			if (RetryingTransactionHelper.extractRetryCause(e) == null) {
				logger.error(e, e);
			}

			throw e;
		}
	}

	protected void checkILL(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String ill, Locale locale) {

		checkILL(productNodeRef, labelingRuleList, ill, locale, null);
	}

	protected void checkILL(final NodeRef productNodeRef, final List<LabelingRuleListDataItem> labelingRuleList, final String ill, Locale locale,
			final String ruleName) {

		logger.info("checkILL : " + ill + (ruleName != null ? " " + ruleName : ""));

		NodeRef grpNodeRef = null;

		for (LabelingRuleListDataItem rule : labelingRuleList) {
			if (rule.getLabelingRuleType().equals(LabelingRuleType.Render)) {
				rule.setNodeRef(new NodeRef("test", "becpg", UUID.randomUUID().toString()));
				if ((ruleName != null) && ruleName.equals(rule.getName())) {
					grpNodeRef = rule.getNodeRef();
				}
			}
		}

		ProductData formulatedProduct = inWriteTx(() -> {
			ProductData ret = alfrescoRepository.findOne(productNodeRef);
			if (labelingRuleList.stream().noneMatch(item -> "Pref 7".equals(item.getName()))) {
				labelingRuleList.add(LabelingRuleListDataItem.build().withName("Pref 7").withFormula("uncapitalizeLegalName = true")
						.withLabelingRuleType(LabelingRuleType.Prefs));
			}
			ret.getLabelingListView().getLabelingRuleList().clear();
			ret.getLabelingListView().getLabelingRuleList().addAll(labelingRuleList);

			productService.formulate(ret);

			alfrescoRepository.save(ret);

			return ret;
		});

		Assert.assertTrue(formulatedProduct.getLabelingListView().getLabelingRuleList().size() > 0);
		// verify IngLabelingList

		Assert.assertNotNull("IngLabelingList is null", formulatedProduct.getLabelingListView().getIngLabelingList());
		Assert.assertTrue(formulatedProduct.getLabelingListView().getIngLabelingList().size() > 0);

		for (IngLabelingListDataItem illDataItem : formulatedProduct.getLabelingListView().getIngLabelingList()) {
			if ((grpNodeRef == null) || grpNodeRef.equals(illDataItem.getGrp())) {

				String formulatedIll = illDataItem.getValue().getValue(locale);
				Assert.assertTrue(illDataItem.getAspects().contains(BeCPGModel.ASPECT_DETAILLABLE_LIST_ITEM));

				Assert.assertEquals("Incorrect label. Formulated :" + formulatedIll + "\n   - junit ref " + ill, ill, formulatedIll);
				Assert.assertNotNull(illDataItem.getLogValue());
			}
		}

	}

	protected NodeRef getCost1() {
		if (cost1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost1");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return cost1;
	}

	protected NodeRef getCost2() {
		if (cost2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost2");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return cost2;
	}

	protected NodeRef getCost3() {
		if (cost3 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost3");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return cost3;
	}

	protected NodeRef getCost4() {
		if (cost4 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost4");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return cost4;
	}

	protected NodeRef getCost5() {
		if (cost5 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost5");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return cost5;
	}

	protected NodeRef getParentCost() {
		if (parentCost == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "parentCost");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			parentCost = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return parentCost;
	}

	protected NodeRef getPkgCost1() {
		if (pkgCost1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "pkgCost1");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			pkgCost1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return pkgCost1;
	}

	protected NodeRef getPkgCost2() {
		if (pkgCost2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "pkgCost2");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			pkgCost2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return pkgCost2;
	}

	protected NodeRef getFixedCost() {
		if (fixedCost == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "fixedCost");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			properties.put(PLMModel.PROP_COSTFIXED, true);
			fixedCost = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
		}
		return fixedCost;
	}

	protected NodeRef getNut1() {
		if (nut1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut1");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, "Nut1 legalName");
			properties.put(PLMModel.PROP_NUTUNIT, "kJ");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP1);
			nut1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
		}
		return nut1;
	}

	protected NodeRef getNut2() {
		if (nut2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut2");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 2000d);
			properties.put(PLMModel.PROP_NUTUL, 2000d);
			nut2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
		}
		return nut2;
	}

	protected NodeRef getNut3() {
		if (nut3 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut3");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 5d);
			properties.put(PLMModel.PROP_NUTUL, 5d);
			nut3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
		}
		return nut3;
	}

	protected NodeRef getNut4() {
		if (nut4 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut4");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 5d);
			properties.put(PLMModel.PROP_NUTUL, 5d);
			nut4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
		}
		return nut4;
	}

	protected NodeRef getAllergen1() {
		if (allergen1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen1");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
		}
		return allergen1;
	}

	protected NodeRef getAllergen2() {
		if (allergen2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen2");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
		}
		return allergen2;
	}

	protected NodeRef getAllergen3() {
		if (allergen3 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen3");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
		}
		return allergen3;
	}

	protected NodeRef getAllergen4() {
		if (allergen4 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen4");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
		}
		return allergen4;
	}

	protected NodeRef getIngType1() {
		if (ingType1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Epaississant");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Epaississant default");
			mlName.addValue(Locale.ENGLISH, "Epaississant english");
			mlName.addValue(Locale.FRENCH, "Epaississant french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			MLText plmlName = new MLText();
			plmlName.addValue(I18NUtil.getContentLocaleLang(), "Epaississants default");
			plmlName.addValue(Locale.ENGLISH, "Epaississants english");
			plmlName.addValue(Locale.FRENCH, "Epaississants french");
			properties.put(PLMModel.PROP_PLURAL_LEGAL_NAME, plmlName);
			ingType1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		}
		return ingType1;
	}

	protected NodeRef getIngType2() {
		if (ingType2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Epices");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Epices default");
			mlName.addValue(Locale.ENGLISH, "Epices english");
			mlName.addValue(Locale.FRENCH, "Epices french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			properties.put(PLMModel.PROP_ING_TYPE_DEC_THRESHOLD, 20);
			ingType2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
		}
		return ingType2;
	}

	protected NodeRef getIng1() {
		if (ing1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing1");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing1 default");
			mlName.addValue(Locale.ENGLISH, "ing1 english");
			mlName.addValue(Locale.FRENCH, "ing1 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ing1;
	}

	protected NodeRef getIng2() {
		if (ing2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing2");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing2 default");
			mlName.addValue(Locale.ENGLISH, "ing2 english");
			mlName.addValue(Locale.FRENCH, "ing2 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);

			ing2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ing2;
	}

	protected NodeRef getIng3() {
		if (ing3 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing3");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing3 default");
			mlName.addValue(Locale.ENGLISH, "ing3 english");
			mlName.addValue(Locale.FRENCH, "ing3 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ing3;
	}

	protected NodeRef getIng4() {
		if (ing4 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing4");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing4 default");
			mlName.addValue(Locale.ENGLISH, "ing4 english");
			mlName.addValue(Locale.FRENCH, "ing4 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ing4;
	}

	protected NodeRef getIng5() {
		if (ing5 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing5");
			properties.put(PLMModel.PROP_ING_TYPE_V2, ingType1);
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing5 default");
			mlName.addValue(Locale.ENGLISH, "ing5 english");
			mlName.addValue(Locale.FRENCH, "ing5 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ing5;
	}

	protected NodeRef getIng6() {
		if (ing6 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing6");
			properties.put(PLMModel.PROP_ING_TYPE_V2, ingType2);
			properties.put(PLMModel.PROP_ING_CEECODE, "CEE6");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing6 default");
			mlName.addValue(Locale.ENGLISH, "ing6 english");
			mlName.addValue(Locale.FRENCH, "ing6 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing6 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ing6;
	}

	protected NodeRef getIngWater() {
		if (ingWater == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "water");
			MLText mlName = new MLText();
			mlName.addValue(Locale.ENGLISH, "water");
			mlName.addValue(Locale.FRENCH, "eau");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ingWater = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
		}
		return ingWater;
	}

	protected NodeRef getGeoOrigin1() {
		if (geoOrigin1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin1");
			properties.put(PLMModel.PROP_GEO_ORIGIN_ISOCODE, "FR");
			geoOrigin1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
		}
		return geoOrigin1;
	}

	protected NodeRef getGeoOrigin2() {
		if (geoOrigin2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin2");
			geoOrigin2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
		}
		return geoOrigin2;
	}

	protected NodeRef getBioOrigin1() {
		if (bioOrigin1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "bioOrigin1");
			bioOrigin1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_BIO_ORIGIN, properties).getChildRef();
		}
		return bioOrigin1;
	}

	protected NodeRef getBioOrigin2() {
		if (bioOrigin2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "bioOrigin2");
			bioOrigin2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_BIO_ORIGIN, properties).getChildRef();
		}
		return bioOrigin2;
	}

	protected NodeRef getPhysicoChem1() {
		if (physicoChem1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem1");
			physicoChem1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem1;
	}

	protected NodeRef getPhysicoChem2() {
		if (physicoChem2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem2");
			physicoChem2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem2;
	}

	protected NodeRef getPhysicoChem3() {
		if (physicoChem3 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem3");
			properties.put(PLMModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			physicoChem3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem3;
	}

	protected NodeRef getPhysicoChem4() {
		if (physicoChem4 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem4");
			properties.put(PLMModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			physicoChem4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem4;
	}

	protected NodeRef getPhysicoChem5() {
		if (physicoChem5 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem5");
			properties.put(PLMModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			properties.put(PLMModel.PROP_PHYSICO_CHEM_UNIT, "%");
			physicoChem5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem5;
	}

	protected NodeRef getPhysicoChem6() {
		if (physicoChem6 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem6");
			physicoChem6 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem6;
	}

	protected NodeRef getPhysicoChem7() {
		if (physicoChem7 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem7");
			physicoChem7 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem7;
	}

	protected NodeRef getPhysicoChem8() {
		if (physicoChem8 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem8");
			physicoChem8 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
		}
		return physicoChem8;
	}

	protected NodeRef getSupplier1() {
		if (supplier1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "supplier1");
			supplier1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_SUPPLIER, properties).getChildRef();
		}
		return supplier1;
	}

	protected NodeRef getSupplier2() {
		if (supplier2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "supplier2");
			supplier2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_SUPPLIER, properties).getChildRef();
		}
		return supplier2;
	}

	protected NodeRef getPlant1() {
		if (plant1 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "plant1");
			plant1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_PLANT,
					properties).getChildRef();
		}
		return plant1;
	}

	protected NodeRef getPlant2() {
		if (plant2 == null) {
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(ContentModel.PROP_NAME, "plant2");
			plant2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_PLANT,
					properties).getChildRef();
		}
		return plant2;
	}

	protected NodeRef getLocalSF1NodeRef() {
		if (localSF1NodeRef == null) {
			LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
			localSF1.setName("Local semi finished 1");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");
			localSF1.setLegalName(mlName);
			localSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF1).getNodeRef();
		}
		return localSF1NodeRef;
	}

	protected NodeRef getRawMaterial1NodeRef() {
		if (rawMaterial1NodeRef == null) {
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			rawMaterial1.setDensity(1d);
			MLText legalName = new MLText("Legal Raw material 1");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 1");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 1");
			rawMaterial1.setLegalName(legalName);
			rawMaterial1.setSuppliers(Collections.singletonList(getSupplier1()));
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 3d, "€/kg", 3.1d, getCost1(), false, null, 1.5d, 6d));
			costList.add(new CostListDataItem(null, 2d, "€/kg", 2.1d, getCost2(), false, null, 1d, 4d));
			rawMaterial1.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut1()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100g").withMini(1.5d).withMaxi(2.2d).withGroup("Groupe 1").withNut(getNut2()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(4d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut3()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(3d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut4()).withIsManual(false)
);

			rawMaterial1.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 20d, true, false, null, null, getAllergen1(), false));
			allergenList.add(new AllergenListDataItem(null, 5d, false, false, null, null, getAllergen2(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen3(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen4(), false));
			rawMaterial1.setAllergenList(allergenList);
			// ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());

			IngListDataItem rawMaterial1Ing1 = new IngListDataItem(null, 100 / 3d, geoOrigins, bioOrigins, false, false, false, getIng1(), false);
			rawMaterial1Ing1.setMini(25d);
			rawMaterial1Ing1.setMaxi(90d);
			ingList.add(rawMaterial1Ing1);

			geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());
			geoOrigins.add(getGeoOrigin2());
			List<NodeRef> geoTransfo = new ArrayList<>();
			geoTransfo.add(getGeoOrigin2());

			IngListDataItem rawMaterial1Ing2 = new IngListDataItem(null, null, 200 / 3d, geoOrigins, geoTransfo, bioOrigins, false, false, false,
					false, getIng2(), false);
			rawMaterial1Ing2.setMini(18d);
			rawMaterial1Ing2.setMaxi(80d);
			ingList.add(rawMaterial1Ing2);

			rawMaterial1.setIngList(ingList);
			// physicoChem
			List<PhysicoChemListDataItem> physicoChemList;
			physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 3d, "-", null, 3.1d, getPhysicoChem1()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, 2.1d, getPhysicoChem2()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, 2.1d, getPhysicoChem3()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", 1.5d, 2.2d, getPhysicoChem4()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 100d, "%", 100d, 100d, getPhysicoChem5()));
			rawMaterial1.setPhysicoChemList(physicoChemList);
			rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();
		}
		return rawMaterial1NodeRef;
	}

	protected NodeRef getRawMaterial2NodeRef() {
		if (rawMaterial2NodeRef == null) {
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			MLText legalName = new MLText("Legal Raw material 2");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 2");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 2");
			rawMaterial2.setLegalName(legalName);
			rawMaterial2.setDensity(1d);
			rawMaterial2.setSuppliers(Collections.singletonList(getSupplier2()));
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/kg", 2.1d, getCost1(), false, null, 0.5d, 2d));
			costList.add(new CostListDataItem(null, 2d, "€/kg", 2.2d, getCost2(), false, null, 1d, 4d));
			rawMaterial2.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(0.8d).withMaxi(1.1d).withGroup("Groupe 1").withNut(getNut1()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut2()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(6d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut3()).withIsManual(false)
);
			rawMaterial2.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 10d, true, false, null, null, getAllergen1(), false));
			allergenList.add(new AllergenListDataItem(null, 50d, false, true, null, null, getAllergen2(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen3(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen4(), false));
			rawMaterial2.setAllergenList(allergenList);
			// ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());

			IngListDataItem rawMaterial2Ing1 = new IngListDataItem(null, 100 / 4d, geoOrigins, bioOrigins, true, true, false, getIng1(), false);
			rawMaterial2Ing1.setMini(15d);
			rawMaterial2Ing1.setMaxi(75d);
			ingList.add(rawMaterial2Ing1);

			bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin2());
			geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());

			IngListDataItem rawMaterial2Ing2 = new IngListDataItem(null, 300 / 4d, geoOrigins, bioOrigins, false, false, false, getIng2(), false);
			rawMaterial2Ing2.setMini(25d);
			rawMaterial2Ing2.setMaxi(86d);
			ingList.add(rawMaterial2Ing2);

			// physicoChem
			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, 2.1d, getPhysicoChem1()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, 2.2d, getPhysicoChem2()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", 0.8d, 1.1d, getPhysicoChem3()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", 0.8d, 2.1d, getPhysicoChem4()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 100d, "%", 100d, 100d, getPhysicoChem5()));
			rawMaterial2.setPhysicoChemList(physicoChemList);
			rawMaterial2.setIngList(ingList);

			rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

		}
		return rawMaterial2NodeRef;
	}

	protected NodeRef getLocalSF2NodeRef() {
		if (localSF2NodeRef == null) {
			LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
			localSF2.setName("Local semi finished 2");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			localSF2.setLegalName(mlName);
			localSF2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF2).getNodeRef();
		}
		return localSF2NodeRef;
	}

	protected NodeRef getLocalSF3NodeRef() {
		if (localSF3NodeRef == null) {
			LocalSemiFinishedProductData localSF3 = new LocalSemiFinishedProductData();
			localSF3.setName("Local semi finished 3");
			localSF3.setLegalName("Legal Local semi finished 3");
			localSF3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF3).getNodeRef();
		}
		return localSF3NodeRef;
	}

	protected NodeRef getRawMaterial3NodeRef() {
		if (rawMaterial3NodeRef == null) {
			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setName("Raw material 3");
			MLText legalName = new MLText("Legal Raw material 3");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 3");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 3");
			rawMaterial3.setLegalName(legalName);
			rawMaterial3.setDensity(1d);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/kg", null, getCost1(), false, null, 0.5d, 2d));
			costList.add(new CostListDataItem(null, 2d, "€/kg", null, getCost2(), false, null, 1d, 4d));
			rawMaterial3.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(getNut1()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(getNut2()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(4d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut3()).withIsManual(false)
);
			rawMaterial3.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 15d, false, false, null, null, getAllergen1(), false));
			allergenList.add(new AllergenListDataItem(null, 15d, false, false, null, null, getAllergen2(), false));
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, getAllergen3(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen4(), false));
			rawMaterial3.setAllergenList(allergenList);
			// ingList : 4 ing3 ; bio1|bio2 ; geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			bioOrigins.add(getBioOrigin2());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, getIng3(), false));
			// physicoChem
			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, null, getPhysicoChem1()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, null, getPhysicoChem2()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, null, getPhysicoChem3()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, null, getPhysicoChem4()));
			physicoChemList.add(new PhysicoChemListDataItem(null, 99d, "%", 98d, 100d, getPhysicoChem5()));
			rawMaterial3.setPhysicoChemList(physicoChemList);
			rawMaterial3.setIngList(ingList);
			rawMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3).getNodeRef();
		}
		return rawMaterial3NodeRef;
	}

	protected NodeRef getRawMaterial4NodeRef() {
		if (rawMaterial4NodeRef == null) {
			RawMaterialData rawMaterial4 = new RawMaterialData();
			rawMaterial4.setName("Raw material 4");
			MLText legalName = new MLText("Legal Raw material 4");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 4");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 4");
			rawMaterial4.setLegalName(legalName);
			rawMaterial4.setDensity(1.1d);
			// ingList : 4 ing3 ; bio1|bio2 ; geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			bioOrigins.add(getBioOrigin2());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, getIng3(), false));
			rawMaterial4.setIngList(ingList);
			rawMaterial4.setCostList(new LinkedList<CostListDataItem>());
			rawMaterial4.setNutList(new LinkedList<NutListDataItem>());
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(0d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(getNut1()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(0d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(getNut2()).withIsManual(false)
);
			rawMaterial4.setNutList(nutList);
			rawMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial4).getNodeRef();
		}
		return rawMaterial4NodeRef;
	}

	protected NodeRef getRawMaterial5NodeRef() {
		if (rawMaterial5NodeRef == null) {
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			bioOrigins.add(getBioOrigin2());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());

			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, getIng3(), false));

			RawMaterialData rawMaterial5 = new RawMaterialData();
			rawMaterial5.setName("Raw material 5");
			MLText legalName = new MLText("Legal Raw material 5");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 5");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 5");
			rawMaterial5.setLegalName(legalName);
			rawMaterial5.setQty(0.1d);
			rawMaterial5.setUnit(ProductUnit.kg);
			rawMaterial5.setNetWeight(0.1d);
			rawMaterial5.setDensity(0.1d);
			rawMaterial5.setTare(9d);
			rawMaterial5.setTareUnit(TareUnit.g);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 5d, "€/m", null, getCost1(), false));
			costList.add(new CostListDataItem(null, 6d, "€/m", null, getCost2(), false));
			rawMaterial5.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(getNut1()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(3d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(getNut2()).withIsManual(false)
);
			rawMaterial5.setNutList(nutList);
			rawMaterial5.setIngList(ingList);
			rawMaterial5NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial5).getNodeRef();
		}
		return rawMaterial5NodeRef;
	}

	protected NodeRef getRawMaterial6NodeRef() {
		if (rawMaterial6NodeRef == null) {
			RawMaterialData rawMaterial6 = new RawMaterialData();
			rawMaterial6.setName("Raw material 6");
			MLText legalName = new MLText("Legal Raw material 6");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 6");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 6");
			rawMaterial6.setLegalName(legalName);
			rawMaterial6.setUnit(ProductUnit.L);
			rawMaterial6.setDensity(0.7d);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/L", 2.1d, getCost1(), false));
			costList.add(new CostListDataItem(null, 2d, "€/L", 2.2d, getCost2(), false));
			rawMaterial6.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100mL").withMini(0.8d).withMaxi(1.1d).withGroup("Groupe 1").withNut(getNut1()).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100mL").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(getNut2()).withIsManual(false)
);
			rawMaterial6.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 100d, true, false, null, null, getAllergen1(), false));
			allergenList.add(new AllergenListDataItem(null, 100d, false, true, null, null, getAllergen2(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen3(), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, getAllergen4(), false));
			rawMaterial6.setAllergenList(allergenList);
			// ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());
			ingList.add(new IngListDataItem(null, 80d, geoOrigins, bioOrigins, true, true, false, getIng1(), false));
			bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin2());
			geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 20d, geoOrigins, bioOrigins, false, false, false, getIng2(), false));
			rawMaterial6.setIngList(ingList);
			rawMaterial6NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial6).getNodeRef();
		}
		return rawMaterial6NodeRef;
	}

	protected NodeRef getRawMaterial7NodeRef() {
		if (rawMaterial7NodeRef == null) {
			RawMaterialData rawMaterial7 = new RawMaterialData();
			rawMaterial7.setName("Raw material 7");
			MLText legalName = new MLText("Legal Raw material 7");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 7");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 7");
			rawMaterial7.setLegalName(legalName);
			rawMaterial7.setUnit(ProductUnit.kg);
			rawMaterial7.setDensity(1d);
			// ingList : ing5

			List<IngListDataItem> ingList = new ArrayList<>();
			ingList.add(new IngListDataItem(null, 100d, null, null, false, false, false, getIng5(), false));
			ingList.add(new IngListDataItem(null, ingList.get(0), 70d, null, null, null, false, false, true, false, getIng1(), false));
			ingList.add(new IngListDataItem(null, ingList.get(0), 30d, null, null, null, false, false, false, false, getIng4(), false));
			rawMaterial7.setIngList(ingList);
			rawMaterial7NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial7).getNodeRef();
		}
		return rawMaterial7NodeRef;
	}

	protected NodeRef getRawMaterial8NodeRef() {
		if (rawMaterial8NodeRef == null) {
			RawMaterialData rawMaterial8 = new RawMaterialData();
			rawMaterial8.setName("Raw material 8");
			MLText legalName = new MLText("Legal Raw material 8");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 5");
			rawMaterial8.setLegalName(legalName);
			rawMaterial8.setUnit(ProductUnit.lb);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 5d, "€/lb", null, getCost1(), false));
			rawMaterial8.setCostList(costList);
			rawMaterial8NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial8).getNodeRef();
		}
		return rawMaterial8NodeRef;
	}

	protected NodeRef getLocalSF11NodeRef() {
		if (localSF11NodeRef == null) {
			LocalSemiFinishedProductData localSF11 = new LocalSemiFinishedProductData();
			localSF11.setName("Local semi finished 11");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");
			localSF11.setLegalName(mlName);
			localSF11NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF11).getNodeRef();
		}
		return localSF11NodeRef;
	}

	protected NodeRef getRawMaterial11NodeRef() {
		if (rawMaterial11NodeRef == null) {
			RawMaterialData rawMaterial11 = new RawMaterialData();
			rawMaterial11.setName("Raw material 11");
			MLText legalName = new MLText("Legal Raw material 11");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 11");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 11");
			rawMaterial11.setLegalName(legalName);
			rawMaterial11.setDensity(1d);
			// ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());
			ingList.add(new IngListDataItem(null, 100 / 3d, geoOrigins, bioOrigins, false, false, false, getIng1(), false));
			geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 200 / 3d, geoOrigins, bioOrigins, false, false, false, getIng2(), false));
			rawMaterial11.setIngList(ingList);
			rawMaterial11NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial11).getNodeRef();
		}
		return rawMaterial11NodeRef;
	}

	protected NodeRef getRawMaterial12NodeRef() {
		if (rawMaterial12NodeRef == null) {
			RawMaterialData rawMaterial12 = new RawMaterialData();
			rawMaterial12.setName("Raw material 12");
			MLText legalName = new MLText("Legal Raw material 12");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 12");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 12");
			rawMaterial12.setLegalName(legalName);
			rawMaterial12.setDensity(1d);
			// ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin1());
			ingList.add(new IngListDataItem(null, 100 / 4d, geoOrigins, bioOrigins, true, true, false, getIng1(), false));
			bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin2());
			geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 300 / 4d, geoOrigins, bioOrigins, false, false, true, getIng2(), false));
			rawMaterial12.setIngList(ingList);
			rawMaterial12NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial12).getNodeRef();
		}
		return rawMaterial12NodeRef;
	}

	protected NodeRef getLocalSF12NodeRef() {
		if (localSF12NodeRef == null) {
			LocalSemiFinishedProductData localSF12 = new LocalSemiFinishedProductData();
			localSF12.setName("Local semi finished 12");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			localSF12.setLegalName(mlName);
			localSF12NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF12).getNodeRef();
		}
		return localSF12NodeRef;
	}

	protected NodeRef getRawMaterial13NodeRef() {
		if (rawMaterial13NodeRef == null) {
			RawMaterialData rawMaterial13 = new RawMaterialData();
			rawMaterial13.setName("Raw material 13");
			MLText legalName = new MLText("Legal Raw material 13");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 13");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 13");
			rawMaterial13.setLegalName(legalName);
			rawMaterial13.setDensity(1d);
			// ingList : 4 ing3 ; bio1|bio2 ; geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			bioOrigins.add(getBioOrigin2());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, getIng3(), false));
			rawMaterial13.setIngList(ingList);
			rawMaterial13NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial13).getNodeRef();
		}
		return rawMaterial13NodeRef;
	}

	protected NodeRef getRawMaterial14NodeRef() {
		if (rawMaterial14NodeRef == null) {
			RawMaterialData rawMaterial14 = new RawMaterialData();
			rawMaterial14.setName("Raw material 14");
			MLText legalName = new MLText("Legal Raw material 14");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 14");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 14");
			rawMaterial14.setLegalName(legalName);
			rawMaterial14.setDensity(1d);
			// ingList : 4 ing3 ; bio1|bio2 ; geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			bioOrigins.add(getBioOrigin2());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, 200 / 3d, geoOrigins, bioOrigins, true, true, false, getIng3(), false));
			ingList.add(new IngListDataItem(null, 100 / 3d, geoOrigins, bioOrigins, true, true, false, getIng4(), false));
			rawMaterial14.setIngList(ingList);
			rawMaterial14NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial14).getNodeRef();
		}
		return rawMaterial14NodeRef;
	}

	protected NodeRef getRawMaterial15NodeRef() {
		if (rawMaterial15NodeRef == null) {
			RawMaterialData rawMaterial15 = new RawMaterialData();
			rawMaterial15.setName("Raw material 15");
			MLText legalName = new MLText("Legal Raw material 15");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 15");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 15");
			rawMaterial15.setLegalName(legalName);
			rawMaterial15.setDensity(40d);
			rawMaterial15.setQty(50d);
			rawMaterial15.setUnit(ProductUnit.mL);
			rawMaterial15.setNetWeight(2d);
			rawMaterial15NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial15).getNodeRef();
		}
		return rawMaterial15NodeRef;
	}

	protected NodeRef getRawMaterial16NodeRef() {
		if (rawMaterial16NodeRef == null) {
			RawMaterialData rawMaterial16 = new RawMaterialData();
			rawMaterial16.setName("Raw material 16");
			MLText legalName = new MLText("Legal Raw material 16");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 16");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 16");
			rawMaterial16.setLegalName(legalName);
			rawMaterial16.setDensity(1d);
			// ingList : 4 ing3 ; bio1|bio2 ; geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(getBioOrigin1());
			bioOrigins.add(getBioOrigin2());
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(getGeoOrigin2());
			ingList.add(new IngListDataItem(null, null, geoOrigins, bioOrigins, true, true, false, getIng1(), false));
			ingList.add(new IngListDataItem(null, 55d, geoOrigins, bioOrigins, true, true, false, getIng3(), false));
			ingList.add(new IngListDataItem(null, null, geoOrigins, bioOrigins, true, true, false, getIng2(), false));
			rawMaterial16.setIngList(ingList);
			rawMaterial16NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial16).getNodeRef();
		}
		return rawMaterial16NodeRef;
	}

	protected NodeRef getRawMaterialWaterNodeRef() {
		if (rawMaterialWaterNodeRef == null) {
			RawMaterialData waterRawMaterial = new RawMaterialData();
			waterRawMaterial.setName("Water");
			MLText legalName = new MLText();
			legalName.addValue(Locale.FRENCH, "eau");
			legalName.addValue(Locale.ENGLISH, "water");
			waterRawMaterial.setLegalName(legalName);
			waterRawMaterial.setUnit(ProductUnit.kg);
			waterRawMaterial.setDensity(1d);
			// ingList : ing5

			List<IngListDataItem> ingList = new ArrayList<>();
			ingList.add(new IngListDataItem(null, 100d, null, null, false, false, false, getIngWater(), false));
			waterRawMaterial.setIngList(ingList);
			rawMaterialWaterNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), waterRawMaterial).getNodeRef();
			nodeService.addAspect(getRawMaterialWaterNodeRef(), PLMModel.ASPECT_WATER, null);
		}
		return rawMaterialWaterNodeRef;
	}

	protected NodeRef getPackagingMaterial1NodeRef() {
		if (packagingMaterial1NodeRef == null) {
			PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
			packagingMaterial1.setName("Packaging material 1");
			packagingMaterial1.setLegalName("Legal Packaging material 1");
			packagingMaterial1.setTare(0.015d);
			packagingMaterial1.setTareUnit(TareUnit.kg);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 3d, "€/P", null, getPkgCost1(), false));
			costList.add(new CostListDataItem(null, 2d, "€/P", null, getPkgCost2(), false));
			packagingMaterial1.setCostList(costList);
			packagingMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial1).getNodeRef();

		}
		return packagingMaterial1NodeRef;
	}

	protected NodeRef getPackagingMaterial2NodeRef() {
		if (packagingMaterial2NodeRef == null) {
			PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
			packagingMaterial2.setName("Packaging material 2");
			packagingMaterial2.setLegalName("Legal Packaging material 2");
			packagingMaterial2.setTare(5d);
			packagingMaterial2.setTareUnit(TareUnit.g);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/m", null, getPkgCost1(), false));
			costList.add(new CostListDataItem(null, 2d, "€/m", null, getPkgCost2(), false));
			packagingMaterial2.setCostList(costList);
			packagingMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial2).getNodeRef();

		}
		return packagingMaterial2NodeRef;
	}

	protected NodeRef getPackagingMaterial3NodeRef() {
		if (packagingMaterial3NodeRef == null) {
			PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
			packagingMaterial3.setName("Packaging material 3");
			packagingMaterial3.setLegalName("Legal Packaging material 3");
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/P", null, getPkgCost1(), false));
			costList.add(new CostListDataItem(null, 2d, "€/P", null, getPkgCost2(), false));
			packagingMaterial3.setCostList(costList);
			packagingMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial3).getNodeRef();
		}
		return packagingMaterial3NodeRef;
	}

	protected NodeRef getPackagingMaterial4NodeRef() {
		if (packagingMaterial4NodeRef == null) {
			PackagingMaterialData packagingMaterial4 = new PackagingMaterialData();
			packagingMaterial4.setName("Packaging material 4");
			packagingMaterial4.setLegalName("Legal Packaging material 4");
			packagingMaterial4.setTare(0.110231d); // 50g
			packagingMaterial4.setTareUnit(TareUnit.lb);
			packagingMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial4).getNodeRef();
		}
		return packagingMaterial4NodeRef;
	}

	protected NodeRef getPackagingMaterial5NodeRef() {
		if (packagingMaterial5NodeRef == null) {
			PackagingMaterialData packagingMaterial5 = new PackagingMaterialData();
			packagingMaterial5.setName("Packaging material 5");
			packagingMaterial5.setLegalName("Legal Packaging material 5");
			packagingMaterial5.setTare(1.410958478d); // 40g
			packagingMaterial5.setTareUnit(TareUnit.oz);
			packagingMaterial5NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial5).getNodeRef();
		}
		return packagingMaterial5NodeRef;
	}

	protected NodeRef getPackagingMaterial6NodeRef() {
		if (packagingMaterial6NodeRef == null) {
			PackagingMaterialData packagingMaterial6 = new PackagingMaterialData();
			packagingMaterial6.setName("Packaging material 6");
			packagingMaterial6.setLegalName("Legal Packaging material 6");
			packagingMaterial6.setUnit(ProductUnit.L);
			packagingMaterial6.setTare(1d);
			packagingMaterial6.setTareUnit(TareUnit.g);
			packagingMaterial6NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial6).getNodeRef();
		}
		return packagingMaterial6NodeRef;
	}

	protected NodeRef getPackagingKit1NodeRef() {
		if (packagingKit1NodeRef == null) {
			PackagingKitData packagingKit1 = new PackagingKitData();
			packagingKit1.setName("Packaging kit 1");
			packagingKit1.setLegalName("Legal Packaging kit 1");
			packagingKit1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingKit1).getNodeRef();
			nodeService.setProperty(getPackagingKit1NodeRef(), PackModel.PROP_PALLET_BOXES_PER_PALLET, 40);
			nodeService.setProperty(getPackagingKit1NodeRef(), PackModel.PROP_PALLET_NUMBER_ON_GROUND, 2);
		}
		return packagingKit1NodeRef;
	}

}

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
	protected NodeRef localSF1NodeRef;

	/** The raw material1 node ref. */
	protected NodeRef rawMaterial1NodeRef;

	/** The raw material2 node ref. */
	protected NodeRef rawMaterial2NodeRef;

	/** The local s f2 node ref. */
	protected NodeRef localSF2NodeRef;

	protected NodeRef localSF3NodeRef;

	/** The raw material3 node ref. */
	protected NodeRef rawMaterial3NodeRef;

	/** The raw material4 node ref. */
	protected NodeRef rawMaterial4NodeRef;

	/** The raw material5 node ref. */
	protected NodeRef rawMaterial5NodeRef;

	protected NodeRef rawMaterial6NodeRef;

	protected NodeRef rawMaterial7NodeRef;

	protected NodeRef rawMaterial8NodeRef;

	protected NodeRef localSF11NodeRef;

	protected NodeRef rawMaterial11NodeRef;

	protected NodeRef rawMaterial12NodeRef;
	protected NodeRef localSF12NodeRef;
	protected NodeRef rawMaterial13NodeRef;
	protected NodeRef rawMaterial14NodeRef;
	protected NodeRef rawMaterial15NodeRef;
	protected NodeRef rawMaterial16NodeRef;

	protected NodeRef rawMaterialWaterNodeRef;

	protected NodeRef packagingMaterial1NodeRef;
	protected NodeRef packagingMaterial2NodeRef;
	protected NodeRef packagingMaterial3NodeRef;
	protected NodeRef packagingMaterial4NodeRef;
	protected NodeRef packagingMaterial5NodeRef;
	protected NodeRef packagingMaterial6NodeRef;

	protected NodeRef packagingKit1NodeRef;

	/** The cost1. */
	protected NodeRef cost1;

	/** The cost2. */
	protected NodeRef cost2;

	protected NodeRef cost3;

	protected NodeRef cost4;

	protected NodeRef cost5;

	protected NodeRef parentCost;

	protected NodeRef fixedCost;

	protected NodeRef pkgCost1;

	protected NodeRef pkgCost2;

	/** The nut1. */
	protected NodeRef nut1;

	/** The nut2. */
	protected NodeRef nut2;

	/** The nut3. */
	protected NodeRef nut3;

	/** The nut4. */
	protected NodeRef nut4;

	/** The allergen1. */
	protected NodeRef allergen1;

	/** The allergen2. */
	protected NodeRef allergen2;

	/** The allergen3. */
	protected NodeRef allergen3;

	/** The allergen4. */
	protected NodeRef allergen4;

	/** The ing1. */
	protected NodeRef ing1;

	/** The ing2. */
	protected NodeRef ing2;

	/** The ing3. */
	protected NodeRef ing3;

	/** The ing4. */
	protected NodeRef ing4;

	protected NodeRef ing5;

	protected NodeRef ing6;

	protected NodeRef ingWater;

	protected NodeRef ingType1;

	protected NodeRef ingType2;

	/** The bio origin1. */
	protected NodeRef bioOrigin1;

	/** The bio origin2. */
	protected NodeRef bioOrigin2;

	/** The geo origin1. */
	protected NodeRef geoOrigin1;

	/** The geo origin2. */
	protected NodeRef geoOrigin2;

	protected NodeRef physicoChem1;

	protected NodeRef physicoChem2;

	protected NodeRef physicoChem3;

	protected NodeRef physicoChem4;

	protected NodeRef physicoChem5;

	protected NodeRef physicoChem6;

	protected NodeRef physicoChem7;

	protected NodeRef physicoChem8;

	protected NodeRef supplier1;

	protected NodeRef supplier2;

	protected NodeRef plant1;

	protected NodeRef plant2;

	/**
	 * Inits the parts.
	 */
	protected void initParts() {

		inWriteTx(() -> {

			/*-- characteristics --*/
			Map<QName, Serializable> properties = new HashMap<>();
			// Costs
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost1");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost2");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost3");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost4");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "cost5");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			cost5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "parentCost");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			parentCost = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "pkgCost1");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			pkgCost1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "pkgCost2");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			pkgCost2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "fixedCost");
			properties.put(PLMModel.PROP_COSTCURRENCY, "€");
			properties.put(PLMModel.PROP_COSTFIXED, true);
			fixedCost = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_COST, properties).getChildRef();

			// Nuts
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut1");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, "Nut1 legalName");
			properties.put(PLMModel.PROP_NUTUNIT, "kJ");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP1);
			nut1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut2");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 2000d);
			properties.put(PLMModel.PROP_NUTUL, 2000d);
			nut2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut3");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 5d);
			properties.put(PLMModel.PROP_NUTUL, 5d);
			nut3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();

			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut4");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			properties.put(PLMModel.PROP_NUTGROUP, GROUP2);
			properties.put(PLMModel.PROP_NUTGDA, 5d);
			properties.put(PLMModel.PROP_NUTUL, 5d);
			nut4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();

			// Allergens
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen1");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen2");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen3");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "allergen4");
			properties.put(PLMModel.PROP_ALLERGEN_TYPE, "Major");
			allergen4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ALLERGEN, properties).getChildRef();
			// Ings
			properties.clear();
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
			properties.clear();
			properties.put(BeCPGModel.PROP_LV_VALUE, "Epices");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Epices default");
			mlName.addValue(Locale.ENGLISH, "Epices english");
			mlName.addValue(Locale.FRENCH, "Epices french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			properties.put(PLMModel.PROP_ING_TYPE_DEC_THRESHOLD, 20);
			ingType2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_LV_VALUE)),
					PLMModel.TYPE_ING_TYPE_ITEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing1");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing1 default");
			mlName.addValue(Locale.ENGLISH, "ing1 english");
			mlName.addValue(Locale.FRENCH, "ing1 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing2");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing2 default");
			mlName.addValue(Locale.ENGLISH, "ing2 english");
			mlName.addValue(Locale.FRENCH, "ing2 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);

			ing2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing3");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing3 default");
			mlName.addValue(Locale.ENGLISH, "ing3 english");
			mlName.addValue(Locale.FRENCH, "ing3 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing4");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing4 default");
			mlName.addValue(Locale.ENGLISH, "ing4 english");
			mlName.addValue(Locale.FRENCH, "ing4 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing5");
			properties.put(PLMModel.PROP_ING_TYPE_V2, ingType1);
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing5 default");
			mlName.addValue(Locale.ENGLISH, "ing5 english");
			mlName.addValue(Locale.FRENCH, "ing5 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "ing6");
			properties.put(PLMModel.PROP_ING_TYPE_V2, ingType2);
			properties.put(PLMModel.PROP_ING_CEECODE, "CEE6");
			mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "ing6 default");
			mlName.addValue(Locale.ENGLISH, "ing6 english");
			mlName.addValue(Locale.FRENCH, "ing6 french");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ing6 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "water");
			mlName = new MLText();
			mlName.addValue(Locale.ENGLISH, "water");
			mlName.addValue(Locale.FRENCH, "eau");
			properties.put(BeCPGModel.PROP_LEGAL_NAME, mlName);
			ingWater = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_ING, properties).getChildRef();
			// Geo origins
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin1");
			geoOrigin1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			nodeService.setProperty(geoOrigin1, PLMModel.PROP_GEO_ORIGIN_ISOCODE, "FR");

			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "geoOrigin2");
			geoOrigin2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_GEO_ORIGIN, properties).getChildRef();
			// Bio origins
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "bioOrigin1");
			bioOrigin1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_BIO_ORIGIN, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "bioOrigin2");
			bioOrigin2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_BIO_ORIGIN, properties).getChildRef();
			// physicoChem
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem1");
			physicoChem1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem2");
			physicoChem2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem3");
			properties.put(PLMModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			physicoChem3 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem4");
			properties.put(PLMModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			physicoChem4 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem5");
			properties.put(PLMModel.PROP_PHYSICO_CHEM_FORMULATED, true);
			properties.put(PLMModel.PROP_PHYSICO_CHEM_UNIT, "%");
			physicoChem5 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem6");
			physicoChem6 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem7");
			physicoChem7 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();
			properties.clear();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "physicoChem8");
			physicoChem8 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_PHYSICO_CHEM, properties).getChildRef();

			// Suppliers
			properties.put(ContentModel.PROP_NAME, "supplier1");
			supplier1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_SUPPLIER, properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "supplier2");
			supplier2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)),
					PLMModel.TYPE_SUPPLIER, properties).getChildRef();

			// Plants
			properties.put(ContentModel.PROP_NAME, "plant1");
			plant1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_PLANT,
					properties).getChildRef();
			properties.clear();
			properties.put(ContentModel.PROP_NAME, "plant2");
			plant2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, (String) properties.get(ContentModel.PROP_NAME)), PLMModel.TYPE_PLANT,
					properties).getChildRef();
			return null;
		});

		inWriteTx(() -> {

			/*-- Create raw materials --*/
			logger.debug("/*-- Create raw materials --*/");
			/*-- Raw material 1 --*/
			RawMaterialData rawMaterial1 = new RawMaterialData();
			rawMaterial1.setName("Raw material 1");
			rawMaterial1.setDensity(1d);
			MLText legalName = new MLText("Legal Raw material 1");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 1");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 1");
			rawMaterial1.setLegalName(legalName);
			rawMaterial1.setSuppliers(Collections.singletonList(supplier1));
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 3d, "€/kg", 3.1d, cost1, false, null, 1.5d, 6d));
			costList.add(new CostListDataItem(null, 2d, "€/kg", 2.1d, cost2, false, null, 1d, 4d));
			rawMaterial1.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100g").withMini(1.5d).withMaxi(2.2d).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(4d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut3).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(3d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut4).withIsManual(false)
);

			rawMaterial1.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 20d, true, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, 5d, false, false, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen4, false));
			rawMaterial1.setAllergenList(allergenList);
			// ingList : 1 ing1 ; bio1 ; geo1 // 2 ing2 ; bio1 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);

			IngListDataItem rawMaterial1Ing1 = new IngListDataItem(null, 100 / 3d, geoOrigins, bioOrigins, false, false, false, ing1, false);
			rawMaterial1Ing1.setMini(25d);
			rawMaterial1Ing1.setMaxi(90d);
			ingList.add(rawMaterial1Ing1);

			geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			List<NodeRef> geoTransfo = new ArrayList<>();
			geoTransfo.add(geoOrigin2);

			IngListDataItem rawMaterial1Ing2 = new IngListDataItem(null, null, 200 / 3d, geoOrigins, geoTransfo, bioOrigins, false, false, false,
					false, ing2, false);
			rawMaterial1Ing2.setMini(18d);
			rawMaterial1Ing2.setMaxi(80d);
			ingList.add(rawMaterial1Ing2);

			rawMaterial1.setIngList(ingList);
			// physicoChem
			List<PhysicoChemListDataItem> physicoChemList;
			physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 3d, "-", null, 3.1d, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, 2.1d, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, 2.1d, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", 1.5d, 2.2d, physicoChem4));
			physicoChemList.add(new PhysicoChemListDataItem(null, 100d, "%", 100d, 100d, physicoChem5));
			rawMaterial1.setPhysicoChemList(physicoChemList);
			rawMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial1).getNodeRef();

			return null;
		});

		inWriteTx(() -> {

			/*-- Raw material 2 --*/
			RawMaterialData rawMaterial2 = new RawMaterialData();
			rawMaterial2.setName("Raw material 2");
			MLText legalName = new MLText("Legal Raw material 2");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 2");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 2");
			rawMaterial2.setLegalName(legalName);
			rawMaterial2.setDensity(1d);
			rawMaterial2.setSuppliers(Collections.singletonList(supplier2));
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/kg", 2.1d, cost1, false, null, 0.5d, 2d));
			costList.add(new CostListDataItem(null, 2d, "€/kg", 2.2d, cost2, false, null, 1d, 4d));
			rawMaterial2.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(0.8d).withMaxi(1.1d).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(6d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut3).withIsManual(false)
);
			rawMaterial2.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 10d, true, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, 50d, false, true, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen4, false));
			rawMaterial2.setAllergenList(allergenList);
			// ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);

			IngListDataItem rawMaterial2Ing1 = new IngListDataItem(null, 100 / 4d, geoOrigins, bioOrigins, true, true, false, ing1, false);
			rawMaterial2Ing1.setMini(15d);
			rawMaterial2Ing1.setMaxi(75d);
			ingList.add(rawMaterial2Ing1);

			bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);

			IngListDataItem rawMaterial2Ing2 = new IngListDataItem(null, 300 / 4d, geoOrigins, bioOrigins, false, false, false, ing2, false);
			rawMaterial2Ing2.setMini(25d);
			rawMaterial2Ing2.setMaxi(86d);
			ingList.add(rawMaterial2Ing2);

			// physicoChem
			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, 2.1d, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, 2.2d, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", 0.8d, 1.1d, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", 0.8d, 2.1d, physicoChem4));
			physicoChemList.add(new PhysicoChemListDataItem(null, 100d, "%", 100d, 100d, physicoChem5));
			rawMaterial2.setPhysicoChemList(physicoChemList);
			rawMaterial2.setIngList(ingList);

			rawMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial2).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 3 --*/
			RawMaterialData rawMaterial3 = new RawMaterialData();
			rawMaterial3.setName("Raw material 3");
			MLText legalName = new MLText("Legal Raw material 3");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 3");
			legalName.addValue(Locale.ENGLISH, "Legal Raw material 3");
			rawMaterial3.setLegalName(legalName);
			rawMaterial3.setDensity(1d);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/kg", null, cost1, false, null, 0.5d, 2d));
			costList.add(new CostListDataItem(null, 2d, "€/kg", null, cost2, false, null, 1d, 4d));
			rawMaterial3.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(4d).withUnit("g/100g").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut3).withIsManual(false)
);
			rawMaterial3.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 15d, false, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, 15d, false, false, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen4, false));
			rawMaterial3.setAllergenList(allergenList);
			// ingList : 4 ing3 ; bio1|bio2 ; geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, ing3, false));
			// physicoChem
			List<PhysicoChemListDataItem> physicoChemList = new ArrayList<>();
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, null, physicoChem1));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, null, physicoChem2));
			physicoChemList.add(new PhysicoChemListDataItem(null, 1d, "-", null, null, physicoChem3));
			physicoChemList.add(new PhysicoChemListDataItem(null, 2d, "-", null, null, physicoChem4));
			physicoChemList.add(new PhysicoChemListDataItem(null, 99d, "%", 98d, 100d, physicoChem5));
			rawMaterial3.setPhysicoChemList(physicoChemList);
			rawMaterial3.setIngList(ingList);
			rawMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial3).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 4 --*/
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
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, ing3, false));
			rawMaterial4.setIngList(ingList);
			rawMaterial4.setCostList(new LinkedList<CostListDataItem>());
			rawMaterial4.setNutList(new LinkedList<NutListDataItem>());
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(0d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(0d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			rawMaterial4.setNutList(nutList);
			rawMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial4).getNodeRef();
			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 5 --*/
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);

			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, ing3, false));

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
			costList.add(new CostListDataItem(null, 5d, "€/m", null, cost1, false));
			costList.add(new CostListDataItem(null, 6d, "€/m", null, cost2, false));
			rawMaterial5.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(3d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			rawMaterial5.setNutList(nutList);
			rawMaterial5.setIngList(ingList);
			rawMaterial5NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial5).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 8 --*/
			RawMaterialData rawMaterial8 = new RawMaterialData();
			rawMaterial8.setName("Raw material 8");
			MLText legalName = new MLText("Legal Raw material 8");
			legalName.addValue(Locale.FRENCH, "Legal Raw material 5");
			rawMaterial8.setLegalName(legalName);
			rawMaterial8.setUnit(ProductUnit.lb);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 5d, "€/lb", null, cost1, false));
			rawMaterial8.setCostList(costList);
			rawMaterial8NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial8).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 6 --*/
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
			costList.add(new CostListDataItem(null, 1d, "€/L", 2.1d, cost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/L", 2.2d, cost2, false));
			rawMaterial6.setCostList(costList);
			// nutList
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(1d).withUnit("g/100mL").withMini(0.8d).withMaxi(1.1d).withGroup("Groupe 1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withNodeRef(null).withValue(2d).withUnit("g/100mL").withMini(0.8d).withMaxi(2.1d).withGroup("Groupe 1").withNut(nut2).withIsManual(false)
);
			rawMaterial6.setNutList(nutList);
			// allergenList
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, 100d, true, false, null, null, allergen1, false));
			allergenList.add(new AllergenListDataItem(null, 100d, false, true, null, null, allergen2, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen3, false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergen4, false));
			rawMaterial6.setAllergenList(allergenList);
			// ingList : 1 ing1 ; bio1 ; geo1 // 3 ing2 ; bio2 ; geo1|geo2
			List<IngListDataItem> ingList = new ArrayList<>();
			List<NodeRef> bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);
			ingList.add(new IngListDataItem(null, 80d, geoOrigins, bioOrigins, true, true, false, ing1, false));
			bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 20d, geoOrigins, bioOrigins, false, false, false, ing2, false));
			rawMaterial6.setIngList(ingList);
			rawMaterial6NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial6).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 7 --*/
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
			ingList.add(new IngListDataItem(null, 100d, null, null, false, false, false, ing5, false));
			ingList.add(new IngListDataItem(null, ingList.get(0), 70d, null, null, null, false, false, true, false, ing1, false));
			ingList.add(new IngListDataItem(null, ingList.get(0), 30d, null, null, null, false, false, false, false, ing4, false));
			rawMaterial7.setIngList(ingList);
			rawMaterial7NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial7).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/** Water **/
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
			ingList.add(new IngListDataItem(null, 100d, null, null, false, false, false, ingWater, false));
			waterRawMaterial.setIngList(ingList);
			rawMaterialWaterNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), waterRawMaterial).getNodeRef();
			nodeService.addAspect(rawMaterialWaterNodeRef, PLMModel.ASPECT_WATER, null);

			return null;
		});

		inWriteTx(() -> {
			/*-- Local semi finished product 1 --*/
			LocalSemiFinishedProductData localSF1 = new LocalSemiFinishedProductData();
			localSF1.setName("Local semi finished 1");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");
			localSF1.setLegalName(mlName);
			localSF1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF1).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Local semi finished product 1 --*/
			LocalSemiFinishedProductData localSF2 = new LocalSemiFinishedProductData();
			localSF2.setName("Local semi finished 2");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			localSF2.setLegalName(mlName);
			localSF2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF2).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			LocalSemiFinishedProductData localSF3 = new LocalSemiFinishedProductData();
			localSF3.setName("Local semi finished 3");
			localSF3.setLegalName("Legal Local semi finished 3");
			localSF3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF3).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			logger.debug("/*-- Create raw materials 11 => 14 with ingList only--*/");
			/*-- Raw material 11 --*/
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
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);
			ingList.add(new IngListDataItem(null, 100 / 3d, geoOrigins, bioOrigins, false, false, false, ing1, false));
			geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 200 / 3d, geoOrigins, bioOrigins, false, false, false, ing2, false));
			rawMaterial11.setIngList(ingList);
			rawMaterial11NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial11).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 12 --*/
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
			bioOrigins.add(bioOrigin1);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin1);
			ingList.add(new IngListDataItem(null, 100 / 4d, geoOrigins, bioOrigins, true, true, false, ing1, false));
			bioOrigins = new ArrayList<>();
			bioOrigins.add(bioOrigin2);
			geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 300 / 4d, geoOrigins, bioOrigins, false, false, true, ing2, false));
			rawMaterial12.setIngList(ingList);
			rawMaterial12NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial12).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 13 --*/
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
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 100d, geoOrigins, bioOrigins, true, true, false, ing3, false));
			rawMaterial13.setIngList(ingList);
			rawMaterial13NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial13).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 14 --*/
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
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, 200 / 3d, geoOrigins, bioOrigins, true, true, false, ing3, false));
			ingList.add(new IngListDataItem(null, 100 / 3d, geoOrigins, bioOrigins, true, true, false, ing4, false));
			rawMaterial14.setIngList(ingList);
			rawMaterial14NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial14).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Local semi finished product 11 --*/
			LocalSemiFinishedProductData localSF11 = new LocalSemiFinishedProductData();
			localSF11.setName("Local semi finished 11");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Pâte default");
			mlName.addValue(Locale.ENGLISH, "Pâte english");
			mlName.addValue(Locale.FRENCH, "Pâte french");
			localSF11.setLegalName(mlName);
			localSF11NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF11).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Local semi finished product 12 --*/
			LocalSemiFinishedProductData localSF12 = new LocalSemiFinishedProductData();
			localSF12.setName("Local semi finished 12");
			MLText mlName = new MLText();
			mlName.addValue(I18NUtil.getContentLocaleLang(), "Garniture default");
			mlName.addValue(Locale.ENGLISH, "Garniture english");
			mlName.addValue(Locale.FRENCH, "Garniture french");
			localSF12.setLegalName(mlName);
			localSF12NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), localSF12).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 15 --*/
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

			return null;
		});

		inWriteTx(() -> {
			/*-- Raw material 16 --*/
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
			bioOrigins.add(bioOrigin1);
			bioOrigins.add(bioOrigin2);
			List<NodeRef> geoOrigins = new ArrayList<>();
			geoOrigins.add(geoOrigin2);
			ingList.add(new IngListDataItem(null, null, geoOrigins, bioOrigins, true, true, false, ing1, false));
			ingList.add(new IngListDataItem(null, 55d, geoOrigins, bioOrigins, true, true, false, ing3, false));
			ingList.add(new IngListDataItem(null, null, geoOrigins, bioOrigins, true, true, false, ing2, false));
			rawMaterial16.setIngList(ingList);
			rawMaterial16NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), rawMaterial16).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Packaging material 1 --*/
			PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
			packagingMaterial1.setName("Packaging material 1");
			packagingMaterial1.setLegalName("Legal Packaging material 1");
			packagingMaterial1.setTare(0.015d);
			packagingMaterial1.setTareUnit(TareUnit.kg);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 3d, "€/P", null, pkgCost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
			packagingMaterial1.setCostList(costList);
			packagingMaterial1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial1).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Packaging material 2 --*/
			PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
			packagingMaterial2.setName("Packaging material 2");
			packagingMaterial2.setLegalName("Legal Packaging material 2");
			packagingMaterial2.setTare(5d);
			packagingMaterial2.setTareUnit(TareUnit.g);
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/m", null, pkgCost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/m", null, pkgCost2, false));
			packagingMaterial2.setCostList(costList);
			packagingMaterial2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial2).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Packaging material 3 --*/
			PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
			packagingMaterial3.setName("Packaging material 3");
			packagingMaterial3.setLegalName("Legal Packaging material 3");
			// costList
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
			packagingMaterial3.setCostList(costList);
			packagingMaterial3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial3).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Packaging material 4 --*/
			PackagingMaterialData packagingMaterial4 = new PackagingMaterialData();
			packagingMaterial4.setName("Packaging material 4");
			packagingMaterial4.setLegalName("Legal Packaging material 4");
			packagingMaterial4.setTare(0.110231d); // 50g
			packagingMaterial4.setTareUnit(TareUnit.lb);
			packagingMaterial4NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial4).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			/*-- Packaging material 5 --*/
			PackagingMaterialData packagingMaterial5 = new PackagingMaterialData();
			packagingMaterial5.setName("Packaging material 5");
			packagingMaterial5.setLegalName("Legal Packaging material 5");
			packagingMaterial5.setTare(1.410958478d); // 40g
			packagingMaterial5.setTareUnit(TareUnit.oz);
			packagingMaterial5NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial5).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			PackagingMaterialData packagingMaterial6 = new PackagingMaterialData();
			packagingMaterial6.setName("Packaging material 6");
			packagingMaterial6.setLegalName("Legal Packaging material 6");
			packagingMaterial6.setUnit(ProductUnit.L);
			packagingMaterial6.setTare(1d);
			packagingMaterial6.setTareUnit(TareUnit.g);
			packagingMaterial6NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingMaterial6).getNodeRef();

			return null;
		});

		inWriteTx(() -> {
			PackagingKitData packagingKit1 = new PackagingKitData();
			packagingKit1.setName("Packaging kit 1");
			packagingKit1.setLegalName("Legal Packaging kit 1");
			packagingKit1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), packagingKit1).getNodeRef();
			nodeService.setProperty(packagingKit1NodeRef, PackModel.PROP_PALLET_BOXES_PER_PALLET, 40);
			nodeService.setProperty(packagingKit1NodeRef, PackModel.PROP_PALLET_NUMBER_ON_GROUND, 2);

			return null;
		});
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
			if (rule.getLabelingRuleType().equals(LabelingRuleType.Render) 
					&& !rule.getFormula().contains("renderAsHtmlTable")) {
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

}

/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import fr.becpg.model.PLMModel;
import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductSpecificationData;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.ResourceParamDataItem;
import fr.becpg.repo.product.report.ProductReportExtractorPlugin;
import fr.becpg.repo.report.entity.EntityReportData;

/**
 * The Class DefaultProductReportExtractorTest.
 *
 * @author querephi
 */
public class DefaultProductReportExtractorIT extends AbstractFinishedProductTest {

	
	private static final Log logger = LogFactory.getLog(DefaultProductReportExtractorIT.class);

	@Autowired
	@Qualifier("productReportExtractor")
	private ProductReportExtractorPlugin defaultProductReportExtractor;

	@Autowired
	private AssociationService associationService;

	@Override
	public void setUp() throws Exception {
		super.setUp();

		// create RM and lSF
		initParts();
	}

	@Test
	public void testReport() throws InterruptedException {

		org.apache.logging.log4j.core.config.Configurator.setLevel("fr.becpg.repo.report.entity.impl.DefaultEntityReportExtractor", org.apache.logging.log4j.Level.DEBUG);
		org.apache.logging.log4j.core.config.Configurator.setLevel("fr.becpg.repo.product.report.ProductReportExtractorPlugin", org.apache.logging.log4j.Level.DEBUG);
		org.apache.logging.log4j.core.config.Configurator.setLevel("fr.becpg.repo.audit.helper.StopWatchSupport", org.apache.logging.log4j.Level.DEBUG);

		logger.debug("testReport()");

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			/*-- Create finished product --*/
			logger.debug("/*-- Create finished product --*/");
			FinishedProductData finishedProduct = new FinishedProductData();
			finishedProduct.setName("Produit fini 1");
			finishedProduct.setLegalName("Legal Produit fini 1");
			finishedProduct.setHierarchy1(HIERARCHY1_FROZEN_REF);
			finishedProduct.setHierarchy2(HIERARCHY2_FISH_REF);
			finishedProduct.setUnit(ProductUnit.kg);
			finishedProduct.setQty(2d);
			finishedProduct.setNetWeight(2d);
			List<PackagingListDataItem> packagingList = new ArrayList<>();
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef));
			packagingList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.m).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial2NodeRef));
			packagingList.add(PackagingListDataItem.build().withQty(8d).withUnit(ProductUnit.PP).withPkgLevel(PackagingLevel.Tertiary).withIsMaster(true).withProduct(packagingMaterial3NodeRef));
			packagingList.add(PackagingListDataItem.build().withQty(2d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Secondary).withIsMaster(true).withProduct(packagingMaterial4NodeRef));
			finishedProduct.getPackagingListView().setPackagingList(packagingList);

			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
			costList.add(new CostListDataItem(null, 0.5d, "€/kg", null, cost1, false));
			costList.add(new CostListDataItem(null, 1.2d, "€/kg", null, cost2, false));
			finishedProduct.setCostList(costList);

			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(12d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Group1").withNut(nut1).withIsManual(false));
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Group1").withNut(nut2).withIsManual(false));
			nutList.add(NutListDataItem.build().withValue(15d).withUnit("g/100g").withMini(10d).withMaxi(20d).withGroup("Group2").withNut(nut3).withIsManual(false));
			nutList.add(NutListDataItem.build().withValue(5d).withUnit("mg/100g").withMini(2d).withMaxi(8d).withGroup("Group2").withNut(nut4).withIsManual(false));
			finishedProduct.setNutList(nutList);

			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(AllergenListDataItem.build().withQtyPerc(1.5d).withVoluntary(true).withInVoluntary(false).withAllergen(allergen1).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withQtyPerc(0.1d).withVoluntary(false).withInVoluntary(true).withAllergen(allergen2).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withQtyPerc(null).withVoluntary(false).withInVoluntary(false).withAllergen(allergen3).withIsManual(false));
			allergenList.add(AllergenListDataItem.build().withQtyPerc(null).withVoluntary(false).withInVoluntary(false).withAllergen(allergen4).withIsManual(false));
			finishedProduct.setAllergenList(allergenList);

			// Level 1: Semi-finished 1
			SemiFinishedProductData sf1 = new SemiFinishedProductData();
			sf1.setName("Standard SF 1");
			sf1.setLegalName("Legal SF 1");
			NodeRef sf1NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sf1).getNodeRef();

			// Level 2: Semi-finished 2
			SemiFinishedProductData sf2 = new SemiFinishedProductData();
			sf2.setName("Standard SF 2");
			sf2.setLegalName("Legal SF 2");
			NodeRef sf2NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sf2).getNodeRef();

			// Level 1: Semi-finished 3
			SemiFinishedProductData sf3 = new SemiFinishedProductData();
			sf3.setName("Standard SF 3");
			sf3.setLegalName("Legal SF 3");
			NodeRef sf3NodeRef = alfrescoRepository.create(getTestFolderNodeRef(), sf3).getNodeRef();

			List<CompoListDataItem> sf1Compo = new ArrayList<>();
			sf1Compo.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail).withProduct(sf2NodeRef));
			sf1Compo.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
			sf1.getCompoListView().setCompoList(sf1Compo);
			alfrescoRepository.save(sf1);

			List<CompoListDataItem> sf2Compo = new ArrayList<>();
			sf2Compo.add(CompoListDataItem.build().withQtyUsed(0.8d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			sf2Compo.add(CompoListDataItem.build().withQtyUsed(0.2d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
			sf2.getCompoListView().setCompoList(sf2Compo);
			alfrescoRepository.save(sf2);

			List<CompoListDataItem> sf3Compo = new ArrayList<>();
			sf3Compo.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial4NodeRef));
			sf3Compo.add(CompoListDataItem.build().withQtyUsed(0.5d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterialWaterNodeRef));
			sf3.getCompoListView().setCompoList(sf3Compo);
			alfrescoRepository.save(sf3);

			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withQtyUsed(1.0d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail).withProduct(sf1NodeRef));
			compoList.add(CompoListDataItem.build().withQtyUsed(1.0d).withUnit(ProductUnit.kg).withDeclarationType(DeclarationType.Detail).withProduct(sf3NodeRef));
			
			finishedProduct.getCompoListView().setCompoList(compoList);
			NodeRef finishedProductNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), finishedProduct).getNodeRef();

			// ProductSpecification
			ProductSpecificationData psd = new ProductSpecificationData();
			psd.setName("PSD");
			List<ResourceParamDataItem> resourceParams = new ArrayList<>();
			resourceParams.add(new ResourceParamDataItem("name", "title", "descr"));
			psd.setResourceParams(resourceParams);
			NodeRef psdNodeRef = alfrescoRepository.create(getTestFolderNodeRef(), psd).getNodeRef();

			// assoc is readonly
			ArrayList<NodeRef> psdNodeRefs = new ArrayList<>();
			psdNodeRefs.add(psdNodeRef);
			associationService.update(finishedProduct.getNodeRef(), PLMModel.ASSOC_PRODUCT_SPECIFICATIONS, psdNodeRefs);

			// add labelingTemplate aspect
			ProductData finishedProductData = (ProductData) alfrescoRepository.findOne(finishedProductNodeRef);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PackModel.PROP_LABELING_POSITION, "Côté de la boîte");
			nodeService.addAspect(finishedProductData.getPackagingList().get(0).getNodeRef(), PackModel.ASPECT_LABELING, properties);
			associationService.update(finishedProductData.getPackagingList().get(0).getNodeRef(), PackModel.ASSOC_LABELING_TEMPLATE,
					labelingTemplateNodeRef);

			nodeService.setProperty(finishedProductNodeRef, ContentModel.PROP_DESCRIPTION,
					"Descr line 1 " + System.getProperty("line.separator") + " descr line 2");

			productService.formulate(sf2NodeRef);
			productService.formulate(sf1NodeRef);
			productService.formulate(sf3NodeRef);
			productService.formulate(finishedProductNodeRef);

			Map<String, String> preferences = new HashMap<>();
			preferences.put("extractInMultiLevel", "true");
			preferences.put("maxCompoListLevelToExtract", "5");
			preferences.put("extractRawMaterial", "true");
			preferences.put("extractPriceBreaks", "true");
			preferences.put("extractWUsed", "true");
			preferences.put("componentDatalistsToExtract", "compoList,packagingList,processList,nutList,allergenList,costList");

			EntityReportData entityReportData = defaultProductReportExtractor.extract(finishedProductNodeRef, preferences);
			Assert.assertNotNull( entityReportData.getXmlDataSource());
			logger.info("XmlData : " + entityReportData.getXmlDataSource().asXML());

			return null;
		}, false, true);
	}
}

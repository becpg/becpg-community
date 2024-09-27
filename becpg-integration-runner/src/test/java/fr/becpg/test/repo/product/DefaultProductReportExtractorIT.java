/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
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
import fr.becpg.repo.product.data.constraints.DeclarationType;
import fr.becpg.repo.product.data.constraints.PackagingLevel;
import fr.becpg.repo.product.data.constraints.ProductUnit;
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
			packagingList.add(PackagingListDataItem.build().withQty(1d).withUnit(ProductUnit.P).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial1NodeRef)
);
			packagingList.add(PackagingListDataItem.build().withQty(3d).withUnit(ProductUnit.m).withPkgLevel(PackagingLevel.Primary).withIsMaster(true).withProduct(packagingMaterial2NodeRef)
);
			packagingList.add(PackagingListDataItem.build().withQty(8d).withUnit(ProductUnit.PP).withPkgLevel(PackagingLevel.Tertiary).withIsMaster(true).withProduct(packagingMaterial3NodeRef)
);
			finishedProduct.getPackagingListView().setPackagingList(packagingList);
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
			costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
			finishedProduct.setCostList(costList);
			List<NutListDataItem> nutList = new ArrayList<>();
			nutList.add(NutListDataItem.build().withValue(12d).withUnit("g/100g").withMini(0d).withMaxi(0d).withGroup("Group1").withNut(nut1).withIsManual(false)
);
			nutList.add(NutListDataItem.build().withValue(null).withUnit("g/100g").withMini(null).withMaxi(null).withGroup("Group1").withNut(nut2).withIsManual(false)
);
			finishedProduct.setNutList(nutList);
			List<CompoListDataItem> compoList = new ArrayList<>();
			compoList.add(CompoListDataItem.build().withParent(null).withQty(1d).withQtyUsed(1d).withUnit(ProductUnit.kg).withLossPerc(3d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial1NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(0.5d).withQtyUsed(0.5d).withUnit(ProductUnit.kg).withLossPerc(3d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial2NodeRef));
			compoList.add(CompoListDataItem.build().withParent(null).withQty(0.5d).withQtyUsed(0.5d).withUnit(ProductUnit.kg).withLossPerc(3d).withDeclarationType(DeclarationType.Declare).withProduct(rawMaterial3NodeRef));
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
			ProductData finishedProductData = alfrescoRepository.findOne(finishedProductNodeRef);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PackModel.PROP_LABELING_POSITION, "Côté de la boîte");
			nodeService.addAspect(finishedProductData.getPackagingList().get(0).getNodeRef(), PackModel.ASPECT_LABELING, properties);
			associationService.update(finishedProductData.getPackagingList().get(0).getNodeRef(), PackModel.ASSOC_LABELING_TEMPLATE,
					labelingTemplateNodeRef);

			nodeService.setProperty(finishedProductNodeRef, ContentModel.PROP_DESCRIPTION,
					"Descr line 1 " + System.getProperty("line.separator") + " descr line 2");

			productService.formulate(finishedProductNodeRef);

			EntityReportData entityReportData = defaultProductReportExtractor.extract(finishedProductNodeRef, new HashMap<>());
			Assert.assertNotNull( entityReportData.getXmlDataSource());
			logger.info("XmlData : " + entityReportData.getXmlDataSource().asXML());

			return null;
		}, false, true);
	}
}

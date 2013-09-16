/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PackModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.PackagingLevel;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListUnit;
import fr.becpg.repo.product.report.DefaultProductReportExtractor;
import fr.becpg.repo.report.entity.EntityReportData;

/**
 * The Class DefaultProductReportExtractorTest.
 * 
 * @author querephi
 */
public class DefaultProductReportExtractorTest extends AbstractFinishedProductTest {

	/** The logger. */
	private static Log logger = LogFactory.getLog(DefaultProductReportExtractorTest.class);

	@Resource
	private DefaultProductReportExtractor defaultProductReportExtractor; 

	@Resource
	private AssociationService associationService;
	
	@Override
	public void setUp() throws Exception {
		super.setUp();

		//create RM and lSF
 		initParts();
	}

	@Test
	public void testReport() throws InterruptedException {

		logger.debug("testReport()");
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@SuppressWarnings("unchecked")
			public NodeRef execute() throws Throwable {

				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
				packagingMaterial1.setName("Packaging material 1");
				packagingMaterial1.setLegalName("Legal Packaging material 1");
				//costList
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial1.setCostList(costList);					
				packagingMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial1).getNodeRef();
				
				/*-- Packaging material 2 --*/					
				PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
				packagingMaterial2.setName("Packaging material 2");
				packagingMaterial2.setLegalName("Legal Packaging material 2");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/m", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/m", null, pkgCost2, false));
				packagingMaterial2.setCostList(costList);					
				packagingMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial2).getNodeRef();
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
				packagingMaterial3.setName("Packaging material 3");
				packagingMaterial3.setLegalName("Legal Packaging material 3");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial3.setCostList(costList);					
				packagingMaterial3NodeRef = alfrescoRepository.create(testFolderNodeRef, packagingMaterial3).getNodeRef();
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
				packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PackagingLevel.Primary, true, packagingMaterial1NodeRef));
				packagingList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.m, PackagingLevel.Primary, true, packagingMaterial2NodeRef));
				packagingList.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PackagingLevel.Tertiary, true, packagingMaterial3NodeRef));
				finishedProduct.getPackagingListView().setPackagingList(packagingList);
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				finishedProduct.setCostList(costList);
				NodeRef finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();	
				
				// add labelingTemplate aspect
				ProductData finishedProductData = alfrescoRepository.findOne(finishedProductNodeRef);
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(PackModel.PROP_LABELING_POSITION, "Côté de la boîte");
				nodeService.addAspect(finishedProductData.getPackagingList().get(0).getNodeRef(), PackModel.ASPECT_LABELING, properties);				
				associationService.update(finishedProductData.getPackagingList().get(0).getNodeRef(), PackModel.ASSOC_LABELING_TEMPLATE, labelingTemplateNodeRef);
				
				nodeService.setProperty(finishedProductNodeRef, ContentModel.PROP_DESCRIPTION, "Descr line 1 " + System.getProperty("line.separator") + " descr line 2");
				
				EntityReportData entityReportData = defaultProductReportExtractor.extract(finishedProductNodeRef);
				logger.info("XmlData : " + entityReportData.getXmlDataSource().asXML());

				return null;
			}
		}, false, true);
	}
}

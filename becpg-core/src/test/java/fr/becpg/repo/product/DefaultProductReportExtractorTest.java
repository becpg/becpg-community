/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.PackagingMaterialData;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.data.productList.CostListDataItem;
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
			public NodeRef execute() throws Throwable {

				Collection<QName> dataLists = productDictionaryService.getDataLists();
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial1 = new PackagingMaterialData();
				packagingMaterial1.setName("Packaging material 1");
				packagingMaterial1.setLegalName("Legal Packaging material 1");
				//costList
				List<CostListDataItem> costList = new ArrayList<CostListDataItem>();
				costList.add(new CostListDataItem(null, 3d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial1.setCostList(costList);					
				packagingMaterial1NodeRef = productDAO.create(testFolderNodeRef, packagingMaterial1, dataLists);
				
				/*-- Packaging material 2 --*/					
				PackagingMaterialData packagingMaterial2 = new PackagingMaterialData();
				packagingMaterial2.setName("Packaging material 2");
				packagingMaterial2.setLegalName("Legal Packaging material 2");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/m", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/m", null, pkgCost2, false));
				packagingMaterial2.setCostList(costList);					
				packagingMaterial2NodeRef = productDAO.create(testFolderNodeRef, packagingMaterial2, dataLists);
				
				/*-- Packaging material 1 --*/					
				PackagingMaterialData packagingMaterial3 = new PackagingMaterialData();
				packagingMaterial3.setName("Packaging material 3");
				packagingMaterial3.setLegalName("Legal Packaging material 3");
				//costList
				costList.clear();
				costList.add(new CostListDataItem(null, 1d, "€/P", null, pkgCost1, false));
				costList.add(new CostListDataItem(null, 2d, "€/P", null, pkgCost2, false));
				packagingMaterial3.setCostList(costList);					
				packagingMaterial3NodeRef = productDAO.create(testFolderNodeRef, packagingMaterial3, dataLists);
				
				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Produit fini 1");
				finishedProduct.setLegalName("Legal Produit fini 1");
				finishedProduct.setUnit(ProductUnit.kg);
				finishedProduct.setQty(2d);
				List<PackagingListDataItem> packagingList = new ArrayList<PackagingListDataItem>();
				packagingList.add(new PackagingListDataItem(null, 1d, PackagingListUnit.P, PACKAGING_PRIMAIRE, true, packagingMaterial1NodeRef));
				packagingList.add(new PackagingListDataItem(null, 3d, PackagingListUnit.m, PACKAGING_PRIMAIRE, true, packagingMaterial2NodeRef));
				packagingList.add(new PackagingListDataItem(null, 8d, PackagingListUnit.PP, PACKAGING_TERTIAIRE, true, packagingMaterial3NodeRef));
				finishedProduct.setPackagingList(packagingList);
				NodeRef finishedProductNodeRef = productDAO.create(testFolderNodeRef, finishedProduct, dataLists);	
				
				EntityReportData entityReportData = defaultProductReportExtractor.extract(finishedProductNodeRef);
				logger.debug("XmlData : " + entityReportData.getXmlDataSource().asXML());

				return null;
			}
		}, false, true);
	}
}

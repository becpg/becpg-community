/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity.datalist;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.datalist.MultiLevelDataListService;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.MultiLevelListData;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.LocalSemiFinishedProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.CompoListUnit;
import fr.becpg.repo.product.data.productList.DeclarationType;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class MultiLevelDataServiceTest.
 * 
 * @author querephi
 */
public class MultiLevelDataServiceTest extends PLMBaseTestCase {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(MultiLevelDataServiceTest.class);

	
	@Resource
	private MultiLevelDataListService multiLevelDataListService;

	private NodeRef rawMaterial1NodeRef = null;
	private NodeRef rawMaterial2NodeRef = null;
	private NodeRef lSF1NodeRef = null;
	private NodeRef lSF2NodeRef = null;
	private NodeRef lSF3NodeRef = null;
	private NodeRef lSF4NodeRef = null;
	private NodeRef finishedProductNodeRef = null;
	
	/**
	 * Test get Multilevel of the compoList
	 */
	@Test
	public void testGetMultiLevelCompoList() {

		logger.debug("testGetMultiLevelCompoList");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				/*-- Create raw material --*/
				logger.debug("/*-- Create raw material --*/");
				RawMaterialData rawMaterial1 = new RawMaterialData();
				rawMaterial1.setName("Raw material 1");
				rawMaterial1NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial1).getNodeRef();
				RawMaterialData rawMaterial2 = new RawMaterialData();
				rawMaterial2.setName("Raw material 2");
				rawMaterial2NodeRef = alfrescoRepository.create(testFolderNodeRef, rawMaterial2).getNodeRef();
				LocalSemiFinishedProductData lSF1 = new LocalSemiFinishedProductData();
				lSF1.setName("Local semi finished 1");
				lSF1NodeRef = alfrescoRepository.create(testFolderNodeRef, lSF1).getNodeRef();

				LocalSemiFinishedProductData lSF2 = new LocalSemiFinishedProductData();
				lSF2.setName("Local semi finished 2");
				lSF2NodeRef = alfrescoRepository.create(testFolderNodeRef, lSF2).getNodeRef();
				
				LocalSemiFinishedProductData lSF3 = new LocalSemiFinishedProductData();
				lSF3.setName("Local semi finished 3");
				lSF3NodeRef = alfrescoRepository.create(testFolderNodeRef, lSF3).getNodeRef();
				
				LocalSemiFinishedProductData lSF4 = new LocalSemiFinishedProductData();
				lSF4.setName("Local semi finished 4");
				lSF4NodeRef = alfrescoRepository.create(testFolderNodeRef, lSF4).getNodeRef();

				/*-- Create finished product --*/
				logger.debug("/*-- Create finished product --*/");
				FinishedProductData finishedProduct = new FinishedProductData();
				finishedProduct.setName("Finished Product");
				
				List<CompoListDataItem> compoList = new LinkedList<CompoListDataItem>();
				CompoListDataItem parent1 = new CompoListDataItem(null, (CompoListDataItem)null, 1d, 1d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF1NodeRef);
				CompoListDataItem child1 =new CompoListDataItem(null,parent1, 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF2NodeRef);
				CompoListDataItem child12 =new CompoListDataItem(null,child1, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial1NodeRef);
				CompoListDataItem parent2 =new CompoListDataItem(null,(CompoListDataItem) null, 1d, 4d, CompoListUnit.P, 0d, DeclarationType.Declare, lSF3NodeRef);
				CompoListDataItem child2 =new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, rawMaterial2NodeRef);
				CompoListDataItem child21 =new CompoListDataItem(null, parent2, 3d, 0d, CompoListUnit.kg, 0d, DeclarationType.Omit, lSF4NodeRef);
				
				compoList.add(parent1);
				compoList.add(child1);
				compoList.add(child12);
				compoList.add(parent2);
				compoList.add(child2);
				compoList.add(child21);
				finishedProduct.getCompoListView().setCompoList(compoList);
				
				finishedProductNodeRef = alfrescoRepository.create(testFolderNodeRef, finishedProduct).getNodeRef();

				return null;
			}
		}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {
				
				DataListFilter dataListFilter = new DataListFilter();
				dataListFilter.setDataType(PLMModel.TYPE_COMPOLIST);
				dataListFilter.setEntityNodeRefs(Arrays.asList(finishedProductNodeRef));
				
				MultiLevelListData mlld = multiLevelDataListService.getMultiLevelListData(dataListFilter);
				int checks = 0;
				
				assertNotNull(mlld);
				
				for (MultiLevelListData mlld2 : mlld.getTree().values()) {
					logger.debug("mlld2, depth : " + mlld2.getDepth() + " - " + mlld2.getEntityNodeRef());
					
					if(mlld2.getEntityNodeRef().equals(lSF1NodeRef)){
						checks++;
						assertEquals(1, mlld2.getDepth());
					}
					else if(mlld2.getEntityNodeRef().equals(lSF2NodeRef)){
						checks++;
						assertEquals(2, mlld2.getDepth());
					}
					else if(mlld2.getEntityNodeRef().equals(rawMaterial1NodeRef)){
						checks++;
						assertEquals(3, mlld2.getDepth());
					}
					else if(mlld2.getEntityNodeRef().equals(lSF3NodeRef)){
						checks++;
						assertEquals(1, mlld2.getDepth());
					}
					else if(mlld2.getEntityNodeRef().equals(rawMaterial2NodeRef)){
						checks++;
						assertEquals(2, mlld2.getDepth());
					}
					else if(mlld2.getEntityNodeRef().equals(lSF4NodeRef)){
						checks++;
						assertEquals(2, mlld2.getDepth());
					}
				}
				
				assertEquals(6, checks);
				return null;

			}
		}, false, true);
	}
}

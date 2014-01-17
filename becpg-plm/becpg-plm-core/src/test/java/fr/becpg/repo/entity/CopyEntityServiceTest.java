/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.EffectiveFilters;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class CopyEntityServiceTest.
 * 
 * @author querephi
 */
public class CopyEntityServiceTest extends PLMBaseTestCase {

	private static Log logger = LogFactory.getLog(CopyEntityServiceTest.class);
	
	@Resource
	private EntityService entityService;

	@Test
	public void testCopyEntity() {
		
		// Create a product
		final NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return BeCPGPLMTestHelper.createMultiLevelProduct(testFolderNodeRef);
			}
		}, false, true);
		

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
			public Void execute() throws Throwable {				
			
				Date startEffectivity = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
				assertNotNull(startEffectivity);
				assertTrue(startEffectivity.before(new Date()));
				
				// First copy
				copyProduct(testFolderNodeRef, productNodeRef, "Test Copy", "Test Copy");
				
				// Second copy
				copyProduct(testFolderNodeRef, productNodeRef, "Test Copy", "Test Copy (1)");
				
				return null;
			}
		}, false, true);
	}
	
	private void copyProduct(final NodeRef testFolderNodeRef, final NodeRef sourceNodeRef, final String givenName, String finalName){
		
		NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return entityService.createOrCopyFrom(sourceNodeRef, testFolderNodeRef, BeCPGModel.TYPE_FINISHEDPRODUCT, givenName);
			}
		}, false, true);
		
		Date startEffectivity = (Date) nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		Date startEffectivity2 = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		assertNotNull(startEffectivity2);
		assertTrue(startEffectivity.getTime() < startEffectivity2.getTime());
		
		// #276 bcpg:parent nodeRef must be different
		ProductData sourceProductData = (ProductData) alfrescoRepository.findOne(sourceNodeRef);
		ProductData copyProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);
		
		int [] arrRawMaterials = {2,4};
		
		for(int rawMaterial : arrRawMaterials){
			
			logger.debug("check rawMaterial " + rawMaterial);
			NodeRef sourceMP1NodeRef = sourceProductData.getCompoList(EffectiveFilters.EFFECTIVE).get(rawMaterial).getProduct();
			NodeRef copyMP1NodeRef = copyProductData.getCompoList(EffectiveFilters.EFFECTIVE).get(rawMaterial).getProduct();
			
			assertEquals(BeCPGModel.TYPE_RAWMATERIAL, nodeService.getType(sourceMP1NodeRef));
			assertEquals(BeCPGModel.TYPE_RAWMATERIAL, nodeService.getType(copyMP1NodeRef));
			assertEquals(sourceMP1NodeRef, copyMP1NodeRef);
			
			// source and copy have different parents
			assertFalse(sourceProductData.getCompoList(EffectiveFilters.EFFECTIVE).get(rawMaterial).getParent().getNodeRef().equals(copyProductData.getCompoList(EffectiveFilters.EFFECTIVE).get(rawMaterial).getParent().getNodeRef()));
			// check parent
			assertEquals(copyProductData.getCompoList(EffectiveFilters.EFFECTIVE).get(rawMaterial-1).getNodeRef(), copyProductData.getCompoList(EffectiveFilters.EFFECTIVE).get(rawMaterial).getParent().getNodeRef());
		}		
	}
}

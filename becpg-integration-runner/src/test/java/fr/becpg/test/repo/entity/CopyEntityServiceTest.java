/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityService;
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

	private static final Log logger = LogFactory.getLog(CopyEntityServiceTest.class);
	
	@Resource
	private EntityService entityService;

	@Test
	public void testCopyEntity() {
		
		// Create a product
		final NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef());
			}
		}, false, true);
		

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
			public Void execute() throws Throwable {				
			
				Date startEffectivity = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
				assertNotNull(startEffectivity);
				assertTrue(startEffectivity.before(new Date()));
				
				// First copy
				copyProduct(productNodeRef, "Test Copy");
				
				// Second copy
				copyProduct( productNodeRef, "Test Copy (1)");
				
				return null;
			}
		}, false, true);
	}
	
	private void copyProduct(final NodeRef sourceNodeRef, final String givenName){
		
		NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return entityService.createOrCopyFrom(sourceNodeRef, getTestFolderNodeRef(), PLMModel.TYPE_FINISHEDPRODUCT, givenName);
			}
		}, false, true);
		
		Date startEffectivity = (Date) nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		Date startEffectivity2 = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
		assertNotNull(startEffectivity2);
		assertTrue(startEffectivity.getTime() < startEffectivity2.getTime());
		
		// #276 bcpg:parent nodeRef must be different
		ProductData sourceProductData = alfrescoRepository.findOne(sourceNodeRef);
		ProductData copyProductData = alfrescoRepository.findOne(productNodeRef);
		
		int [] arrRawMaterials = {2,4};
		
		for(int rawMaterial : arrRawMaterials){
			
			logger.debug("check rawMaterial " + rawMaterial);
			NodeRef sourceMP1NodeRef = sourceProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getProduct();
			NodeRef copyMP1NodeRef = copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getProduct();
			
			assertEquals(PLMModel.TYPE_RAWMATERIAL, nodeService.getType(sourceMP1NodeRef));
			assertEquals(PLMModel.TYPE_RAWMATERIAL, nodeService.getType(copyMP1NodeRef));
			assertEquals(sourceMP1NodeRef, copyMP1NodeRef);
			
			// source and copy have different parents
			assertFalse(sourceProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getParent().getNodeRef().equals(copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getParent().getNodeRef()));
			// check parent
			assertEquals(copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial-1).getNodeRef(), copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getParent().getNodeRef());
		}		
	}
}

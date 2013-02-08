/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class CopyEntityServiceTest.
 * 
 * @author querephi
 */
//Test if not used
@Deprecated
public class CopyEntityServiceTest extends RepoBaseTestCase {

	@Resource
	private EntityService entityService;

	@Test
	public void testCopyEntity() {
		
		// Create a product
		final NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
			}
		}, false, true);
		

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
			public Void execute() throws Throwable {				
				
				Date start = new Date();
			
				Date startEffectivity = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
				assertNotNull(startEffectivity);
				assertTrue(start.getTime() < startEffectivity.getTime());

				
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

	
	}

}

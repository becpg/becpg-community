/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.util.Date;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

/**
 * The Class ProductReportServiceTest.
 * 
 * @author querephi
 */
public class CopyEntityServiceTest extends RepoBaseTestCase {

	private static Log logger = LogFactory.getLog(CopyEntityServiceTest.class);
	
	@Resource
	private EntityService entityService;

	@Test
	public void testCopyEntity() {


		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
			public Void execute() throws Throwable {				
				
				Date start = new Date();
				
				// Create a product
				NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
					public NodeRef execute() throws Throwable {

						return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
					}
				}, false, true);
				
				Date startEffectivity = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
				assertNotNull(startEffectivity);
				assertTrue(start.getTime() < startEffectivity.getTime());

				NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
				QName parentEntityType = nodeService.getType(parentEntityNodeRef);

				// Actual entity parent is not a entity folder
				assertTrue(parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER));
				
				boolean test = ((String) nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME)).startsWith((String) nodeService.getProperty(productNodeRef,
						ContentModel.PROP_NAME));

				assertTrue(test);

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

		NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(productNodeRef).getParentRef();
		QName parentEntityType = nodeService.getType(parentEntityNodeRef);

		assertTrue(parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER));

		assertEquals(nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME), finalName);

		boolean test = ((String) nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME)).startsWith((String) nodeService.getProperty(productNodeRef,
				ContentModel.PROP_NAME));
		
		assertTrue(test);
	}

}

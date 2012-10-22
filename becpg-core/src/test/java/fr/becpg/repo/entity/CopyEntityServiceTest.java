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
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductReportServiceTest.
 * 
 * @author querephi
 */
public class CopyEntityServiceTest extends RepoBaseTestCase {

	@Resource
	private EntityService entityService;

	/** The sf node ref. */
	private NodeRef sfNodeRef;

	@Test
	public void testCopyEntity() {


		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<Void>() {
			public Void execute() throws Throwable {

				Date start = new Date();
				
				// Create a product
				sfNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
					public NodeRef execute() throws Throwable {

						return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
					}
				}, false, true);

				Date startEffectivity = (Date) nodeService.getProperty(sfNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
				assertNotNull(startEffectivity);
				assertTrue(start.getTime() < startEffectivity.getTime());

				NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(sfNodeRef).getParentRef();
				QName parentEntityType = nodeService.getType(parentEntityNodeRef);

				// Actual entity parent is not a entity folder
				assertTrue(parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER));
				
				boolean test = ((String) nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME)).startsWith((String) nodeService.getProperty(sfNodeRef,
						ContentModel.PROP_NAME));

				assertTrue(test);

				sfNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
					public NodeRef execute() throws Throwable {

						return entityService.createOrCopyFrom(sfNodeRef, testFolderNodeRef, BeCPGModel.TYPE_FINISHEDPRODUCT, "Test Copy");
					}
				}, false, true);

				Date startEffectivity2 = (Date) nodeService.getProperty(sfNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
				assertNotNull(startEffectivity2);
				assertTrue(startEffectivity.getTime() < startEffectivity2.getTime());

				parentEntityNodeRef = nodeService.getPrimaryParent(sfNodeRef).getParentRef();
				parentEntityType = nodeService.getType(parentEntityNodeRef);

				assertTrue(parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER));

				assertNotSame(nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME), "Test Copy");

				 test = ((String) nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME)).startsWith((String) nodeService.getProperty(sfNodeRef,
						ContentModel.PROP_NAME));
				
				assertTrue(test);
				
				return null;

			}
		}, false, true);
	}

}

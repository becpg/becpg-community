/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.test.BeCPGTestHelper;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoteEntityServiceTest.
 * 
 * @author matthieu
 */
public class RemoteEntityServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(RemoteEntityServiceTest.class);

	private RemoteEntityService remoteEntityService;
	

	/** The sf node ref. */
	private NodeRef sfNodeRef;

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		logger.debug("RemoteEntityServiceTest:setUp");

		productDAO = (ProductDAO) ctx.getBean("productDAO");
		remoteEntityService = (RemoteEntityService) ctx.getBean("remoteEntityService");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see fr.becpg.test.RepoBaseTestCase#tearDown()
	 */
	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}


	public void testRemoteEntity() throws FileNotFoundException {
		
		final RepoBaseTestCase repoBaseTestCase = this;
		// create product
		sfNodeRef  = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
			}
		}, false, true);

		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				try {
					
					File tempFile = File.createTempFile("remoteEntity", "xml");
					
					List<NodeRef> entities = new ArrayList<NodeRef>();
					entities.add(sfNodeRef);
					
					remoteEntityService.listEntities(entities, System.out,  RemoteEntityFormat.xml);
					
					remoteEntityService.getEntity(sfNodeRef, new FileOutputStream(tempFile), RemoteEntityFormat.xml);

//					nodeService.deleteNode(sfNodeRef);
					
					sfNodeRef = remoteEntityService.createOrUpdateEntity(sfNodeRef, new FileInputStream(tempFile), RemoteEntityFormat.xml,null);
					
					remoteEntityService.getEntity(sfNodeRef, System.out, RemoteEntityFormat.xml);
					
					remoteEntityService.getEntityData(sfNodeRef, System.out, RemoteEntityFormat.xml);
					
					remoteEntityService.getEntityData(sfNodeRef, new FileOutputStream(tempFile), RemoteEntityFormat.xml);
					
					remoteEntityService.addOrUpdateEntityData(sfNodeRef,  new FileInputStream(tempFile), RemoteEntityFormat.xml);
					
					tempFile.delete();
					
				} catch (BeCPGException e) {
					logger.error(e,e);
					Assert.fail(e.getMessage());
				}

				return null;
			}
		}, false, true);	
	}
}

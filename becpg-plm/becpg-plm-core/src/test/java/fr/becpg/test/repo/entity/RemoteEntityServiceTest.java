/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.common.BeCPGException;
import fr.becpg.repo.entity.remote.RemoteEntityFormat;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.test.BeCPGPLMTestHelper;
import fr.becpg.test.PLMBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class RemoteEntityServiceTest.
 * 
 * @author matthieu
 */
public class RemoteEntityServiceTest extends PLMBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(RemoteEntityServiceTest.class);

	@Resource
	private RemoteEntityService remoteEntityService;
	

	/** The sf node ref. */
	private NodeRef sfNodeRef;

	@Test
	public void testRemoteEntity() throws FileNotFoundException {
		
		// create product
		sfNodeRef  = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			public NodeRef execute() throws Throwable {

				return BeCPGPLMTestHelper.createMultiLevelProduct(testFolderNodeRef);
			}
		}, false, true);

		
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {
				try {
					
					File tempFile = File.createTempFile("remoteEntity", "xml");
					File tempFile2 = File.createTempFile("remoteEntity2", "xml");
					
					List<NodeRef> entities = new ArrayList<NodeRef>();
					entities.add(sfNodeRef);
					
					remoteEntityService.listEntities(entities, new FileOutputStream(tempFile2),  RemoteEntityFormat.xml);
					
					remoteEntityService.getEntity(sfNodeRef, new FileOutputStream(tempFile), RemoteEntityFormat.xml);

//					nodeService.deleteNode(sfNodeRef);
					
					sfNodeRef = remoteEntityService.createOrUpdateEntity(sfNodeRef, new FileInputStream(tempFile), RemoteEntityFormat.xml,null);
					
					remoteEntityService.getEntity(sfNodeRef, new FileOutputStream(tempFile2), RemoteEntityFormat.xml);
					
					remoteEntityService.getEntityData(sfNodeRef, new FileOutputStream(tempFile2), RemoteEntityFormat.xml);
					
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

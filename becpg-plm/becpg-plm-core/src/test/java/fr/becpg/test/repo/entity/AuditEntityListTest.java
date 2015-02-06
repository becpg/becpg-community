/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.PLMModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.test.PLMBaseTestCase;

/**
 * The Class AuditEntityListTest.
 * 
 * @author matthieu
 */
public class AuditEntityListTest extends PLMBaseTestCase {

	private static Log logger = LogFactory.getLog(AuditEntityListTest.class);

	@Resource
	private EntityListDAO entityListDAO;


	@Test
	public void testDateModified() throws InterruptedException {

		long timestamps = Calendar.getInstance().getTimeInMillis();
		
		logger.debug("testHasDataListsModified()");

		// create product
		final NodeRef sfNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null,null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null,null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null,null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null,null, false, false, null, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);


				return alfrescoRepository.create(getTestFolderNodeRef(), sfData).getNodeRef();

			}
		}, false, true);

		// load SF and test it
	
		Date modified = (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);

		logger.info("Compare : "+timestamps+" "+modified.getTime());
		assertTrue(timestamps<modified.getTime());
		
		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps<modified.getTime());
		
		final SemiFinishedProductData sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);

		
		// setProperty of allergen without changing anything => nothing changed
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = sfData.getAllergenList().get(0).getAllergen();
				nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);
				return null;

			}
		}, false, true);
		
		modified = (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);

		assertFalse(timestamps<modified.getTime());

		// setProperty of allergen and change smth => modified
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = sfData.getAllergenList().get(0).getNodeRef();
				logger.info("allergen prev value " + nodeService.getProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY));
				nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);

				return null;

			}
		}, false, true);

		modified = (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		logger.info("Compare : "+timestamps+" "+modified.getTime());
		
		assertTrue(timestamps<modified.getTime());
		
		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps<modified.getTime());

		// add an allergen
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_ALLERGENLIST);
				NodeRef allergen = allergens.get(5);
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY, true);
				properties.put(PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);
				ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergen.getId()), PLMModel.TYPE_ALLERGENLIST, properties);
				NodeRef linkNodeRef = childAssocRef.getChildRef();
				nodeService.createAssociation(linkNodeRef, allergen, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN);

				logger.debug("listNodeRef: " + listNodeRef);
				logger.debug("added allergen modified: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_MODIFIED));
				logger.debug("added allergen created: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_CREATED));

				return null;

			}
		}, false, true);
		
		modified = (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		logger.info("Compare : "+timestamps+" "+modified.getTime());
		assertTrue(timestamps<modified.getTime());
		
		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps<modified.getTime());

		// remove an allergen
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = sfData.getAllergenList().get(1).getNodeRef();
				nodeService.deleteNode(nodeRef);

				return null;

			}
		}, false, true);
		
		modified = (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);

		assertTrue(timestamps<modified.getTime());
		
		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps<modified.getTime());

	}
	
	
}

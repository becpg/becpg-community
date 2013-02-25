/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.SemiFinishedProductData;
import fr.becpg.repo.product.data.productList.AllergenListDataItem;
import fr.becpg.test.RepoBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductReportServiceTest.
 * 
 * @author querephi
 */
public class EntityServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityServiceTest.class);

	@Resource
	private BehaviourFilter policyBehaviourFilter;

	@Resource
	private EntityListDAO entityListDAO;

	@Resource
	private EntityService entityService;	
	
	//force init repo (otherwise failed depending of previous tests)
	protected boolean forceInit = true;

	/** The sf node ref. */
	private NodeRef sfNodeRef;


	/**
	 * Reset the property modified.
	 */
	private void resetModified() {

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				Calendar cal = Calendar.getInstance();

				policyBehaviourFilter.disableBehaviour(sfNodeRef, ContentModel.ASPECT_AUDITABLE);
				nodeService.setProperty(sfNodeRef, ContentModel.PROP_MODIFIED, cal.getTime());
				policyBehaviourFilter.enableBehaviour(sfNodeRef, ContentModel.ASPECT_AUDITABLE);
				return null;

			}
		}, false, true);

	}

	/**
	 * Test is report up to date.
	 * 
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	@Test
	public void testHasDataListsModified() throws InterruptedException {

		logger.debug("testHasDataListsModified()");

		// create product
		sfNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				// create SF
				SemiFinishedProductData sfData = new SemiFinishedProductData();
				sfData.setName("SF");
				List<AllergenListDataItem> allergenList = new ArrayList<AllergenListDataItem>();
				allergenList.add(new AllergenListDataItem(null, true, true, null, null, allergens.get(0), false));
				allergenList.add(new AllergenListDataItem(null, false, true, null, null, allergens.get(1), false));
				allergenList.add(new AllergenListDataItem(null, true, false, null, null, allergens.get(2), false));
				allergenList.add(new AllergenListDataItem(null, false, false, null, null, allergens.get(3), false));
				sfData.setAllergenList(allergenList);


				return alfrescoRepository.create(testFolderNodeRef, sfData).getNodeRef();

			}
		}, false, true);

		// load SF and test it
		final SemiFinishedProductData sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);

		// reset
		resetModified();

		assertEquals("datalist has not been modified", false, entityService.hasDataListModified(sfNodeRef));

		// setProperty of allergen without changing anything => nothing changed
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = sfData.getAllergenList().get(0).getAllergen();
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, true);
				return null;

			}
		}, false, true);

		assertEquals("datalist has not been modified", false, entityService.hasDataListModified(sfNodeRef));

		// setProperty of allergen and change smth => modified
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = sfData.getAllergenList().get(0).getNodeRef();
				logger.debug("allergen prev value " + nodeService.getProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY));
				nodeService.setProperty(nodeRef, BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, false);

				return null;

			}
		}, false, true);

		assertEquals("datalist has been modified", true, entityService.hasDataListModified(sfNodeRef));

		// reset
		resetModified();

		// add an allergen
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
				NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_ALLERGENLIST);
				NodeRef allergen = allergens.get(5);
				Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
				properties.put(BeCPGModel.PROP_ALLERGENLIST_INVOLUNTARY, true);
				properties.put(BeCPGModel.PROP_ALLERGENLIST_VOLUNTARY, false);
				ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
						QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergen.getId()), BeCPGModel.TYPE_ALLERGENLIST, properties);
				NodeRef linkNodeRef = childAssocRef.getChildRef();
				nodeService.createAssociation(linkNodeRef, allergen, BeCPGModel.ASSOC_ALLERGENLIST_ALLERGEN);

				logger.debug("listNodeRef: " + listNodeRef);
				logger.debug("added allergen modified: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_MODIFIED));
				logger.debug("added allergen created: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_CREATED));

				return null;

			}
		}, false, true);

		assertEquals("datalist has been modified", true, entityService.hasDataListModified(sfNodeRef));

		// reset
		resetModified();

		// remove an allergen
		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				NodeRef nodeRef = sfData.getAllergenList().get(1).getNodeRef();
				nodeService.deleteNode(nodeRef);

				return null;

			}
		}, false, true);

		assertEquals("datalist has been modified", true, entityService.hasDataListModified(sfNodeRef));

	}
	
//	@Test
//	public void testEntityFolder(){
//		 Date start = new Date();
//		
//		// Create a product
//		sfNodeRef  = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
//			public NodeRef execute() throws Throwable {
//
//				return BeCPGTestHelper.createMultiLevelProduct(testFolderNodeRef, repoBaseTestCase);
//			}
//		}, false, true);
//		
//		Date startEffectivity = (Date)nodeService.getProperty(sfNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
//		assertNotNull(startEffectivity);
//		assertTrue(start.getTime()<startEffectivity.getTime());
//		
//		// entityFolder check
//		NodeRef parentEntityNodeRef = nodeService.getPrimaryParent(sfNodeRef).getParentRef();
//		QName parentEntityType = nodeService.getType(parentEntityNodeRef);
//		assertTrue(parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER));
//		
//		// compare names
//		String entityFolderName = (String)nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME);
//		String productName = (String)nodeService.getProperty(sfNodeRef, ContentModel.PROP_NAME);
//		assertEquals(entityFolderName, BeCPGTestHelper.PRODUCT_NAME);
//		assertEquals(entityFolderName, productName);
//		
//		sfNodeRef  = transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
//			public NodeRef execute() throws Throwable {
//
//				return copyService.copyAndRename(sfNodeRef, testFolderNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN, true);
//			}
//		}, false, true);
//		
//		Date startEffectivity2 = (Date)nodeService.getProperty(sfNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
//		assertNotNull(startEffectivity2);
//		assertTrue(startEffectivity.getTime()<startEffectivity2.getTime());
//		
//		// entityFolder check
//		parentEntityNodeRef = nodeService.getPrimaryParent(sfNodeRef).getParentRef();
//		parentEntityType = nodeService.getType(parentEntityNodeRef);		
//		assertTrue(parentEntityType.equals(BeCPGModel.TYPE_ENTITY_FOLDER));
//
//		// compare names
//		entityFolderName = (String)nodeService.getProperty(parentEntityNodeRef, ContentModel.PROP_NAME);
//		productName = (String)nodeService.getProperty(sfNodeRef, ContentModel.PROP_NAME);		
//		assertNotSame(parentEntityNodeRef, BeCPGTestHelper.PRODUCT_NAME);
//		assertTrue(entityFolderName.contains(productName));
//	}
	
}

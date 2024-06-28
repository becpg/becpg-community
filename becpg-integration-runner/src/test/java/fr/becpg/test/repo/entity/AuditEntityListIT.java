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

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
public class AuditEntityListIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(AuditEntityListIT.class);

	@Autowired
	private EntityListDAO entityListDAO;

	@Test
	public void testDateModified() throws InterruptedException {

		long timestamps = Calendar.getInstance().getTimeInMillis();

		logger.debug("testHasDataListsModified()");

		// create product
		final NodeRef sfNodeRef = inWriteTx(() -> {

			// create SF
			SemiFinishedProductData sfData = new SemiFinishedProductData();
			sfData.setName("SF");
			List<AllergenListDataItem> allergenList = new ArrayList<>();
			allergenList.add(new AllergenListDataItem(null, null, true, true, null, null, allergens.get(0), false));
			allergenList.add(new AllergenListDataItem(null, null, false, true, null, null, allergens.get(1), false));
			allergenList.add(new AllergenListDataItem(null, null, true, false, null, null, allergens.get(2), false));
			allergenList.add(new AllergenListDataItem(null, null, false, false, null, null, allergens.get(3), false));
			sfData.setAllergenList(allergenList);

			return alfrescoRepository.create(getTestFolderNodeRef(), sfData).getNodeRef();

		});

		// load SF and test it

		Date modified = inWriteTx(() -> {

			return (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		});

		logger.info("Compare : " + timestamps + " " + modified.getTime());
		assertTrue(timestamps < modified.getTime());

		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps < modified.getTime());

		final SemiFinishedProductData sfData = (SemiFinishedProductData) alfrescoRepository.findOne(sfNodeRef);

		// setProperty of allergen without changing anything => nothing changed
		inWriteTx(() -> {

			NodeRef nodeRef = sfData.getAllergenList().get(0).getAllergen();
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, true);
			return null;

		});

		modified = inWriteTx(() -> {

			return (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		});

		assertFalse(timestamps < modified.getTime());

		// setProperty of allergen and change smth => modified
		inWriteTx(() -> {

			NodeRef nodeRef = sfData.getAllergenList().get(0).getNodeRef();
			logger.info(
					"allergen prev value " + nodeService.getProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY));
			nodeService.setProperty(nodeRef, PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);

			return null;

		});

		modified = inWriteTx(() -> {

			return (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		});
		logger.info("Compare : " + timestamps + " " + modified.getTime());

		assertTrue(timestamps < modified.getTime());

		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps < modified.getTime());

		// add an allergen
		inWriteTx(() -> {

			NodeRef listContainerNodeRef = entityListDAO.getListContainer(sfNodeRef);
			NodeRef listNodeRef = entityListDAO.getList(listContainerNodeRef, PLMModel.TYPE_ALLERGENLIST);
			NodeRef allergen = allergens.get(5);
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(PLMModel.PROP_ALLERGENLIST_INVOLUNTARY, true);
			properties.put(PLMModel.PROP_ALLERGENLIST_VOLUNTARY, false);
			ChildAssociationRef childAssocRef = nodeService.createNode(listNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, allergen.getId()),
					PLMModel.TYPE_ALLERGENLIST, properties);
			NodeRef linkNodeRef = childAssocRef.getChildRef();
			nodeService.createAssociation(linkNodeRef, allergen, PLMModel.ASSOC_ALLERGENLIST_ALLERGEN);

			logger.debug("listNodeRef: " + listNodeRef);
			logger.debug(
					"added allergen modified: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_MODIFIED));
			logger.debug("added allergen created: " + nodeService.getProperty(linkNodeRef, ContentModel.PROP_CREATED));

			return null;

		});

		modified = inWriteTx(() -> {

			return (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		});
		logger.info("Compare : " + timestamps + " " + modified.getTime());
		assertTrue(timestamps < modified.getTime());

		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps < modified.getTime());

		// remove an allergen
		inWriteTx(() -> {

			NodeRef nodeRef = sfData.getAllergenList().get(1).getNodeRef();
			nodeService.deleteNode(nodeRef);

			return null;

		});

		modified = inWriteTx(() -> {

			return (Date) nodeService.getProperty(sfNodeRef, ContentModel.PROP_MODIFIED);
		});

		assertTrue(timestamps < modified.getTime());

		timestamps = Calendar.getInstance().getTimeInMillis();

		assertFalse(timestamps < modified.getTime());

	}

}

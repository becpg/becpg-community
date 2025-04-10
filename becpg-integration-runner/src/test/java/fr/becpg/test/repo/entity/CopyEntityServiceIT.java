/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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
public class CopyEntityServiceIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(CopyEntityServiceIT.class);

	@Autowired
	private EntityService entityService;

	@Test
	public void testCopyEntity() {

		// Create a product
		final NodeRef productNodeRef = transactionService.getRetryingTransactionHelper()
				.doInTransaction(() -> BeCPGPLMTestHelper.createMultiLevelProduct(getTestFolderNodeRef()), false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			Date startEffectivity = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
			assertNotNull(startEffectivity);
			assertTrue(startEffectivity.before(new Date()));

			// First copy
			copyProduct(productNodeRef, "Test Copy");

			// Second copy
			copyProduct(productNodeRef, "Test Copy (1)");

			return null;
		}, false, true);
	}

	private void copyProduct(final NodeRef sourceNodeRef, final String givenName) {

		NodeRef productNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(
				() -> entityService.createOrCopyFrom(sourceNodeRef, getTestFolderNodeRef(), PLMModel.TYPE_FINISHEDPRODUCT, givenName), false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			Date startEffectivity = (Date) nodeService.getProperty(sourceNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
			Date startEffectivity2 = (Date) nodeService.getProperty(productNodeRef, BeCPGModel.PROP_START_EFFECTIVITY);
			assertNotNull(startEffectivity2);
			assertTrue(startEffectivity.getTime() < startEffectivity2.getTime());

			// #276 bcpg:parent nodeRef must be different
			ProductData sourceProductData = (ProductData) alfrescoRepository.findOne(sourceNodeRef);
			ProductData copyProductData = (ProductData) alfrescoRepository.findOne(productNodeRef);

			int[] arrRawMaterials = { 2, 4 };

			for (int rawMaterial : arrRawMaterials) {

				logger.debug("check rawMaterial " + rawMaterial);
				NodeRef sourceMP1NodeRef = sourceProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial)
						.getProduct();
				NodeRef copyMP1NodeRef = copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial)
						.getProduct();

				assertEquals(PLMModel.TYPE_RAWMATERIAL, nodeService.getType(sourceMP1NodeRef));
				assertEquals(PLMModel.TYPE_RAWMATERIAL, nodeService.getType(copyMP1NodeRef));
				assertEquals(sourceMP1NodeRef, copyMP1NodeRef);

				// source and copy have different parents
				assertFalse(sourceProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getParent()
						.getNodeRef().equals(copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial)
								.getParent().getNodeRef()));
				// check parent
				assertEquals(copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial - 1).getNodeRef(),
						copyProductData.getCompoList(new EffectiveFilters<>(EffectiveFilters.EFFECTIVE)).get(rawMaterial).getParent().getNodeRef());
			}
			return true;
		}, false, true);
	}
}

/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.RepoBaseTestCase;

/**
 * 
 * @author querephi
 */
public class EntityTplServiceTest extends RepoBaseTestCase {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityTplServiceTest.class);

	@Resource
	private EntityService entityService;
	
	@Resource
	private EntityTplService entityTplService;
	
	@Resource
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Test
	public void testSynchronize() throws InterruptedException {

		logger.debug("testSynchronize");

		transactionService.getRetryingTransactionHelper().doInTransaction(new RetryingTransactionCallback<NodeRef>() {
			@Override
			public NodeRef execute() throws Throwable {

				RawMaterialData rmTplData = new RawMaterialData();
				rmTplData.setName("Raw material Tpl");				
				rmTplData.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);				
				NodeRef rmTplNodeRef = alfrescoRepository.create(testFolderNodeRef, rmTplData).getNodeRef();
				
				RawMaterialData rm1Data = new RawMaterialData();
				rm1Data.setName("Raw material 1");
				rm1Data.setEntityTplRef(rmTplNodeRef);
				NodeRef rm1NodeRef = alfrescoRepository.create(testFolderNodeRef, rm1Data).getNodeRef();
				
				assertFalse(alfrescoRepository.hasDataList(rm1NodeRef, BeCPGModel.TYPE_COSTLIST));
				
				// add costList on template
				List<CostListDataItem> costList = new ArrayList<>();
				costList.add(new CostListDataItem(null, null, null, null, costs.get(0), null));
				costList.add(new CostListDataItem(null, null, null, null, costs.get(1), null));
				rmTplData.setCostList(costList);
				alfrescoRepository.save(rmTplData);
				
				entityTplService.synchronizeEntities(rmTplNodeRef);
				
				assertTrue(alfrescoRepository.hasDataList(rm1NodeRef, BeCPGModel.TYPE_COSTLIST));
				rm1Data = (RawMaterialData)alfrescoRepository.findOne(rm1NodeRef);
				assertEquals(2, rm1Data.getCostList().size());
				
				return null;

			}
		}, false, true);

	}
	
}

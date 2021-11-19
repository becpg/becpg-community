/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.test.PLMBaseTestCase;

/**
 *
 * @author querephi
 */
public class EntityTplServiceIT extends PLMBaseTestCase {

	
	private static final Log logger = LogFactory.getLog(EntityTplServiceIT.class);

	@Autowired
	private AlfrescoRepository<ProductData> alfrescoRepository;

	@Autowired
	private EntityTplService entityTplService;

	@Autowired
	private FileFolderService fileFolderService;
	

	@Test
	public void testSynchronize() throws InterruptedException {

		logger.debug("testSynchronize");

		final NodeRef rmTplNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			RawMaterialData rmTplData = new RawMaterialData();
			rmTplData.setName("Raw material Tpl");
			rmTplData.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);
			return alfrescoRepository.create(getTestFolderNodeRef(), rmTplData).getNodeRef();

		}, false, true);

		final NodeRef rm1NodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			RawMaterialData rmTplData = (RawMaterialData) alfrescoRepository.findOne(rmTplNodeRef);
			RawMaterialData rm1Data = new RawMaterialData();
			rm1Data.setName("Raw material 1");			
			rm1Data.setEntityTpl(rmTplData);
			rm1Data = (RawMaterialData) alfrescoRepository.create(getTestFolderNodeRef(), rm1Data);
			
			assertNull(rm1Data.getCostList());

			// add costList on template
			List<CostListDataItem> costList = new ArrayList<>();
			costList.add(new CostListDataItem(null, null, null, null, costs.get(0), null));
			costList.add(new CostListDataItem(null, null, null, null, costs.get(1), null));
			rmTplData.setCostList(costList);
			alfrescoRepository.save(rmTplData);

			assertEquals(2, rmTplData.getCostList().size());
					
			
			return rm1Data.getNodeRef();
		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

             BatchInfo batch = entityTplService.synchronizeEntities(rmTplNodeRef);
			
			waitForBatchEnd(batch);

			return null;

		}, false, true);

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			RawMaterialData rm1Data = (RawMaterialData) alfrescoRepository.findOne(rm1NodeRef);

			for (CostListDataItem cost : rm1Data.getCostList()) {
				logger.debug(cost.toString());
			}

			assertEquals(2, rm1Data.getCostList().size());

			return null;
		}, false, true);

		// synchronize folders

		final String name = "Dossier test";
		logger.debug("Test synchronize folders");

		NodeRef newFolderNodeRef = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FileInfo newFolder = fileFolderService.create(rmTplNodeRef, name, ContentModel.TYPE_FOLDER);
			

			for (FileInfo folder : fileFolderService.listFolders(rmTplNodeRef)) {
				logger.debug("Template Folder: " + folder.getName() + ", template NR: " + rmTplNodeRef);
			}

			return newFolder.getNodeRef();
		}, false, true);

		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

            BatchInfo batch = entityTplService.synchronizeEntities(rmTplNodeRef);
			
			waitForBatchEnd(batch);
			return null;
			}, false, true);
		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {

			FileInfo newFolder = fileFolderService.getFileInfo(newFolderNodeRef);
			assertNotNull(newFolder);

			List<FileInfo> rm1Folders = fileFolderService.listFolders(rm1NodeRef);
			for (FileInfo folder : rm1Folders) {
				logger.debug("RM1 Folder post sync: " + folder.getName());
			}

			FileInfo tmpFolder = rm1Folders.stream().filter(f1 -> name.equals(f1.getName())).findAny().orElse(null);
			logger.debug("Check if folder exists: " + tmpFolder);
			assertNotNull(rm1Folders.stream().filter(f2 -> name.equals(f2.getName())).findAny().orElse(null));

			logger.debug("It exists, deleting it");
			fileFolderService.delete(newFolder.getNodeRef());
			return null;
		}, false, true);

		
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			BatchInfo batch = entityTplService.synchronizeEntities(rmTplNodeRef);
			
			waitForBatchEnd(batch);
			
			return null;
			}, false, true);
	
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.debug("Node deleted, synchronizing again");

			List<FileInfo> rm1Folders = fileFolderService.listFolders(rm1NodeRef);
			logger.debug("Check if folder was removed");
			assertNull(rm1Folders.stream().filter(f -> name.equals(f.getName())).findAny().orElse(null));

			return null;
		}, false, true);

		
		
	}

		


}

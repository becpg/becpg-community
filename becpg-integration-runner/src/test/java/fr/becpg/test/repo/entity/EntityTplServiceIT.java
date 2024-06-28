/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.test.repo.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.repo.activity.data.ActivityListDataItem;
import fr.becpg.repo.activity.helper.AuditActivityHelper;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.entity.EntityTplService;
import fr.becpg.repo.product.data.FinishedProductData;
import fr.becpg.repo.product.data.ProductData;
import fr.becpg.repo.product.data.RawMaterialData;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.product.data.productList.NutListDataItem;
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

		final NodeRef rmTplNodeRef = inWriteTx(() -> {

			RawMaterialData rmTplData = new RawMaterialData();
			rmTplData.setName("Raw material Tpl");
			rmTplData.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);
			return alfrescoRepository.create(getTestFolderNodeRef(), rmTplData).getNodeRef();

		});

		final NodeRef rm1NodeRef = inWriteTx(() -> {

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
		});

		inWriteTx(() -> {

			BatchInfo batch = entityTplService.synchronizeEntities(rmTplNodeRef);

			waitForBatchEnd(batch);

			return null;

		});

		inWriteTx(() -> {

			RawMaterialData rm1Data = (RawMaterialData) alfrescoRepository.findOne(rm1NodeRef);

			for (CostListDataItem cost : rm1Data.getCostList()) {
				logger.debug(cost.toString());
			}

			assertEquals(2, rm1Data.getCostList().size());

			return null;
		});

		// synchronize folders

		final String name = "Dossier test";
		logger.debug("Test synchronize folders");

		NodeRef newFolderNodeRef = inWriteTx(() -> {

			FileInfo newFolder = fileFolderService.create(rmTplNodeRef, name, ContentModel.TYPE_FOLDER);

			for (FileInfo folder : fileFolderService.listFolders(rmTplNodeRef)) {
				logger.debug("Template Folder: " + folder.getName() + ", template NR: " + rmTplNodeRef);
			}

			return newFolder.getNodeRef();
		});

		inWriteTx(() -> {

			BatchInfo batch = entityTplService.synchronizeEntities(rmTplNodeRef);

			waitForBatchEnd(batch);
			return null;
		});

		inWriteTx(() -> {

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
		});

		inWriteTx(() -> {
			BatchInfo batch = entityTplService.synchronizeEntities(rmTplNodeRef);

			waitForBatchEnd(batch);

			return null;
		});

		inWriteTx(() -> {
			logger.debug("Node deleted, synchronizing again");

			List<FileInfo> rm1Folders = fileFolderService.listFolders(rm1NodeRef);
			logger.debug("Check if folder was removed");
			assertNull(rm1Folders.stream().filter(f -> name.equals(f.getName())).findAny().orElse(null));

			return null;
		});

		final NodeRef fpTplNodeRef = inWriteTx(() -> {

			FinishedProductData fpTplData = new FinishedProductData();
			fpTplData.setName("Finished product Tpl");
			fpTplData.getAspects().add(BeCPGModel.ASPECT_ENTITY_TPL);

			List<NutListDataItem> nutList = new LinkedList<>();

			NutListDataItem parentNut = new NutListDataItem();
			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut1");
			properties.put(PLMModel.PROP_NUTUNIT, "kcal");
			NodeRef nut1 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
							(String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();
			parentNut.setNut(nut1);

			nutList.add(parentNut);

			fpTplData.setNutList(nutList);

			return alfrescoRepository.create(getTestFolderNodeRef(), fpTplData).getNodeRef();

		});

		inWriteTx(() -> {

			FinishedProductData fpTplData = (FinishedProductData) alfrescoRepository.findOne(fpTplNodeRef);

			List<NutListDataItem> nutList = fpTplData.getNutList();
			NutListDataItem parentNut = nutList.get(0);

			Map<QName, Serializable> properties = new HashMap<>();
			properties.put(BeCPGModel.PROP_CHARACT_NAME, "nut2");
			NodeRef nut2 = nodeService.createNode(getTestFolderNodeRef(), ContentModel.ASSOC_CONTAINS,
					QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI,
							(String) properties.get(BeCPGModel.PROP_CHARACT_NAME)),
					PLMModel.TYPE_NUT, properties).getChildRef();

			NutListDataItem childNut = new NutListDataItem();
			childNut.setNut(nut2);
			childNut.setParent(parentNut);

			nutList.add(childNut);

			return alfrescoRepository.create(getTestFolderNodeRef(), fpTplData).getNodeRef();

		});

		// check that an activity is present for the template
		inReadTx(() -> {

			AuditQuery auditFilter = AuditQuery.createQuery().sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
					.filter(ActivityAuditPlugin.ENTITY_NODEREF, fpTplNodeRef.toString());

			List<ActivityListDataItem> activities = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter)
					.stream().map(json -> AuditActivityHelper.parseActivity(json)).toList();

			assertEquals(1, activities.size());

			return null;
		});

		final NodeRef fpNodeRef = inWriteTx(() -> {
			FinishedProductData fpData = new FinishedProductData();
			fpData.setName("Finished product");
			fpData.setEntityTpl(alfrescoRepository.findOne(fpTplNodeRef));

			return alfrescoRepository.create(getTestFolderNodeRef(), fpData).getNodeRef();
		});

		// check no extra activity is created during synchronization with template
		inReadTx(() -> {
			AuditQuery auditFilter = AuditQuery.createQuery().sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
					.filter(ActivityAuditPlugin.ENTITY_NODEREF, fpNodeRef.toString());
			List<ActivityListDataItem> activities = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditFilter)
					.stream().map(json -> AuditActivityHelper.parseActivity(json)).toList();
			assertEquals(1, activities.size());

			return null;
		});

	}

}

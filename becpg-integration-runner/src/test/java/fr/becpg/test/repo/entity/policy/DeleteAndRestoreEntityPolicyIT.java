package fr.becpg.test.repo.entity.policy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.archive.NodeArchiveService;
import org.alfresco.repo.node.archive.RestoreNodeReport;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.PLMModel;
import fr.becpg.test.PLMBaseTestCase;

/**
 * Integration test for DeleteAndRestoreEntityPolicy
 * 
 * Tests the archive, restore, and purge functionality for entities and folders
 * 
 * @author matthieu
 */
public class DeleteAndRestoreEntityPolicyIT extends PLMBaseTestCase {

	private static final Log logger = LogFactory.getLog(DeleteAndRestoreEntityPolicyIT.class);

	@Autowired
	private NodeService nodeService;

	@Autowired
	private NodeArchiveService nodeArchiveService;

	/**
	 * Test that when an entity is archived and restored, its content is properly saved and restored
	 */
	@Test
	public void testArchiveAndRestoreEntity() {
		
		logger.info("Testing archive and restore for entity");
		
		final NodeRef[] testNodes = new NodeRef[1];
		final String testName = "Test Product " + System.currentTimeMillis();
		
		// Create test entity
		testNodes[0] = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			NodeRef productNodeRef = createTestProduct(testName);
			
			// Verify entity exists
			assertTrue("Product should exist", nodeService.exists(productNodeRef));
			assertEquals("Product name should match", testName, nodeService.getProperty(productNodeRef, ContentModel.PROP_NAME));
			
			return productNodeRef;
			
		}, false, true);
		
		// Archive the entity
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("Archiving entity: " + testNodes[0]);
			nodeService.deleteNode(testNodes[0]);
			
			// Verify entity is archived
			assertFalse("Entity should not exist in workspace", nodeService.exists(testNodes[0]));
			
			// Verify archived content exists
			NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testNodes[0].getId());
			assertTrue("Entity should exist in archive", nodeService.exists(archivedNode));
			
			// Verify entity_deleted file exists
			NodeRef entityDeletedRef = getEntityDeletedRef(testNodes[0]);
			assertTrue("Entity deleted file should exist in archive", nodeService.exists(entityDeletedRef));
			
			return null;
			
		}, false, true);
		
		// Restore the entity
		final NodeRef[] restoredNodes = new NodeRef[1];
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("Restoring entity: " + testNodes[0]);
			
			NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testNodes[0].getId());
			List<RestoreNodeReport> reports = nodeArchiveService.restoreArchivedNodes(List.of(archivedNode));
			
			assertNotNull("Restore reports should not be null", reports);
			assertEquals("Should restore one node", 1, reports.size());
			
			RestoreNodeReport report = reports.get(0);
			assertEquals("Restore should be successful", RestoreNodeReport.RestoreStatus.SUCCESS, report.getStatus());
			
			restoredNodes[0] = report.getRestoredNodeRef();
			
			// Verify entity is restored
			assertTrue("Entity should exist after restore", nodeService.exists(restoredNodes[0]));
			assertEquals("Product name should be preserved", testName, nodeService.getProperty(restoredNodes[0], ContentModel.PROP_NAME));
			
			// Verify entity_deleted file is cleaned up
			NodeRef entityDeletedRef = getEntityDeletedRef(testNodes[0]);
			assertFalse("Entity deleted file should be cleaned up after restore", nodeService.exists(entityDeletedRef));
			
			return null;
			
		}, false, true);
		
		// Clean up
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			if (nodeService.exists(restoredNodes[0])) {
				nodeService.deleteNode(restoredNodes[0]);
			}
			NodeRef archivedNode = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, restoredNodes[0].getId());
			if (nodeService.exists(archivedNode)) {
				nodeService.deleteNode(archivedNode);
			}
			return null;
		}, false, true);
	}

	/**
	 * Test that when an entity with children is purged, all entity_deleted files are removed
	 */
	@Test
	public void testPurgeEntityWithHierarchy() {
		
		logger.info("Testing purge with hierarchy");
		
		final NodeRef[] testNodes = new NodeRef[2]; // parent entity, child entity
		
		// Create test hierarchy
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			// Create parent product
			NodeRef parentProduct = createTestProduct("Parent Product " + System.currentTimeMillis());
			testNodes[0] = parentProduct;
			
			// Create child folder and product
			NodeRef childFolder = nodeService.createNode(
				parentProduct,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "childFolder"),
				ContentModel.TYPE_FOLDER
			).getChildRef();
			
			NodeRef childProduct = createTestProduct("Child Product " + System.currentTimeMillis(), childFolder);
			testNodes[1] = childProduct;
			
			// Verify all nodes exist
			assertTrue("Parent product should exist", nodeService.exists(parentProduct));
			assertTrue("Child product should exist", nodeService.exists(childProduct));
			
			return null;
			
		}, false, true);
		
		// Archive the parent (which should archive children too)
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("Archiving parent entity with children");
			nodeService.deleteNode(testNodes[0]);
			
			// Verify all are archived
			assertFalse("Parent should not exist in workspace", nodeService.exists(testNodes[0]));
			
			// Verify entity_deleted files exist for entities
			for (NodeRef testNode : testNodes) {
				if (testNode != null) {
					NodeRef entityDeletedRef = getEntityDeletedRef(testNode);
					assertTrue("Entity deleted file should exist for " + testNode.getId(), nodeService.exists(entityDeletedRef));
				}
			}
			
			return null;
			
		}, false, true);
		
		NodeRef archivedParent = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testNodes[0].getId());
		// Purge the parent
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			logger.info("Purging archived entity");
			assertTrue("Archived parent should exist before purge", nodeService.exists(archivedParent));
			// Purge
			nodeArchiveService.purgeArchivedNode(archivedParent);
			return null;
		}, false, true);
		
		// Purge the parent
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			// Verify parent is purged
			assertFalse("Parent should not exist after purge", nodeService.exists(archivedParent));
			
			// Verify all entity_deleted files are cleaned up
			for (NodeRef testNode : testNodes) {
				if (testNode != null) {
					NodeRef entityDeletedRef = getEntityDeletedRef(testNode);
					assertFalse("Entity deleted file should be cleaned up for " + testNode.getId(), nodeService.exists(entityDeletedRef));
				}
			}
			
			return null;
			
		}, false, true);
	}

	/**
	 * Test that folders can be archived and purged without errors
	 */
	@Test
	public void testArchiveAndPurgeFolders() {
		
		logger.info("Testing archive and purge for folders");
		
		final NodeRef[] testNodes = new NodeRef[2];
		
		// Create test folder hierarchy
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			NodeRef parentFolder = nodeService.createNode(
				getTestFolderNodeRef(),
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testParentFolder"),
				ContentModel.TYPE_FOLDER,
				createFolderProperties("Test Parent Folder " + System.currentTimeMillis())
			).getChildRef();
			
			testNodes[0] = parentFolder;
			
			NodeRef childFolder = nodeService.createNode(
				parentFolder,
				ContentModel.ASSOC_CONTAINS,
				QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "testChildFolder"),
				ContentModel.TYPE_FOLDER,
				createFolderProperties("Test Child Folder " + System.currentTimeMillis())
			).getChildRef();
			
			testNodes[1] = childFolder;
			
			assertTrue("Parent folder should exist", nodeService.exists(parentFolder));
			assertTrue("Child folder should exist", nodeService.exists(childFolder));
			
			return null;
			
		}, false, true);
		
		// Archive the parent folder
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("Archiving folder hierarchy");
			nodeService.deleteNode(testNodes[0]);
			
			assertFalse("Parent folder should not exist in workspace", nodeService.exists(testNodes[0]));
			
			NodeRef archivedParent = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testNodes[0].getId());
			assertTrue("Parent folder should exist in archive", nodeService.exists(archivedParent));
			
			return null;
			
		}, false, true);
		
		// Purge the parent folder
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			
			logger.info("Purging archived folder");
			
			NodeRef archivedParent = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, testNodes[0].getId());
			
			// This should not throw an exception even though folders don't have entity_deleted files
			nodeService.deleteNode(archivedParent);
			
			assertFalse("Parent folder should not exist after purge", nodeService.exists(archivedParent));
			
			return null;
			
		}, false, true);
	}

	// Helper methods
	private NodeRef getEntityDeletedRef(NodeRef testNode) {
		NodeRef entityDeletedRef = new NodeRef(StoreRef.STORE_REF_ARCHIVE_SPACESSTORE, entityDeletedContentId(testNode));
		return entityDeletedRef;
	}

	private String entityDeletedContentId(NodeRef entityNodeRef) {
		return "X_" + entityNodeRef.getId().substring(2);
	}

	private NodeRef createTestProduct(String name) {
		return createTestProduct(name, getTestFolderNodeRef());
	}

	private NodeRef createTestProduct(String name, NodeRef parentRef) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		properties.put(BeCPGModel.PROP_CODE, "TEST-" + System.currentTimeMillis());
		
		return nodeService.createNode(
			parentRef,
			ContentModel.ASSOC_CONTAINS,
			QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, QName.createValidLocalName(name)),
			PLMModel.TYPE_FINISHEDPRODUCT,
			properties
		).getChildRef();
	}

	private Map<QName, Serializable> createFolderProperties(String name) {
		Map<QName, Serializable> properties = new HashMap<>();
		properties.put(ContentModel.PROP_NAME, name);
		return properties;
	}

}
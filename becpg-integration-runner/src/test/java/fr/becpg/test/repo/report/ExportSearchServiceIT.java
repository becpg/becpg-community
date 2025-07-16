package fr.becpg.test.repo.report;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadModel;
import org.alfresco.service.cmr.download.DownloadService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.report.search.ExportSearchService;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.test.RepoBaseTestCase;

/**
 *
 * @author matthieu
 *
 */
public class ExportSearchServiceIT extends RepoBaseTestCase {

	private Log logger = LogFactory.getLog(ExportSearchServiceIT.class);

	public static final long MAX_TIME = 5000;

	private static final long PAUSE_TIME = 1000;

	@Autowired
	DownloadService downloadService;

	@Autowired
	ExportSearchService exportSearchService;

	@Autowired
	NamespaceService namespaceService;

	public NodeRef createTestNode(NodeRef parentNodeRef, QName type, String name) {

		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			NodeRef entityNodeRef = nodeService.createNode(parentNodeRef, ContentModel.ASSOC_CONTAINS,
					QName.createQNameWithValidLocalName(NamespaceService.CONTENT_MODEL_1_0_URI, name), type).getChildRef();
			nodeService.setProperty(entityNodeRef, ContentModel.PROP_NAME, name);
			return entityNodeRef;

		}, false, true);
	}

	public NodeRef createNodeWithContent(NodeRef parentNodeRef, QName type, String name, String contentPath) {
		NodeRef entityNodeRef = createTestNode(parentNodeRef, type, name);
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			ContentWriter writer = contentService.getWriter(entityNodeRef, ContentModel.PROP_CONTENT, true);
			if (writer != null) {

				ClassPathResource resource = new ClassPathResource(contentPath);
				writer.putContent(resource.getInputStream());
			}
			return entityNodeRef;

		}, false, true);
	}

	@Test
	public void testExportZip() throws Exception {

		Set<String> allEntries = new HashSet<>();
		NodeRef templateNodeRef = createNodeWithContent(getTestFolderNodeRef(), ReportModel.TYPE_REPORT_TPL, "ExportZip.xml",
				"beCPG/birt/exportsearch/ExportZip.xml");

		List<NodeRef> searchResults = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			NodeRef entityNodeRef = createTestNode(getTestFolderNodeRef(), BeCPGModel.TYPE_ENTITY_V2, "Export Zip Test " + i);

			NodeRef subFolder = createTestNode(entityNodeRef, ContentModel.TYPE_FOLDER, "Test folder");

			createNodeWithContent(entityNodeRef, ContentModel.TYPE_FOLDER, "img" + i + ".jpeg", "beCPG/birt/productImage.jpg");

			createNodeWithContent(subFolder, ContentModel.TYPE_FOLDER, "subimg" + i + ".jpeg", "beCPG/birt/productImage.jpg");

			searchResults.add(entityNodeRef);

			allEntries.add("subimg" + i + ".jpeg - Export Zip Test " + i + ".jpeg");
			allEntries.add("Sub folder/Image - Export Zip Test " + i + ".jpeg");
			allEntries.add("Sub folder2/img" + i + ".jpeg");

		}

		NodeRef downloadNode = transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			return exportSearchService.createReport(BeCPGModel.TYPE_ENTITY_V2, templateNodeRef, searchResults, ReportFormat.ZIP);
		}, false, true);

		// Validate that the download node has been persisted correctly.
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			Map<QName, Serializable> properties = nodeService.getProperties(downloadNode);
			Assert.assertEquals(Boolean.FALSE, properties.get(DownloadModel.PROP_RECURSIVE));

			List<AssociationRef> associations = nodeService.getTargetAssocs(downloadNode, DownloadModel.ASSOC_REQUESTED_NODES);
			for (AssociationRef association : associations) {
				Assert.assertTrue(searchResults.contains(association.getTargetRef()));
			}

			org.junit.Assert.assertTrue(nodeService.hasAspect(downloadNode, ContentModel.ASPECT_INDEX_CONTROL));
			Assert.assertEquals(Boolean.FALSE, properties.get(ContentModel.PROP_IS_INDEXED));
			Assert.assertEquals(Boolean.FALSE, properties.get(ContentModel.PROP_IS_CONTENT_INDEXED));

			return null;
		}, false, true);

		DownloadStatus status = getDownloadStatus(downloadNode);
		System.out.println("Downloading");
		while (status.getStatus() == Status.PENDING) {
			Thread.sleep(PAUSE_TIME);
			status = getDownloadStatus(downloadNode);
			System.out.println("...");
		}

		Assert.assertEquals(30l, status.getTotalFiles());

		long elapsedTime = waitForDownload(downloadNode);

		Assert.assertTrue("Maximum creation time exceeded!", elapsedTime < MAX_TIME);

		// Validate the content.
		final Set<String> entryNames = getEntries(downloadNode);

		for (String expectedEntry : entryNames) {
			logger.info("Testing Zip Entry:" + expectedEntry);
		}

		validateEntries(entryNames, allEntries, true);

	}

	private void validateEntries(final Set<String> entryNames, final Set<String> expectedEntries, boolean onlyExpected) {
		Set<String> copy = new TreeSet<>(entryNames);
		for (String expectedEntry : expectedEntries) {
			Assert.assertTrue("Missing entry:- " + expectedEntry, copy.contains(expectedEntry));
			copy.remove(expectedEntry);
		}

		if (onlyExpected == true) {
			Assert.assertTrue("Unexpected entries", copy.isEmpty());
		}
	}

	private Set<String> getEntries(final NodeRef downloadNode) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			Set<String> entryNames = new TreeSet<>();
			ContentReader reader = contentService.getReader(downloadNode, ContentModel.PROP_CONTENT);
			try(ZipArchiveInputStream zipInputStream = new ZipArchiveInputStream(reader.getContentInputStream())){
				ZipArchiveEntry zipEntry = zipInputStream.getNextEntry();
				while (zipEntry != null) {
					String name = zipEntry.getName();
					entryNames.add(name);
					zipEntry = zipInputStream.getNextEntry();
				}
			} 
			return entryNames;
		});
	}

	private long waitForDownload(final NodeRef downloadNode) throws InterruptedException {
		long startTime = System.currentTimeMillis();
		// Wait for the staus to become done.
		DownloadStatus status;
		long elapsedTime;
		do {
			status = getDownloadStatus(downloadNode);
			elapsedTime = System.currentTimeMillis() - startTime;
			if (status.isComplete() == false) {
				Thread.sleep(PAUSE_TIME);
			}
		} while ((status.isComplete() == false) && (elapsedTime < MAX_TIME));
		return elapsedTime;
	}

	private DownloadStatus getDownloadStatus(final NodeRef downloadNode) {
		return transactionService.getRetryingTransactionHelper().doInTransaction(() -> downloadService.getDownloadStatus(downloadNode), false, true);
	}

}

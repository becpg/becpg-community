
package fr.becpg.repo.report.search.actions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.download.DownloadCancelledException;
import org.alfresco.repo.download.DownloadStatusUpdateService;
import org.alfresco.repo.download.DownloadStorage;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.download.DownloadStatus;
import org.alfresco.service.cmr.download.DownloadStatus.Status;
import org.alfresco.service.cmr.repository.ContentData;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessPermission;
import org.alfresco.service.cmr.view.Exporter;
import org.alfresco.service.cmr.view.ExporterContext;
import org.alfresco.service.cmr.view.ExporterException;
import org.alfresco.service.namespace.QName;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream.UnicodeExtraFieldPolicy;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.expressions.ExpressionService;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Handler for exporting node content to a ZIP file
 *
 * @author Matthieu
 * @version $Id: $Id
 */
public class ZipSearchDownloadExporter implements Exporter {
	private static Logger log = LoggerFactory.getLogger(ZipSearchDownloadExporter.class);

	private ZipArchiveOutputStream zipStream;
	private OutputStream outputStream;

	private NodeRef downloadNodeRef;
	private int sequenceNumber = 1;
	private long done;
	private long filesAddedCount;

	private long size = 0;
	private long fileCount = 0;

	private List<FileToExtract> fileToExtract = new ArrayList<>();

	private class FileToExtract {

		String path;
		String name;
		String destFolder;
		String entityFilter;

		public FileToExtract(String path, String name, String destFolder, String entityFilter) {
			super();
			this.path = path;
			this.name = name;
			this.destFolder = destFolder;
			this.entityFilter = entityFilter;
		}

		@Override
		public String toString() {
			return "FileToExtract [path=" + path + ", name=" + name + ", destFolder=" + destFolder + ", entityFilter = " + entityFilter + "]";
		}

	}

	private RetryingTransactionHelper transactionHelper;
	private DownloadStorage downloadStorage;
	private DownloadStatusUpdateService updateService;
	private CheckOutCheckInService checkOutCheckInService;
	private NodeService nodeService;
	private ContentService contentService;
	private ExpressionService expressionService;
	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	/**
	 * <p>
	 * Getter for the field <code>size</code>.
	 * </p>
	 *
	 * @return a long.
	 */
	public long getSize() {
		return size;
	}

	/**
	 * <p>
	 * Getter for the field <code>fileCount</code>.
	 * </p>
	 *
	 * @return a long.
	 */
	public long getFileCount() {
		return fileCount;
	}

	/**
	 * <p>
	 * Constructor for ZipSearchDownloadExporter.
	 * </p>
	 *
	 * @param namespaceService
	 *            a {@link org.alfresco.service.namespace.NamespaceService}
	 *            object.
	 * @param checkOutCheckInService
	 *            a {@link org.alfresco.service.cmr.coci.CheckOutCheckInService}
	 *            object.
	 * @param nodeService
	 *            a {@link org.alfresco.service.cmr.repository.NodeService}
	 *            object.
	 * @param transactionHelper
	 *            a
	 *            {@link org.alfresco.repo.transaction.RetryingTransactionHelper}
	 *            object.
	 * @param updateService
	 *            a
	 *            {@link org.alfresco.repo.download.DownloadStatusUpdateService}
	 *            object.
	 * @param downloadStorage
	 *            a {@link org.alfresco.repo.download.DownloadStorage} object.
	 * @param contentService
	 *            a {@link org.alfresco.service.cmr.repository.ContentService}
	 *            object.
	 * @param downloadNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param templateNodeRef
	 *            a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public ZipSearchDownloadExporter( CheckOutCheckInService checkOutCheckInService, NodeService nodeService,
			RetryingTransactionHelper transactionHelper, DownloadStatusUpdateService updateService, DownloadStorage downloadStorage,
			ContentService contentService, ExpressionService expressionService, AlfrescoRepository<RepositoryEntity> alfrescoRepository, NodeRef downloadNodeRef, NodeRef templateNodeRef) {

		this.updateService = updateService;
		this.transactionHelper = transactionHelper;
		this.downloadStorage = downloadStorage;

		this.downloadNodeRef = downloadNodeRef;
		this.checkOutCheckInService = checkOutCheckInService;
		this.nodeService = nodeService;
		this.contentService = contentService;
		this.expressionService = expressionService;
		this.alfrescoRepository = alfrescoRepository;

		try {
			readFileMapping(templateNodeRef);
		} catch (Exception e) {
			throw new ExporterException("Failed to read zip search mapping", e);
		}

	}

	private void readFileMapping(NodeRef templateNodeRef) throws DocumentException {

		//
		//
		// <?xml version="1.0" encoding="UTF-8"?>
		// <export format="zip">
		// <node type="bcpg:product">
		// <file path="Images/produit.jpg" name="img_produit${bcpg:erpCode}.jpg"
		// destFolder="."></file>
		// <file path="Documents/${cm:name} - Fiche Technique Client.pdf"
		// name="FT_client${bcpg:erpCode}.pdf" destFolder="./FTs/"></file>
		// <file path="Annexes/*.pdf" />
		// </node>
		// </export>

		ContentReader reader = contentService.getReader(templateNodeRef, ContentModel.PROP_CONTENT);

		SAXReader saxReader = new SAXReader();
		try {
			saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (SAXException e) {
			log.error(e.getMessage(), e);
		}

		Document doc = saxReader.read(reader.getContentInputStream());
		Element queryElt = doc.getRootElement();
		List<Node> columnNodes = queryElt.selectNodes("file");
		for (Node columnNode : columnNodes) {

			FileToExtract tmp = new FileToExtract(columnNode.valueOf("@path"), columnNode.valueOf("@name"), columnNode.valueOf("@destFolder"), columnNode.valueOf("@entityFilter"));

			fileToExtract.add(tmp);
		}

	}

	/**
	 * <p>
	 * setZipFile.
	 * </p>
	 *
	 * @param zipFile
	 *            a {@link java.io.File} object.
	 */
	public void setZipFile(File zipFile) {
		try {
			this.outputStream = new FileOutputStream(zipFile);
		} catch (FileNotFoundException e) {
			throw new ExporterException("Failed to create zip file", e);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void start(final ExporterContext context) {
		if (outputStream != null) {
			zipStream = new ZipArchiveOutputStream(outputStream);
			zipStream.setEncoding("UTF-8");
			zipStream.setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy.ALWAYS);
			zipStream.setUseLanguageEncodingFlag(true);
			zipStream.setFallbackToUTF8(true);
		}
	}

	Map<String, Set<NodeRef>> cache = new HashMap<>();

	/** {@inheritDoc} */
	@Override
	public void startNode(NodeRef entityNodeRef) {

		for (FileToExtract fileMapping : fileToExtract) {
			String key = fileMapping.path + fileMapping.path + fileMapping.entityFilter + entityNodeRef.toString();

			Set<NodeRef> toExtractNodes = cache.get(key);

			if (toExtractNodes == null) {
				toExtractNodes = new HashSet<>();

				if (testEntityCondition(fileMapping.entityFilter, alfrescoRepository.findOne(entityNodeRef))) {
					List<NodeRef> files = BeCPGQueryBuilder.createQuery().selectNodesByPath(entityNodeRef, expressionService.extractExpr(entityNodeRef, fileMapping.path));
					toExtractNodes.addAll(files);
				}

				cache.put(key, toExtractNodes);
			}

			for (NodeRef fileNodeRef : toExtractNodes) {
				if ((fileNodeRef != null) && isExportable(fileNodeRef)) {

					// Estimator mode
					if (outputStream == null) {

						if (ReportModel.TYPE_REPORT.equals(nodeService.getType(fileNodeRef))) {
							// arbitrary 500k file for report
							size = size + 500;
						} else {
							ContentReader reader = contentService.getReader(fileNodeRef, ContentModel.PROP_CONTENT);
							if ((reader != null) && (reader.exists())) {
								// export an empty url for the content
								ContentData contentData = reader.getContentData();
								size = size + contentData.getSize();

							}
						}
						fileCount = fileCount + 1;
					} else {
						ContentReader reader = contentService.getReader(fileNodeRef, ContentModel.PROP_CONTENT);
						if ((reader != null) && (reader.exists())) {
							reader.getContentData();

							String folderName = null;

							if ((fileMapping.destFolder != null) && !fileMapping.destFolder.isEmpty()) {
								folderName =  expressionService.extractExpr(entityNodeRef, fileMapping.destFolder);
								if (!folderName.endsWith("/")) {
									folderName += "/";
								}
							}

							String path = (folderName != null ? folderName : "") + createName(fileMapping, fileNodeRef, entityNodeRef);

							try {

								// ALF-2016
								ZipArchiveEntry zipEntry = new ZipArchiveEntry(path);
								zipStream.putArchiveEntry(zipEntry);

								// copy export stream to zip
								copyStream(zipStream, reader.getContentInputStream());

								zipStream.closeArchiveEntry();
								filesAddedCount++;
								updateStatus();
							} catch (IOException e) {
								throw new ExporterException("Failed to zip export stream", e);
							}

						}

					}

				}
			}
		}

	}
	
	private boolean testEntityCondition(String condition, RepositoryEntity entity) {

		if (condition == null || condition.isBlank()) {
			return true;
		}
		
		if (!(condition.startsWith("spel") || condition.startsWith("js"))) {
			condition = "spel(" + condition + ")";
		}
		
		Object filter = expressionService.eval(condition, entity);
		
		return filter != null && Boolean.parseBoolean(filter.toString());

	}

	/** {@inheritDoc} */
	@Override
	public void end() {
		if (outputStream != null) {
			try {
				zipStream.close();
				outputStream.close();
			} catch (IOException error) {
				throw new ExporterException("Unexpected error closing zip stream!", error);
			}
		}
	}

	private boolean isExportable(NodeRef fileNodeRef) {
		if (checkOutCheckInService.isCheckedOut(fileNodeRef)) {
			String owner = (String) nodeService.getProperty(fileNodeRef, ContentModel.PROP_LOCK_OWNER);
			if (AuthenticationUtil.getRunAsUser().equals(owner)) {
				return false;
			}
		}

		if (checkOutCheckInService.isWorkingCopy(fileNodeRef)) {
			String owner = (String) nodeService.getProperty(fileNodeRef, ContentModel.PROP_WORKING_COPY_OWNER);
			if (!AuthenticationUtil.getRunAsUser().equals(owner)) {
				return false;
			}
		}
		return true;
	}

	private String createName(FileToExtract fileMapping, NodeRef docNodeRef, NodeRef entityNodeRef) {
		if ((fileMapping.name != null) && !fileMapping.name.isEmpty()) {
			return expressionService.extractExpr(entityNodeRef, docNodeRef, fileMapping.name);
		}
		return (String) nodeService.getProperty(docNodeRef, ContentModel.PROP_NAME);
	}

	

	/**
	 * Copy input stream to output stream
	 *
	 * @param output
	 *            output stream
	 * @param in
	 *            input stream
	 * @throws IOException
	 */
	private void copyStream(OutputStream output, InputStream in) throws IOException {
		byte[] buffer = new byte[2048 * 10];
		int read = in.read(buffer, 0, 2048 * 10);
		while (read != -1) {
			output.write(buffer, 0, read);
			done = done + read;

			read = in.read(buffer, 0, 2048 * 10);
		}
	}

	private void updateStatus() {
		transactionHelper.doInTransaction(() -> {
			DownloadStatus status = new DownloadStatus(Status.IN_PROGRESS, done, size, filesAddedCount, fileCount);

			updateService.update(downloadNodeRef, status, getNextSequenceNumber());
			return null;
		}, false, true);

		boolean downloadCancelled = transactionHelper.doInTransaction(() -> downloadStorage.isCancelled(downloadNodeRef), true, true);

		if (downloadCancelled) {
			log.debug("Download cancelled");
			throw new DownloadCancelledException();
		}
	}

	/**
	 * <p>
	 * getNextSequenceNumber.
	 * </p>
	 *
	 * @return a int.
	 */
	public int getNextSequenceNumber() {
		return sequenceNumber++;
	}

	/**
	 * <p>
	 * Getter for the field <code>done</code>.
	 * </p>
	 *
	 * @return a long.
	 */
	public long getDone() {
		return done;
	}

	/**
	 * <p>
	 * getFilesAdded.
	 * </p>
	 *
	 * @return a long.
	 */
	public long getFilesAdded() {
		return filesAddedCount;
	}

	/** {@inheritDoc} */
	@Override
	public void startNamespace(String prefix, String uri) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endNamespace(String prefix) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endNode(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startReference(NodeRef nodeRef, QName childName) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endReference(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startAspects(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startAspect(NodeRef nodeRef, QName aspect) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endAspect(NodeRef nodeRef, QName aspect) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endAspects(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startACL(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void permission(NodeRef nodeRef, AccessPermission permission) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endACL(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startProperties(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startProperty(NodeRef nodeRef, QName property) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endProperty(NodeRef nodeRef, QName property) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endProperties(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startValueCollection(NodeRef nodeRef, QName property) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startValueMLText(NodeRef nodeRef, Locale locale, boolean isNull) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endValueMLText(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void value(NodeRef nodeRef, QName property, Object value, int index) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void content(NodeRef nodeRef, QName property, InputStream content, ContentData contentData, int index) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endValueCollection(NodeRef nodeRef, QName property) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startAssocs(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void startAssoc(NodeRef nodeRef, QName assoc) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endAssoc(NodeRef nodeRef, QName assoc) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void endAssocs(NodeRef nodeRef) {
		// empty

	}

	/** {@inheritDoc} */
	@Override
	public void warning(String warning) {
		// empty

	}
}

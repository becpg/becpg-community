package fr.becpg.repo.jscript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.model.FileExistsException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.TempFileProvider;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * <p>PDFScriptHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PDFScriptHelper extends BaseScopableProcessorExtension {

	private static final String FILE_MIMETYPE = "application/pdf";

	private NodeService nodeService;
	private ContentService contentService;
	private DictionaryService dictionaryService;

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	/**
	 * <p>Setter for the field <code>dictionaryService</code>.</p>
	 *
	 * @param dictionaryService a {@link org.alfresco.service.cmr.dictionary.DictionaryService} object.
	 */
	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	private static Log logger = LogFactory.getLog(PDFScriptHelper.class);

	/**
	 * <p>appendPDF.</p>
	 *
	 * @param targetPDFNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @param toAppendPDFNode a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 * @return a {@link org.alfresco.repo.jscript.ScriptNode} object.
	 */
	public ScriptNode appendPDF(ScriptNode targetPDFNode, ScriptNode toAppendPDFNode) {
		logger.debug("Append PDF " + toAppendPDFNode.getName() + " to " + targetPDFNode.getName());

		try (InputStream is = getReader(toAppendPDFNode.getNodeRef()).getContentInputStream();
				InputStream tis = getReader(targetPDFNode.getNodeRef()).getContentInputStream();
				PDDocument pdf = PDDocument.load(is);
				PDDocument pdfTarget = PDDocument.load(tis)) {
			// Append the PDFs using PDFMergerUtility
			PDFMergerUtility merger = new PDFMergerUtility();
			merger.appendDocument(pdfTarget, pdf);
			merger.setDestinationFileName(targetPDFNode.getName());
			merger.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());

			// Create a temporary directory for saving the merged PDF
			Path tempDir = Files.createTempDirectory(TempFileProvider.getTempDir().toPath(), targetPDFNode.getNodeRef().getId());

			Path mergedPDFPath = tempDir.resolve(targetPDFNode.getName());
			pdfTarget.save(mergedPDFPath.toFile());

			// Save the merged PDF back to the repository
			saveMergedPDF(targetPDFNode, mergedPDFPath.toFile());

			// Clean up the temporary file and directory

			Files.delete(mergedPDFPath); 

			Files.delete(tempDir); 

		} catch (IOException e) {
			throw new AlfrescoRuntimeException("Error processing PDF documents", e);
		}

		return targetPDFNode;
	}

	private void saveMergedPDF(ScriptNode targetPDFNode, File mergedPDFFile) {
		ContentReader targetReader = getReader(targetPDFNode.getNodeRef());
		ContentWriter writer = contentService.getWriter(targetPDFNode.getNodeRef(), ContentModel.PROP_CONTENT, true);

		writer.setEncoding(targetReader.getEncoding()); // Use original encoding
		writer.setMimetype(FILE_MIMETYPE);

		// Write the content back to the repository
		try {
			writer.putContent(mergedPDFFile);
		} catch (FileExistsException e) {
			throw new AlfrescoRuntimeException("Failed to save merged PDF to repository", e);
		}
	}

	private ContentReader getReader(NodeRef nodeRef) {
		// first, make sure the node exists
		if (!nodeService.exists(nodeRef)) {
			// node doesn't exist - can't do anything
			throw new AlfrescoRuntimeException("NodeRef: " + nodeRef + " does not exist");
		}

		// Next check that the node is a sub-type of content
		QName typeQName = nodeService.getType(nodeRef);
		if (!dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT)) {
			// it is not content, so can't transform
			throw new AlfrescoRuntimeException("The selected node is not a content node");
		}

		// Get the content reader. If it is null, can't do anything here
		ContentReader contentReader = contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		if (contentReader == null) {
			throw new AlfrescoRuntimeException("The content reader for NodeRef: " + nodeRef + "is null");
		}

		return contentReader;
	}

}

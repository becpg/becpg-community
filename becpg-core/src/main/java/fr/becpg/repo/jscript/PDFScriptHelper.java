package fr.becpg.repo.jscript;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

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
		
		logger.debug("Append PDF "+toAppendPDFNode.getName()+" to "+targetPDFNode.getName());
		
		PDDocument pdf = null;
		PDDocument pdfTarget = null;
		InputStream is = null;
		InputStream tis = null;
		File tempDir = null;
		ContentWriter writer = null;

		try {

			ContentReader append = getReader(toAppendPDFNode.getNodeRef());
			is = append.getContentInputStream();

			ContentReader targetReader = getReader(targetPDFNode.getNodeRef());
			tis = targetReader.getContentInputStream();

			// stream the document in
			pdf = PDDocument.load(is);
			pdfTarget = PDDocument.load(tis);

			// Append the PDFs
			PDFMergerUtility merger = new PDFMergerUtility();
			merger.appendDocument(pdfTarget, pdf);
			merger.setDestinationFileName(targetPDFNode.getName());
			merger.mergeDocuments();

			// build a temp dir name based on the ID of the noderef we are
			// importing
			File alfTempDir = TempFileProvider.getTempDir();
			tempDir = new File(alfTempDir.getPath() + File.separatorChar + targetPDFNode.getNodeRef().getId());
			tempDir.mkdir();

			pdfTarget.save(tempDir + "" + File.separatorChar + targetPDFNode.getName());

			for (File file : tempDir.listFiles()) {
				try {
					if (file.isFile()) {

						writer = contentService.getWriter(targetPDFNode.getNodeRef(), ContentModel.PROP_CONTENT, true);

						writer.setEncoding(targetReader.getEncoding()); // original
						// encoding
						writer.setMimetype(FILE_MIMETYPE);

						// Put it in the repo
						writer.putContent(file);

						// Clean up
						if(!file.delete()) {
							logger.error("Cannot delete file: "+file.getName());
						}
					}
				} catch (FileExistsException e) {
					throw new AlfrescoRuntimeException("Failed to process file.", e);
				}
			}
		} catch (IOException e) {
			throw new AlfrescoRuntimeException(e.getMessage(), e);
		}

		finally {
			if (pdf != null) {
				try {
					pdf.close();
				} catch (IOException e) {
					throw new AlfrescoRuntimeException(e.getMessage(), e);
				}
			}
			if (pdfTarget != null) {
				try {
					pdfTarget.close();
				} catch (IOException e) {
					throw new AlfrescoRuntimeException(e.getMessage(), e);
				}
			}
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
					throw new AlfrescoRuntimeException(e.getMessage(), e);
				}
			}

			if (tempDir != null) {
				if(!tempDir.delete()) {
					logger.error("Cannot delete dir: "+tempDir.getName());
				}
			}
		}

		return targetPDFNode;
	}

	private ContentReader getReader(NodeRef nodeRef) {
		// first, make sure the node exists
		if (nodeService.exists(nodeRef) == false) {
			// node doesn't exist - can't do anything
			throw new AlfrescoRuntimeException("NodeRef: " + nodeRef + " does not exist");
		}

		// Next check that the node is a sub-type of content
		QName typeQName = nodeService.getType(nodeRef);
		if (dictionaryService.isSubClass(typeQName, ContentModel.TYPE_CONTENT) == false) {
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

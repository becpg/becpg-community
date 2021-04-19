package fr.becpg.repo.web.scripts.document;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.redsix.pdfcompare.CompareResult;
import de.redsix.pdfcompare.PdfComparator;

@Service
public class PdfFileComparePlugin implements CompareDocumentPlugin {

	private static final String PDF_IDENTIFIER = "pdf";
	
	private static final String RESULT_FILE_NAME = "result";
	
	private static final String PDF_EXTENSION = ".pdf";

	private static final Log logger = LogFactory.getLog(PdfFileComparePlugin.class);

	@Autowired
	private ContentService contentService;

	@Autowired
	private MimetypeService mimetypeService;
	
	@Autowired
	private NodeService nodeService;

	@Override
	public File compare(NodeRef node1, NodeRef node2) throws IOException {
		
		File file1 = createContentFile(node1);
		File file2 = createContentFile(node2);

		PdfComparator<CompareResult> pdfComparator = new PdfComparator<>(file1, file2);
		
		File result = File.createTempFile(RESULT_FILE_NAME, PDF_EXTENSION);
		
		String name = result.getName();
        String fileName = name.replace(PDF_EXTENSION, "");
		
		pdfComparator.compare().writeTo(result.getPath().replace(name, fileName));

		deleteFile(file1);
		deleteFile(file2);

		return result;
		
	}
	
	@Override
	public boolean accepts(NodeRef nodeRef) {
		
		ContentReader reader = this.contentService.getReader(nodeRef, ContentModel.PROP_CONTENT);

		String mimeType = mimetypeService.guessMimetype(null, reader);

		return mimeType.contains(PDF_IDENTIFIER);
		
	}
	
	
	private File createContentFile(NodeRef entity) throws IOException {
		String name = (String) nodeService.getProperty(entity, ContentModel.PROP_NAME);
		
		File file = File.createTempFile(name.split("\\.")[0], "." + name.split("\\.")[1]);
		
		ContentReader reader = this.contentService.getReader(entity, ContentModel.PROP_CONTENT);

		if (reader != null) {
			reader.getContent(new FileOutputStream(file));
		}

		return file;
	}
	
	private void deleteFile(File file) throws IOException {
		try {
			Files.delete(file.toPath());
		} catch (NoSuchFileException e) {
			logger.debug("File does not exist : " + file.getName(), e);
		}
	}
}

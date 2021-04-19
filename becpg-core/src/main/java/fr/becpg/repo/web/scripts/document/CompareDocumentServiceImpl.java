package fr.becpg.repo.web.scripts.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;

import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.helper.AttachmentHelper;

@Service
public class CompareDocumentServiceImpl implements CompareDocumentService {
	
	@Autowired
	private MimetypeService mimetypeService;
	
	@Autowired
	private CompareDocumentPlugin[] compareDocumentPlugins;

	@Override
	public void compare(NodeRef actualNode, NodeRef versionNode, WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		File resultFile = null;

		// iterate over the compare services and find the one that can handle the files
		for (CompareDocumentPlugin compareDocumentPlugin : compareDocumentPlugins) {
			if (compareDocumentPlugin.accepts(actualNode) && compareDocumentPlugin.accepts(versionNode)) {
				
				resultFile = compareDocumentPlugin.compare(actualNode, versionNode);
				
				break;
			}
		}

		// write the result content into the webscript response
		if (resultFile != null) {
			writeOutputResult(req, res, resultFile);
		}

	}

	private void writeOutputResult(WebScriptRequest req, WebScriptResponse res, File resultFile) throws IOException {
		try (FileInputStream in = new FileInputStream(resultFile)) {

			IOUtils.copy(in, res.getOutputStream());
			
		}

		res.setContentType(mimetypeService.guessMimetype(resultFile.getName()));
		AttachmentHelper.setAttachment(req, res, resultFile.getName());
		Files.delete(resultFile.toPath());
	}

}

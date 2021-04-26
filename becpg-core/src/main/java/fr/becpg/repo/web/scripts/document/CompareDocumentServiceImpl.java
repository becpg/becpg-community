package fr.becpg.repo.web.scripts.document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.poi.util.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CompareDocumentServiceImpl implements CompareDocumentService {
	
	@Autowired
	private CompareDocumentPlugin[] compareDocumentPlugins;

	@Override
	public String compare(NodeRef actualNode, NodeRef versionNode, OutputStream out) throws IOException {
		
		File resultFile = null;

		// iterate over the compare services and find the one that can handle the files
		for (CompareDocumentPlugin compareDocumentPlugin : compareDocumentPlugins) {
			if (compareDocumentPlugin.accepts(actualNode) && compareDocumentPlugin.accepts(versionNode)) {
				
				resultFile = compareDocumentPlugin.compare(actualNode, versionNode);
				
				break;
			}
		}

		if (resultFile != null) {
			writeOutputResult(out, resultFile);
			return resultFile.getName();
		}
		
		return null;

	}

	private void writeOutputResult(OutputStream out, File resultFile) throws IOException {
		try (FileInputStream in = new FileInputStream(resultFile)) {
			IOUtils.copy(in, out);
		}
		Files.delete(resultFile.toPath());
	}

}

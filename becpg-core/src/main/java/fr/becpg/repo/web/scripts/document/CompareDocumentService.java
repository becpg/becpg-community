package fr.becpg.repo.web.scripts.document;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

public interface CompareDocumentService {

	public void compare(NodeRef actualNode, NodeRef versionNode, WebScriptRequest req, WebScriptResponse res) throws IOException;

}
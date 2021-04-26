package fr.becpg.repo.web.scripts.document;

import java.io.IOException;
import java.io.OutputStream;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CompareDocumentService {

	public String compare(NodeRef actualNode, NodeRef versionNode, OutputStream out) throws IOException;

}
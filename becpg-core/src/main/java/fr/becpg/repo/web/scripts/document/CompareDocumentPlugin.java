package fr.becpg.repo.web.scripts.document;

import java.io.File;
import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;

public interface CompareDocumentPlugin {

	public File compare(NodeRef node1, NodeRef node2) throws IOException;

	public boolean accepts(NodeRef nodeRef);

}
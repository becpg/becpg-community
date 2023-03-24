package fr.becpg.repo.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.ProjectData;

public interface SignatureProjectPlugin {
	
	NodeRef prepareEntitySignatureFolder(NodeRef projectNodeRef, NodeRef entityNodeRef);
	
	NodeRef getExternalSignatureFolder(NodeRef projectNodeRef, List<NodeRef> documents, List<NodeRef> viewRecipients);

	void createClosingTask(ProjectData project, List<NodeRef> lastsTasks);

	boolean applyTo(String projectType);

	List<NodeRef> extractRecipients(NodeRef document);

}

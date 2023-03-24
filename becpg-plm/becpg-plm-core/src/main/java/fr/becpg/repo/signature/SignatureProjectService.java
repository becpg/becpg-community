package fr.becpg.repo.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface SignatureProjectService {
	
	NodeRef prepareSignatureProject(NodeRef projectNodeRef, List<NodeRef> documents);

	NodeRef createEntitySignatureTasks(NodeRef projectNodeRef, NodeRef previousTask, String projectType);

	List<NodeRef> extractRecipients(NodeRef nodeRef);
		
}

package fr.becpg.repo.annotation;

import org.alfresco.service.cmr.repository.NodeRef;

public interface AnnotationService {

	public String uploadDocument(NodeRef nodeRef);
	public String createSession(NodeRef nodeRef, String userId, int sessionDurationInDays);
	public void exportDocument(NodeRef nodeRef);
	public void deleteDocument(NodeRef nodeRef);
}

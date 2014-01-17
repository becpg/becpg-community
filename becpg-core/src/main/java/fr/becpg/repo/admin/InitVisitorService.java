package fr.becpg.repo.admin;

import org.alfresco.service.cmr.repository.NodeRef;

public interface InitVisitorService {
	
	void run(NodeRef companyHomeNodeRef);

	boolean shouldInit(NodeRef companyHomeNodeRef);
	
}

package fr.becpg.repo.admin;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * @author querephi
 */
public interface InitVisitor {
	
	public void visitContainer(NodeRef nodeRef);

	public boolean shouldInit(NodeRef companyHomeNodeRef);
}

package fr.becpg.repo.importer.user;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.importer.ImporterException;

/**
 * 
 * @author matthieu
 *
 */
public interface UserImporterService {

	public void importUser(NodeRef nodeRef) throws ImporterException;


}

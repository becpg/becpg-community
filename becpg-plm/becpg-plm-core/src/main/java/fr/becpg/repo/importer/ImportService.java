/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * The Interface ImportService.
 *
 * @author querephi
 */
public interface ImportService {
		
	/**
	 * Import text.
	 *
	 * @param nodeRef the node ref
	 * @param doUpdate the do update
	 */
	List<String> importText(NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction) throws Exception;
	
	/**
	 * Move the imported file in the Succeeded or Failed folder
	 * @param nodeRef
	 * @param hasFailed
	 */
	void moveImportedFile(NodeRef nodeRef, boolean hasFailed, String titleLog, String fileLog);

	
	void writeLogInFileTitle(NodeRef nodeRef, String log, boolean hasFailed);
}

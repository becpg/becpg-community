/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.batch.BatchInfo;


/**
 * The Interface ImportService.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface ImportService {
		
	/**
	 * Import text.
	 *
	 * @param nodeRef the node ref
	 * @param doUpdate the do update
	 * @param requiresNewTransaction a boolean.
	 * @return a {@link java.util.List} object.
	 * @throws java.lang.Exception if any.
	 */
	BatchInfo importText(NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction, List<String> errors) throws Exception;
	
	/**
	 * Move the imported file in the Succeeded or Failed folder
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param hasFailed a boolean.
	 * @param titleLog a {@link java.lang.String} object.
	 * @param fileLog a {@link java.lang.String} object.
	 */
	void moveImportedFile(NodeRef nodeRef, boolean hasFailed, String titleLog, String fileLog);

	
	/**
	 * <p>writeLogInFileTitle.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param log a {@link java.lang.String} object.
	 * @param hasFailed a boolean.
	 */
	void writeLogInFileTitle(NodeRef nodeRef, String log, boolean hasFailed);
}

/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.importer;

import java.io.IOException;
import java.text.ParseException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;


// TODO: Auto-generated Javadoc
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
	public List<String> importText(NodeRef nodeRef, boolean doUpdate, boolean requiresNewTransaction) throws  ImporterException, IOException, ParseException, Exception;
	
	/**
	 * Move the imported file in the Succeeded or Failed folder
	 * @param nodeRef
	 * @param hasFailed
	 */
	public void moveImportedFile(NodeRef nodeRef, boolean hasFailed, String titleLog, String fileLog);

	
	public void writeLogInFileTitle( NodeRef nodeRef,String log, boolean hasFailed);
}

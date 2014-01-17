/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
 *  
 * This file is part of beCPG 
 *  
 * beCPG is free software: you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version. 
 *  
 * beCPG is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 * GNU Lesser General Public License for more details. 
 *  
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.action.executer;

import java.util.Calendar;
import java.util.List;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.importer.user.UserImporterService;

/**
 * 
 * @author matthieu
 * 
 */
public class UserImporterActionExecuter extends ActionExecuterAbstractBase {
	
	private static Log _logger = LogFactory.getLog(UserImporterActionExecuter.class);

	public static final String NAME = "import-user";
	public static final String PARAM_VALUE_EXTENSION = ".csv";
	
	private static final String LOG_STARTING_DATE = "Starting date: ";	
	private static final String LOG_ENDING_DATE = "Ending date: ";	
	private static final String LOG_ERROR = "Error: ";	
	private static final String LOG_SEPARATOR = "\n";

	private UserImporterService userImporterService;
	
	private ImportService importService;
	
	private NodeService nodeService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * Sets the import service.
	 *
	 * @param importService the new import service
	 */
	public void setImportService(ImportService importService) {
		this.importService = importService;
	}	

	public void setUserImporterService(UserImporterService userImporterService) {
		this.userImporterService = userImporterService;
	}

	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		// import file
		String log = LOG_STARTING_DATE + Calendar.getInstance().getTime();
		boolean hasFailed = false;
		
		if (_logger.isDebugEnabled()) {
			_logger.debug("Executing importusercsv action");
		}
		
		try {
			userImporterService.importUser(actionedUponNodeRef);
		} catch (ImporterException e) {
			hasFailed = true;
			_logger.error("Cannot import users",e);
		
			
			log += LOG_SEPARATOR;
			log += LOG_ERROR + e.getMessage();
		} 
		finally{
			log += LOG_SEPARATOR;
			log += LOG_ENDING_DATE + Calendar.getInstance().getTime();
		}
		
		if(nodeService.exists(actionedUponNodeRef)){
   		 
			_logger.debug("move file in folder. HasFailed: " + hasFailed);             		
    		importService.moveImportedFile(actionedUponNodeRef, hasFailed, log, null);                		
    	}        
	}

	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	}
}

/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG. 
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
import java.util.concurrent.atomic.AtomicBoolean;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.importer.ImportService;
import fr.becpg.repo.importer.ImporterException;
import fr.becpg.repo.importer.user.UserImporterService;

/**
 * <p>UserImporterActionExecuter class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UserImporterActionExecuter extends ActionExecuterAbstractBase {
	
	private static final Log _logger = LogFactory.getLog(UserImporterActionExecuter.class);

	/** Constant <code>NAME="import-user"</code> */
	public static final String NAME = "import-user";
	/** Constant <code>PARAM_VALUE_EXTENSION=".csv"</code> */
	public static final String PARAM_CSV_EXTENSION = ".csv";
	/** Constant <code>PARAM_XLSX_EXTENSION=".xlsx"</code> */
	public static final String PARAM_XLSX_EXTENSION = ".xlsx";
	
	private static final String LOG_STARTING_DATE = "Starting date: ";	
	private static final String LOG_ENDING_DATE = "Ending date: ";	
	private static final String LOG_ERROR = "Error: ";	
	private static final String LOG_SEPARATOR = "\n";

	private UserImporterService userImporterService;
	
	private ImportService importService;

	/**
	 * Sets the import service.
	 *
	 * @param importService the new import service
	 */
	public void setImportService(ImportService importService) {
		this.importService = importService;
	}	

	/**
	 * <p>Setter for the field <code>userImporterService</code>.</p>
	 *
	 * @param userImporterService a {@link fr.becpg.repo.importer.user.UserImporterService} object.
	 */
	public void setUserImporterService(UserImporterService userImporterService) {
		this.userImporterService = userImporterService;
	}

	/** {@inheritDoc} */
	@Override
	protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
		// import file
		StringBuilder logBuilder = new StringBuilder(LOG_STARTING_DATE);
		logBuilder.append(Calendar.getInstance().getTime());
		
		AtomicBoolean hasFailed = new AtomicBoolean(Boolean.FALSE);
		
		if (_logger.isDebugEnabled()) {
			_logger.debug("Executing importusercsv action");
		}
		
		try {
			userImporterService.importUser(actionedUponNodeRef);
		} catch (ImporterException e) {
			hasFailed.set(Boolean.TRUE);
			_logger.error("Cannot import users",e);
		
			logBuilder.append(LOG_SEPARATOR);
			logBuilder.append(LOG_ERROR).append(e.getMessage());
		} 
		finally{
			logBuilder.append(LOG_SEPARATOR);
			logBuilder.append(LOG_ENDING_DATE).append(Calendar.getInstance().getTime());
		}
		
		AlfrescoTransactionSupport.bindListener(new TransactionListenerAdapter() {
			@Override
			public void afterCommit() {
				_logger.debug("move file in folder. HasFailed: " + hasFailed.get());
				importService.moveImportedFile(actionedUponNodeRef, hasFailed.get(), logBuilder.toString(), null);
			}
		});
		
	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {

	}
}

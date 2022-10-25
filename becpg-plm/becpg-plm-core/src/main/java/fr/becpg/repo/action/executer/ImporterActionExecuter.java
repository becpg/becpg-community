/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.action.executer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.importer.ImportService;

/**
 * Action used to import text files.
 *
 * @author Quere
 * @version $Id: $Id
 */
public class ImporterActionExecuter extends ActionExecuterAbstractBase {

	/** Constant <code>NAME="import-content"</code> */
	public static final String NAME = "import-content";
	/** Constant <code>PARAM_DO_NOT_MOVE_NODE="doNotMoveNodeParam"</code> */
	public static final String PARAM_DO_NOT_MOVE_NODE = "doNotMoveNodeParam";
	/** Constant <code>CSV_EXTENSION=".csv"</code> */
	public static final String CSV_EXTENSION = ".csv";
	/** Constant <code>XLSX_EXTENSION=".xlsx"</code> */
	public static final String XLSX_EXTENSION = ".xlsx";

	private static final String LOG_STARTING_DATE = "Starting date: ";
	private static final String LOG_ENDING_DATE = "Ending date: ";
	private static final String LOG_ERROR = "Error: ";
	private static final int ERROR_LOGS_LIMIT = 50;
	private static final String LOG_ERROR_MAX_REACHED = "More than " + ERROR_LOGS_LIMIT + " errors, stop printing";
	private static final String LOG_SEPARATOR = "\n";
	private static final String KEY_FILES_TO_IMPORT = "keyFilesToImport";

	private static final Log logger = LogFactory.getLog(ImporterActionExecuter.class);

	private ImportService importService;
	private final TransactionListenerAdapter transactionListener;
	private ThreadPoolExecutor importThreadExecuter;

	/**
	 * <p>Setter for the field <code>importService</code>.</p>
	 *
	 * @param importService a {@link fr.becpg.repo.importer.ImportService} object.
	 */
	public void setImportService(ImportService importService) {
		this.importService = importService;
	}

	/**
	 * <p>Constructor for ImporterActionExecuter.</p>
	 */
	public ImporterActionExecuter() {
		this.transactionListener = new ImportServiceTransactionListener();
	}

	/**
	 * <p>Setter for the field <code>importThreadExecuter</code>.</p>
	 *
	 * @param importThreadExecuter a {@link java.util.concurrent.ThreadPoolExecutor} object.
	 */
	public void setImportThreadExecuter(ThreadPoolExecutor importThreadExecuter) {
		this.importThreadExecuter = importThreadExecuter;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Execute when a file is uploaded
	 */
	@Override
	protected void executeImpl(Action ruleAction, NodeRef nodeRef) {

		Boolean doNotMoveNode = (Boolean) ruleAction.getParameterValue(PARAM_DO_NOT_MOVE_NODE);

		// Bind the listener to the transaction
		AlfrescoTransactionSupport.bindListener(transactionListener);

		TransactionSupportUtil.bindResource(PARAM_DO_NOT_MOVE_NODE, doNotMoveNode);
		// Get the set of nodes read
		Set<NodeRef> nodeRefs = TransactionSupportUtil.getResource(KEY_FILES_TO_IMPORT);
		if (nodeRefs == null) {
			nodeRefs = new HashSet<>(5);
			TransactionSupportUtil.bindResource(KEY_FILES_TO_IMPORT, nodeRefs);
		}
		nodeRefs.add(nodeRef);

	}

	/** {@inheritDoc} */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
		paramList.add(
				new ParameterDefinitionImpl(PARAM_DO_NOT_MOVE_NODE, DataTypeDefinition.BOOLEAN, false, getParamDisplayLabel(PARAM_DO_NOT_MOVE_NODE)));

	}

	private class ImportServiceTransactionListener extends TransactionListenerAdapter {

		@Override
		public void afterCommit() {

			Set<NodeRef> nodeRefs = TransactionSupportUtil.getResource(KEY_FILES_TO_IMPORT);

			Boolean doNotMoveNode = TransactionSupportUtil.getResource(PARAM_DO_NOT_MOVE_NODE);

			if (nodeRefs != null) {
				for (NodeRef nodeRef : nodeRefs) {

					Runnable command = new FileImporter(nodeRef, AuthenticationUtil.getFullyAuthenticatedUser(), doNotMoveNode);
					if (!importThreadExecuter.getQueue().contains(command)) {
						importThreadExecuter.execute(command);
					} else {
						logger.warn("Import job already in queue for " + nodeRef);
					}

				}
			}
		}
	}

	/**
	 * The Class FileImporter.
	 *
	 * @author querephi
	 */
	private class FileImporter implements Runnable {

		private final NodeRef nodeRef;
		private final String runAsUser;
		private final Boolean doNotMoveNode;

		private FileImporter(NodeRef nodeRef, String runAsUser, Boolean doNotMoveNode) {
			this.nodeRef = nodeRef;
			this.runAsUser = runAsUser;
			this.doNotMoveNode = doNotMoveNode;
		}

		@Override
		public void run() {
			RunAsWork<Object> actionRunAs = () -> {
				// import file
				String startlog = LOG_STARTING_DATE + Calendar.getInstance().getTime();
				String endlog;
				String unhandledLog = null;
				String first50ErrorsLog = ""; // log stored in title, first
												// 50 errors
				String after50ErrorsLog = ""; // log store in
				boolean hasFailed = false;

				try {

					/*
					 * need a new transaction, otherwise impossible to do another action like create a content do it in several transaction to avoid timeout connection
					 */
					List<String> errors = new ArrayList<>();
							
					importService.importText(nodeRef, true, true, errors);

					if ((errors != null) && !errors.isEmpty()) {
						int limit = 0;
						for (String error : errors) {
							if (limit <= ERROR_LOGS_LIMIT) {
								first50ErrorsLog += LOG_SEPARATOR;
								first50ErrorsLog += error;
							} else {
								after50ErrorsLog += LOG_ERROR;
								after50ErrorsLog += error;
							}
							limit++;
						}

						hasFailed = true;
					}

				} catch (Exception e) {

					hasFailed = true;
					logger.error("Failed to import file text", e);

					// set printStackTrance in description
					try (StringWriter sw = new StringWriter()) {
						try (PrintWriter pw = new PrintWriter(sw)) {
							e.printStackTrace(pw);
							String stackTrace = sw.toString();
							unhandledLog = LOG_ERROR + stackTrace;
						}
					}
				} finally {
					endlog = LOG_ENDING_DATE + Calendar.getInstance().getTime().toString();
				}

				String log = startlog + LOG_SEPARATOR + (unhandledLog != null ? unhandledLog + LOG_SEPARATOR : "")
						+ (first50ErrorsLog.isEmpty() ? "" : first50ErrorsLog + LOG_SEPARATOR)
						+ (after50ErrorsLog.isEmpty() ? "" : LOG_ERROR_MAX_REACHED + LOG_SEPARATOR) + endlog;

				String allLog = startlog + LOG_SEPARATOR + (unhandledLog != null ? unhandledLog + LOG_SEPARATOR : "")
						+ (first50ErrorsLog.isEmpty() ? "" : first50ErrorsLog + LOG_SEPARATOR)
						+ (after50ErrorsLog.isEmpty() ? "" : after50ErrorsLog + LOG_SEPARATOR) + endlog;

				// set log, stackTrace and move file
				if ((doNotMoveNode == null) || Boolean.FALSE.equals(doNotMoveNode)) {
					importService.moveImportedFile(nodeRef, hasFailed, log, allLog);
				} else {
					importService.writeLogInFileTitle(nodeRef, log, hasFailed);
				}
				return null;
			};
			AuthenticationUtil.runAs(actionRunAs, runAsUser);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = (prime * result) + getOuterType().hashCode();
			result = (prime * result) + ((nodeRef == null) ? 0 : nodeRef.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			FileImporter other = (FileImporter) obj;
			if (!getOuterType().equals(other.getOuterType())) {
				return false;
			}
			if (nodeRef == null) {
				if (other.nodeRef != null) {
					return false;
				}
			} else if (!nodeRef.equals(other.nodeRef)) {
				return false;
			}
			return true;
		}

		private ImporterActionExecuter getOuterType() {
			return ImporterActionExecuter.this;
		}

	}

}

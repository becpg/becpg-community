/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.action.executer;

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
				importService.importText(nodeRef, true, true, doNotMoveNode);
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

/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.action.executer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.importer.ImportService;

// TODO: Auto-generated Javadoc
/**
 * Action used to import text files.
 *
 * @author Quere
 */
public class ImporterActionExecuter extends ActionExecuterAbstractBase{
	
	/** The Constant NAME. */
	public static final String NAME = "import-content";
	public static final String PARAM_VALUE_EXTENSION = ".csv";
		
	private static final String LOG_STARTING_DATE = "Starting date: ";	
	private static final String LOG_ENDING_DATE = "Ending date: ";	
	private static final String LOG_ERROR = "Error: ";	
	private static final String LOG_SEPARATOR = "\n";
	private static final String KEY_FILES_TO_IMPORT = "keyFilesToImport";	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImporterActionExecuter.class);			
	
	private ImportService importService;
	private NodeService nodeService;
	private TransactionService transactionService;
	private TransactionListener transactionListener;

	/**
	 * Sets the import service.
	 *
	 * @param importService the new import service
	 */
	public void setImportService(ImportService importService) {
		this.importService = importService;
	}	
	
	public ImporterActionExecuter(){
		this.transactionListener = new ImportServiceTransactionListener();
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}	
	
	/**
	 * Execute when a file is uploaded
	 */
	@Override
	protected void executeImpl(Action action, NodeRef nodeRef){
		
		// Bind the listener to the transaction
		AlfrescoTransactionSupport.bindListener(transactionListener);
		// Get the set of nodes read
		@SuppressWarnings("unchecked")
		Set<NodeRef> nodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_FILES_TO_IMPORT);
		if (nodeRefs == null) {
			nodeRefs = new HashSet<NodeRef>(5);
			AlfrescoTransactionSupport.bindResource(KEY_FILES_TO_IMPORT, nodeRefs);
		}
		nodeRefs.add(nodeRef);
		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.action.ParameterizedItemAbstractBase#addParameterDefinitions(java.util.List)
	 */
	@Override
	protected void addParameterDefinitions(List<ParameterDefinition> arg0) {
		// TODO Auto-generated method stub
		
	}	
	
	
	/**
	 * The listener interface for receiving productReportServiceTransaction events.
	 * The class that is interested in processing a productReportServiceTransaction
	 * event implements this interface, and the object created
	 * with that class is registered with a component using the
	 * component's <code>addProductReportServiceTransactionListener<code> method. When
	 * the productReportServiceTransaction event occurs, that object's appropriate
	 * method is invoked.
	 *
	 * @see ProductReportServiceTransactionEvent
	 */
	private class ImportServiceTransactionListener extends TransactionListenerAdapter {
		
		/* (non-Javadoc)
		 * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
		 */
		@Override
		public void afterCommit() {

			@SuppressWarnings("unchecked")
			Set<NodeRef> nodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
					.getResource(KEY_FILES_TO_IMPORT);
			if (nodeRefs != null) {
				for (NodeRef nodeRef : nodeRefs) {
					Runnable runnable = new FileImporter(nodeRef);						
					runnable.run();										
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
		
		private NodeRef nodeRef;
		
		private FileImporter(NodeRef nodeRef) {
			this.nodeRef = nodeRef;
		}
		
		@Override
		public void run()
        {
			// import file
			String log = LOG_STARTING_DATE + Calendar.getInstance().getTime();
			boolean hasFailed = false;
			
            try
            {
//            	RetryingTransactionCallback<List<String>> actionCallback = new RetryingTransactionCallback<List<String>>()
//                {
//                    @Override
//    				public List<String> execute() throws Exception
//                    {                
//                    	if(nodeService.exists(nodeRef)){
//                    		
//                    		return importService.importText(nodeRef, true, true); // need a new transaction, otherwise impossible to do another action like create a content
//                    	}
//                            			        
//                        return null;
//                    }
//                };
//                List<String> errors = transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
                
            	/*
            	 * need a new transaction, otherwise impossible to do another action like create a content
            	 * do it in several transaction to avoid timeout connection
            	 */
            	List<String> errors = null;
            	if(nodeService.exists(nodeRef)){
            		
            		errors = importService.importText(nodeRef, true, true);
            	}
                  
                if(errors != null && !errors.isEmpty()){
 	                
                 	for(String error : errors){
 	                	log += LOG_SEPARATOR;
 	                    log += error;
 	                }
                 
                     hasFailed = true;
                 }
            	                               
            }            
            catch (Exception e) {
    			hasFailed = true;
            	logger.error("Failed to import file text", e);	
    			
    			// set printStackTrance in description
            	StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);                           
                String stackTrace = sw.toString();
    			
    			log += LOG_SEPARATOR;
    			log += LOG_ERROR + stackTrace;
			} 
			finally{
				log += LOG_SEPARATOR;
				log += LOG_ENDING_DATE + Calendar.getInstance().getTime();
			}
			
			// set log, stackTrace and move file
			// create a new transaction to avoid concurrencyFailureException
			final boolean finalHasFailed = hasFailed;
			final String finalLog = log;
						
			RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>()
            {
                @Override
				public Object execute() throws Exception
                {                
                	if(nodeService.exists(nodeRef)){
                		 
                		logger.debug("move file in folder. HasFailed: " + finalHasFailed);
                		nodeService.setProperty(nodeRef, ContentModel.PROP_TITLE, finalLog);                		
                		importService.moveImportedFile(nodeRef, finalHasFailed);                		
                	}
                        			        
                    return null;
                }
            };
            transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);   			
        }
	}	

}

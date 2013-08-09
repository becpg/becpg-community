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
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.importer.ImportService;

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
	private static final int ERROR_LOGS_LIMIT = 50;
	private static final String LOG_ERROR_MAX_REACHED = "More than "+ERROR_LOGS_LIMIT+" errors, stop printing";	
	private static final String LOG_SEPARATOR = "\n";
	private static final String KEY_FILES_TO_IMPORT = "keyFilesToImport";	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ImporterActionExecuter.class);			
	
	private ImportService importService;
	private TransactionListener transactionListener;
	private ThreadPoolExecutor threadExecuter;

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
	
	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
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
					Runnable runnable = new FileImporter(nodeRef, AuthenticationUtil.getRunAsUser());						
					threadExecuter.execute(runnable);					
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
		private String runAsUser;
		
		private FileImporter(NodeRef nodeRef, String runAsUser) {
			this.nodeRef = nodeRef;
			this.runAsUser = runAsUser;
		}
		
		@Override
		public void run()
        {	
			RunAsWork<Object> actionRunAs = new RunAsWork<Object>()
            {
                @Override
				public Object doWork() throws Exception
                {
                	// import file
                	String startlog = LOG_STARTING_DATE + Calendar.getInstance().getTime();
                	String endlog;
                	String unhandledLog = null;
                	String first50ErrorsLog = ""; // log stored in title, first 50 errors
                	String after50ErrorsLog = ""; // log store in 
        			boolean hasFailed = false;
                	
                	try
                    {
//                    	RetryingTransactionCallback<List<String>> actionCallback = new RetryingTransactionCallback<List<String>>()
//                        {
//                            @Override
//            				public List<String> execute() throws Exception
//                            {                
//                            	if(nodeService.exists(nodeRef)){
//                            		
//                            		return importService.importText(nodeRef, true, true); // need a new transaction, otherwise impossible to do another action like create a content
//                            	}
//                                    			        
//                                return null;
//                            }
//                        };
//                        List<String> errors = transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
                        
                    	/*
                    	 * need a new transaction, otherwise impossible to do another action like create a content
                    	 * do it in several transaction to avoid timeout connection
                    	 */
                    	List<String> errors = importService.importText(nodeRef, true, true);
                    	logger.info("###DDDDD ");
                        if(errors != null && !errors.isEmpty()){
         	                int limit = 0;
                         	for(String error : errors){
                         		logger.info("###error: " + error);
         	                	if(limit<=ERROR_LOGS_LIMIT){
         	                		first50ErrorsLog += LOG_SEPARATOR;
         	                		first50ErrorsLog += error;
         	                	}
         	                	else{
         	                		after50ErrorsLog += LOG_ERROR;
         	                		after50ErrorsLog += error;
         	                	}                         		
         	                    limit++;
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
                        unhandledLog = LOG_ERROR + stackTrace;
        			} 
        			finally{
        				endlog = LOG_ENDING_DATE + Calendar.getInstance().getTime().toString();
        			}
                	
                	String log = startlog + 
                				LOG_SEPARATOR +
                				(unhandledLog != null ? unhandledLog + LOG_SEPARATOR : "") +
                				first50ErrorsLog +    
                				LOG_SEPARATOR +
                				(after50ErrorsLog.isEmpty() ? "" : LOG_ERROR_MAX_REACHED + LOG_SEPARATOR) +                				
                				endlog;
                	                	
        			String allLog = startlog + 
            				LOG_SEPARATOR +
            				(unhandledLog != null ? unhandledLog + LOG_SEPARATOR : "") +
            				first50ErrorsLog +
            				LOG_SEPARATOR +
            				after50ErrorsLog +
            				LOG_SEPARATOR +
            				endlog;
        			
        			logger.info("###first50ErrorsLog: " + first50ErrorsLog);
        			logger.info("###log: " + log);
        			
        			// set log, stackTrace and move file
                    importService.moveImportedFile(nodeRef, hasFailed, log, allLog);   
                    
                    return null;
                }
            };
            AuthenticationUtil.runAs(actionRunAs, runAsUser);			   	
        }
	}	

}

/*
 * 
 */
package fr.becpg.repo.workflow.activiti.nc;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.delegate.DelegateTask;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.workflow.activiti.ActivitiScriptNode;
import org.alfresco.repo.workflow.activiti.tasklistener.ScriptTaskListener;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;

/**
 * Update the NC based on NC WF data, need a java class to avoid problem with retrying transaction
 * 
 * @author "Philippe QUÉRÉ <philippe.quere@becpg.fr>"
 * 
 */
public class UpdateNC extends ScriptTaskListener {

	private static Log logger = LogFactory.getLog(UpdateNC.class);

	private NodeService nodeService;
	private FileFolderService fileFolderService;
	private TransactionService transactionService;
	
	@Override
	public void notify(final DelegateTask task) {
		
		nodeService = getServiceRegistry().getNodeService();
		fileFolderService = getServiceRegistry().getFileFolderService();
		transactionService = getServiceRegistry().getTransactionService();
				
		final NodeRef pkgNodeRef = ((ActivitiScriptNode) task.getVariable("bpm_package")).getNodeRef();

		// use retrying transaction to avoid exception ConcurrencyFailureException
		RetryingTransactionCallback<List<String>> actionCallback = new RetryingTransactionCallback<List<String>>()
        {
            @Override
			public List<String> execute() throws Exception
            {                
            	//update state and comments
        		List<FileInfo> files = fileFolderService.listFiles(pkgNodeRef);
        		for(FileInfo file : files){
        			
        			if (QualityModel.TYPE_NC.equals(nodeService.getType(file.getNodeRef()))) {
        				
        				NodeRef ncNodeRef = file.getNodeRef();
            			
            			Map<QName, Serializable> properties = new HashMap<QName, Serializable>(2);
            			properties.put(QualityModel.PROP_NC_STATE, (String) task.getVariable("ncwf_ncState"));
            			properties.put(QualityModel.PROP_NC_COMMENT, (String) task.getVariable("bpm_comment"));        			
            			
            			if(logger.isDebugEnabled()){
            				logger.debug("UpdateNC: " + ncNodeRef + " - "  + properties);
            			}            			
            			nodeService.addProperties(ncNodeRef, properties); 
        			}        			           			     		
        		}
                    			        
                return null;
            }
        };
        transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback, false, true);
	}
}

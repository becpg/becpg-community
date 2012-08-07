/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * Generate documents when product properties are updated.
 *
 * @author querephi, matthieu
 */
@Service
public class EntityReportPolicy extends AbstractBeCPGPolicy implements
		NodeServicePolicies.OnUpdateNodePolicy,
		NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy{

	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityReportPolicy.class);

	
	/** The transaction service. */
	private TransactionService transactionService;
	
	/** The thread pool executor. */
	private ThreadPoolExecutor threadExecuter;

	/** The entityReportService **/
	private EntityReportService entityReportService;
	

	/**
	 * Sets the transaction service.
	 *
	 * @param transactionService the new transaction service
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}
	
	/**
	 * @param threadExecuter the threadExecuter to set
	 */
	public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}

	
	/**
	 * @param entityReportService the entityReportService to set
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {				
		
		logger.debug("Init EntityReportPolicy...");
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnUpdateNodePolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onUpdateNode"));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onCreateAssociation"));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onDeleteAssociation"));
		
		disableOnCopyBehaviour(ReportModel.ASPECT_REPORT_ENTITY);
		
	}

	/**
	 * Regenerate report every time a property is updated
	 */
	@Override
	public void onUpdateNode(NodeRef entityNodeRef) {		
		
		onUpdateProduct(entityNodeRef);		
	}
	
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		onUpdateProduct(assocRef.getSourceRef());
	}	
	
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		
		onUpdateProduct(assocRef.getSourceRef());
	}

	private void onUpdateProduct(NodeRef entityNodeRef){

		queueNode(entityNodeRef);
	
	}

	
	
    /**
	 * Check if the system should generate the report for this product
	 * @param entityNodeRef
	 * @return
	 */
	public boolean IsReportable(NodeRef entityNodeRef) {
		
    	if(nodeService.exists(entityNodeRef) ){
    		
    		// do not generate report for product version
    		if(!isVersionNode(entityNodeRef)){
    			return true;
    		}
    	}
		return false;			
	}


	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
			for (NodeRef nodeRef : pendingNodes) {					
				if(IsReportable(nodeRef)){
					Runnable runnable = new ProductReportGenerator(nodeRef, AuthenticationUtil.getAdminUserName());
					threadExecuter.execute(runnable);
				}
				
			}
	}
	
	

	/**
	 * The Class ProductReportGenerator.
	 *
	 * @author querephi
	 */
	private class ProductReportGenerator implements Runnable {
		
		/** The product node ref. */
		private NodeRef entityNodeRef;
		private String runAsUser;

		/**
		 * Instantiates a new product report generator.
		 *
		 * @param entityNodeRef the product node ref
		 */
		private ProductReportGenerator(NodeRef entityNodeRef, String runAsUser) {
			this.entityNodeRef = entityNodeRef;
			this.runAsUser = runAsUser;
		}
		
		/* (non-Javadoc)
		 * @see java.lang.Runnable#run()
		 */
		@Override
		public void run()
        {
            try
            {                                
                RunAsWork<Object> actionRunAs = new RunAsWork<Object>()
                {
                    @Override
					public Object doWork() throws Exception
                    {
                        RetryingTransactionCallback<Object> actionCallback = new RetryingTransactionCallback<Object>()
                        {
                            @Override
							public Object execute()
                            {                                   

                            	if(nodeService.exists(entityNodeRef) && isNotLocked(entityNodeRef) ){
                            		
                            		entityReportService.generateReport(entityNodeRef);
                            	}
            			        
                                return null;
                            }
                        };
                        return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
                    }
                };
                AuthenticationUtil.runAs(actionRunAs, runAsUser);
            }
            catch (InvalidNodeRefException e) {
				
				logger.error("Unable to generate product report, due to invalidNodeRef: " + entityNodeRef, e);
			} 
			catch (Throwable e) {
				
				logger.error("Unable to generate product report: " + entityNodeRef, e);
				// We are the last call on the thread
			}
        }
	}

	
	
}

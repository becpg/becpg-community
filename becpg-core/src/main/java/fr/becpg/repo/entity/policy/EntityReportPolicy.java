/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.report.entity.EntityReportService;

// TODO: Auto-generated Javadoc
/**
 * Generate documents when product properties are updated.
 *
 * @author querephi, matthieu
 */
public class EntityReportPolicy extends TransactionListenerAdapter implements
		NodeServicePolicies.OnUpdateNodePolicy,
		NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy{

	/** The Constant KEY_ENTITYREPORT_TO_GENERATE. */
	private static final String KEY_ENTITYREPORT_TO_GENERATE = "entityReportToGenerate";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityReportPolicy.class);

	/** The policy component. */
	private PolicyComponent policyComponent;
	
	/** The transaction service. */
	private TransactionService transactionService;
	
	/** The thread pool executor. */
	 private ThreadPoolExecutor threadExecuter;
	
	/** The transaction listener. */
	private TransactionListener transactionListener;	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The policy behaviour filter. */
	private BehaviourFilter policyBehaviourFilter;
	
	/** The Lock Service **/
	private LockService lockService;
	
	/** The entityReportService **/
	private EntityReportService entityReportService;
	
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

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
	 * Sets the policy behaviour filter.
	 *
	 * @param policyBehaviourFilter the new policy behaviour filter
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}	
	
	/**
	 * 
	 * @param lockService
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}
	
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
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
	public void init() {				
		
		logger.debug("Init EntityReportPolicy...");
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnUpdateNodePolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onDeleteAssociation", NotificationFrequency.TRANSACTION_COMMIT));
		
		// transaction listeners
		this.transactionListener = new ProductReportServiceTransactionListener();
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
	
		// The policy is disabled but this is an after commit treatment, so the policy is enabled...  
		if(!IsReportable(entityNodeRef)){
			return;
		}
		
		// Bind the listener to the transaction
		AlfrescoTransactionSupport.bindListener(transactionListener);
		// Get the set of nodes read
		@SuppressWarnings("unchecked")
		Set<NodeRef> updatedNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_ENTITYREPORT_TO_GENERATE);
		if (updatedNodeRefs == null) {
			updatedNodeRefs = new HashSet<NodeRef>(5);
			AlfrescoTransactionSupport.bindResource(KEY_ENTITYREPORT_TO_GENERATE, updatedNodeRefs);
		}
		updatedNodeRefs.add(entityNodeRef);
	}

    /**
	 * Check if the system should generate the report for this product
	 * @param entityNodeRef
	 * @return
	 */
	public boolean IsReportable(NodeRef entityNodeRef) {
		
    	if(nodeService.exists(entityNodeRef) ){
    		
    		// do not generate report for product version
    		if(!nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)){
    			return true;
    		}
    	}
		return false;			
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
	private class ProductReportServiceTransactionListener extends TransactionListenerAdapter {
		
		/* (non-Javadoc)
		 * @see org.alfresco.repo.transaction.TransactionListenerAdapter#afterCommit()
		 */
		@Override
		public void afterCommit() {
			// Get all the nodes that need their read counts incremented
			@SuppressWarnings("unchecked")
			Set<NodeRef> readNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport
					.getResource(KEY_ENTITYREPORT_TO_GENERATE);
			if (readNodeRefs != null) {
				for (NodeRef nodeRef : readNodeRefs) {					
					
					Runnable runnable = new ProductReportGenerator(nodeRef);
					threadExecuter.execute(runnable);
					
				}
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

		/**
		 * Instantiates a new product report generator.
		 *
		 * @param entityNodeRef the product node ref
		 */
		private ProductReportGenerator(NodeRef entityNodeRef) {
			this.entityNodeRef = entityNodeRef;
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

                            	if(nodeService.exists(entityNodeRef) && lockService.getLockStatus(entityNodeRef) == LockStatus.NO_LOCK){
                            		try{
                                		// Ensure that the policy doesn't refire for this node
                        				// on this thread
                        				// This won't prevent background processes from
                        				// refiring, though
                        	            policyBehaviourFilter.disableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);	
                        	            policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);	
                        	     
                        	            // generate reports
                        	            entityReportService.generateReport(entityNodeRef);		
                        	            
                        	        	// set reportNodeGenerated property to now
                    			        nodeService.setProperty(entityNodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, Calendar.getInstance().getTime());
                        	        }
                        	        finally{
                        	        	policyBehaviourFilter.enableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);		
                        	        	policyBehaviourFilter.enableBehaviour(entityNodeRef,  ContentModel.ASPECT_AUDITABLE);		
                        	        }	         
                            	}
            			        
                                return null;
                            }
                        };
                        return transactionService.getRetryingTransactionHelper().doInTransaction(actionCallback);
                    }
                };
                AuthenticationUtil.runAs(actionRunAs, AuthenticationUtil.getAdminUserName());
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

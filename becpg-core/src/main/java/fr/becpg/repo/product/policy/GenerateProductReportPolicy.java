/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.product.policy;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductService;

// TODO: Auto-generated Javadoc
/**
 * Generate documents when product properties are updated.
 *
 * @author querephi
 */
public class GenerateProductReportPolicy extends TransactionListenerAdapter implements
		NodeServicePolicies.OnUpdateNodePolicy,
		NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy{

	/** The Constant KEY_PRODUCTREPORT_TO_GENERATE. */
	private static final String KEY_PRODUCTREPORT_TO_GENERATE = "dfProductReportToGenerate";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(GenerateProductReportPolicy.class);

	/** The policy component. */
	private PolicyComponent policyComponent;
	
	/** The transaction service. */
	private TransactionService transactionService;
	
	/** The thread pool executor. */
	private TaskExecutor taskExecutor;
	
	/** The transaction listener. */
	private TransactionListener transactionListener;	
	
	/** The product service. */
	private ProductService productService;	
	
	/** The node service. */
	private NodeService nodeService;
	
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
	
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	/**
	 * Sets the product service.
	 *
	 * @param productService the new product service
	 */
	public void setProductService(ProductService productService) {
		this.productService = productService;
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
	 * Inits the.
	 */
	public void init() {				
		
		logger.debug("Init GenerateProductReportPolicy...");
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnUpdateNodePolicy.QNAME,
				BeCPGModel.TYPE_PRODUCT, new JavaBehaviour(this,
						"onUpdateNode", NotificationFrequency.TRANSACTION_COMMIT));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				BeCPGModel.TYPE_PRODUCT, new JavaBehaviour(this,
						"onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				BeCPGModel.TYPE_PRODUCT, new JavaBehaviour(this,
						"onDeleteAssociation", NotificationFrequency.TRANSACTION_COMMIT));
		
		// transaction listeners
		this.transactionListener = new ProductReportServiceTransactionListener();
	}

	/**
	 * Regenerate report every time a property is updated
	 */
	@Override
	public void onUpdateNode(NodeRef productNodeRef) {		
		
		onUpdateProduct(productNodeRef);		
	}
	
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		onUpdateProduct(assocRef.getSourceRef());
	}	
	
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		
		onUpdateProduct(assocRef.getSourceRef());
	}

	private void onUpdateProduct(NodeRef productNodeRef){
	
		// The policy is disabled but this is an after commit treatment, so the policy is enabled...  
		if(!productService.IsReportable(productNodeRef)){
			return;
		}
		
		// Bind the listener to the transaction
		AlfrescoTransactionSupport.bindListener(transactionListener);
		// Get the set of nodes read
		@SuppressWarnings("unchecked")
		Set<NodeRef> updatedNodeRefs = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_PRODUCTREPORT_TO_GENERATE);
		if (updatedNodeRefs == null) {
			updatedNodeRefs = new HashSet<NodeRef>(5);
			AlfrescoTransactionSupport.bindResource(KEY_PRODUCTREPORT_TO_GENERATE, updatedNodeRefs);
		}
		updatedNodeRefs.add(productNodeRef);
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
					.getResource(KEY_PRODUCTREPORT_TO_GENERATE);
			if (readNodeRefs != null) {
				for (NodeRef nodeRef : readNodeRefs) {					
					
					Runnable runnable = new ProductReportGenerator(nodeRef);
					taskExecutor.execute(runnable);
					
					//runnable.run();
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
		private NodeRef productNodeRef;

		/**
		 * Instantiates a new product report generator.
		 *
		 * @param productNodeRef the product node ref
		 */
		private ProductReportGenerator(NodeRef productNodeRef) {
			this.productNodeRef = productNodeRef;
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
                                
                            	if(nodeService.exists(productNodeRef)){
                            		productService.generateReport(productNodeRef);
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
				
				logger.error("Unable to generate product report, due to invalidNodeRef: " + productNodeRef, e);
			} 
			catch (Throwable e) {
				
				logger.error("Unable to generate product report: " + productNodeRef, e);
				// We are the last call on the thread
			}
        }
	}

}

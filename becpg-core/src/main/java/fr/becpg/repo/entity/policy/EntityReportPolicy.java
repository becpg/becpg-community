/*
 *  Copyright (C) 2010-2011 beCPG. All rights reserved.
 */
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.RetryingTransactionHelper.RetryingTransactionCallback;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
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
		NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnDeleteAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy,
		ContentServicePolicies.OnContentUpdatePolicy{

	
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
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnCreateAssociationPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onCreateAssociation"));
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onDeleteAssociation"));
		
		policyComponent.bindClassBehaviour(
				NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME,
				ReportModel.ASPECT_REPORT_ENTITY, new JavaBehaviour(this,
						"onUpdateProperties"));	
		
		// report Tpl policies
		
		policyComponent.bindAssociationBehaviour(
				NodeServicePolicies.OnDeleteAssociationPolicy.QNAME,
				ReportModel.TYPE_REPORT, ReportModel.ASSOC_REPORT_TPL, new JavaBehaviour(this,
						"onDeleteAssociation"));
		
//		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, ReportModel.TYPE_REPORT_TPL,  new JavaBehaviour(this,
//				"beforeDeleteNode"));
		
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, ReportModel.TYPE_REPORT_TPL,  new JavaBehaviour(this,
				"onContentUpdate", NotificationFrequency.TRANSACTION_COMMIT));
	}
	
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		onUpdateProduct(assocRef.getSourceRef());
	}	
	
	@Override
	public void onDeleteAssociation(AssociationRef assocRef) {
		
		if(ReportModel.ASSOC_REPORT_TPL.equals(assocRef.getTypeQName())){			
			if(!nodeService.hasAspect(assocRef.getSourceRef(), ContentModel.ASPECT_PENDING_DELETE)){
				
				if (logger.isDebugEnabled()) {
					logger.debug("Policy delete report " + assocRef.getSourceRef() + " - name: "
							+ nodeService.getProperty(assocRef.getSourceRef(), ContentModel.PROP_NAME));
				}
				nodeService.deleteNode(assocRef.getSourceRef());
			}			
		}
		else{
			onUpdateProduct(assocRef.getSourceRef());
		}		
	}

	private void onUpdateProduct(NodeRef entityNodeRef){
		
		queueNode(entityNodeRef);	
	}

	@Override
	public void onUpdateProperties(NodeRef entityNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		
		for(Map.Entry<QName, Serializable> kv : after.entrySet()){
			
			boolean hasChanged = false;
			
			if(kv.getValue() == null){
				if(before.get(kv.getKey()) != null){
					hasChanged = true;
				}			
			}
			else if(!kv.getValue().equals(before.get(kv.getKey()))){
				hasChanged = true;
			}
			
			// generate report depending of properties updated
			if(hasChanged && !ReportModel.PROP_REPORT_ENTITY_GENERATED.equals(kv.getKey()) &&
					!ContentModel.PROP_MODIFIED.equals(kv.getKey()) &&
					!ContentModel.PROP_MODIFIER.equals(kv.getKey()) &&
					!ContentModel.PROP_VERSION_LABEL.equals(kv.getKey()) &&
					!ContentModel.PROP_LAST_THUMBNAIL_MODIFICATION_DATA.equals(kv.getKey())){
				
				if(logger.isDebugEnabled()){
					logger.debug("Generate report since prop has changed. Prop: " + kv.getKey() +  
							" before: " + before.get(kv.getKey()) + " after: " + kv.getValue());
				}
				
				onUpdateProduct(entityNodeRef);
				return;
			}
		}				
	}

	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {					
			Runnable runnable = new ProductReportGenerator(nodeRef, AuthenticationUtil.getSystemUserName());
			threadExecuter.execute(runnable);			
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
                            	if(nodeService.exists(entityNodeRef) && !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITY_TPL) &&
                            			isNotLocked(entityNodeRef) && !isVersionStoreNode(entityNodeRef)){
                            		
                            		try{
                                		// Ensure that the policy doesn't refire for this node
                            			// on this thread
                            			// This won't prevent background processes from
                            			// refiring, though
                                        policyBehaviourFilter.disableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);	
                                        policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_AUDITABLE);
                                        policyBehaviourFilter.disableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
                            		
                                        if(logger.isDebugEnabled()){
                                        	logger.info("generate report: " + entityNodeRef + " - " + nodeService.getProperty(entityNodeRef, ContentModel.PROP_NAME));
                                        }                                        
                                        entityReportService.generateReport(entityNodeRef);
                                        
                            		}
                                    finally{
                                    	policyBehaviourFilter.enableBehaviour(entityNodeRef, ReportModel.ASPECT_REPORT_ENTITY);		
                                    	policyBehaviourFilter.enableBehaviour(entityNodeRef,  ContentModel.ASPECT_AUDITABLE);
                                    	policyBehaviourFilter.enableBehaviour(entityNodeRef, ContentModel.ASPECT_VERSIONABLE);
                                    }
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

	@Override
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {
		
		if(nodeService.exists(nodeRef)){			
			
			List<AssociationRef> reportAssocRefs = nodeService.getSourceAssocs(nodeRef,
					ReportModel.ASSOC_REPORT_TPL);
			
			logger.debug("Policy onContentUpdate reportTpl " + nodeRef + " - reports to generate: " + reportAssocRefs.size());
			
			for (AssociationRef reportAssocRef : reportAssocRefs) {
				
				List<AssociationRef> entityAssocRefs = nodeService.getSourceAssocs(reportAssocRef.getSourceRef(),
						ReportModel.ASSOC_REPORTS);
				
				if(!entityAssocRefs.isEmpty()){
					NodeRef entityNodeRef = entityAssocRefs.get(0).getSourceRef();
					Runnable runnable = new ProductReportGenerator(entityNodeRef, AuthenticationUtil.getSystemUserName());
					threadExecuter.execute(runnable);
				}			
			}
		}		
	}
}

package fr.becpg.repo.security.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;

import org.alfresco.repo.dictionary.DictionaryDAO;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.SecurityModel;

public class AclGroupPolicies implements NodeServicePolicies.OnUpdatePropertiesPolicy,
															NodeServicePolicies.OnDeleteNodePolicy {

	private static Log logger = LogFactory.getLog(AclGroupPolicies.class);
	
	private PolicyComponent policyComponent;
	private DictionaryDAO dictionaryDAO;
	private ThreadPoolExecutor threadExecuter;
	
    public void setDictionaryDAO(DictionaryDAO dictionaryDAO) {
		this.dictionaryDAO = dictionaryDAO;
	}
		
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
    
    public void setThreadExecuter(ThreadPoolExecutor threadExecuter) {
		this.threadExecuter = threadExecuter;
	}


	private TransactionListener transactionListener = new ACLGroupTransactionListener();
  


	public void init() {				
		
		logger.debug("Init AclGroupPolicies...");
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
				SecurityModel.TYPE_ACL_GROUP, 
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnDeleteNodePolicy.QNAME, 
				SecurityModel.TYPE_ACL_GROUP, 
				new JavaBehaviour(this, "onDeleteNode", NotificationFrequency.TRANSACTION_COMMIT));		
			
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
			logger.debug("Acl Group changed");
			
	        // Bind the listener to the transaction
	        AlfrescoTransactionSupport.bindListener(transactionListener);
	       
			
			
		
	}


	private class ACLGroupTransactionListener extends TransactionListenerAdapter
	{
		@Override
		public void afterCommit()
		{
			logger.debug("Reload model constraints");
			threadExecuter.execute(new Runnable() {
				
				@Override
				public void run() {
					//Wait for indexation;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						logger.error(e);
					}
					dictionaryDAO.reset();
					
				}
			});
			
		}
		
		
	}


	@Override
	public void onDeleteNode(ChildAssociationRef arg0, boolean arg1) {
		logger.debug("Acl Group deleted");
		
        // Bind the listener to the transaction
        AlfrescoTransactionSupport.bindListener(transactionListener);
		
	}




}

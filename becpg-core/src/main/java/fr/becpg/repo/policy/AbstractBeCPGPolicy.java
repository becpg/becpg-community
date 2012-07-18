package fr.becpg.repo.policy;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;

public abstract class AbstractBeCPGPolicy {

	protected BehaviourFilter policyBehaviourFilter;

	protected PolicyComponent policyComponent;


	protected NodeService nodeService;
	

	private AbstractBeCPGPolicyTransactionListener transactionListener;
	

	private static final String KEY_PENDING_NODES = "AbstractBeCPGPolicy.pendingNodes";


	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	
	public void init(){

		PropertyCheck.mandatory(this, "policyComponent", policyComponent);
		PropertyCheck.mandatory(this, "policyBehaviourFilter", policyBehaviourFilter);
		
		doInit();
		

		// Create the transaction listener
		this.transactionListener = new AbstractBeCPGPolicyTransactionListener();
	}

	public void disableOnCopyBehaviour(QName type) {
		DisableBehaviourOnCopy  disableBehaviourOnCopy = new DisableBehaviourOnCopy(type, policyBehaviourFilter);
		
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, type, new JavaBehaviour(disableBehaviourOnCopy, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, type, new JavaBehaviour(disableBehaviourOnCopy, "onCopyComplete"));
		
	}
	

	protected boolean isWorkingCopyOrVersion(NodeRef nodeRef) {

		boolean workingCopy = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);

		boolean isVersionNode = nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);

		// Ignore if the node is a working copy or version node
		return workingCopy || isVersionNode;
	}
	
	 public abstract void doInit();
	 
	 protected void doBeforeCommit(Set<NodeRef> pendingNodes){
		 //Do Nothing
	 }
	
	 
	 protected void queueNode(NodeRef nodeRef) {
			@SuppressWarnings("unchecked")
			Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_PENDING_NODES);
			if (pendingNodes == null) {
				pendingNodes = new CopyOnWriteArraySet<NodeRef>();
				AlfrescoTransactionSupport.bindResource(KEY_PENDING_NODES, pendingNodes);
			}
			pendingNodes.add(nodeRef);

			AlfrescoTransactionSupport.bindListener(this.transactionListener);
		}

	  class AbstractBeCPGPolicyTransactionListener extends TransactionListenerAdapter {

			@Override
			public void beforeCommit(boolean readOnly) {
				@SuppressWarnings("unchecked")
				Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_PENDING_NODES);

				if (pendingNodes != null) {
					doBeforeCommit(pendingNodes);
				}
			}

		}
}

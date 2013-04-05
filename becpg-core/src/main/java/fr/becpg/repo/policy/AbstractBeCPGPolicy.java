package fr.becpg.repo.policy;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DefaultCopyBehaviourCallback;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;

public abstract class AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy,
													CopyServicePolicies.OnCopyCompletePolicy{

	protected BehaviourFilter policyBehaviourFilter;

	protected PolicyComponent policyComponent;

	protected LockService lockService;

	protected NodeService nodeService;

	private AbstractBeCPGPolicyTransactionListener transactionListener;

	private Set<String> keys = new HashSet<String>();
	
	private static Log logger = LogFactory.getLog(AbstractBeCPGPolicy.class);

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	public void init() {

		PropertyCheck.mandatory(this, "policyComponent", policyComponent);
		PropertyCheck.mandatory(this, "policyBehaviourFilter", policyBehaviourFilter);
		PropertyCheck.mandatory(this, "nodeService", nodeService);

		doInit();

		// Create the transaction listener
		this.transactionListener = new AbstractBeCPGPolicyTransactionListener();
	}

	public void disableOnCopyBehaviour(QName type) {
		//DisableBehaviourOnCopy disableBehaviourOnCopy = new DisableBehaviourOnCopy(type, policyBehaviourFilter);

		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, type, new JavaBehaviour(this, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, type, new JavaBehaviour(this, "onCopyComplete"));

	}
	
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		policyBehaviourFilter.disableBehaviour(copyDetails.getTargetNodeRef(), classRef);
		return new DefaultCopyBehaviourCallback();
	}
	
	@Override
	public void onCopyComplete(QName classRef,
            NodeRef sourceNodeRef,
            NodeRef destinationRef,
            boolean copyToNewNode,
            Map<NodeRef, NodeRef> copyMap){
		
		policyBehaviourFilter.enableBehaviour(destinationRef, classRef);     
    }

	protected boolean isWorkingCopyOrVersion(NodeRef nodeRef) {

		boolean workingCopy = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);

		// Ignore if the node is a working copy or version node
		return workingCopy || isVersionNode(nodeRef);
	}

	protected boolean isBeCPGVersion(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION);
	}
	
	protected boolean isVersionStoreNode(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);
	}
	
	protected boolean isVersionNode(NodeRef nodeRef) {
		// Ignore if the node is a working copy or version node
		return isBeCPGVersion(nodeRef) || isVersionStoreNode(nodeRef);
	}

	protected boolean isNotLocked(NodeRef nodeRef) {
		return nodeService.exists(nodeRef) && lockService.getLockStatus(nodeRef) == LockStatus.NO_LOCK;

	}

	public abstract void doInit();

	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		// Do Nothing
	}

	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		// Do Nothing
	}

	protected void queueNode(NodeRef nodeRef){
		queueNode(generateDefaultKey(),nodeRef);
	}
	
	protected void queueNode(String key, NodeRef nodeRef) {
		@SuppressWarnings("unchecked")
		Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(key);
		if (pendingNodes == null) {
			pendingNodes = new LinkedHashSet<NodeRef>();
			if(logger.isDebugEnabled()){
				logger.debug("Bind key to transaction : "+key);
			}
			keys.add(key);
			AlfrescoTransactionSupport.bindResource(key, pendingNodes);
		}
		if(!pendingNodes.contains(nodeRef)){
			pendingNodes.add(nodeRef);
		}	
		
		AlfrescoTransactionSupport.bindListener(this.transactionListener);

	}

	protected void unQueueNode(NodeRef nodeRef){
		unQueueNode(generateDefaultKey(),nodeRef);
	}
	
	protected void unQueueNode(String key, NodeRef entityNodeRef) {
		@SuppressWarnings("unchecked")
		Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(key);
		if (pendingNodes != null) {
			pendingNodes.remove(entityNodeRef);
		}
	}
	
	protected boolean containsNodeInQueue(NodeRef nodeRef){
		return containsNodeInQueue(generateDefaultKey(),nodeRef);
	}
	
	protected boolean containsNodeInQueue(String key, NodeRef entityNodeRef) {
		@SuppressWarnings("unchecked")
		Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(key);
		if (pendingNodes != null) {
			return pendingNodes.contains(entityNodeRef);
		}
		return false;
	}
	
	protected String generateDefaultKey(){
		return "KEY_"+this.getClass().getName();
	}
	

	class AbstractBeCPGPolicyTransactionListener extends TransactionListenerAdapter {

		@Override
		public void beforeCommit(boolean readOnly) {

			for (String key : keys) {

				@SuppressWarnings("unchecked")
				Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(key);

				if (pendingNodes != null) {
					doBeforeCommit(key, pendingNodes);
				}
			}
		}

		@Override
		public void afterCommit() {
			for (String key : keys) {

				@SuppressWarnings("unchecked")
				Set<NodeRef> pendingNodes = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(key);

				if (pendingNodes != null) {
					doAfterCommit(key, pendingNodes);
				}
			}
		}

	}
}

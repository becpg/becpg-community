/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.policy;

import java.io.Serializable;
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
import org.alfresco.repo.transaction.TransactionalResourceHelper;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport.TxnReadState;
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.GUID;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;

/**
 * <p>Abstract AbstractBeCPGPolicy class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy, CopyServicePolicies.OnCopyCompletePolicy {

	protected BehaviourFilter policyBehaviourFilter;

	protected PolicyComponent policyComponent;

	protected LockService lockService;

	protected NodeService nodeService;
	
	protected BeCPGPolicyTransactionListener transactionListener = new BeCPGPolicyTransactionListener("pre");
	
	protected BeCPGPolicyTransactionListener postTransactionListener = new BeCPGPolicyTransactionListener("post");

	/** Constant <code>KEY_REGISTRY="key_registry"</code> */
	protected static final String KEY_REGISTRY = "key_registry";
	/** Constant <code>ASSOC_REGISTRY="assoc_registry"</code> */
	protected static final String ASSOC_REGISTRY = "assoc_registry";

	private static final Log logger = LogFactory.getLog(AbstractBeCPGPolicy.class);

	/**
	 * <p>Setter for the field <code>policyComponent</code>.</p>
	 *
	 * @param policyComponent a {@link org.alfresco.repo.policy.PolicyComponent} object.
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	/**
	 * <p>Setter for the field <code>policyBehaviourFilter</code>.</p>
	 *
	 * @param policyBehaviourFilter a {@link org.alfresco.repo.policy.BehaviourFilter} object.
	 */
	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object.
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/**
	 * <p>init.</p>
	 */
	public void init() {

		PropertyCheck.mandatory(this, "policyComponent", policyComponent);
		PropertyCheck.mandatory(this, "policyBehaviourFilter", policyBehaviourFilter);
		PropertyCheck.mandatory(this, "nodeService", nodeService);

		doInit();
		
	}

	/**
	 * <p>disableOnCopyBehaviour.</p>
	 *
	 * @param type a {@link org.alfresco.service.namespace.QName} object.
	 */
	public void disableOnCopyBehaviour(QName type) {
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, type, new JavaBehaviour(this, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, type, new JavaBehaviour(this, "onCopyComplete"));

	}

	/** {@inheritDoc} */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		policyBehaviourFilter.disableBehaviour(copyDetails.getTargetNodeRef(), classRef);
		return new DefaultCopyBehaviourCallback();
	}

	/** {@inheritDoc} */
	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

		policyBehaviourFilter.enableBehaviour(destinationRef, classRef);
	}

	/**
	 * <p>isWorkingCopyOrVersion.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean isWorkingCopyOrVersion(NodeRef nodeRef) {

		boolean workingCopy = nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY);

		// Ignore if the node is a working copy or version node
		return workingCopy || isVersionNode(nodeRef);
	}

	/**
	 * <p>isBeCPGVersion.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean isBeCPGVersion(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION);
	}

	/**
	 * <p>isEntityTemplate.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean isEntityTemplate(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL);
	}

	/**
	 * <p>isVersionStoreNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean isVersionStoreNode(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);
	}

	/**
	 * <p>isVersionNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean isVersionNode(NodeRef nodeRef) {
		// Ignore if the node is a working copy or version node
		return isBeCPGVersion(nodeRef) || isVersionStoreNode(nodeRef);
	}

	/**
	 * <p>isNotLocked.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean isNotLocked(NodeRef nodeRef) {
		return nodeService.exists(nodeRef) && (lockService.getLockStatus(nodeRef) == LockStatus.NO_LOCK);
	}


	/** Constant <code>KEY_PENDING_DELETE_NODES="DbNodeServiceImpl.pendingDeleteNodes"</code> */
	public static final String KEY_PENDING_DELETE_NODES = "DbNodeServiceImpl.pendingDeleteNodes";

	/**
	 * <p>isPendingDelete.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a boolean
	 */
	protected boolean isPendingDelete(NodeRef nodeRef) {
		// Avoid creating a Set if the transaction is read-only
		if (AlfrescoTransactionSupport.getTransactionReadState() != TxnReadState.TXN_READ_WRITE) {
			return false;
		}
		Set<NodeRef> nodesPendingDelete = TransactionalResourceHelper.getSet(KEY_PENDING_DELETE_NODES);
		return nodesPendingDelete.stream().anyMatch(n -> n.getId().equals(nodeRef.getId()));
	}
	
	/**
	 * <p>doInit.</p>
	 */
	public abstract void doInit();

	/**
	 * <p>doBeforeCommit.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param pendingNodes a {@link java.util.Set} object.
	 * @return a boolean.
	 */
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		// Do Nothing
		return false;
	}
	
	/**
	 * <p>doBeforeAssocsCommit.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param pendingAssocs a {@link java.util.Set} object.
	 * @return a boolean.
	 */
	protected boolean doBeforeAssocsCommit(String key, Set<AssociationRef> pendingAssocs) {
		// Do Nothing
		return false;
	}

	/**
	 * <p>doAfterCommit.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param pendingNodes a {@link java.util.Set} object.
	 */
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		// Do Nothing

	}
	
	/**
	 * <p>doAfterAssocsCommit.</p>
	 *
	 * @param key a {@link java.lang.String} object
	 * @param pendingAssocs a {@link java.util.Set} object
	 */
	protected void doAfterAssocsCommit(String key, Set<AssociationRef> pendingAssocs) {
		// Do Nothing
		
	}

	/**
	 * <p>queueNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected void queueNode(NodeRef nodeRef) {
		queueNode(generateDefaultKey(), nodeRef);
	}

	/**
	 * <p>queueNode.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected void queueNode(String key, NodeRef nodeRef) {	
	
		addKeyRegistry(KEY_REGISTRY, key);
		
		Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);
		if (pendingNodes == null) {
			pendingNodes = new LinkedHashSet<>();
			TransactionSupportUtil.bindResource(key, pendingNodes);
			AlfrescoTransactionSupport.bindListener(transactionListener);
		}
	
		
		pendingNodes.add(nodeRef);
	}
	
	private void addKeyRegistry(String registry, String key) {
		Set<String> keys = getKeyRegistry(registry);

		keys.add(key);
		
	}
	
	/**
	 * <p>getKeyRegistry.</p>
	 *
	 * @param registry a {@link java.lang.String} object
	 * @return a {@link java.util.Set} object
	 */
	protected Set<String> getKeyRegistry(String registry) {
		Set<String> keys = TransactionSupportUtil.getResource(generateDefaultKey()+"_"+registry);
		if (keys == null) {
			keys = new HashSet<>();
			TransactionSupportUtil.bindResource(generateDefaultKey()+"_"+registry, keys);
		}
		return keys;
	}
	

	/**
	 * <p>queueAssoc.</p>
	 *
	 * @param associationRef a {@link org.alfresco.service.cmr.repository.AssociationRef} object.
	 */
	protected void queueAssoc(AssociationRef associationRef) {
		queueAssoc(generateDefaultKey(), associationRef);
		
	}
	
	
	/**
	 * <p>queueAssoc.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param associationRef a {@link org.alfresco.service.cmr.repository.AssociationRef} object.
	 */
	protected void queueAssoc(String key, AssociationRef associationRef) {
		addKeyRegistry(ASSOC_REGISTRY, key);
		
		
		Set<AssociationRef> pendingNodes = TransactionSupportUtil.getResource(key);
		if (pendingNodes == null) {
			pendingNodes = new LinkedHashSet<>();

			TransactionSupportUtil.bindResource(key, pendingNodes);
			AlfrescoTransactionSupport.bindListener(transactionListener);
		}
		pendingNodes.add(associationRef);
		
	}


	/**
	 * <p>unQueueNode.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected void unQueueNode(NodeRef nodeRef) {
		unQueueNode(generateDefaultKey(), nodeRef);
	}

	/**
	 * <p>unQueueNode.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	protected void unQueueNode(String key, NodeRef entityNodeRef) {
		Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);
		if (pendingNodes != null) {
			pendingNodes.remove(entityNodeRef);
		}
	}

	/**
	 * <p>containsNodeInQueue.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean containsNodeInQueue(NodeRef nodeRef) {
		return containsNodeInQueue(generateDefaultKey(), nodeRef);
	}

	/**
	 * <p>containsNodeInQueue.</p>
	 *
	 * @param key a {@link java.lang.String} object.
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	protected boolean containsNodeInQueue(String key, NodeRef entityNodeRef) {
		Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);
		return (pendingNodes != null) && pendingNodes.contains(entityNodeRef);
	}

	/**
	 * <p>generateDefaultKey.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	protected String generateDefaultKey() {
		return "KEY_" + this.getClass().getName();
	}

	/**
	 * <p>isPropChanged.</p>
	 *
	 * @param before a {@link java.util.Map} object.
	 * @param after a {@link java.util.Map} object.
	 * @param propertyQName a {@link org.alfresco.service.namespace.QName} object.
	 * @return a boolean.
	 */
	protected boolean isPropChanged(Map<QName, Serializable> before, Map<QName, Serializable> after, QName propertyQName) {
		Serializable beforeProp = before.get(propertyQName);
		Serializable afterProp = after.get(propertyQName);

		if (((afterProp != null) && !afterProp.equals(beforeProp)) || (afterProp == null && beforeProp != null)) {
			return true;
		}
		return false;
	}

	protected class BeCPGPolicyTransactionListener extends TransactionListenerAdapter {

		private String id = GUID.generate();
	
		
		public BeCPGPolicyTransactionListener(String prefix) {
			this.id =prefix+"-"+this.id;
		}

		@Override
		public void beforeCommit(boolean readOnly) {

			StopWatch watch = null;

			boolean setPostTransactionListener = false;
			
			//Avoid concurrency issue by making a copy
			for (String key : new HashSet<>(getKeyRegistry(KEY_REGISTRY))) {

				Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);

				if (pendingNodes != null) {

					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}

					if(doBeforeCommit(key, pendingNodes)) {
						//Nodes has been consumed by doBeforeCommit unbind it and set postTransaction 
						//else keep it in the transaction for after commit use
						TransactionSupportUtil.bindResource(key, null);
						setPostTransactionListener = true;
					}

					if (logger.isDebugEnabled() && watch!=null) {
						watch.stop();
						logger.debug(id + " - BeforeCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + key+"  - pendingNodesSize : "+pendingNodes.size());
					}
					
					
				}
			}
			
			for (String key : new HashSet<>(getKeyRegistry(ASSOC_REGISTRY))) {

				Set<AssociationRef> pendingAssocs = TransactionSupportUtil.getResource(key);

				if (pendingAssocs != null) {

					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}

					if(doBeforeAssocsCommit(key, pendingAssocs)) {
						//Nodes has been consumed by doBeforeCommit unbind it and set postTransaction 
						//else keep it in the transaction for after commit use
						TransactionSupportUtil.bindResource(key, null);
						setPostTransactionListener = true;
					}

					if (logger.isDebugEnabled()  && watch!=null) {
						watch.stop();
						logger.debug(id + " - BeforeCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + key+"  - pendingAssocsSize : "+pendingAssocs.size());
					}
					
					
				}
			}
			
			
			if(setPostTransactionListener) {
			   AlfrescoTransactionSupport.bindListener(postTransactionListener);
			}
			
		}

		@Override
		public void afterCommit() {

			StopWatch watch = null;

			for (String key : getKeyRegistry(KEY_REGISTRY)) {

				Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);

				if (pendingNodes != null) {

					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}

					doAfterCommit(key, pendingNodes);

					if (logger.isDebugEnabled()  && watch!=null) {
						watch.stop();
						logger.debug(id + " - AfterCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + key+"  - pendingNodesSize : "+pendingNodes.size());

					}
				}
			}
			for (String key : getKeyRegistry(ASSOC_REGISTRY)) {
				
				Set<AssociationRef> pendingAssocs = TransactionSupportUtil.getResource(key);
				
				if (pendingAssocs != null) {
					
					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}
					
					doAfterAssocsCommit(key, pendingAssocs);
					
					if (logger.isDebugEnabled()  && watch!=null) {
						watch.stop();
						logger.debug(id + " - AfterCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + key+"  - pendingASSOCSSize : "+pendingAssocs.size());
						
					}
				}
			}
		}
		

		/**
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode()
		{
			return this.id.hashCode();
		}
		
		/**
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
	        {
	            return true;
	        }
	        if (obj instanceof BeCPGPolicyTransactionListener)
	        {
	        	BeCPGPolicyTransactionListener that = (BeCPGPolicyTransactionListener) obj;
	            return (this.id.equals(that.id));
	        }
	        else
	        {
	            return false;
	        }
		}

	}
}

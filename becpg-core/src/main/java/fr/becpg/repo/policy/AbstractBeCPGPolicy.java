/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG.
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
import org.alfresco.repo.version.Version2Model;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.alfresco.util.transaction.TransactionSupportUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;

@EnableAspectJAutoProxy
public abstract class AbstractBeCPGPolicy implements CopyServicePolicies.OnCopyNodePolicy, CopyServicePolicies.OnCopyCompletePolicy {

	protected BehaviourFilter policyBehaviourFilter;

	protected PolicyComponent policyComponent;

	protected LockService lockService;

	protected NodeService nodeService;

	private AbstractBeCPGPolicyTransactionListener transactionListener;

	private final Set<String> keys = new HashSet<>();

	private static final Log logger = LogFactory.getLog(AbstractBeCPGPolicy.class);

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
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyNodePolicy.QNAME, type, new JavaBehaviour(this, "getCopyCallback"));
		policyComponent.bindClassBehaviour(CopyServicePolicies.OnCopyCompletePolicy.QNAME, type, new JavaBehaviour(this, "onCopyComplete"));

	}

	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		policyBehaviourFilter.disableBehaviour(copyDetails.getTargetNodeRef(), classRef);
		return new DefaultCopyBehaviourCallback();
	}

	@Override
	public void onCopyComplete(QName classRef, NodeRef sourceNodeRef, NodeRef destinationRef, boolean copyToNewNode, Map<NodeRef, NodeRef> copyMap) {

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

	protected boolean isEntityTemplate(NodeRef nodeRef) {
		return nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL);
	}

	protected boolean isVersionStoreNode(NodeRef nodeRef) {
		return nodeRef.getStoreRef().getIdentifier().equals(Version2Model.STORE_ID);
	}

	protected boolean isVersionNode(NodeRef nodeRef) {
		// Ignore if the node is a working copy or version node
		return isBeCPGVersion(nodeRef) || isVersionStoreNode(nodeRef);
	}

	protected boolean isNotLocked(NodeRef nodeRef) {
		return nodeService.exists(nodeRef) && (lockService.getLockStatus(nodeRef) == LockStatus.NO_LOCK);
	}

	public abstract void doInit();

	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		// Do Nothing
	}

	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		// Do Nothing
	}

	protected void queueNode(NodeRef nodeRef) {
		queueNode(generateDefaultKey(), nodeRef);
	}

	protected void queueNode(String key, NodeRef nodeRef) {
		Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);
		if (pendingNodes == null) {
			pendingNodes = new LinkedHashSet<>();
			keys.add(key);
			TransactionSupportUtil.bindResource(key, pendingNodes);
			AlfrescoTransactionSupport.bindListener(this.transactionListener);
		}
		if (!pendingNodes.contains(nodeRef)) {
			pendingNodes.add(nodeRef);
		}
	}

	protected void unQueueNode(NodeRef nodeRef) {
		unQueueNode(generateDefaultKey(), nodeRef);
	}

	protected void unQueueNode(String key, NodeRef entityNodeRef) {
		Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);
		if (pendingNodes != null) {
			pendingNodes.remove(entityNodeRef);
		}
	}

	protected boolean containsNodeInQueue(NodeRef nodeRef) {
		return containsNodeInQueue(generateDefaultKey(), nodeRef);
	}

	protected boolean containsNodeInQueue(String key, NodeRef entityNodeRef) {
		Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);
		return (pendingNodes != null) && pendingNodes.contains(entityNodeRef);
	}

	protected String generateDefaultKey() {
		return "KEY_" + this.getClass().getName();
	}

	protected boolean isPropChanged(Map<QName, Serializable> before, Map<QName, Serializable> after, QName propertyQName) {
		Serializable beforeProp = before.get(propertyQName);
		Serializable afterProp = after.get(propertyQName);

		if ((afterProp != null) && !afterProp.equals(beforeProp)) {
			return true;
		}
		return false;
	}

	class AbstractBeCPGPolicyTransactionListener extends TransactionListenerAdapter {

		@Override
		public void beforeCommit(boolean readOnly) {

			StopWatch watch = null;

			for (String key : keys) {

				Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);

				if (pendingNodes != null) {

					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}

					doBeforeCommit(key, pendingNodes);

					if (logger.isDebugEnabled()) {
						watch.stop();
						logger.debug("BeforeCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + key+"  - pendingNodesSize : "+pendingNodes.size());
					}
				}
			}
		}

		@Override
		public void afterCommit() {

			StopWatch watch = null;

			for (String key : keys) {

				Set<NodeRef> pendingNodes = TransactionSupportUtil.getResource(key);

				if (pendingNodes != null) {

					if (logger.isDebugEnabled()) {
						watch = new StopWatch();
						watch.start();
					}

					doAfterCommit(key, pendingNodes);

					if (logger.isDebugEnabled()) {
						watch.stop();
						logger.debug("AfterCommit run in  " + watch.getTotalTimeSeconds() + " seconds for key " + key+"  - pendingNodesSize : "+pendingNodes.size());

					}
				}
			}
		}

	}
}

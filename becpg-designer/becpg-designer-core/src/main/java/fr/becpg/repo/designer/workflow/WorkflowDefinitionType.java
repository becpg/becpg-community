/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.designer.workflow;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.content.ContentServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListenerAdapter;
import org.alfresco.repo.workflow.WorkflowModel;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Workflow Definition type behaviour.
 * 
 * Hook on workflow deployer that create corresponding model
 * 
 * @author matthieu
 */
public class WorkflowDefinitionType implements ContentServicePolicies.OnContentUpdatePolicy, NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.OnUpdateNodePolicy {

	/** The policy component */
	private PolicyComponent policyComponent;

	private NodeService nodeService;

	private DesignerWorkflowDeployer designerWorkflowDeployer;

	/** Transaction listener */
	private WorkflowDefinitionTypeTransactionListener transactionListener;

	/** Key to the pending models */
	private static final String KEY_PENDING_DEFS = "workflowDefinitionType.pendingDefs";

	private static Log logger = LogFactory.getLog(WorkflowDefinitionType.class);

	/**
	 * Set the policy component
	 * 
	 * @param policyComponent
	 *            the policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setDesignerWorkflowDeployer(DesignerWorkflowDeployer designerWorkflowDeployer) {
		this.designerWorkflowDeployer = designerWorkflowDeployer;
	}

	/**
	 * The initialise method
	 */
	public void init() {
		// Register interest in the onContentUpdate policy for the workflow
		// definition type
		policyComponent.bindClassBehaviour(ContentServicePolicies.OnContentUpdatePolicy.QNAME, WorkflowModel.TYPE_WORKFLOW_DEF, new JavaBehaviour(this, "onContentUpdate"));

		// Register interest in the onCreateNode policy - for content
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, WorkflowModel.TYPE_WORKFLOW_DEF, new JavaBehaviour(this, "onCreateNode"));

		// Register interest in the onUpdateProperties policy for the dictionary
		// model type
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdateNodePolicy.QNAME, WorkflowModel.TYPE_WORKFLOW_DEF, new JavaBehaviour(this, "onUpdateNode"));

		this.transactionListener = new WorkflowDefinitionTypeTransactionListener(this.nodeService);

	}

	/**
	 * On content update behaviour implementation
	 * 
	 * @param nodeRef
	 *            the node reference whose content has been updated
	 */
	public void onContentUpdate(NodeRef nodeRef, boolean newContent) {

		if (logger.isDebugEnabled()) {
			logger.debug("onContentUpdate: nodeRef=" + nodeRef + " [" + AlfrescoTransactionSupport.getTransactionId() + "]");
		}

		Boolean value = (Boolean) nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
		if ((value == null) || (value == false)) {
			queueModel(nodeRef);

		}
	}

	@SuppressWarnings("unchecked")
	private void queueModel(NodeRef nodeRef) {

		Set<NodeRef> pendingModels = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_PENDING_DEFS);
		if (pendingModels == null) {
			pendingModels = new CopyOnWriteArraySet<NodeRef>();
			AlfrescoTransactionSupport.bindResource(KEY_PENDING_DEFS, pendingModels);
		}
		pendingModels.add(nodeRef);

		AlfrescoTransactionSupport.bindListener(this.transactionListener);

	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {

		NodeRef nodeRef = childAssocRef.getChildRef();
		if (logger.isDebugEnabled()) {
			logger.debug("onCreateNode: nodeRef=" + nodeRef + " [" + AlfrescoTransactionSupport.getTransactionId() + "]");
		}

		if (nodeService.getType(nodeRef).equals(WorkflowModel.TYPE_WORKFLOW_DEF)) {
			Boolean value = (Boolean) nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
			if ((value == null) || (value == false)) {
				queueModel(nodeRef);
			}
		}

	}

	@Override
	public void onUpdateNode(NodeRef nodeRef) {

		if (logger.isDebugEnabled()) {
			logger.debug("onUpdateNode: nodeRef=" + nodeRef + " [" + AlfrescoTransactionSupport.getTransactionId() + "]");
		}
		if (nodeService.getType(nodeRef).equals(WorkflowModel.TYPE_WORKFLOW_DEF)) {
			Boolean value = (Boolean) nodeService.getProperty(nodeRef, WorkflowModel.PROP_WORKFLOW_DEF_DEPLOYED);
			if ((value == null) || (value == false)) {
				queueModel(nodeRef);

			}
		}

	}

	/**
	 * Dictionary model type transaction listener class.
	 */
	public class WorkflowDefinitionTypeTransactionListener extends TransactionListenerAdapter {

		private NodeService nodeService;

		public WorkflowDefinitionTypeTransactionListener(NodeService nodeService) {
			this.nodeService = nodeService;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void beforeCommit(boolean readOnly) {
			Set<NodeRef> pendingModels = (Set<NodeRef>) AlfrescoTransactionSupport.getResource(KEY_PENDING_DEFS);
			
			if (pendingModels != null) {

				for (final NodeRef nodeRef : pendingModels) {

					AuthenticationUtil.runAsSystem(new RunAsWork<Object>() {
						public Object doWork() {
							// Ignore if the node no longer exists
							if (!nodeService.exists(nodeRef)) {
								return null;
							}

							// Ignore if the node is a working copy
							if (!(nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY))) {
								designerWorkflowDeployer.createMissingFormsAndType(nodeRef);

							}

							return null;
						}
					});
				}
				// unbind the resource from the transaction
				AlfrescoTransactionSupport.unbindResource(KEY_PENDING_DEFS);

			}

		}

	}

}

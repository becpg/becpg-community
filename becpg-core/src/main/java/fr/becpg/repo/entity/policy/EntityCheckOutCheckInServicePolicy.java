/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
package fr.becpg.repo.entity.policy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.report.entity.EntityReportAsyncGenerator;

/**
 * 
 * @author quere
 * 
 */
public class EntityCheckOutCheckInServicePolicy extends AbstractBeCPGPolicy implements CheckOutCheckInServicePolicies.OnCheckOut,
		CheckOutCheckInServicePolicies.BeforeCheckIn, CheckOutCheckInServicePolicies.OnCheckIn, CheckOutCheckInServicePolicies.BeforeCancelCheckOut,
		NodeServicePolicies.OnRemoveAspectPolicy, NodeServicePolicies.OnDeleteNodePolicy, CheckOutCheckInServicePolicies.OnCancelCheckOut {

	private static final Log logger = LogFactory.getLog(EntityCheckOutCheckInServicePolicy.class);

	private EntityVersionService entityVersionService;

	private EntityReportAsyncGenerator entityReportAsyncGenerator;

	private RuleService ruleService;

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setEntityReportAsyncGenerator(EntityReportAsyncGenerator entityReportAsyncGenerator) {
		this.entityReportAsyncGenerator = entityReportAsyncGenerator;
	}

	/**
	 * Inits the.
	 */
	public void doInit() {
		logger.debug("Init EntityCheckOutCheckInServicePolicy...");

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckOut.QNAME, BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this,
				"onCheckOut"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCheckIn.QNAME, BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this,
				"beforeCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(this,
				"onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCancelCheckOut.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "beforeCancelCheckOut"));

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCancelCheckOut.QNAME, BeCPGModel.ASPECT_ENTITYLISTS, new JavaBehaviour(
				this, "onCancelCheckOut"));

		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"), ContentModel.ASPECT_VERSIONABLE,
				new JavaBehaviour(this, "onRemoveAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), ContentModel.ASPECT_VERSIONABLE,
				new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));
	}

	@Override
	public void onCheckOut(final NodeRef workingCopyNodeRef) {
		ruleService.disableRules();
		try {
			NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);
			entityVersionService.doCheckOut(origNodeRef, workingCopyNodeRef);
		} finally {
			ruleService.enableRules();
		}
	}

	@Override
	public void beforeCheckIn(NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties, String contentUrl, boolean keepCheckedOut) {
		ruleService.disableRules();
		try {
			NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);
			queueNode(entityVersionService.createVersionAndCheckin(origNodeRef, workingCopyNodeRef, versionProperties));
		} finally {
			ruleService.enableRules();
		}
	}

	@Override
	public void onCheckIn(NodeRef nodeRef) {

		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_EFFECTIVITY)) {
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, new Date());
			nodeService.removeProperty(nodeRef, BeCPGModel.PROP_END_EFFECTIVITY);
		}
		queueNode(nodeRef);
	}

	@Override
	public void beforeCancelCheckOut(final NodeRef workingCopyNodeRef) {
		ruleService.disableRules();
		try {
			NodeRef origNodeRef = getCheckedOut(workingCopyNodeRef);
			entityVersionService.cancelCheckOut(origNodeRef, workingCopyNodeRef);
		} finally {
			ruleService.enableRules();
		}
	}

	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {
		ruleService.disableRules();
		try {
			logger.debug("OnDeleteNode cm:versionable " + childAssocRef.getChildRef() + " isNodeArchived: " + isNodeArchived);
			// if (isNodeArchived == false) { //Move history under archive store
			// instead
			// If we are permanantly deleting the node then we need to
			// remove
			// the associated version history
			entityVersionService.deleteVersionHistory(childAssocRef.getChildRef());
			// }
		} finally {
			ruleService.enableRules();
		}
	}

	@Override
	public void onRemoveAspect(NodeRef nodeRef, QName aspectTypeQName) {
		ruleService.disableRules();
		try {
			// When the versionable aspect is removed from a node, then delete
			// the
			// associated version history
			entityVersionService.deleteVersionHistory(nodeRef);
		} finally {
			ruleService.enableRules();
		}
	}

	private NodeRef getCheckedOut(NodeRef nodeRef) {
		NodeRef original = null;
		if (nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)) {
			List<AssociationRef> assocs = nodeService.getSourceAssocs(nodeRef, ContentModel.ASSOC_WORKING_COPY_LINK);
			// It is a 1:1 relationship
			if (!assocs.isEmpty()) {
				if (logger.isWarnEnabled()) {
					if (assocs.size() > 1) {
						logger.warn("Found multiple " + ContentModel.ASSOC_WORKING_COPY_LINK + " associations to node: " + nodeRef);
					}
				}
				original = assocs.get(0).getSourceRef();
			}
		}

		return original;
	}

	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		entityReportAsyncGenerator.queueNodes(new ArrayList<>(pendingNodes));

	}

	@Override
	public void onCancelCheckOut(NodeRef nodeRef) {
		ruleService.disableRules();
		try {
			entityVersionService.afterCancelCheckOut(nodeRef);

		} finally {
			ruleService.enableRules();
		}

	}

}

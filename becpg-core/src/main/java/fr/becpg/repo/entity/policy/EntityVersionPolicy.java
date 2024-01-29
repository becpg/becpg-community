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
package fr.becpg.repo.entity.policy;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.batch.BatchProcessWorkProvider;
import org.alfresco.repo.batch.BatchProcessor;
import org.alfresco.repo.batch.BatchProcessor.BatchProcessWorker;
import org.alfresco.repo.coci.CheckOutCheckInServicePolicies;
import org.alfresco.repo.node.NodeArchiveServicePolicies;
import org.alfresco.repo.node.NodeArchiveServicePolicies.BeforePurgeNodePolicy;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.version.VersionServicePolicies;
import org.alfresco.repo.version.common.VersionUtil;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.rule.RuleService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.batch.BatchInfo;
import fr.becpg.repo.batch.BatchQueueService;
import fr.becpg.repo.batch.EntityListBatchProcessWorkProvider;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * <p>EntityVersionPolicy class.</p>
 *
 * @author quere
 * @version $Id: $Id
 */
public class EntityVersionPolicy extends AbstractBeCPGPolicy
		implements CheckOutCheckInServicePolicies.OnCheckIn,
		CheckOutCheckInServicePolicies.BeforeCancelCheckOut, NodeServicePolicies.OnRemoveAspectPolicy,
		NodeArchiveServicePolicies.BeforePurgeNodePolicy, CheckOutCheckInServicePolicies.OnCancelCheckOut, NodeServicePolicies.OnDeleteNodePolicy, VersionServicePolicies.AfterCreateVersionPolicy {

	private static final Log logger = LogFactory.getLog(EntityVersionPolicy.class);

	private EntityVersionService entityVersionService;

	private EntityReportService entityReportService;
	
	private BatchQueueService batchQueueService;

	private RuleService ruleService;
	
	/**
	 * <p>Setter for the field <code>ruleService</code>.</p>
	 *
	 * @param ruleService a {@link org.alfresco.service.cmr.rule.RuleService} object.
	 */
	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setBatchQueueService(BatchQueueService batchQueueService) {
		this.batchQueueService = batchQueueService;
	}

	/**
	 * {@inheritDoc}
	 *
	 * Inits the.
	 */
	@Override
	public void doInit() {
		logger.debug("Init EntityVersionPolicy...");

	
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCheckIn.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "onCheckIn"));
		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.BeforeCancelCheckOut.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "beforeCancelCheckOut"));

		policyComponent.bindClassBehaviour(CheckOutCheckInServicePolicies.OnCancelCheckOut.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "onCancelCheckOut"));
		
		policyComponent.bindClassBehaviour(VersionServicePolicies.AfterCreateVersionPolicy.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "afterCreateVersion"));
		
		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onRemoveAspect"), ContentModel.ASPECT_VERSIONABLE,
				new JavaBehaviour(this, "onRemoveAspect", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		this.policyComponent.bindClassBehaviour(BeforePurgeNodePolicy.QNAME, BeCPGModel.ASPECT_ENTITYLISTS,
				new JavaBehaviour(this, "beforePurgeNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

		this.policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "onDeleteNode"), ContentModel.ASPECT_VERSIONABLE,
				new JavaBehaviour(this, "onDeleteNode", Behaviour.NotificationFrequency.TRANSACTION_COMMIT));

	}

	/** {@inheritDoc} */
	@Override
	public void onCheckIn(NodeRef nodeRef) {
		if (nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_EFFECTIVITY)) {
			nodeService.setProperty(nodeRef, BeCPGModel.PROP_START_EFFECTIVITY, new Date());
			nodeService.removeProperty(nodeRef, BeCPGModel.PROP_END_EFFECTIVITY);
		}
		queueNode(nodeRef);
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void beforePurgeNode(NodeRef entityNodeRef) {
		ruleService.disableRules();
		try {
			logger.debug("OnDeleteNode cm:versionable " + entityNodeRef);
			entityVersionService.deleteVersionHistory(entityNodeRef);
		} finally {
			ruleService.enableRules();
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onDeleteNode(ChildAssociationRef childAssocRef, boolean isNodeArchived) {

		ruleService.disableRules();
		try {

			// instead
			// If we are permanently deleting the node then we need to
			// remove
			// the associated version history
			
			entityVersionService.deleteVersionHistory(childAssocRef.getChildRef());
		} finally {
			ruleService.enableRules();
		}
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		
		String entityDescription = null;

		if (!pendingNodes.isEmpty()) {
			
			NodeRef versionNode = pendingNodes.iterator().next();
			
			entityDescription = nodeService.getProperty(versionNode, BeCPGModel.PROP_CODE) + " " + nodeService.getProperty(versionNode, ContentModel.PROP_NAME);
		}

		BatchInfo batchInfo = new BatchInfo(String.format("generateVersionReports-%s", Calendar.getInstance().getTimeInMillis()),
				"becpg.batch.entityVersion.generateReports", entityDescription);
		batchInfo.setRunAsSystem(true);

		BatchProcessWorkProvider<NodeRef> workProvider = new EntityListBatchProcessWorkProvider<>(new ArrayList<>(pendingNodes));

		BatchProcessWorker<NodeRef> processWorker = new BatchProcessor.BatchProcessWorkerAdaptor<>() {

			@Override
			public void process(NodeRef entityNodeRef) throws Throwable {

				NodeRef extractedNode = entityNodeRef;
				if (entityVersionService.isVersion(entityNodeRef)
						&& (nodeService.getProperty(entityNodeRef, BeCPGModel.PROP_ENTITY_FORMAT) != null)) {
					extractedNode = entityVersionService.extractVersion(entityNodeRef);
				}

				entityReportService.generateReports(extractedNode, entityNodeRef);

			}
		};

		batchQueueService.queueBatch(batchInfo, workProvider, processWorker, null);
		
	}

	/** {@inheritDoc} */
	@Override
	public void onCancelCheckOut(NodeRef nodeRef) {
		ruleService.disableRules();
		try {
			entityVersionService.afterCancelCheckOut(nodeRef);

		} finally {
			ruleService.enableRules();
		}

	}

	@Override
	public void afterCreateVersion(NodeRef versionableNode, Version version) {
		queueNode(VersionUtil.convertNodeRef(version.getFrozenStateNodeRef()));
	}


}

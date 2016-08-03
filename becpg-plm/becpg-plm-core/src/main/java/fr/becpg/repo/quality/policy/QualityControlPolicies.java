/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
package fr.becpg.repo.quality.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.quality.QualityControlService;

public class QualityControlPolicies extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
		NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateNodePolicy {

	private static final Log logger = LogFactory.getLog(QualityControlPolicies.class);

	private QualityControlService qualityControlService;

	private TransactionService transactionService;

	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	public void setQualityControlService(QualityControlService qualityControlService) {
		this.qualityControlService = qualityControlService;
	}

	@Override
	public void doInit() {

		logger.debug("Init QualityControlPolicies...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, QualityModel.TYPE_QUALITY_CONTROL,
				QualityModel.ASSOC_QC_CONTROL_PLANS, new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, QualityModel.TYPE_SAMPLING_LIST,
				QualityModel.ASSOC_SL_CONTROL_POINT, new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, QualityModel.TYPE_CONTROL_LIST,
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, QualityModel.TYPE_SAMPLING_LIST,
				new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

	}

	@Override
	public void onCreateAssociation(AssociationRef assocRef) {

		logger.debug("QualityControlPolicies onCreateAssociation");
		if (assocRef.getTypeQName().equals(QualityModel.ASSOC_QC_CONTROL_PLANS)) {
			qualityControlService.createSamplingList(assocRef.getSourceRef(), assocRef.getTargetRef());
		} else if (assocRef.getTypeQName().equals(QualityModel.ASSOC_SL_CONTROL_POINT)) {
			qualityControlService.createControlList(assocRef.getSourceRef());
		}
	}

	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (isPropChanged(before, after, QualityModel.PROP_CL_VALUE) || isPropChanged(before, after, QualityModel.PROP_CL_STATE)) {
			logger.debug("QualityControlPolicies onUpdateProperties.");
			qualityControlService.updateControlListState(nodeRef);
		}
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		// Needed as beCPG Code can be create beforeCommit
		queueNode(childAssocRef.getChildRef());
	}

	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {

		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			for (NodeRef nodeRef : pendingNodes) {
				if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef)) {
					qualityControlService.createSamplingListId(nodeRef);
				}
			}
			return null;
		}, false, true);

	}
}

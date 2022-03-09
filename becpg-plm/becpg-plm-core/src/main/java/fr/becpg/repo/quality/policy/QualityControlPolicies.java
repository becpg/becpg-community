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
package fr.becpg.repo.quality.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.copy.CopyBehaviourCallback;
import org.alfresco.repo.copy.CopyDetails;
import org.alfresco.repo.copy.CopyServicePolicies;
import org.alfresco.repo.copy.DoNothingCopyBehaviourCallback;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.service.transaction.TransactionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.quality.QualityControlService;

/**
 * <p>QualityControlPolicies class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class QualityControlPolicies extends AbstractBeCPGPolicy implements NodeServicePolicies.OnCreateAssociationPolicy,
NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateNodePolicy, NodeServicePolicies.BeforeDeleteNodePolicy,CopyServicePolicies.OnCopyNodePolicy, EntityVersionPlugin {

	private static final Log logger = LogFactory.getLog(QualityControlPolicies.class);

	private QualityControlService qualityControlService;

	private TransactionService transactionService;
	
	private EntityListDAO entityListDAO;

	private static final  String KEY_PREFIX_CTRL_PLAN_ASSOC = QualityControlPolicies.class.getName() + "_CONTROL_PLANS_ASSOC_";
	private static final  String KEY_PREFIX_PRODUCT_ASSOC = QualityControlPolicies.class.getName() + "_PRODUCT_ASSOC_";


	/**
	 * <p>Setter for the field <code>transactionService</code>.</p>
	 *
	 * @param transactionService a {@link org.alfresco.service.transaction.TransactionService} object.
	 */
	public void setTransactionService(TransactionService transactionService) {
		this.transactionService = transactionService;
	}

	/**
	 * <p>Setter for the field <code>qualityControlService</code>.</p>
	 *
	 * @param qualityControlService a {@link fr.becpg.repo.quality.QualityControlService} object.
	 */
	public void setQualityControlService(QualityControlService qualityControlService) {
		this.qualityControlService = qualityControlService;
	}
	
	

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/** {@inheritDoc} */
	@Override
	public void doInit() {

		logger.debug("Init QualityControlPolicies...");

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, QualityModel.TYPE_QUALITY_CONTROL,
				QualityModel.ASSOC_QC_CONTROL_PLANS, new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, QualityModel.TYPE_QUALITY_CONTROL,
				QualityModel.ASSOC_PRODUCT, new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, QualityModel.TYPE_SAMPLING_LIST,
				QualityModel.ASSOC_SL_CONTROL_POINT, new JavaBehaviour(this, "onCreateAssociation", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, QualityModel.TYPE_CONTROL_LIST,
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, QualityModel.TYPE_SAMPLING_LIST,
				new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, QualityModel.TYPE_SAMPLING_LIST,
				new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));

		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), QualityModel.TYPE_CONTROL_LIST,
				new JavaBehaviour(this, "getCopyCallback"));
		
		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), QualityModel.TYPE_SAMPLING_LIST,
				new JavaBehaviour(this, "getCopyCallback"));
		
		policyComponent.bindClassBehaviour(QName.createQName(NamespaceService.ALFRESCO_URI, "getCopyCallback"), QualityModel.TYPE_STOCK_LIST,
				new JavaBehaviour(this, "getCopyCallback"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, QualityModel.TYPE_SAMPLING_LIST,
				new JavaBehaviour(this, "beforeDeleteNode"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.BeforeDeleteNodePolicy.QNAME, QualityModel.TYPE_CONTROL_LIST,
				new JavaBehaviour(this, "beforeDeleteNode"));

	}

	/** {@inheritDoc} */
	@Override
	//TODO used queueAssoc instead
	public void onCreateAssociation(AssociationRef assocRef) {
		logger.debug("QualityControlPolicies onCreateAssociation");
		if (assocRef.getTypeQName().equals(QualityModel.ASSOC_QC_CONTROL_PLANS)) {
			// Needed as beCPG Code can be create beforeCommit
			queueNode(KEY_PREFIX_CTRL_PLAN_ASSOC + assocRef.getSourceRef().toString(), assocRef.getTargetRef());
		} else if (assocRef.getTypeQName().equals(QualityModel.ASSOC_PRODUCT)) {
			queueNode(KEY_PREFIX_PRODUCT_ASSOC + assocRef.getSourceRef().toString(), assocRef.getTargetRef());
		} else if (assocRef.getTypeQName().equals(QualityModel.ASSOC_SL_CONTROL_POINT)) {
			qualityControlService.createControlList(assocRef.getSourceRef());
		} 
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		if (isPropChanged(before, after, QualityModel.PROP_CL_VALUE) || isPropChanged(before, after, QualityModel.PROP_CL_STATE)) {
			logger.debug("QualityControlPolicies onUpdateProperties.");
			qualityControlService.updateControlListState(nodeRef);
		}
		if (isPropChanged(before, after, QualityModel.PROP_SL_SAMPLE_STATE)) {
			qualityControlService.updateQualityControlState(nodeRef);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		qualityControlService.createSamplingListId(childAssocRef.getChildRef());
	}


	/** {@inheritDoc} */
	@Override
	public CopyBehaviourCallback getCopyCallback(QName classRef, CopyDetails copyDetails) {
		return DoNothingCopyBehaviourCallback.getInstance();
	}


	/** {@inheritDoc} */
	@Override
	protected void doAfterCommit(String key, Set<NodeRef> pendingNodes) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			for (NodeRef nodeRef : pendingNodes) {
				if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef)) {
					if (key.startsWith(KEY_PREFIX_CTRL_PLAN_ASSOC)) {
						qualityControlService.createSamplingList(new NodeRef(key.replaceFirst(KEY_PREFIX_CTRL_PLAN_ASSOC, "")), nodeRef);
					} else if (key.startsWith(KEY_PREFIX_PRODUCT_ASSOC)) {
						qualityControlService.copyProductDataList(new NodeRef(key.replaceFirst(KEY_PREFIX_PRODUCT_ASSOC, "")), nodeRef);
					}
				}
			}
			return null;
		}, false, true);

	}

	/** {@inheritDoc} */
	@Override
	public void beforeDeleteNode(NodeRef nodeRef) {
		if (QualityModel.TYPE_CONTROL_LIST.equals(nodeService.getType(nodeRef))) {
			qualityControlService.updateControlListState(nodeRef);
		} else {
			qualityControlService.deleteSamplingListId(nodeRef);
		}

	}

	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		NodeRef listContainerNodeRef = entityListDAO.getListContainer(origNodeRef);
		if(listContainerNodeRef!=null) {
			NodeRef stockListNodeRef = entityListDAO.getList(listContainerNodeRef, QualityModel.TYPE_STOCK_LIST);
			if(stockListNodeRef!=null) {
				try {
					policyBehaviourFilter.disableBehaviour( QualityModel.TYPE_STOCK_LIST);
					entityListDAO.copyDataList(stockListNodeRef, workingCopyNodeRef, true);
				} finally {
					policyBehaviourFilter.disableBehaviour( QualityModel.TYPE_STOCK_LIST);
				}
			}
		}
		
	}

	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description) {
		// TODO Auto-generated method stub
		
	}
}

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
import java.util.Date;
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

	private static final  String KEY_PREFIX_CTRL_PLAN_ASSOC = QualityControlPolicies.class.getName() + "_CONTROL_PLANS_ASSOC";
	private static final  String KEY_PREFIX_PRODUCT_ASSOC = QualityControlPolicies.class.getName() + "_PRODUCT_ASSOC";
	private static final  String KEY_UPDATE_CONTROL_LIST_STATE = QualityControlPolicies.class.getName() + "_UPDATE_CONTROL_LIST_STATE";
	private static final  String KEY_UPDATE_QUALITY_CONTROL_STATE = QualityControlPolicies.class.getName() + "_UPDATE_QUALITY_CONTROL_STATE";
	private static final  String KEY_CREATE_CONTROL_LIST = QualityControlPolicies.class.getName() + "_CREATE_CONTROL_LIST";


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
	
	

	/**
	 * <p>Setter for the field <code>entityListDAO</code>.</p>
	 *
	 * @param entityListDAO a {@link fr.becpg.repo.entity.EntityListDAO} object
	 */
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
	public void onCreateAssociation(AssociationRef assocRef) {
		logger.debug("QualityControlPolicies onCreateAssociation");
		if (assocRef.getTypeQName().equals(QualityModel.ASSOC_QC_CONTROL_PLANS)) {
			// Needed as beCPG Code can be create beforeCommit
			queueAssoc(KEY_PREFIX_CTRL_PLAN_ASSOC, assocRef);
		} else if (assocRef.getTypeQName().equals(QualityModel.ASSOC_PRODUCT)) {
			queueAssoc(KEY_PREFIX_PRODUCT_ASSOC, assocRef);
		} else if (assocRef.getTypeQName().equals(QualityModel.ASSOC_SL_CONTROL_POINT)) {
			queueNode(KEY_CREATE_CONTROL_LIST, assocRef.getSourceRef());
		} 
	}

	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		if (isPropChanged(before, after, QualityModel.PROP_CL_VALUE) || isPropChanged(before, after, QualityModel.PROP_CL_STATE)) {
			logger.debug("QualityControlPolicies onUpdateProperties.");
			queueNode(KEY_UPDATE_CONTROL_LIST_STATE, nodeRef);
		}
		if (isPropChanged(before, after, QualityModel.PROP_SL_SAMPLE_STATE)) {
			queueNode(KEY_UPDATE_QUALITY_CONTROL_STATE, nodeRef);
		}
	}
	
	/** {@inheritDoc} */
	@Override
	protected boolean doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		if (KEY_UPDATE_CONTROL_LIST_STATE.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				qualityControlService.updateControlListState(pendingNode);
			}
		} else if (KEY_UPDATE_QUALITY_CONTROL_STATE.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				qualityControlService.updateQualityControlState(pendingNode);
			}
		} else if (KEY_CREATE_CONTROL_LIST.equals(key)) {
			for (NodeRef pendingNode : pendingNodes) {
				qualityControlService.createControlList(pendingNode);
			}
		}
		return true;
	}

	/** {@inheritDoc} */
	@Override
	protected void doAfterAssocsCommit(String key, Set<AssociationRef> pendingAssocs) {
		transactionService.getRetryingTransactionHelper().doInTransaction(() -> {
			for (AssociationRef assoc : pendingAssocs) {
				if (isNotLocked(assoc.getTargetRef()) && !isWorkingCopyOrVersion(assoc.getTargetRef())) {
					if (key.startsWith(KEY_PREFIX_CTRL_PLAN_ASSOC)) {
						qualityControlService.createSamplingList(assoc.getSourceRef(), assoc.getTargetRef());
					} else if (key.startsWith(KEY_PREFIX_PRODUCT_ASSOC)) {
						qualityControlService.copyProductDataList(assoc.getSourceRef(), assoc.getTargetRef());
					}
				}
			}
			return null;
		}, false, true);
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
	public void beforeDeleteNode(NodeRef nodeRef) {
		if (QualityModel.TYPE_CONTROL_LIST.equals(nodeService.getType(nodeRef))) {
			qualityControlService.updateControlListState(nodeRef);
		} else {
			qualityControlService.deleteSamplingListId(nodeRef);
		}

	}

	/** {@inheritDoc} */
	@Override
	public void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	@Override
	public void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef) {
		// TODO Auto-generated method stub
		
	}

	/** {@inheritDoc} */
	@Override
	public void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effetiveDate) {
		// TODO Auto-generated method stub
		
	}
}

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
package fr.becpg.repo.product.policy.productListUnits;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.product.data.productList.CostListDataItem;
import fr.becpg.repo.repository.AlfrescoRepository;
import fr.becpg.repo.repository.RepositoryEntity;

public class PriceListPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateNodePolicy {

	private static int PREF_RANK = 1;

	private static Log logger = LogFactory.getLog(PriceListPolicy.class);

	private EntityListDAO entityListDAO;

	private AlfrescoRepository<RepositoryEntity> alfrescoRepository;

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}


	public void setAlfrescoRepository(AlfrescoRepository<RepositoryEntity> alfrescoRepository) {
		this.alfrescoRepository = alfrescoRepository;
	}


	public void doInit() {
		logger.debug("Init productListUnits.PriceListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_PRICELIST, new JavaBehaviour(this, "onCreateNode"));

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_PRICELIST, new JavaBehaviour(this, "onUpdateProperties"));

		super.disableOnCopyBehaviour(BeCPGModel.TYPE_PRICELIST);
	}

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		NodeRef priceListItemNodeRef = childAssocRef.getChildRef();

		queueNode(priceListItemNodeRef);

	}

	@Override
	public void onUpdateProperties(NodeRef priceListItemNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		Integer beforePrefRank = (Integer) before.get(BeCPGModel.PROP_PRICELIST_PREF_RANK);
		Integer afterPrefRank = (Integer) after.get(BeCPGModel.PROP_PRICELIST_PREF_RANK);
		Double beforeValue = (Double) before.get(BeCPGModel.PROP_PRICELIST_VALUE);
		Double afterValue = (Double) after.get(BeCPGModel.PROP_PRICELIST_VALUE);
		boolean doUpdate = false;

		logger.debug("onUpdateProperties, prefRank before: " + beforePrefRank + "after: " + afterPrefRank);

		if (afterPrefRank != null && afterPrefRank.equals(PREF_RANK)) {

			if (!afterPrefRank.equals(beforePrefRank)) {
				doUpdate = true;
			} else if (afterValue != null && !afterValue.equals(beforeValue)) {
				doUpdate = true;
			}
		}

		if (doUpdate) {
			queueNode(priceListItemNodeRef);
		}
	}

	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (nodeService.exists(nodeRef)) {
				Integer prefRank = (Integer) nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRICELIST_PREF_RANK);

				logger.debug("onCreateNode, prefRank: " + prefRank);

				if (prefRank != null && prefRank.equals(PREF_RANK)) {
					updateCostList(nodeRef);
				}
			}
		}
	}

	private void updateCostList(NodeRef priceListItemNodeRef) {

		logger.debug("updateCostList");

		NodeRef priceListNodeRef = nodeService.getPrimaryParent(priceListItemNodeRef).getParentRef();
		NodeRef listContainerNodeRef = nodeService.getPrimaryParent(priceListNodeRef).getParentRef();
		NodeRef costListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
		Double value = (Double) nodeService.getProperty(priceListItemNodeRef, BeCPGModel.PROP_PRICELIST_VALUE);

		NodeRef costNodeRef = null;
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(priceListItemNodeRef, BeCPGModel.ASSOC_PRICELIST_COST);
		if (!assocRefs.isEmpty()) {
			costNodeRef = assocRefs.get(0).getTargetRef();
		}

		if (costListNodeRef != null) {
			NodeRef linkNodeRef = entityListDAO.getListItem(costListNodeRef, BeCPGModel.ASSOC_COSTLIST_COST, costNodeRef);

			if (linkNodeRef != null) {
				nodeService.setProperty(linkNodeRef, BeCPGModel.PROP_COSTLIST_VALUE, value);
			}
		} else {

			costListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
			
			CostListDataItem costListDataItem  = new CostListDataItem(null, value, null, null, costNodeRef, false);
			costListDataItem.setParentNodeRef(costListNodeRef);
			alfrescoRepository.save(costListDataItem);
		}
	}

}

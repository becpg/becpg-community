package fr.becpg.repo.product.policy.productListUnits;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.productList.CostListDataItem;

public class PriceListPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy, NodeServicePolicies.OnCreateNodePolicy {

	private static int PREF_RANK = 1;
	
	private static Log logger = LogFactory.getLog(PriceListPolicy.class);
	
	private PolicyComponent policyComponent;		
		
	private NodeService nodeService;
	
	private EntityListDAO entityListDAO;
	
	private ProductDAO productDAO;
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	public void init(){
		logger.debug("Init productListUnits.PriceListPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, 
				BeCPGModel.TYPE_PRICELIST, new JavaBehaviour(this, "onCreateNode", NotificationFrequency.TRANSACTION_COMMIT));
		
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
				BeCPGModel.TYPE_PRICELIST, new JavaBehaviour(this, "onUpdateProperties", NotificationFrequency.TRANSACTION_COMMIT));
	}		

	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		
		NodeRef priceListItemNodeRef = childAssocRef.getChildRef();
		
		Integer prefRank = (Integer)nodeService.getProperty(priceListItemNodeRef, BeCPGModel.PROP_PRICELIST_PREF_RANK);
		
		logger.debug("onCreateNode, prefRank: " + prefRank);
		
		if(prefRank != null && prefRank.equals(PREF_RANK)){
			updateCostList(priceListItemNodeRef);
		}
	}

	@Override
	public void onUpdateProperties(NodeRef priceListItemNodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		
		Integer beforePrefRank = (Integer)before.get(BeCPGModel.PROP_PRICELIST_PREF_RANK);
		Integer afterPrefRank = (Integer)after.get(BeCPGModel.PROP_PRICELIST_PREF_RANK);
		Double beforeValue = (Double)before.get(BeCPGModel.PROP_PRICELIST_VALUE);
		Double afterValue = (Double)after.get(BeCPGModel.PROP_PRICELIST_VALUE);
		boolean doUpdate = false;
		
		logger.debug("onUpdateProperties, prefRank before: " + beforePrefRank + "after: " + afterPrefRank);
		
		if(afterPrefRank != null && afterPrefRank.equals(PREF_RANK)){
			
			if(!afterPrefRank.equals(beforePrefRank)){
				doUpdate = true;
			}
			else if(afterValue != null && !afterValue.equals(beforeValue)){
				doUpdate = true;
			}
		}
		
		if(doUpdate){
			updateCostList(priceListItemNodeRef);
		}
	}
	
	private void updateCostList(NodeRef priceListItemNodeRef){
		
		logger.debug("updateCostList");
		
		NodeRef priceListNodeRef = nodeService.getPrimaryParent(priceListItemNodeRef).getParentRef();
		NodeRef listContainerNodeRef = nodeService.getPrimaryParent(priceListNodeRef).getParentRef();		
		NodeRef costListNodeRef = entityListDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
		Double value = (Double)nodeService.getProperty(priceListItemNodeRef, BeCPGModel.PROP_PRICELIST_VALUE);
		
		NodeRef costNodeRef = null;
		List<AssociationRef> assocRefs = nodeService.getTargetAssocs(priceListItemNodeRef, BeCPGModel.ASSOC_PRICELIST_COST);
		if(assocRefs.size() > 0){
			costNodeRef = assocRefs.get(0).getTargetRef();
		}
		
		if(costListNodeRef != null){
			NodeRef linkNodeRef = entityListDAO.getLink(costListNodeRef, BeCPGModel.ASSOC_COSTLIST_COST, costNodeRef);
			
			nodeService.setProperty(linkNodeRef, BeCPGModel.PROP_COSTLIST_VALUE, value);
		}
		else{
			
			costListNodeRef = entityListDAO.createList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
			productDAO.createCostListItem(costListNodeRef, new CostListDataItem(null, value, null, null, costNodeRef, false), null);
		}
	}

}

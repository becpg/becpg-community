package fr.becpg.repo.product.policy.productListUnits;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.formulation.CostsCalculatingVisitor;
import fr.becpg.repo.product.formulation.NutsCalculatingVisitor;

@Service
public class ProductListPolicy implements NodeServicePolicies.OnCreateAssociationPolicy {

	private static Log logger = LogFactory.getLog(ProductListPolicy.class);
	
	private PolicyComponent policyComponent;		
		
	private NodeService nodeService;
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void init(){
		logger.debug("Init productListUnits.ProductListPolicy...");
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
				BeCPGModel.TYPE_COSTLIST, BeCPGModel.ASSOC_COSTLIST_COST, new JavaBehaviour(this, "onCreateAssociation"));
		
		policyComponent.bindAssociationBehaviour(NodeServicePolicies.OnCreateAssociationPolicy.QNAME, 
				BeCPGModel.TYPE_NUTLIST, BeCPGModel.ASSOC_NUTLIST_NUT, new JavaBehaviour(this, "onCreateAssociation"));
				
	}
	
	@Override
	public void onCreateAssociation(AssociationRef assocRef) {
		
		NodeRef targetNodeRef = assocRef.getTargetRef();
		NodeRef productListItemNodeRef = assocRef.getSourceRef();
		QName type = nodeService.getType(productListItemNodeRef);		
		
		if(type.equals(BeCPGModel.TYPE_COSTLIST)){
			
			Boolean costFixed = (Boolean)nodeService.getProperty(targetNodeRef, BeCPGModel.PROP_COSTFIXED);
			String costCurrency = (String)nodeService.getProperty(targetNodeRef, BeCPGModel.PROP_COSTCURRENCY);
			String costListUnit = (String)nodeService.getProperty(productListItemNodeRef, BeCPGModel.PROP_COSTLIST_UNIT);
			
			if(costFixed != null && costFixed == true){
				
				if(!(costListUnit != null && costListUnit.equals(costCurrency))){
					nodeService.setProperty(productListItemNodeRef, BeCPGModel.PROP_COSTLIST_UNIT, costCurrency);
				}
			}
			else{
											
				if(!(costListUnit != null && !costListUnit.isEmpty() && costListUnit.startsWith(costCurrency + CostsCalculatingVisitor.UNIT_SEPARATOR))){
					
					NodeRef listNodeRef = nodeService.getPrimaryParent(productListItemNodeRef).getParentRef();
					
					if(listNodeRef != null){
						NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef(); 
						
						if(listContainerNodeRef != null){
							
							NodeRef productNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
							
							if(productNodeRef != null){
								
								ProductUnit productUnit = ProductUnit.getUnit((String)nodeService.getProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_UNIT));							
								nodeService.setProperty(productListItemNodeRef, BeCPGModel.PROP_COSTLIST_UNIT, CostsCalculatingVisitor.calculateUnit(productUnit, costCurrency));
							}
						}
						
					}				
				}
			}			
		}
		else if(type.equals(BeCPGModel.TYPE_NUTLIST)){
			String nutUnit = (String)nodeService.getProperty(targetNodeRef, BeCPGModel.PROP_NUTUNIT);
			String nutListUnit = (String)nodeService.getProperty(productListItemNodeRef, BeCPGModel.PROP_NUTLIST_UNIT);
			
			// nutListUnit
			if(!(nutListUnit != null && !nutListUnit.isEmpty() && nutListUnit.startsWith(nutUnit + CostsCalculatingVisitor.UNIT_SEPARATOR))){
				
				NodeRef listNodeRef = nodeService.getPrimaryParent(productListItemNodeRef).getParentRef();
				
				if(listNodeRef != null){
					NodeRef listContainerNodeRef = nodeService.getPrimaryParent(listNodeRef).getParentRef(); 
					
					if(listContainerNodeRef != null){
						
						NodeRef productNodeRef = nodeService.getPrimaryParent(listContainerNodeRef).getParentRef();
						
						if(productNodeRef != null){
							
							ProductUnit productUnit = ProductUnit.getUnit((String)nodeService.getProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_UNIT));
							nodeService.setProperty(productListItemNodeRef, BeCPGModel.PROP_NUTLIST_UNIT, NutsCalculatingVisitor.calculateUnit(productUnit, nutUnit));
						}
					}
					
				}				
			}
			
			// nutListGroup
			String nutGroup = (String)nodeService.getProperty(targetNodeRef, BeCPGModel.PROP_NUTGROUP);
			nodeService.setProperty(productListItemNodeRef, BeCPGModel.PROP_NUTLIST_GROUP, nutGroup);
		}
		
	}

	
}

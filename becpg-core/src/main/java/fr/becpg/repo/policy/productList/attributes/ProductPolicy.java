package fr.becpg.repo.policy.productList.attributes;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDAO;
import fr.becpg.repo.product.data.ProductUnit;
import fr.becpg.repo.product.formulation.CostsCalculatingVisitor;
import fr.becpg.repo.product.formulation.NutsCalculatingVisitor;

public class ProductPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static Log logger = LogFactory.getLog(ProductPolicy.class);
	
	private PolicyComponent policyComponent;				

	private NodeService nodeService;
	
	private ProductDAO productDAO;
	
	private FileFolderService fileFolderService;
	
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setProductDAO(ProductDAO productDAO) {
		this.productDAO = productDAO;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}
	
	public void init(){
		logger.debug("Init productListUnits.ProductPolicy...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, 
				BeCPGModel.TYPE_PRODUCT, 
				new JavaBehaviour(this, "onUpdateProperties"));		
	}
	
	@Override
	public void onUpdateProperties(NodeRef productNodeRef,
			Map<QName, Serializable> before,
			Map<QName, Serializable> after) {
		
		if(after.containsKey(BeCPGModel.PROP_PRODUCT_UNIT)){
			ProductUnit productUnit = ProductUnit.getUnit((String)after.get(BeCPGModel.PROP_PRODUCT_UNIT));
			
			// look for product lists
			NodeRef listContainerNodeRef = productDAO.getListContainer(productNodeRef);
			if(listContainerNodeRef != null){
				
				// costList
				NodeRef costListNodeRef = productDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_COSTLIST);
				if(costListNodeRef != null){
					
					List<FileInfo> nodes = fileFolderService.listFiles(costListNodeRef);
					
					for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
			    	{	    			
		    			FileInfo node = nodes.get(z_idx);
		    			NodeRef productListItemNodeRef = node.getNodeRef();	    					    		
			    		String costListUnit = (String)nodeService.getProperty(productListItemNodeRef, BeCPGModel.PROP_COSTLIST_UNIT);
			    		
			    		List<AssociationRef> costAssocRefs = nodeService.getTargetAssocs(productListItemNodeRef, BeCPGModel.ASSOC_COSTLIST_COST);
			    		NodeRef costNodeRef = (costAssocRefs.get(0)).getTargetRef();
			    		String costCurrency = (String)nodeService.getProperty(costNodeRef, BeCPGModel.PROP_COSTCURRENCY); 
			    		
			    		String suffix = CostsCalculatingVisitor.UNIT_SEPARATOR + costCurrency;
			    		if(!(costListUnit != null && !costListUnit.isEmpty() && costListUnit.endsWith(suffix))){
			    			nodeService.setProperty(productListItemNodeRef, BeCPGModel.PROP_COSTLIST_UNIT, CostsCalculatingVisitor.calculateUnit(productUnit, costCurrency));
			    		}
			    	}					
				}
				
				// nutList
				NodeRef nutListNodeRef = productDAO.getList(listContainerNodeRef, BeCPGModel.TYPE_NUTLIST);
				if(nutListNodeRef != null){
					
					List<FileInfo> nodes = fileFolderService.listFiles(nutListNodeRef);
					
					for(int z_idx=0 ; z_idx<nodes.size() ; z_idx++)
			    	{	    			
		    			FileInfo node = nodes.get(z_idx);
		    			NodeRef productListItemNodeRef = node.getNodeRef();	    					    		
			    		String nutListUnit = (String)nodeService.getProperty(productListItemNodeRef, BeCPGModel.PROP_NUTLIST_UNIT);
			    		
			    		List<AssociationRef> nutAssocRefs = nodeService.getTargetAssocs(productListItemNodeRef, BeCPGModel.ASSOC_NUTLIST_NUT);
			    		NodeRef nutNodeRef = (nutAssocRefs.get(0)).getTargetRef();
			    		String nutUnit= (String)nodeService.getProperty(nutNodeRef, BeCPGModel.PROP_NUTUNIT);
			    		
			    		if(!(nutListUnit != null && !nutListUnit.isEmpty() && nutListUnit.endsWith(NutsCalculatingVisitor.calculateSuffixUnit(productUnit)))){
			    			nodeService.setProperty(productListItemNodeRef, BeCPGModel.PROP_NUTLIST_UNIT, NutsCalculatingVisitor.calculateUnit(productUnit, nutUnit));
			    		}
			    	}					
				}
			}
		}		
	}

}

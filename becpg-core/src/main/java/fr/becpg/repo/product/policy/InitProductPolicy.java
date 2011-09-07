/*
 * 
 */
package fr.becpg.repo.product.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.AutoNumService;
import fr.becpg.repo.product.ProductService;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductPolicies.
 *
 * @author querephi
 */
public class InitProductPolicy implements NodeServicePolicies.OnCreateNodePolicy {	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(InitProductPolicy.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;		
	
	/** The node service. */
	private NodeService nodeService;	
	
	/** The product service. */
	private ProductService productService;
			
	/**
	 * Sets the policy component.
	 *
	 * @param policyComponent the new policy component
	 */
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the product service.
	 *
	 * @param productService the new product service
	 */
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}		
	
	/**
	 * Inits the.
	 */
	public void init(){
		logger.debug("Init ProductPolicies...");
		policyComponent.bindClassBehaviour(NodeServicePolicies.OnCreateNodePolicy.QNAME, BeCPGModel.TYPE_PRODUCT, new JavaBehaviour(this, "onCreateNode"));		
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.node.NodeServicePolicies.OnCreateNodePolicy#onCreateNode(org.alfresco.service.cmr.repository.ChildAssociationRef)
	 */
	@Override
	public void onCreateNode(ChildAssociationRef childAssocRef) {
		
		NodeRef productNodeRef = childAssocRef.getChildRef();
		
		/*-- initialize folder --*/
		productService.initializeProductFolder(productNodeRef);		
	}
	
}

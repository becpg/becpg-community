/*
 * 
 */
package fr.becpg.repo.policy;

import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductService;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductPolicies.
 *
 * @author querephi
 */
public class ProductPolicies implements NodeServicePolicies.OnCreateNodePolicy {	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductPolicies.class);
	
	/** The policy component. */
	private PolicyComponent policyComponent;		
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The auto num service. */
	private AutoNumService autoNumService;	
	
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
	 * Sets the auto num service.
	 *
	 * @param autoNumService the new auto num service
	 */
	public void setAutoNumService(AutoNumService autoNumService) {
		this.autoNumService = autoNumService;
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
		
		/*-- Product code generation --*/
		long productCode = autoNumService.getAutoNumValue(BeCPGModel.TYPE_PRODUCT.getLocalName(), BeCPGModel.PROP_PRODUCT_CODE.getLocalName());
		nodeService.setProperty(productNodeRef, BeCPGModel.PROP_PRODUCT_CODE, productCode);
		
		/*-- initialize folder --*/
		productService.initializeProductFolder(productNodeRef);		
	}
	
}

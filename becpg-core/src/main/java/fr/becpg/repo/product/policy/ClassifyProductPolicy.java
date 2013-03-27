package fr.becpg.repo.product.policy;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.PropertyCheck;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.CompanyHomeHelper;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.policy.AbstractBeCPGPolicy;
import fr.becpg.repo.product.ProductService;

@Service
public class ClassifyProductPolicy extends AbstractBeCPGPolicy implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static Log logger = LogFactory.getLog(ClassifyProductPolicy.class);

	private ProductService productService;
	private NamespaceService namespaceService;
	private Repository repositoryHelper;


	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void doInit() {

		PropertyCheck.mandatory(this, "productService", productService);
		PropertyCheck.mandatory(this, "namespaceService", namespaceService);
		PropertyCheck.mandatory(this, "repositoryHelper", repositoryHelper);
		
		
		logger.debug("Init ProductPolicies...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.ASPECT_PRODUCT,
				new JavaBehaviour(this, "onUpdateProperties"));
		
		super.disableOnCopyBehaviour(BeCPGModel.ASPECT_PRODUCT);
	}

	/**
	 * Classify product:
	 * 	- if state is changed, it is always classified
	 * 	- if hierarchy1 and hierarchy2 is changed, it is classified when a product is not in a site
	 */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		String beforeState = (String) before.get(BeCPGModel.PROP_PRODUCT_STATE);
		String afterState = (String) after.get(BeCPGModel.PROP_PRODUCT_STATE);

		NodeRef beforeHierarchy1 = (NodeRef) before.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);
		NodeRef afterHierarchy1 = (NodeRef) after.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);

		NodeRef beforeHierarchy2 = (NodeRef) before.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);
		NodeRef afterHierarchy2 = (NodeRef) after.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);

		boolean classify = false;

		//state
		if (afterState != null && !afterState.isEmpty() && !afterState.equals(beforeState)) {
			classify = true;
		} 
		//hierarchy1 and hierarchy2
		else if ((afterHierarchy1 != null  && !afterHierarchy1.equals(beforeHierarchy1)) || 
					(afterHierarchy2 != null  && !afterHierarchy2.equals(beforeHierarchy2))) {
			classify = true;					
		}
			
		//don't classify product that are in a site, 
		// force to use wf && don't classify user dierctory
		
		if(classify){
			
			String path = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
			if (!SiteHelper.isSitePath(path) && !CompanyHomeHelper.isInUserHome(path)) {
				queueNode(nodeRef);
			}
		}
	}
	
	
	@Override
	protected void doBeforeCommit(String key, Set<NodeRef> pendingNodes) {
		for (NodeRef nodeRef : pendingNodes) {
			if (isNotLocked(nodeRef) && !isWorkingCopyOrVersion(nodeRef) &&
					!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITY_TPL) &&
					!nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)) {
				
				productService.classifyProduct(repositoryHelper.getCompanyHome(), nodeRef);
			}
		}
	}
}

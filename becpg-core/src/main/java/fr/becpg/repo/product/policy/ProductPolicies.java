package fr.becpg.repo.product.policy;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.repo.model.Repository;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.product.ProductService;

public class ProductPolicies implements NodeServicePolicies.OnUpdatePropertiesPolicy {

	private static Log logger = LogFactory.getLog(ProductPolicies.class);

	private PolicyComponent policyComponent;
	private ProductService productService;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private Repository repositoryHelper;

	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setProductService(ProductService productService) {
		this.productService = productService;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}

	public void init() {

		logger.debug("Init ProductPolicies...");

		policyComponent.bindClassBehaviour(NodeServicePolicies.OnUpdatePropertiesPolicy.QNAME, BeCPGModel.TYPE_PRODUCT,
				new JavaBehaviour(this, "onUpdateProperties"));
	}

	/**
	 * Classify product if it is not created in a site
	 */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {

		String beforeState = (String) before.get(BeCPGModel.PROP_PRODUCT_STATE);
		String afterState = (String) after.get(BeCPGModel.PROP_PRODUCT_STATE);

		String beforeHierarchy1 = (String) before.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);
		String afterHierarchy1 = (String) after.get(BeCPGModel.PROP_PRODUCT_HIERARCHY1);

		String beforeHierarchy2 = (String) before.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);
		String afterHierarchy2 = (String) after.get(BeCPGModel.PROP_PRODUCT_HIERARCHY2);

		boolean classify = false;

		if (afterState != null && !afterState.isEmpty() && !afterState.equals(beforeState)) {
			classify = true;
		} else if (afterHierarchy1 != null && !afterHierarchy1.isEmpty() && !afterHierarchy1.equals(beforeHierarchy1)) {
			classify = true;
		} else if (afterHierarchy2 != null && !afterHierarchy2.isEmpty() && !afterHierarchy2.equals(beforeHierarchy2)) {
			classify = true;
		}

		if (classify) {

			String path = nodeService.getPath(nodeRef).toPrefixString(namespaceService);
			if (!SiteHelper.isSitePath(path)) {
				productService.classifyProduct(repositoryHelper.getCompanyHome(), nodeRef);
			}
		}

	}

}

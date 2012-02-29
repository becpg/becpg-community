package fr.becpg.repo.product.listener;

import org.alfresco.repo.model.Repository;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;

import fr.becpg.repo.entity.event.CheckInEntityEvent;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.product.ProductService;

public class ProductCheckInListener implements ApplicationListener<CheckInEntityEvent> {

	private static Log logger = LogFactory.getLog(ProductCheckInListener.class);
	
	private ProductService productService;
	private Repository repositoryHelper;
	private NodeService nodeService;
	private NamespaceService namespaceService;
	
	public void setProductService(ProductService productService) {
		this.productService = productService;
	}
	
	public void setRepositoryHelper(Repository repositoryHelper) {
		this.repositoryHelper = repositoryHelper;
	}
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	@Override
	public void onApplicationEvent(CheckInEntityEvent event) {
		
		String path = nodeService.getPath(event.getEntityNodeRef()).toPrefixString(namespaceService);
		
		logger.debug("path: " + path);
		
		// classify if product is not in a site
		if(!SiteHelper.isSitePath(path)){
			productService.classifyProduct(repositoryHelper.getCompanyHome(), event.getEntityNodeRef());
		}
		
	}

}

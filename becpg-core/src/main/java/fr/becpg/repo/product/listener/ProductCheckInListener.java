package fr.becpg.repo.product.listener;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationListener;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.event.CheckInEntityEvent;

//TODO remove if unused
@Deprecated
public class ProductCheckInListener implements ApplicationListener<CheckInEntityEvent> {

	private static Log logger = LogFactory.getLog(ProductCheckInListener.class);
	
	private NodeService nodeService;
	private NamespaceService namespaceService;
	private DictionaryService dictionaryService;
		
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
	}

	@Override
	public void onApplicationEvent(CheckInEntityEvent event) {
		
		String path = nodeService.getPath(event.getEntityNodeRef()).toPrefixString(namespaceService);
		
		logger.debug("path: " + path);
				
		if(dictionaryService.isSubClass(nodeService.getType(event.getEntityNodeRef()), BeCPGModel.TYPE_PRODUCT)){
		
			//TODO should be generic
			// reset state to ToValidate
			nodeService.setProperty(event.getEntityNodeRef(), BeCPGModel.PROP_PRODUCT_STATE, SystemState.ToValidate);
		
//			// classify if product is not in a site
//			if(!SiteHelper.isSitePath(path)){
//				productService.classifyProduct(repositoryHelper.getCompanyHome(), event.getEntityNodeRef());
//			}						
		}		
	}

}

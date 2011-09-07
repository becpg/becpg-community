/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.entity.EntityListDAO;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.ProductService;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductListsWebScript.
 *
 * @author querephi
 */
public class EntityListsWebScript extends DeclarativeWebScript  {

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";	
	// model key names
	/** The Constant MODEL_KEY_NAME_PRODUCT. */
	private static final String MODEL_KEY_NAME_ENTITY = "entity";
	
	/** The Constant MODEL_KEY_NAME_CONTAINER. */
	private static final String MODEL_KEY_NAME_CONTAINER = "container";
	
	/** The Constant MODEL_KEY_NAME_LISTS. */
	private static final String MODEL_KEY_NAME_LISTS = "lists";
	
	/** The Constant MODEL_HAS_WRITE_PERMISSION. */
	private static final String MODEL_HAS_WRITE_PERMISSION = "hasWritePermission";
	
	/** The Constant MODEL_SHOW_WUSED_ITEMS. */
	private static final String MODEL_SHOW_WUSED_ITEMS = "showWUsedItems";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityListsWebScript.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The file folder service. */
	private FileFolderService fileFolderService;		
	
	/** The product dictionary service. */
	private ProductDictionaryService productDictionaryService;
		
	private EntityListDAO entityListDAO;
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Sets the file folder service.
	 *
	 * @param fileFolderService the new file folder service
	 */
	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}	
		
	/**
	 * Sets the product dictionary service.
	 *
	 * @param productDictionaryService the new product dictionary service
	 */
	public void setProductDictionaryService(
			ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}
	
	public void setEntityListDAO(EntityListDAO entityListDAO) {
		this.entityListDAO = entityListDAO;
	}

	/**
	 * Suggest values according to query
	 * 
	 * url : /becpg/productlists/node/{store_type}/{store_id}/{id}.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		
		logger.debug("entityListsWebScript executeImpl()");
			
		List<NodeRef> listsNodeRef = new ArrayList<NodeRef>();
		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);		
		NodeRef containerDataLists = null;
		QName type = nodeService.getType(nodeRef);
		boolean hasWritePermission = false;
		boolean showWUsedItems = false;
		
		// TODO : renommer le webscript car ce n'est pas utilisé que par les produits
		if(nodeService.hasAspect(nodeRef,BeCPGModel.ASPECT_PRODUCT)){
			//Product
			NodeRef templateNodeRef = productDictionaryService.getProductTemplate(nodeRef);
			entityListDAO.copyDataLists(templateNodeRef, nodeRef, false);
			containerDataLists = nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_ENTITYLISTS, RepoConsts.CONTAINER_DATALISTS);
			hasWritePermission = false;		
			showWUsedItems = true;
		}		
		else {
			
			// Template product, micriobio criteria, product specification
			// Control plan, Quality control, Control point
			containerDataLists = entityListDAO.getListContainer(nodeRef);			
			if(containerDataLists == null){			    
				containerDataLists = entityListDAO.createListContainer(nodeRef);
			}
			hasWritePermission = true;
		}		
		
		
		logger.debug("productListsNodeRef : " + containerDataLists);
		
		if(containerDataLists != null){						
			List<FileInfo> productListsFileInfo = fileFolderService.listFolders(containerDataLists);
			
			for(FileInfo fileInfo : productListsFileInfo){
				listsNodeRef.add(fileInfo.getNodeRef());
			}					
		}
		
		logger.debug("productListsFileInfo.size() : " + listsNodeRef.size() + " - object type : " + type.toString() + " - hasWritePermission : " + hasWritePermission);
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_ENTITY, nodeRef);
		model.put(MODEL_KEY_NAME_CONTAINER, containerDataLists);
		model.put(MODEL_KEY_NAME_LISTS, listsNodeRef);
		model.put(MODEL_HAS_WRITE_PERMISSION, hasWritePermission);
		model.put(MODEL_SHOW_WUSED_ITEMS, showWUsedItems);
		return model;
	}
}

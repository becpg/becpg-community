/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.ProductDictionaryService;
import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.productList.CompoListDataItem;
import fr.becpg.repo.product.data.productList.PackagingListDataItem;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductWUsedWebScript.
 *
 * @author querephi
 */
public class ProductWUsedWebScript extends DeclarativeWebScript  {

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";	
	// model key names
	/** The Constant MODEL_KEY_NAME_TOTALRECORDS. */
	private static final String MODEL_KEY_NAME_TOTALRECORDS = "totalRecords";
	
	/** The Constant MODEL_KEY_NAME_STARTINDEX. */
	private static final String MODEL_KEY_NAME_STARTINDEX = "startIndex";
	
	private static final String MODEL_KEY_NAME_WUSEDTYPE = "wusedType";
	
	/** The Constant MODEL_KEY_NAME_WUSEDITEMS. */
	private static final String MODEL_KEY_NAME_WUSEDITEMS = "wUsedItems";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductWUsedWebScript.class);
	
	/** The product service. */
	private ProductService productService;
	private ProductDictionaryService productDictionaryService;
	
	/**
	 * Sets the product service.
	 *
	 * @param productService the new product service
	 */
	public void setProductService(ProductService productService){
		this.productService = productService;
	}	
		
	public void setProductDictionaryService(
			ProductDictionaryService productDictionaryService) {
		this.productDictionaryService = productDictionaryService;
	}

	/**
	 * Return the product where useds
	 * 
	 * url : /becpg/product/wused/node/{store_type}/{store_id}/{id}.
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
		
		logger.debug("productWUsedWebScript executeImpl()");
			
		NodeRef productNodeRef = new NodeRef(storeType, storeId, nodeId);
		
		QName wusedType = productDictionaryService.getWUsedList(productNodeRef);
		
		Map<String, Object> model = new HashMap<String, Object>();		
		model.put(MODEL_KEY_NAME_STARTINDEX, 0);
		model.put(MODEL_KEY_NAME_WUSEDTYPE, wusedType.getLocalName());		
		
		if(BeCPGModel.TYPE_COMPOLIST.equals(wusedType)){
		
			List<CompoListDataItem> wUsedList = productService.getWUsedCompoList(productNodeRef);
			model.put(MODEL_KEY_NAME_TOTALRECORDS, wUsedList.size());
			model.put(MODEL_KEY_NAME_WUSEDITEMS, wUsedList);
		}
		else if(BeCPGModel.TYPE_PACKAGINGLIST.equals(wusedType)){
			
			List<PackagingListDataItem> wUsedList = productService.getWUsedPackagingList(productNodeRef);
			model.put(MODEL_KEY_NAME_TOTALRECORDS, wUsedList.size());
			model.put(MODEL_KEY_NAME_WUSEDITEMS, wUsedList);
		}
		
		return model;
	}
}

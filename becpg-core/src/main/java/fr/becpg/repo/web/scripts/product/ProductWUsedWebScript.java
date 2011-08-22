/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.product.ProductService;
import fr.becpg.repo.product.data.productList.CompoListDataItem;

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
	
	/** The Constant MODEL_KEY_NAME_WUSEDITEMS. */
	private static final String MODEL_KEY_NAME_WUSEDITEMS = "wUsedItems";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductWUsedWebScript.class);
	
	/** The product service. */
	private ProductService productService;
	
	/**
	 * Sets the product service.
	 *
	 * @param productService the new product service
	 */
	public void setProductService(ProductService productService){
		this.productService = productService;
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
		List<CompoListDataItem> wUsedList = productService.getWUsedProduct(productNodeRef);
				
		logger.debug("wUsedList() : " + wUsedList.size());
		
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_TOTALRECORDS, wUsedList.size());
		model.put(MODEL_KEY_NAME_STARTINDEX, 0);
		model.put(MODEL_KEY_NAME_WUSEDITEMS, wUsedList);
		
		return model;
	}
}

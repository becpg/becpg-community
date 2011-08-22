/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

// TODO: Auto-generated Javadoc
/**
 * The Class CheckInWebScript.
 *
 * @author querephi
 */
public class CheckInWebScript extends DeclarativeWebScript  {	
	
	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";	
	// model key names
	/** The Constant MODEL_KEY_NAME_NODEREF. */
	private static final String MODEL_KEY_NAME_NODEREF = "noderef";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CheckInWebScript.class);	
	
	/** The product check out check in service. */
	private CheckOutCheckInService productCheckOutCheckInService;

	/**
	 * Sets the product check out check in service.
	 *
	 * @param productCheckOutCheckInService the new product check out check in service
	 */
	public void setProductCheckOutCheckInService(
			CheckOutCheckInService productCheckOutCheckInService) {
		this.productCheckOutCheckInService = productCheckOutCheckInService;
	}
	
	/**
	 * Checkin a product.
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
		
		logger.debug("CheckInWebScript executeImpl()");			
					
		NodeRef workingCopyNodeRef = new NodeRef(storeType, storeId, nodeId);
		NodeRef newProductNodeRef = productCheckOutCheckInService.checkin(workingCopyNodeRef, null);
				
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_NODEREF, newProductNodeRef);
		
		return model;
	}
}

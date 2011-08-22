/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.product.ProductService;

// TODO: Auto-generated Javadoc
/**
 * The Class FormulateWebScript.
 *
 * @author querephi
 */
public class FormulateWebScript extends AbstractWebScript
{	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(FormulateWebScript.class);
		
		//request parameter names
		/** The Constant PARAM_STORE_TYPE. */
		private static final String PARAM_STORE_TYPE = "store_type";
		
		/** The Constant PARAM_STORE_ID. */
		private static final String PARAM_STORE_ID = "store_id";
		
		/** The Constant PARAM_ID. */
		private static final String PARAM_ID = "id";
		
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
		
	    /* (non-Javadoc)
    	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
    	 */
    	@Override
		public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
	    {
	    	logger.debug("start formulate webscript");
	    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
	    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
	    	
			NodeRef productNodeRef = new NodeRef(storeType, storeId, nodeId);
			
			productService.formulate(productNodeRef);
	    }
}

/*
 * 
 */
package fr.becpg.repo.web.scripts.product;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;

import fr.becpg.repo.formulation.FormulateException;

// TODO: Auto-generated Javadoc
/**
 * The Class FormulateWebScript.
 *
 * @author querephi
 */
@Service
public class FormulateWebScript extends AbstractProductWebscript {
	

		/** The logger. */
		private static Log logger = LogFactory.getLog(FormulateWebScript.class);
		
	    /* (non-Javadoc)
    	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
    	 */
    	@Override
		public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
	    {
	    	logger.debug("start formulate webscript");
	    
			
			NodeRef productNodeRef = getProductNodeRef(req);    
			try {
				productService.formulate(productNodeRef);
			} catch (FormulateException e) {
				handleFormulationError(e);
			}
	    }

		
}

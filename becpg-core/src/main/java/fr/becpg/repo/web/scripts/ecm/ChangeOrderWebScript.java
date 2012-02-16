/*
 * 
 */
package fr.becpg.repo.web.scripts.ecm;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.ecm.ECOService;

// TODO: Auto-generated Javadoc
/**
 * The Class FormulateWebScript.
 *
 * @author querephi
 */
public class ChangeOrderWebScript extends AbstractWebScript
{	
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ChangeOrderWebScript.class);
		
		//request parameter names
		/** The Constant PARAM_STORE_TYPE. */
		private static final String PARAM_STORE_TYPE = "store_type";
		
		/** The Constant PARAM_STORE_ID. */
		private static final String PARAM_STORE_ID = "store_id";
		
		/** The Constant PARAM_ID. */
		private static final String PARAM_ID = "id";
		
		private static final String PARAM_ACTION = "action";
		
		private static final String ACTION_CALCULATE_WUSED = "calculatewused";
		private static final String ACTION_DO_SIMULATION = "dosimulation";
		private static final String ACTION_APPLY = "apply";
		
		private ECOService ecoService;
				
	    public void setEcoService(ECOService ecoService) {
			this.ecoService = ecoService;
		}

		/* (non-Javadoc)
    	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
    	 */
    	@Override
		public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
	    {
	    	logger.debug("start eco webscript");
	    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
	    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			String action = templateArgs.get(PARAM_ACTION);
	    	
			NodeRef ecoNodeRef = new NodeRef(storeType, storeId, nodeId);
			try {
				
				if(ACTION_CALCULATE_WUSED.equals(action)){
					ecoService.calculateWUsedList(ecoNodeRef);
				}
				else if(ACTION_DO_SIMULATION.equals(action)){
					ecoService.doSimulation(ecoNodeRef);
				}
				else if(ACTION_APPLY.equals(action)){
					ecoService.apply(ecoNodeRef);
				}
				else{
					logger.error("Unknown action '" + action + "'.");
				}
				
			} catch (Exception e) {
				logger.error(e,e);
				throw new WebScriptException(e.getMessage());
			}
	    }
}

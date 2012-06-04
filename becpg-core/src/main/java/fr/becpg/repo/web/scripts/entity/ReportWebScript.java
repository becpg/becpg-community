/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * The Class ReportWebScript.
 *
 * @author querephi
 */
public class ReportWebScript extends AbstractWebScript
{		
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReportWebScript.class);
	
	private static final String ACTION_CHECK_DATALISTS = "check-datalists";
	private static final String ACTION_FORCE = "force";
		
	//request parameter names
	private static final String PARAM_ACTION = "action";
	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";			
	
	private EntityService entityService;
	
	private EntityReportService entityReportService;	
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

    public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/* (non-Javadoc)
	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("start report webscript");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
    	String action = templateArgs.get(PARAM_ACTION);
    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
    	
		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
		boolean generateReport = false;
				
		if(ACTION_CHECK_DATALISTS.equals(action)){
			generateReport = entityService.hasDataListModified(nodeRef);
		}
		else if(ACTION_FORCE.equals(action)){
			generateReport = true;
		}
		else{
			String error = "Unsupported action: " + action;
			logger.error(error);
			throw new WebScriptException(error);
		}
		
		if(generateReport){
			entityReportService.generateReport(nodeRef);
		}
    }    	  
}

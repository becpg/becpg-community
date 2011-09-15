/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.util.Date;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.entity.EntityService;

/**
 * The Class ReportWebScript.
 *
 * @author querephi
 */
public class ReportWebScript extends AbstractWebScript
{		
	/** The logger. */
	private static Log logger = LogFactory.getLog(ReportWebScript.class);
		
	//request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";
			
	private NodeService nodeService;
	
	private EntityService entityService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

    /* (non-Javadoc)
	 * @see org.springframework.extensions.webscripts.WebScript#execute(org.springframework.extensions.webscripts.WebScriptRequest, org.springframework.extensions.webscripts.WebScriptResponse)
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws WebScriptException
    {
    	logger.debug("start report webscript");
    	Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();	    	
    	String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
    	
		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
		
		// update node to indicate it is modified (audit and that will fire policies)
		if(entityService.hasDataListModified(nodeRef)){
			//nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIED, new Date()); // doesn't work
			nodeService.setProperty(nodeRef, ReportModel.PROP_REPORT_ENTITY_GENERATED, null);
		}
    }    	  
}

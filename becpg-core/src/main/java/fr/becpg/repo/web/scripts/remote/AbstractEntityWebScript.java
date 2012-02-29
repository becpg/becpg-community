package fr.becpg.repo.web.scripts.remote;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Abstract remote entity webscript
 * @author matthieu
 *
 */
public abstract class AbstractEntityWebScript extends AbstractWebScript{

	protected static Log logger = LogFactory.getLog(AbstractEntityWebScript.class);
	
	/** The Constant PARAM_QUERY. */
	protected static final String PARAM_QUERY = "query";
	
	/** The Constant PARAM_PATH. */
	protected static final String PARAM_PATH = "path";
	
	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_NODEREF = "nodeRef";

	/** Services **/

	protected NodeService nodeService;
	
	protected EntityService entityService;
	
	protected BeCPGSearchService beCPGSearchService;
	
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}


	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}


	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	

	protected NodeRef findEntity(WebScriptRequest req) {
		// TODO Auto-generated method stub
		return null;
	}
	
	protected void sendOKStatus(NodeRef entityNodeRef, WebScriptResponse resp) {
		// TODO Auto-generated method stub
		
	}
}

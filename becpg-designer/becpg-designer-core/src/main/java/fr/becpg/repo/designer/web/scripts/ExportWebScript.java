/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.designer.DesignerService;

// TODO: Auto-generated Javadoc
/**
 * The Class PublishWebScript.
 *
 * @author matthieu
 */
public class ExportWebScript extends AbstractWebScript   {
	

	private static final String PARAM_NODEREF = "nodeRef";


	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(ExportWebScript.class);
	
	/** The node service. */
	private DesignerService designerService;
	

	/**
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}


	/**
	 * Publish 
	 * 
	 * url : /becpg/designer/form/export?nodeRef={nodeRef}.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
				
		logger.debug("PublishWebScript executeImpl()");
			
		NodeRef parentNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));		

		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");
		res.getWriter().write(designerService.export(parentNodeRef));
	}
	
	

}

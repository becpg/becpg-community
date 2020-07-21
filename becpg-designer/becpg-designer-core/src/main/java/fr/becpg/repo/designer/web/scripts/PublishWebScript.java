/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.designer.DesignerService;


/**
 * The Class PublishWebScript.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class PublishWebScript extends DeclarativeWebScript  {
	

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String PERSISTED_OBJECT = "persistedObject";

	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(PublishWebScript.class);
	
	/** The node service. */
	private DesignerService designerService;
	

	/**
	 * <p>Setter for the field <code>designerService</code>.</p>
	 *
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}


	/**
	 * {@inheritDoc}
	 *
	 * Publish
	 *
	 * url : /becpg/designer/model/publish?nodeRef={nodeRef}.
	 * url : /becpg/designer/form/publish?nodeRef={nodeRef}.
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		logger.debug("PublishWebScript executeImpl()");
			
		NodeRef parentNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));		

		
		Map<String, Object> model = new HashMap<>();
		
		designerService.writeXml(parentNodeRef);
		designerService.publish(parentNodeRef);
		
		model.put(PERSISTED_OBJECT, parentNodeRef.toString() );
	
		return model;
	}
	
	

}

/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.designer.DesignerService;

// TODO: Auto-generated Javadoc
/**
 * The Class PublishModelWebScript.
 *
 * @author matthieu
 */
public class PublishModelWebScript extends DeclarativeWebScript  {
	

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String PERSISTED_OBJECT = "persistedObject";

	
	/** The logger. */
	private static Log logger = LogFactory.getLog(PublishModelWebScript.class);
	
	/** The node service. */
	private DesignerService designerService;
	

	/**
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}


	/**
	 * Publish model
	 * 
	 * url : /becpg/designer/model/publish?nodeRef={nodeRef}.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		logger.debug("CreateModelElementWebScript executeImpl()");
			
		NodeRef parentNodeRef = new NodeRef( req.getParameter(PARAM_NODEREF));		

		
		Map<String, Object> model = new HashMap<String, Object>();
		
		designerService.writeXmlFromModelAspectNode(parentNodeRef);
		designerService.publish(parentNodeRef);
		
		model.put(PERSISTED_OBJECT, parentNodeRef.toString() );
	
		return model;
	}
	
	

}

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
 * The Class ModelTreeWebScript.
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ModelTreeWebScript extends DeclarativeWebScript  {
	

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";
	
	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";
	
	/** The Constant PARAM_ID. */
	private static final String PARAM_ID = "id";

	private static final String MODEL_TREE = "tree";	

	
	/** The logger. */
	private static final Log logger = LogFactory.getLog(ModelTreeWebScript.class);
	
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
	 * Retrieve model Tree
	 *
	 * url : /becpg/designer/tree/node/{store_type}/{store_id}/{id}.
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String storeType = templateArgs.get(PARAM_STORE_TYPE);
		String storeId = templateArgs.get(PARAM_STORE_ID);
		String nodeId = templateArgs.get(PARAM_ID);
		
		logger.debug("ModelTreeWebScript executeImpl()");
			
		NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);		
		
		
		Map<String, Object> model = new HashMap<>();

		model.put(MODEL_TREE, designerService.getDesignerTree(nodeRef));
	
		
		return model;
	}

}

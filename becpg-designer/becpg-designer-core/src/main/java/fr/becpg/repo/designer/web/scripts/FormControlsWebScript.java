/*
 * 
 */
package fr.becpg.repo.designer.web.scripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.designer.DesignerService;

// TODO: Auto-generated Javadoc
/**
 * The Class FormControlsWebScript.
 *
 * @author matthieu
 */
public class FormControlsWebScript extends DeclarativeWebScript  {
	

	private static final String FORM_CONTROLS = "controls";

	/** The logger. */
	private static final Log logger = LogFactory.getLog(FormControlsWebScript.class);
	
	/** The node service. */
	private DesignerService designerService;


	/**
	 * @param designerService the designerService to set
	 */
	public void setDesignerService(DesignerService designerService) {
		this.designerService = designerService;
	}





	/**
	 * Retrieve available controls
	 * 
	 * url : /becpg/designer/controls.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){
				
		logger.debug("Retrieve form controls");
		Map<String, Object> model = new HashMap<>();

		model.put(FORM_CONTROLS, designerService.getFormControls());
	
		
		return model;
	}

}

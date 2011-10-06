/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.util.VersionNumber;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.entity.version.EntityVersionService;

// TODO: Auto-generated Javadoc
/**
 * The Class FormCheckInWebScript.
 *
 * @author querephi
 */
public class FormCheckInWebScript extends DeclarativeWebScript {

	// request parameter names
	/** The Constant PARAM_NODEREF. */
	private static final String PARAM_NODEREF = "nodeRef";
	
	/** The Constant PARAM_VERSION. */
	private static final String PARAM_VERSION = "version";
	
	/** The Constant PARAM_MAJOR_VERSION. */
	private static final String PARAM_MAJOR_VERSION = "majorVersion";
	
	/** The Constant PARAM_DESCRIPTION. */
	private static final String PARAM_DESCRIPTION = "description";	
	// model key names
	/** The Constant MODEL_KEY_NAME_NODEREF. */
	private static final String MODEL_KEY_NAME_NODEREF = "noderef";
	// values
	/** The Constant VALUE_TRUE. */
	private static final String VALUE_TRUE = "true";
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(CheckInWebScript.class);	
	
	/** The entity check out check in service. */
	private CheckOutCheckInService entityCheckOutCheckInService;

	/**
	 * Sets the entity check out check in service.
	 *
	 * @param entityCheckOutCheckInService the new entity check out check in service
	 */
	public void setEntityCheckOutCheckInService(
			CheckOutCheckInService entityCheckOutCheckInService) {
		this.entityCheckOutCheckInService = entityCheckOutCheckInService;
	}
	
	/**
	 * Form checkin a entity.
	 *
	 * @param req the req
	 * @param status the status
	 * @param cache the cache
	 * @return the map
	 */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache){				
		
		logger.debug("FormCheckInWebScript executeImpl()");
		
		NodeRef nodeRef = null;
		String version = "";
		String description = "";
		boolean majorVersion = false;
		
		JSONObject json = (JSONObject)req.parseContent();
		try{
			nodeRef = new NodeRef((String)json.get(PARAM_NODEREF));
			version = (String)json.get(PARAM_VERSION);
			description = (String)json.get(PARAM_DESCRIPTION);
			majorVersion = ((String)json.get(PARAM_MAJOR_VERSION)).equals(VALUE_TRUE) ? true : false;
			
			logger.debug("nodeRef: " + nodeRef);
			logger.debug("version: " + version);
			logger.debug("description: " + description);
			logger.debug("majorVersion: " + majorVersion);
		}
		catch(JSONException e){
			logger.error("Failed to parse form fields", e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Failed to parse form fields ", e);
		}
		
		//Calculate new version
		VersionNumber versionNumber = new VersionNumber(version);
		if(majorVersion){
			int majorNb = versionNumber.getPart(0) + 1;
			versionNumber = new VersionNumber(majorNb + EntityVersionService.VERSION_DELIMITER + 0);			
		}
		else{
			int minorNb = versionNumber.getPart(1) + 1;
			versionNumber = new VersionNumber(versionNumber.getPart(0) + EntityVersionService.VERSION_DELIMITER + minorNb);
		}
			
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), versionNumber.toString());
		properties.put(Version.PROP_DESCRIPTION, description);		
		NodeRef newEntityNodeRef = entityCheckOutCheckInService.checkin(nodeRef, properties);

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_NODEREF, newEntityNodeRef);		
		return model;
	}
}

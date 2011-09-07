/*
 * 
 */
package fr.becpg.repo.web.scripts.document;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.ContentWriter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
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

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.entity.version.VersionData;

// TODO: Auto-generated Javadoc
/**
 * The Class FormRevertVersionWebScript.
 *
 * @author querephi
 */
public class FormRevertVersionWebScript extends DeclarativeWebScript {

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
	private static Log logger = LogFactory.getLog(FormRevertVersionWebScript.class);	
	
	/** The node service. */
	private NodeService nodeService;
	
	/** The version service. */
	private VersionService versionService;
	
	/** The lock service. */
	private LockService lockService;
	
	/** The coci service. */
	private CheckOutCheckInService cociService;
	
	/** The content service. */
	private ContentService contentService;
	
	/** The entity version service. */
	private EntityVersionService entityVersionService;	
	
	/** The entity check out check in service. */
	private CheckOutCheckInService entityCheckOutCheckInService;
	
	/**
	 * Sets the entity version service.
	 *
	 * @param entityVersionService the new entity version service
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}	

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
		}
		catch(JSONException e){
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Failed to parse form fields ", e);
		}
		
		//Calculate new version
		VersionNumber versionNumber = new VersionNumber(version);
		if(majorVersion){
			int majorNb = versionNumber.getPart(0) + 1;
			versionNumber = new VersionNumber(majorNb + EntityVersionService.VERSION_DELIMITER + versionNumber.getPart(1));			
		}
		else{
			int minorNb = versionNumber.getPart(1) + 1;
			versionNumber = new VersionNumber(versionNumber.getPart(0) + EntityVersionService.VERSION_DELIMITER + minorNb);
		}
			
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), versionNumber.toString());
		properties.put(Version.PROP_DESCRIPTION, description);
		
		//Check if the node is locked
		LockStatus lockStatus =  lockService.getLockStatus(nodeRef);		
		if(lockStatus != LockStatus.NO_LOCK){
			throw new WebScriptException(Status.STATUS_NOT_FOUND, "error.nodeLocked");
		}
		
		// entity
		if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_ENTITYLISTS)){
			
			List<VersionData> versionHistory = entityVersionService.getVersionHistoryWithProperties(nodeRef);
			VersionData v = null;
			
			for(VersionData versionData : versionHistory){				
				if(versionData.getLabel().equals(version)){
					v = versionData;
				}
			}
			
			//Check version exists
			if(v == null){
				throw new WebScriptException(Status.STATUS_NOT_FOUND, "error.versionNotFound");
			}
			
			if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)){
				
				// Ensure the original file is versionable - may have been uploaded via different route
				if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)){
					// We cannot revert a non versionable document
					throw new WebScriptException(Status.STATUS_NOT_FOUND, "error.nodeNotVersionable");
				}
				
				// It's not a working copy, do a check out to get the actual working copy
				nodeRef = entityCheckOutCheckInService.checkout(nodeRef);
			}

			// Update the working copy content
			ContentReader reader = contentService.getReader(v.getNodeRef(), ContentModel.PROP_CONTENT);
			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			writer.putContent(reader);
			writer.setMimetype(reader.getMimetype());
			writer.setEncoding(reader.getEncoding());
			
			// check it in again, with supplied version history note
			nodeRef = entityCheckOutCheckInService.checkin(nodeRef, properties);
			
		}
		else{ // document
			
			VersionHistory versionHistory = versionService.getVersionHistory(nodeRef);
			Version v = versionHistory.getVersion(version);
			
			//Check version exists
			if(v == null){
				throw new WebScriptException(Status.STATUS_NOT_FOUND, "error.versionNotFound");
			}
			
			if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_WORKING_COPY)){
				
				// Ensure the original file is versionable - may have been uploaded via different route
				if(!nodeService.hasAspect(nodeRef, ContentModel.ASPECT_VERSIONABLE)){
					// We cannot revert a non versionable document
					throw new WebScriptException(Status.STATUS_NOT_FOUND, "error.nodeNotVersionable");
				}
				
				// It's not a working copy, do a check out to get the actual working copy
				nodeRef = cociService.checkout(nodeRef);
			}
			
			// Update the working copy content
			ContentReader reader = contentService.getReader(v.getVersionedNodeRef(), ContentModel.PROP_CONTENT);
			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			writer.putContent(reader);
			writer.setMimetype(reader.getMimetype());
			writer.setEncoding(reader.getEncoding());
			
			// check it in again, with supplied version history note
			nodeRef = cociService.checkin(nodeRef, properties);
		}
		
		

		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_NODEREF, nodeRef);		
		return model;
	}
}

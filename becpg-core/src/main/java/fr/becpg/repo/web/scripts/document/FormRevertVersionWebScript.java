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
import fr.becpg.repo.product.version.ProductVersionService;
import fr.becpg.repo.product.version.VersionData;

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
	
	/** The product version service. */
	private ProductVersionService productVersionService;	
	
	/** The product check out check in service. */
	private CheckOutCheckInService productCheckOutCheckInService;
	
	/**
	 * Sets the product version service.
	 *
	 * @param productVersionService the new product version service
	 */
	public void setProductVersionService(ProductVersionService productVersionService) {
		this.productVersionService = productVersionService;
	}	

	/**
	 * Sets the product check out check in service.
	 *
	 * @param productCheckOutCheckInService the new product check out check in service
	 */
	public void setProductCheckOutCheckInService(
			CheckOutCheckInService productCheckOutCheckInService) {
		this.productCheckOutCheckInService = productCheckOutCheckInService;
	}
	
	/**
	 * Form checkin a product.
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
			versionNumber = new VersionNumber(majorNb + ProductVersionService.VERSION_DELIMITER + versionNumber.getPart(1));			
		}
		else{
			int minorNb = versionNumber.getPart(1) + 1;
			versionNumber = new VersionNumber(versionNumber.getPart(0) + ProductVersionService.VERSION_DELIMITER + minorNb);
		}
			
		Map<String, Serializable> properties = new HashMap<String, Serializable>();
		properties.put(ContentModel.PROP_VERSION_LABEL.toPrefixString(), versionNumber.toString());
		properties.put(Version.PROP_DESCRIPTION, description);
		
		//Check if the node is locked
		LockStatus lockStatus =  lockService.getLockStatus(nodeRef);		
		if(lockStatus != LockStatus.NO_LOCK){
			throw new WebScriptException(Status.STATUS_NOT_FOUND, "error.nodeLocked");
		}
		
		// product
		if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_PRODUCT_TYPE)){
			
			List<VersionData> versionHistory = productVersionService.getVersionHistoryWithProperties(nodeRef);
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
				nodeRef = productCheckOutCheckInService.checkout(nodeRef);
			}

			// Update the working copy content
			ContentReader reader = contentService.getReader(v.getNodeRef(), ContentModel.PROP_CONTENT);
			ContentWriter writer = contentService.getWriter(nodeRef, ContentModel.PROP_CONTENT, true);
			writer.putContent(reader);
			writer.setMimetype(reader.getMimetype());
			writer.setEncoding(reader.getEncoding());
			
			// check it in again, with supplied version history note
			nodeRef = productCheckOutCheckInService.checkin(nodeRef, properties);
			
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

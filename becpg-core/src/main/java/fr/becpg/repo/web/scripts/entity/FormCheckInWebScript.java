/*
 * 
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.version.VersionModel;
import org.alfresco.service.cmr.coci.CheckOutCheckInService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;
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

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_MAJOR_VERSION = "majorVersion";
	private static final String PARAM_DESCRIPTION = "description";
	private static final String MODEL_KEY_NAME_NODEREF = "noderef";
	private static final String PARAM_BRANCH_TO_NODEREF = "branchToNodeRef";

	private static final String VALUE_TRUE = "true";

	private static final Log logger = LogFactory.getLog(FormCheckInWebScript.class);

	private CheckOutCheckInService checkOutCheckInService;

	private EntityVersionService entityVersionService;

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		NodeRef nodeRef;
		NodeRef branchToNodeRef = null;
		String description;
		VersionType versionType;

		JSONObject json = (JSONObject) req.parseContent();
		try {
			nodeRef = new NodeRef((String) json.get(PARAM_NODEREF));
			description = (String) json.get(PARAM_DESCRIPTION);
			versionType = json.get(PARAM_MAJOR_VERSION).equals(VALUE_TRUE) ? VersionType.MAJOR : VersionType.MINOR;

			if (json.has(PARAM_BRANCH_TO_NODEREF)) {
				branchToNodeRef = new NodeRef((String) json.get(PARAM_BRANCH_TO_NODEREF));
			}

			if (logger.isDebugEnabled()) {
				logger.debug("branchToNodeRef: " + branchToNodeRef);
				logger.debug("nodeRef: " + nodeRef);
				logger.debug("description: " + description);
				logger.debug("versionType: " + versionType);
			}
		} catch (JSONException e) {
			logger.error("Failed to parse form fields", e);
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Failed to parse form fields ", e);
		}
		NodeRef newEntityNodeRef = null;

		if (branchToNodeRef != null) {
			entityVersionService.mergeBranch(nodeRef, branchToNodeRef, versionType, description);
		} else {
			// Calculate new version
			Map<String, Serializable> properties = new HashMap<>();
			properties.put(VersionModel.PROP_VERSION_TYPE, versionType);
			properties.put(Version.PROP_DESCRIPTION, description);
			newEntityNodeRef = checkOutCheckInService.checkin(nodeRef, properties);
		}

		Map<String, Object> model = new HashMap<>();
		model.put(MODEL_KEY_NAME_NODEREF, newEntityNodeRef);
		return model;
	}
}

/*
 *
 */
package fr.becpg.repo.web.scripts.entity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.repo.version.VersionBaseModel;
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

import fr.becpg.repo.entity.version.EntityVersionPlugin;
import fr.becpg.repo.entity.version.EntityVersionService;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

/**
 * The Class FormCheckInWebScript.
 *
 * @author querephi
 * @version $Id: $Id
 */
public class FormCheckInWebScript extends DeclarativeWebScript {

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_MAJOR_VERSION = "majorVersion";
	private static final String PARAM_DESCRIPTION = "description";
	private static final String MODEL_KEY_NAME_NODEREF = "noderef";
	private static final String PARAM_BRANCH_TO_NODEREF = "branchToNodeRef";
	private static final String PARAM_IMPACT_WUSED = "impactWused";
	private static final String PARAM_RENAME_ON_MERGE = "renameOnMerge";

	private static final String VALUE_TRUE = "true";

	private static final Log logger = LogFactory.getLog(FormCheckInWebScript.class);

	private static final Tracer tracer = Tracing.getTracer();

	private CheckOutCheckInService checkOutCheckInService;

	private EntityVersionService entityVersionService;

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * <p>Setter for the field <code>checkOutCheckInService</code>.</p>
	 *
	 * @param checkOutCheckInService a {@link org.alfresco.service.cmr.coci.CheckOutCheckInService} object.
	 */
	public void setCheckOutCheckInService(CheckOutCheckInService checkOutCheckInService) {
		this.checkOutCheckInService = checkOutCheckInService;
	}

	/** {@inheritDoc} */
	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {
		try (Scope scope = tracer.spanBuilder("/internal/merge").startScopedSpan()) {
			NodeRef nodeRef;
			NodeRef branchToNodeRef = null;
			String description;
			VersionType versionType;
			boolean impactWused = false;
			boolean rename = false;

			JSONObject json = (JSONObject) req.parseContent();
			try {
				nodeRef = new NodeRef((String) json.get(PARAM_NODEREF));
				description = (String) json.get(PARAM_DESCRIPTION);
				versionType = json.get(PARAM_MAJOR_VERSION).equals(VALUE_TRUE) ? VersionType.MAJOR : VersionType.MINOR;

				if (json.has(PARAM_BRANCH_TO_NODEREF)) {
					branchToNodeRef = new NodeRef((String) json.get(PARAM_BRANCH_TO_NODEREF));
				}

				if (json.has(PARAM_IMPACT_WUSED) && VALUE_TRUE.equals(json.get(PARAM_IMPACT_WUSED))) {
					impactWused = true;
				}

				if (json.has(PARAM_RENAME_ON_MERGE) && VALUE_TRUE.equals(json.get(PARAM_RENAME_ON_MERGE))) {
					rename = true;
				}

				if (logger.isDebugEnabled()) {
					logger.debug("branchToNodeRef: " + branchToNodeRef);
					logger.debug("nodeRef: " + nodeRef);
					logger.debug("description: " + description);
					logger.debug("versionType: " + versionType);
					logger.debug("impactWused: " + impactWused);
				}
			} catch (JSONException e) {
				logger.error("Failed to parse form fields", e);
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Failed to parse form fields ", e);
			}
			NodeRef newEntityNodeRef = null;

			if (branchToNodeRef != null) {
				newEntityNodeRef = entityVersionService.mergeBranch(nodeRef, branchToNodeRef, versionType, description, impactWused, rename);
			} else {
				// Calculate new version
				Map<String, Serializable> properties = new HashMap<>();
				properties.put(VersionBaseModel.PROP_VERSION_TYPE, versionType);
				properties.put(Version.PROP_DESCRIPTION, description);
				if (impactWused) {
					properties.put(EntityVersionPlugin.POST_UPDATE_HISTORY_NODEREF, null);
				}

				newEntityNodeRef = checkOutCheckInService.checkin(nodeRef, properties);
			}

			if (impactWused) {
				entityVersionService.impactWUsed(newEntityNodeRef, versionType, description);
			}

			Map<String, Object> model = new HashMap<>();
			model.put(MODEL_KEY_NAME_NODEREF, newEntityNodeRef);
			return model;
		}
	}
}

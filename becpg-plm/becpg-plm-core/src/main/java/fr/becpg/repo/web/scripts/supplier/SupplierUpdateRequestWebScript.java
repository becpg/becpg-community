package fr.becpg.repo.web.scripts.supplier;

import java.io.IOException;

import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.supplier.SupplierPortalService;

/**
 * {@code POST /becpg/supplier/update-request} — a supplier asks for the update of
 * one of its validated sheets, or of its supplier record.
 *
 * <p>Body: {@code {"nodeRef": "workspace://SpacesStore/…", "template": "workspace://SpacesStore/…", "reason": "…"}},
 * the template being one the supplier may start — a supplier project template on
 * which {@code GROUP_ExternalUser} holds a read permission.
 * Answers {@code {"project": "<nodeRef>", "entity": "<working copy nodeRef>"}}: the
 * caller deposits the new technical sheet on that working copy.</p>
 *
 * <p>A refusal keeps its meaning: 403 for an account that is not the supplier's,
 * 409 when a supplier project is already open on the sheet, 400 for a working copy
 * or a blank reason.</p>
 *
 * @author matthieu
 */
public class SupplierUpdateRequestWebScript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";

	private static final String PARAM_REASON = "reason";

	private static final String PARAM_TEMPLATE = "template";

	private SupplierPortalService supplierPortalService;

	private AssociationService associationService;

	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>supplierPortalService</code>.</p>
	 *
	 * @param supplierPortalService a {@link fr.becpg.repo.supplier.SupplierPortalService} object
	 */
	public void setSupplierPortalService(SupplierPortalService supplierPortalService) {
		this.supplierPortalService = supplierPortalService;
	}

	/**
	 * <p>Setter for the field <code>associationService</code>.</p>
	 *
	 * @param associationService a {@link fr.becpg.repo.helper.AssociationService} object
	 */
	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef entityNodeRef;
		NodeRef templateNodeRef;
		String reason;
		try {
			JSONObject body = new JSONObject(req.getContent().getContent());
			String nodeRef = body.optString(PARAM_NODEREF, "");
			if (!NodeRef.isNodeRef(nodeRef) || !nodeService.exists(new NodeRef(nodeRef))) {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A valid nodeRef is required");
			}
			entityNodeRef = new NodeRef(nodeRef);
			String template = body.optString(PARAM_TEMPLATE, "");
			if (!NodeRef.isNodeRef(template)) {
				throw new WebScriptException(Status.STATUS_BAD_REQUEST, "A valid project template is required");
			}
			templateNodeRef = new NodeRef(template);
			reason = body.optString(PARAM_REASON, "");
		} catch (JSONException e) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Expected a JSON body", e);
		}

		NodeRef projectNodeRef;
		try {
			projectNodeRef = supplierPortalService.requestSupplierUpdate(entityNodeRef, templateNodeRef, reason);
		} catch (AccessDeniedException e) {
			// Includes a template that is not open to suppliers.
			throw new WebScriptException(Status.STATUS_FORBIDDEN, e.getMessage(), e);
		} catch (IllegalArgumentException e) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage(), e);
		} catch (IllegalStateException e) {
			throw new WebScriptException(Status.STATUS_CONFLICT, e.getMessage(), e);
		}

		// The supplier may not read the project node, which is filed with the customer.
		NodeRef workingCopy = AuthenticationUtil
				.runAsSystem(() -> associationService.getTargetAssoc(projectNodeRef, ProjectModel.ASSOC_PROJECT_ENTITY));
		JSONObject ret = new JSONObject();
		try {
			ret.put("project", projectNodeRef.toString());
			ret.put("entity", workingCopy != null ? workingCopy.toString() : JSONObject.NULL);
		} catch (JSONException e) {
			throw new WebScriptException(Status.STATUS_INTERNAL_SERVER_ERROR, "Cannot write the answer", e);
		}
		res.setContentType("application/json");
		res.setContentEncoding("UTF-8");
		ret.write(res.getWriter());
	}

}

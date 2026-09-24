package fr.becpg.repo.web.scripts.regulatory;

import java.io.IOException;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;

import fr.becpg.repo.regulatory.becpg.regulatory.RegulatoryComplianceViewService;

/**
 * Returns the compliance view of a product, computed on demand by becpg-regulatory, for the
 * embedded regulatory view of Share. Read only: nothing is written in the repository.
 * <p>
 * Failures are answered with a JSON body {@code {"error": "<code>"}} that the view turns
 * into a message: {@code notConfigured}, {@code invalidProduct} or {@code unavailable}.
 *
 * @author matthieu
 */
public class RegulatoryViewWebScript extends AbstractWebScript {

	private static final Log logger = LogFactory.getLog(RegulatoryViewWebScript.class);

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_REFRESH = "refresh";
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String ERROR_KEY = "error";
	static final String ERROR_NOT_CONFIGURED = "notConfigured";
	static final String ERROR_INVALID_PRODUCT = "invalidProduct";
	static final String ERROR_UNAVAILABLE = "unavailable";

	private RegulatoryComplianceViewService regulatoryComplianceViewService;

	/**
	 * @param regulatoryComplianceViewService the compliance view service
	 */
	public void setRegulatoryComplianceViewService(RegulatoryComplianceViewService regulatoryComplianceViewService) {
		this.regulatoryComplianceViewService = regulatoryComplianceViewService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		NodeRef productNodeRef = parseNodeRef(req.getParameter(PARAM_NODEREF));
		boolean refresh = Boolean.parseBoolean(req.getParameter(PARAM_REFRESH));
		res.setContentType(CONTENT_TYPE_JSON);
		res.setContentEncoding("UTF-8");
		if (regulatoryComplianceViewService.uiUrl().isEmpty()) {
			writeError(res, Status.STATUS_SERVICE_UNAVAILABLE, ERROR_NOT_CONFIGURED);
			return;
		}
		try {
			res.getWriter().write(regulatoryComplianceViewService.fetchView(productNodeRef, refresh));
		} catch (HttpClientErrorException.BadRequest e) {
			logger.warn("Regulatory compliance view rejected for " + productNodeRef + ": " + e.getStatusCode());
			writeError(res, Status.STATUS_BAD_REQUEST, ERROR_INVALID_PRODUCT);
		} catch (RestClientException | JSONException e) {
			logger.error("Regulatory compliance view failed for " + productNodeRef, e);
			writeError(res, Status.STATUS_BAD_GATEWAY, ERROR_UNAVAILABLE);
		}
	}

	private static NodeRef parseNodeRef(String nodeRef) {
		if (nodeRef == null || !NodeRef.isNodeRef(nodeRef)) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "Invalid nodeRef");
		}
		return new NodeRef(nodeRef);
	}

	private static void writeError(WebScriptResponse res, int status, String code) throws IOException {
		res.setStatus(status);
		try {
			res.getWriter().write(new JSONObject().put(ERROR_KEY, code).toString());
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}
}

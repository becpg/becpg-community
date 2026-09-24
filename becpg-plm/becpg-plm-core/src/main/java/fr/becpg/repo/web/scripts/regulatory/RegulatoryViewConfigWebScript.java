package fr.becpg.repo.web.scripts.regulatory;

import java.io.IOException;
import java.util.Optional;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.regulatory.becpg.regulatory.RegulatoryComplianceViewService;

/**
 * Tells Share whether the embedded regulatory view is enabled, and which origin serves it.
 *
 * @author matthieu
 */
public class RegulatoryViewConfigWebScript extends AbstractWebScript {

	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final String KEY_ENABLED = "enabled";
	private static final String KEY_UI_URL = "uiUrl";

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
		res.setContentType(CONTENT_TYPE_JSON);
		Optional<String> uiUrl = regulatoryComplianceViewService.uiUrl();
		try {
			JSONObject config = new JSONObject();
			config.put(KEY_ENABLED, uiUrl.isPresent());
			if (uiUrl.isPresent()) {
				config.put(KEY_UI_URL, uiUrl.get());
			}
			res.getWriter().write(config.toString());
		} catch (JSONException e) {
			throw new IOException(e);
		}
	}
}

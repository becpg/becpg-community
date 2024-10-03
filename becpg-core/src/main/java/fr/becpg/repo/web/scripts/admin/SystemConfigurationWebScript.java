package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.system.SystemConfigurationService;

/**
 * <p>SystemConfigurationWebScript class.</p>
 *
 * @author matthieu
 */
public class SystemConfigurationWebScript extends AbstractWebScript {

	private SystemConfigurationService systemConfigurationService;
	
	/**
	 * <p>Setter for the field <code>systemConfigurationService</code>.</p>
	 *
	 * @param systemConfigurationService a {@link fr.becpg.repo.system.SystemConfigurationService} object
	 */
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse resp) throws IOException {
		
		String action = req.getServiceMatch().getTemplateVars().get("action");
		
		JSONObject json = new JSONObject(req.getContent().getContent());
		
		JSONObject ret = new JSONObject();
		
		if ("update".equals(action)) {
			String key = json.getString("key");
			String value = json.getString("value");
			systemConfigurationService.updateConfValue(key, value);
		} else if ("reset".equals(action)) {
			String key = json.getString("key");
			systemConfigurationService.resetConfValue(key);
		}
		
		ret.put("status", "OK");
		
		resp.setContentType("application/json");
		resp.setContentEncoding("UTF-8");
		ret.write(resp.getWriter());
	}
	
}

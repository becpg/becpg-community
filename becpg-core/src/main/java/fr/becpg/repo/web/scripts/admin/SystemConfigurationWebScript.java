package fr.becpg.repo.web.scripts.admin;

import java.io.IOException;

import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.system.SystemConfigurationService;

public class SystemConfigurationWebScript extends AbstractWebScript {

	private SystemConfigurationService systemConfigurationService;
	
	public void setSystemConfigurationService(SystemConfigurationService systemConfigurationService) {
		this.systemConfigurationService = systemConfigurationService;
	}

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

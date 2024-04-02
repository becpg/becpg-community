package fr.becpg.repo.web.scripts.statistics;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;

public class DeleteStatisticsWebScript extends AbstractWebScript {

	private static final String PARAM_TYPE = "type";
	private static final String PARAM_FROM_ID = "fromId";
	private static final String PARAM_TO_ID = "toId";
	
	private BeCPGAuditService beCPGAuditService;
	
	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String reqType = templateArgs.get(PARAM_TYPE);
		String fromId = req.getParameter(PARAM_FROM_ID);
		String toId = req.getParameter(PARAM_TO_ID);
		
		if (fromId == null || toId == null) {
			throw new WebScriptException("Missing 'fromId' or 'toId' request parameter");
		}
		
		beCPGAuditService.deleteAuditEntries(getAuditType(reqType), Long.parseLong(fromId), Long.parseLong(toId));
		
		try {
			JSONObject ret = new JSONObject();
			
			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.put("status", "SUCCESS");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}
		
	}
	
	private AuditType getAuditType(String reqType) {
		AuditType[] auditTypes = AuditType.class.getEnumConstants();
		for (AuditType auditType : auditTypes) {
			if (auditType.toString().equalsIgnoreCase(reqType)) {
				return auditType;
			}
		}
		throw new WebScriptException("Unknown audit type : '" + reqType + "'");
	}
	
}

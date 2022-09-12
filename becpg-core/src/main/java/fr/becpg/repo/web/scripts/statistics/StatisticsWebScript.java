package fr.becpg.repo.web.scripts.statistics;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;

public class StatisticsWebScript extends AbstractWebScript {

	private static final String PARAM_TYPE = "type";
	private static final String PARAM_SORT_BY = "sortBy";
	private static final String PARAM_FILTER = "filter";
	private static final String PARAM_MAX_RESULTS = "maxResults";
	
	private BeCPGAuditService beCPGAuditService;
	
	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}
	
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String reqType = templateArgs.get(PARAM_TYPE);
		String reqMaxResults = req.getParameter(PARAM_MAX_RESULTS);
		String sortBy = req.getParameter(PARAM_SORT_BY);
		String filter = req.getParameter(PARAM_FILTER);
		
		Integer maxResults = RepoConsts.MAX_RESULTS_256;
		
		if (reqMaxResults != null) {
			maxResults = Integer.parseInt(reqMaxResults);
		}
		
		AuditType type = null;
		
		switch (reqType) {
		case "batch":
			type = AuditType.BATCH;
			break;
		case "formulation":
			type = AuditType.FORMULATION;
			break;
		case "activity":
			type = AuditType.ACTIVITY;
			break;
		default:
			throw new WebScriptException("Unknown audit type : '" + reqType + "'");
		}
		
		List<JSONObject> statistics = beCPGAuditService.getAuditStatistics(type, maxResults, sortBy, filter);
		
		try {
			JSONObject ret = new JSONObject();
			
			ret.put("statistics", statistics);

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}
		
	}
	
}

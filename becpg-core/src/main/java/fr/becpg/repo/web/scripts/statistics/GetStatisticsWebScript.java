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

import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;

/**
 * <p>GetStatisticsWebScript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GetStatisticsWebScript extends AbstractWebScript {

	private static final String PARAM_TYPE = "type";
	private static final String PARAM_SORT_BY = "sortBy";
	private static final String PARAM_FILTER = "filter";
	private static final String PARAM_MAX_RESULTS = "maxResults";
	private static final String PARAM_ASCENDING_ORDER = "asc";
	private static final String PARAM_DB_ASCENDING_ORDER = "dbAsc";
	
	private BeCPGAuditService beCPGAuditService;
	
	/**
	 * <p>Setter for the field <code>beCPGAuditService</code>.</p>
	 *
	 * @param beCPGAuditService a {@link fr.becpg.repo.audit.service.BeCPGAuditService} object
	 */
	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}
	
	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();

		String reqType = templateArgs.get(PARAM_TYPE);
		String reqMaxResults = req.getParameter(PARAM_MAX_RESULTS);
		String sortBy = req.getParameter(PARAM_SORT_BY);
		String filter = req.getParameter(PARAM_FILTER);
		String ascendingOrder = req.getParameter(PARAM_ASCENDING_ORDER);
		String dbAscendingOrder = req.getParameter(PARAM_DB_ASCENDING_ORDER);
		
		AuditQuery auditQuery = AuditQuery.createQuery().sortBy(sortBy).filter(filter);
		
		if (reqMaxResults != null) {
			auditQuery.maxResults(Integer.parseInt(reqMaxResults));
		}
		
		if (ascendingOrder != null) {
			auditQuery.asc(Boolean.parseBoolean(ascendingOrder));
		}
		
		if (dbAscendingOrder != null) {
			auditQuery.dbAsc(Boolean.parseBoolean(dbAscendingOrder));
		}
		
		List<JSONObject> statistics = beCPGAuditService.listAuditEntries(getAuditType(reqType), auditQuery);
		
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

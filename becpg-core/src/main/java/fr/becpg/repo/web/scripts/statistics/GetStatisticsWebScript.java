package fr.becpg.repo.web.scripts.statistics;

import java.io.IOException;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.audit.model.AuditPage;
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

	/** Constant <code>PARAM_TYPE="type"</code> */
	private static final String PARAM_TYPE = "type";
	/** Constant <code>PARAM_SORT_BY="sortBy"</code> */
	private static final String PARAM_SORT_BY = "sortBy";
	/** Constant <code>PARAM_FILTER="filter"</code> */
	private static final String PARAM_FILTER = "filter";
	/** Constant <code>PARAM_MAX_RESULTS="maxResults"</code> */
	private static final String PARAM_MAX_RESULTS = "maxResults";
	/** Constant <code>PARAM_ASCENDING_ORDER="asc"</code> */
	private static final String PARAM_ASCENDING_ORDER = "asc";
	/** Constant <code>PARAM_DB_ASCENDING_ORDER="dbAsc"</code> */
	private static final String PARAM_DB_ASCENDING_ORDER = "dbAsc";
	/** Constant <code>PARAM_START_AFTER_ID="startAfterId"</code> */
	private static final String PARAM_START_AFTER_ID = "startAfterId";
	/** Constant <code>RESP_STATISTICS="statistics"</code> */
	private static final String RESP_STATISTICS = "statistics";
	/** Constant <code>RESP_NEXT_START_AFTER_ID="nextStartAfterId"</code> */
	private static final String RESP_NEXT_START_AFTER_ID = "nextStartAfterId";
	/** Constant <code>RESP_SCAN_INTERRUPTED="scanInterrupted"</code> */
	private static final String RESP_SCAN_INTERRUPTED = "scanInterrupted";
	
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

		AuditPage page = beCPGAuditService.listAuditPage(getAuditType(reqType), buildAuditQuery(req));

		writePage(res, page);
	}

	/**
	 * <p>buildAuditQuery.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 */
	private AuditQuery buildAuditQuery(WebScriptRequest req) {
		AuditQuery auditQuery = AuditQuery.createQuery().sortBy(req.getParameter(PARAM_SORT_BY)).filter(req.getParameter(PARAM_FILTER));

		String reqMaxResults = req.getParameter(PARAM_MAX_RESULTS);
		if (reqMaxResults != null) {
			auditQuery.maxResults(Integer.parseInt(reqMaxResults));
		}

		String ascendingOrder = req.getParameter(PARAM_ASCENDING_ORDER);
		if (ascendingOrder != null) {
			auditQuery.asc(Boolean.parseBoolean(ascendingOrder));
		}

		String dbAscendingOrder = req.getParameter(PARAM_DB_ASCENDING_ORDER);
		if (dbAscendingOrder != null) {
			auditQuery.dbAsc(Boolean.parseBoolean(dbAscendingOrder));
		}

		String startAfterId = req.getParameter(PARAM_START_AFTER_ID);
		if (startAfterId != null) {
			auditQuery.startAfterId(Long.parseLong(startAfterId));
		}

		return auditQuery;
	}

	/**
	 * <p>writePage.</p>
	 *
	 * @param res a {@link org.springframework.extensions.webscripts.WebScriptResponse} object
	 * @param page a {@link fr.becpg.repo.audit.model.AuditPage} object
	 * @throws java.io.IOException if the response cannot be written
	 */
	private void writePage(WebScriptResponse res, AuditPage page) throws IOException {
		try {
			JSONObject ret = new JSONObject();

			ret.put(RESP_STATISTICS, page.entries());
			ret.put(RESP_SCAN_INTERRUPTED, page.scanInterrupted());
			if (page.nextStartAfterId() != null) {
				ret.put(RESP_NEXT_START_AFTER_ID, page.nextStartAfterId());
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			ret.write(res.getWriter());
		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		}
	}
	
	/**
	 * <p>getAuditType.</p>
	 *
	 * @param reqType a {@link java.lang.String} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditType} object
	 */
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

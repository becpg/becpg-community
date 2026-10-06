package fr.becpg.repo.web.scripts.statistics;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import org.alfresco.error.AlfrescoRuntimeException;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
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
	/** Constant <code>PARAM_FROM_DATE="fromDate"</code> */
	private static final String PARAM_FROM_DATE = "fromDate";
	/** Constant <code>PARAM_TO_DATE="toDate"</code> */
	private static final String PARAM_TO_DATE = "toDate";
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
	
	/**
	 * {@inheritDoc}
	 *
	 * A malformed parameter, a filter on an unknown key or a filter value the audit query cannot
	 * hold is a client error, reported as such rather than as a server failure.
	 */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String reqType = templateArgs.get(PARAM_TYPE);

		AuditPage page;
		try {
			page = beCPGAuditService.listAuditPage(getAuditType(reqType), buildAuditQuery(req));
		} catch (BeCPGAuditException | NumberFormatException e) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, e.getMessage(), e);
		}

		writePage(res, page);
	}

	/**
	 * <p>buildAuditQuery.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 */
	private AuditQuery buildAuditQuery(WebScriptRequest req) {
		AuditQuery auditQuery = AuditQuery.createQuery().sortBy(req.getParameter(PARAM_SORT_BY));

		String[] filters = req.getParameterValues(PARAM_FILTER);
		if (filters != null) {
			auditQuery.filters(Arrays.asList(filters));
		}

		applyCreationDateRange(req, auditQuery);

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
	 * Restrict the query to the entries written between the 'fromDate' and 'toDate' parameters,
	 * both inclusive. A missing bound leaves the range open on its side. A 'toDate' without time
	 * stands for the whole day, up to its last millisecond.
	 *
	 * The date is the one the audit entry was written at: an entry completed after its start is
	 * written again, so that it then holds its completion date.
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @throws org.springframework.extensions.webscripts.WebScriptException if a bound is not an ISO 8601 date or the range is reversed
	 */
	private void applyCreationDateRange(WebScriptRequest req, AuditQuery auditQuery) {
		Date fromDate = parseDate(req, PARAM_FROM_DATE, false);
		Date toDate = parseDate(req, PARAM_TO_DATE, true);

		if ((fromDate == null) && (toDate == null)) {
			return;
		}

		Date rangeStart = fromDate != null ? fromDate : new Date(0);
		Date rangeEnd = toDate != null ? toDate : new Date();
		if (rangeStart.after(rangeEnd)) {
			String upperBound = toDate != null ? "'" + PARAM_TO_DATE + "'" : "the current date";
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'" + PARAM_FROM_DATE + "' must not be after " + upperBound);
		}
		auditQuery.timeRange(rangeStart, rangeEnd);
	}

	/**
	 * <p>parseDate.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object
	 * A date without time is read as the start of that day in the server time zone, or as its last
	 * millisecond when it is an upper bound.
	 *
	 * @param paramName the name of the parameter holding an ISO 8601 date
	 * @param isUpperBound whether the date closes a range
	 * @return the parsed date, or null when the parameter is missing or blank
	 * @throws org.springframework.extensions.webscripts.WebScriptException if the parameter is not an ISO 8601 date
	 */
	private Date parseDate(WebScriptRequest req, String paramName, boolean isUpperBound) {
		String value = req.getParameter(paramName);
		if ((value == null) || value.isBlank()) {
			return null;
		}
		try {
			String isoDate = value.trim().replace(' ', '+');
			Date date = ISO8601DateFormat.parse(isoDate);
			if (isUpperBound && !ISO8601DateFormat.isTimeComponentDefined(isoDate)) {
				return Date.from(date.toInstant().plus(1, ChronoUnit.DAYS).minusMillis(1));
			}
			return date;
		} catch (AlfrescoRuntimeException | IndexOutOfBoundsException e) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "'" + paramName + "' is not an ISO 8601 date: " + value, e);
		}
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

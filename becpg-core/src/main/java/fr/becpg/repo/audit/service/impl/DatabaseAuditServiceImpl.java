package fr.becpg.repo.audit.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.regex.Pattern;

import org.alfresco.repo.audit.AuditComponent;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.resource.parameters.SortColumn;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
import org.alfresco.service.transaction.TransactionService;
import org.alfresco.util.ISO8601DateFormat;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.helper.StopWatchSupport;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditPage;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.plugin.ExtraQueryDatabaseAuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditService;

/**
 * <p>DatabaseAuditServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DatabaseAuditServiceImpl implements DatabaseAuditService {
	
	/** Constant <code>BECPG_AUDIT_PATH="/becpg/audit"</code> */
	private static final String BECPG_AUDIT_PATH = "/becpg/audit";

	/** Constant <code>VALUE_SUFFIX="/value"</code> */
	private static final String VALUE_SUFFIX = "/value";

	/** Constant <code>UNSUPPORTED_FILTER_VALUE_CHARS</code> */
	private static final Pattern UNSUPPORTED_FILTER_VALUE_CHARS = Pattern.compile("['\\\\\\p{Cntrl}]");

	/** Constant <code>ID_BOUND_MIN="min"</code> */
	private static final String ID_BOUND_MIN = "min";

	/** Constant <code>ID_BOUND_MAX="max"</code> */
	private static final String ID_BOUND_MAX = "max";

	/** Constant <code>INITIAL_WINDOW_FACTOR=4</code> */
	private static final int INITIAL_WINDOW_FACTOR = 4;

	/** Constant <code>WINDOW_GROWTH_FACTOR=4</code> */
	private static final int WINDOW_GROWTH_FACTOR = 4;

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(DatabaseAuditServiceImpl.class);

	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;
	
	@Autowired
	private AuditComponent auditComponent;

	@Autowired
	private TransactionService transactionService;

	private int maxScannedWindows;
	
	/**
	 * <p>Setter for the field <code>maxScannedWindows</code>.</p>
	 *
	 * @param maxScannedWindows a int
	 */
	@Value("${becpg.audit.maxScannedWindows}")
	public void setMaxScannedWindows(int maxScannedWindows) {
		this.maxScannedWindows = maxScannedWindows;
	}

	/**
	 * <p>Getter for the field <code>maxScannedWindows</code>.</p>
	 *
	 * @return a int
	 */
	public int getMaxScannedWindows() {
		return maxScannedWindows;
	}

	/**
	 * {@inheritDoc}
	 *
	 * An entry recorded on start is written in its own transaction: the trace of an operation that
	 * may never complete cannot depend on the outcome of that very operation, and replacing it
	 * requires a writable transaction that the read only callers do not provide.
	 */
	@Override
	public int recordAuditEntry(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry) {
		if (auditPlugin.isRecordOnStart()) {
			RetryingTransactionHelper transactionHelper = transactionService.getRetryingTransactionHelper();
			return transactionHelper.doInTransaction(() -> internalRecordAuditEntry(auditPlugin, auditValues, deleteOldEntry), false, true);
		}

		return internalRecordAuditEntry(auditPlugin, auditValues, deleteOldEntry);
	}

	private int internalRecordAuditEntry(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry) {
		return StopWatchSupport.build().logger(logger).run(() -> {
			auditPlugin.beforeRecordAuditEntry(auditValues);
			try {
				AuditEntry entryToDelete = null;
				int id = Integer.parseInt(auditValues.get(AuditPlugin.ID).toString());
				if (deleteOldEntry) {
					AuditQuery auditFilter = AuditQuery.createQuery().filter(AuditPlugin.ID, String.valueOf(id)).maxResults(1);
					Collection<AuditEntry> entries = internalListAuditEntries(auditPlugin, auditFilter);
					if (!entries.isEmpty()) {
						entryToDelete = entries.iterator().next();
					}
				}
				auditComponent.recordAuditValues(BECPG_AUDIT_PATH, recreateAuditMap(auditPlugin, auditValues, false));
				StopWatchSupport.addCheckpoint("recordAuditValues");
				if (entryToDelete != null) {
					auditComponent.deleteAuditEntries(Arrays.asList(entryToDelete.getId()));
					StopWatchSupport.addCheckpoint("deleteAuditEntries");
				}
				return id;
			} finally {
				auditPlugin.afterRecordAuditEntry(auditValues);
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public List<JSONObject> listAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditQuery) {
		Collection<AuditEntry> auditEntries = internalListAuditEntries(plugin, auditQuery);
		if (plugin instanceof ExtraQueryDatabaseAuditPlugin extraQueryPlugin) {
			AuditQuery extraAuditQuery = extraQueryPlugin.extraQuery(auditQuery);
			if (extraAuditQuery != null) {
				auditEntries.addAll(internalListAuditEntries(plugin, extraAuditQuery));
			}
		}
		return toStatistics(plugin, auditQuery, auditEntries);
	}

	/**
	 * {@inheritDoc}
	 *
	 * The audit query of Alfresco carries no 'limit': whatever the requested page size, the
	 * database produces every entry of the application and the driver buffers them all. The page
	 * is therefore read by keyset paging, one window of entry identifiers after the other, which
	 * bounds each query on the index leading with the identifier and keeps the database order.
	 */
	@Override
	public AuditPage listAuditPage(DatabaseAuditPlugin plugin, AuditQuery auditQuery) {
		return AuthenticationUtil.runAsSystem(() -> scanAuditPage(plugin, auditQuery));
	}

	private AuditPage scanAuditPage(DatabaseAuditPlugin plugin, AuditQuery auditQuery) {
		IdRange range = scanRange(plugin, auditQuery);

		if (range.isEmpty()) {
			return AuditPage.empty();
		}

		ScanResult scan = scanWindows(plugin, auditQuery, range);
		List<AuditEntry> page = firstEntries(scan.collected(), auditQuery.getMaxResults());

		return new AuditPage(toStatistics(plugin, auditQuery, page), resumeId(scan, auditQuery, page.size()),
				isScanInterrupted(scan, auditQuery, page.size()));
	}

	/**
	 * Walk through the identifier range, window by window, until the page is filled, the range is
	 * exhausted or the window budget is spent.
	 *
	 * Windows grow geometrically: an application whose entries are sparse in the identifier space,
	 * or a filter matching few entries, is reached in a logarithmic number of queries.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @param range the identifiers left to read
	 * @return what the scan gathered and where it stopped
	 */
	private ScanResult scanWindows(DatabaseAuditPlugin plugin, AuditQuery auditQuery, IdRange range) {
		List<String> filters = scanFilters(plugin, auditQuery);
		List<AuditEntry> collected = new ArrayList<>();
		long windowSize = (long) auditQuery.getMaxResults() * INITIAL_WINDOW_FACTOR;
		IdRange remaining = range;

		for (int window = 0; (window < maxScannedWindows) && !remaining.isEmpty()
				&& (collected.size() < auditQuery.getMaxResults()); window++) {
			IdRange scanned = nextWindow(remaining, windowSize, auditQuery.isDbAscending());
			for (String filter : filters) {
				collected.addAll(queryAuditEntries(plugin, windowQuery(auditQuery, filter, scanned)));
			}
			remaining = shrink(remaining, scanned, auditQuery.isDbAscending());
			windowSize *= WINDOW_GROWTH_FACTOR;
		}

		if (filters.size() > 1) {
			collected.sort(byEntryId(auditQuery.isDbAscending()));
		}
		return new ScanResult(collected, remaining);
	}

	/**
	 * The filters a scan queries the database with: the one of the request, plus the one an extra
	 * query plugin derives from it.
	 *
	 * A plugin deriving an extra query mutates the query it is handed, hence the copy: the request
	 * has to keep its own filter for the windows still to come.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link java.util.List} object
	 */
	private List<String> scanFilters(DatabaseAuditPlugin plugin, AuditQuery auditQuery) {
		List<String> filters = new ArrayList<>();
		filters.add(auditQuery.getFilter());

		if (plugin instanceof ExtraQueryDatabaseAuditPlugin extraQueryPlugin) {
			AuditQuery extraAuditQuery = extraQueryPlugin.extraQuery(auditQuery.copy());
			if (extraAuditQuery != null) {
				filters.add(extraAuditQuery.getFilter());
			}
		}
		return filters;
	}

	/**
	 * The query of one window: the filter it reads with, the identifiers it is bounded to, and one
	 * entry more than the page holds.
	 *
	 * That extra entry is what tells a window the scan drained from a window it had to cut short:
	 * without it, a window holding more matches than the page could be taken for the last one and
	 * the entries it still held would be skipped by the resumed scan.
	 *
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @param filter the filter the window is read with
	 * @param window the identifiers the window covers
	 * @return a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 */
	private AuditQuery windowQuery(AuditQuery auditQuery, String filter, IdRange window) {
		return auditQuery.copy().filter(filter).maxResults(auditQuery.getMaxResults() + 1).idRange(window.lowId(), window.highId());
	}

	/**
	 * The range of entry identifiers a scan walks through: the one of the audit application,
	 * narrowed to what a resumed scan has left to read.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a closed range of entry identifiers
	 */
	private IdRange scanRange(DatabaseAuditPlugin plugin, AuditQuery auditQuery) {
		IdRange applicationRange = applicationIdRange(plugin);
		Long startAfterId = auditQuery.getStartAfterId();

		if (applicationRange.isEmpty() || (startAfterId == null)) {
			return applicationRange;
		}
		if (auditQuery.isDbAscending()) {
			return new IdRange(Math.max(applicationRange.lowId(), startAfterId + 1), applicationRange.highId());
		}
		return new IdRange(applicationRange.lowId(), Math.min(applicationRange.highId(), startAfterId - 1));
	}

	/**
	 * The identifiers of the first and the last entry of the audit application.
	 *
	 * The database resolves both from the index without reading a single row, so a scan can afford
	 * to read them on every request.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @return a closed range of entry identifiers
	 */
	private IdRange applicationIdRange(DatabaseAuditPlugin plugin) {
		Map<String, Long> bounds = auditComponent.getAuditMinMaxByApp(plugin.getAuditApplicationId(),
				Arrays.asList(ID_BOUND_MIN, ID_BOUND_MAX));
		Long minId = bounds.get(ID_BOUND_MIN);
		Long maxId = bounds.get(ID_BOUND_MAX);

		if ((minId == null) || (maxId == null)) {
			return IdRange.EMPTY;
		}
		return new IdRange(minId, maxId);
	}

	private IdRange nextWindow(IdRange remaining, long windowSize, boolean ascending) {
		if (ascending) {
			return new IdRange(remaining.lowId(), Math.min(remaining.highId(), (remaining.lowId() + windowSize) - 1));
		}
		return new IdRange(Math.max(remaining.lowId(), (remaining.highId() - windowSize) + 1), remaining.highId());
	}

	private IdRange shrink(IdRange remaining, IdRange scanned, boolean ascending) {
		if (ascending) {
			return new IdRange(scanned.highId() + 1, remaining.highId());
		}
		return new IdRange(remaining.lowId(), scanned.lowId() - 1);
	}

	private Comparator<AuditEntry> byEntryId(boolean ascending) {
		Comparator<AuditEntry> byId = Comparator.comparing(AuditEntry::getId);
		return ascending ? byId : byId.reversed();
	}

	private List<AuditEntry> firstEntries(List<AuditEntry> entries, int maxResults) {
		return entries.size() > maxResults ? entries.subList(0, maxResults) : entries;
	}

	/**
	 * The identifier the next page starts after: the last entry of this page when the scan left
	 * matching entries behind it, the last identifier it read when it stopped earlier, and none
	 * when it reached the end of the audit application.
	 *
	 * @param scan the outcome of the scan
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @param pageSize the number of entries the page holds
	 * @return a {@link java.lang.Long} object
	 */
	private Long resumeId(ScanResult scan, AuditQuery auditQuery, int pageSize) {
		if ((pageSize > 0) && (scan.collected().size() > pageSize)) {
			return scan.collected().get(pageSize - 1).getId();
		}
		if (scan.remaining().isEmpty()) {
			return null;
		}
		return auditQuery.isDbAscending() ? scan.remaining().lowId() - 1 : scan.remaining().highId() + 1;
	}

	/**
	 * True when the scan spent its window budget without filling the page: the caller has to
	 * resume it to know whether further entries match.
	 *
	 * @param scan the outcome of the scan
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @param pageSize the number of entries the page holds
	 * @return a boolean
	 */
	private boolean isScanInterrupted(ScanResult scan, AuditQuery auditQuery, int pageSize) {
		return (pageSize < auditQuery.getMaxResults()) && !scan.remaining().isEmpty();
	}

	/**
	 * Turn audit entries into the statistics the callers expose, ordered on the requested column.
	 *
	 * The order is applied in memory, hence on the read entries only: the audit values a business
	 * column holds live in the property tables, which the audit query cannot sort on.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditQuery a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @param auditEntries a {@link java.util.Collection} object
	 * @return a {@link java.util.List} object
	 */
	private List<JSONObject> toStatistics(DatabaseAuditPlugin plugin, AuditQuery auditQuery, Collection<AuditEntry> auditEntries) {
		List<JSONObject> statistics = new ArrayList<>(auditEntries.size());

		for (AuditEntry auditEntry : auditEntries) {
			statistics.add(toStatItem(plugin, auditEntry));
		}

		sortStatistics(plugin, auditQuery, statistics);
		return statistics;
	}

	private JSONObject toStatItem(DatabaseAuditPlugin plugin, AuditEntry auditEntry) {
		JSONObject statItem = new JSONObject();
		statItem.put(AuditPlugin.ID, auditEntry.getId());

		for (String auditKey : plugin.getKeyMap().keySet()) {
			String key = "/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + auditKey + VALUE_SUFFIX;
			if (auditEntry.getValues().containsKey(key)) {
				statItem.put(auditKey, auditEntry.getValues().get(key));
			}
		}
		return statItem;
	}

	private void sortStatistics(DatabaseAuditPlugin plugin, AuditQuery auditQuery, List<JSONObject> statistics) {
		String sortBy = auditQuery.getSortBy();

		if ((sortBy == null) || sortBy.isBlank()) {
			return;
		}
		if (AuditDataType.DATE.equals(plugin.getKeyMap().get(sortBy))) {
			sortStatisticsByDate(statistics, sortBy, auditQuery.isAscending());
			return;
		}
		Collections.sort(statistics, new StatisticsComparator(plugin.getKeyMap(), sortBy, auditQuery.isAscending()));
	}

	private void sortStatisticsByDate(List<JSONObject> statistics, String sortBy, boolean ascending) {
		Map<JSONObject, Date> parsedDates = HashMap.newHashMap(statistics.size());

		for (JSONObject item : statistics) {
			if (item.has(sortBy)) {
				Object raw = item.get(sortBy);
				parsedDates.put(item, raw instanceof Date d ? d : ISO8601DateFormat.parse(raw.toString()));
			}
		}

		int factor = ascending ? 1 : -1;
		statistics.sort((o1, o2) -> {
			Date d1 = parsedDates.get(o1);
			Date d2 = parsedDates.get(o2);
			if (d1 == null && d2 == null) {
				return 0;
			}
			if (d1 == null) {
				return -factor;
			}
			if (d2 == null) {
				return factor;
			}
			return factor * d1.compareTo(d2);
		});
	}

	/** {@inheritDoc} */
	@Override
	public void completeAuditEntry(DatabaseAuditPlugin plugin, String filterKey, String filterValue) {
		AuditQuery auditQuery = AuditQuery.createQuery().filter(filterKey, filterValue).dbAsc(false).maxResults(1);

		List<AuditEntry> entries = internalListAuditEntries(plugin, auditQuery);

		if (entries.isEmpty()) {
			if (logger.isDebugEnabled()) {
				logger.debug("No audit entry to complete for '" + filterKey + "=" + filterValue + "'");
			}
			return;
		}

		Map<String, Serializable> auditValues = extractAuditValues(plugin, entries.get(0));

		if ((auditValues.get(AuditPlugin.ID) == null) || (auditValues.get(AuditPlugin.STARTED_AT) == null)) {
			logger.warn("Cannot complete the audit entry of '" + filterKey + "=" + filterValue + "': it carries no id or no start date");
			return;
		}

		Date end = new Date();
		Date start = ISO8601DateFormat.parse(auditValues.get(AuditPlugin.STARTED_AT).toString());

		auditValues.put(AuditPlugin.COMPLETED_AT, ISO8601DateFormat.format(end));
		auditValues.put(AuditPlugin.DURATION, end.getTime() - start.getTime());
		auditValues.put(AuditPlugin.IS_COMPLETED, true);

		recordAuditEntry(plugin, auditValues, true);
	}

	/**
	 * Read back the values of an audit entry, keyed by audit key.
	 *
	 * The identifier is read as an integer: it is the key the entry is replaced on, and it is
	 * recorded as an integer by {@link fr.becpg.repo.audit.service.DatabaseAuditScope}.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditEntry a {@link org.alfresco.rest.api.model.AuditEntry} object
	 * @return a {@link java.util.Map} object
	 */
	private Map<String, Serializable> extractAuditValues(DatabaseAuditPlugin plugin, AuditEntry auditEntry) {
		String prefix = "/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/";
		Map<String, Serializable> auditValues = new HashMap<>();

		for (Entry<String, Serializable> value : auditEntry.getValues().entrySet()) {
			if (value.getKey().startsWith(prefix) && value.getKey().endsWith(VALUE_SUFFIX)) {
				auditValues.put(value.getKey().substring(prefix.length(), value.getKey().length() - VALUE_SUFFIX.length()), value.getValue());
			}
		}

		Serializable id = auditValues.get(AuditPlugin.ID);
		if (id != null) {
			auditValues.put(AuditPlugin.ID, Integer.valueOf(id.toString()));
		}

		return auditValues;
	}

	/** {@inheritDoc} */
	@Override
	public void deleteAuditEntries(DatabaseAuditPlugin plugin, Long fromId, Long toId) {
		auditComponent.deleteAuditEntriesByIdRange(plugin.getAuditApplicationId(), fromId, toId);
	}

	/**
	 * List the audit entries matching the given filter.
	 *
	 * Read as system: the Alfresco audit service is reserved to the administrators, whereas the
	 * entries are read back on behalf of the user having requested the audited operation.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditFilter a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link java.util.List} object
	 */
	private List<AuditEntry> internalListAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
		return AuthenticationUtil.runAsSystem(() -> queryAuditEntries(plugin, auditFilter));
	}

	private List<AuditEntry> queryAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
		String whereClause = buildWhereClause(plugin, auditFilter);
		Query query = buildQuery(whereClause);
		Paging paging = Paging.valueOf(Paging.DEFAULT_SKIP_COUNT, auditFilter.getMaxResults());
		String[] trueArray = { "true" };
		RecognizedParams recognizedParams = new RecognizedParams(Map.of("omitTotalItems", trueArray), paging, null, null, Arrays.asList("values"),
				null, query, List.of(new SortColumn("createdAt", auditFilter.isDbAscending())), false);
		Parameters params = Params.valueOf(recognizedParams, plugin.getAuditApplicationId(), null, null);
		return new ArrayList<>(audit.listAuditEntries(plugin.getAuditApplicationId(), params).getCollection().stream().filter(Objects::nonNull).toList());
	}

	/**
	 * <p>buildWhereClause.</p>
	 *
	 * The identifier range is written as an inclusive 'between': the walker of Alfresco raises the
	 * upper bound by one to turn it into the exclusive bound the query needs.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditFilter a {@link fr.becpg.repo.audit.model.AuditQuery} object
	 * @return a {@link java.lang.String} object
	 */
	private String buildWhereClause(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
		List<String> statements = new ArrayList<>();

		if (auditFilter.getFilter() != null) {
			statements.add(buildFilterStatement(plugin, auditFilter.getFilter()));
		}
		if ((auditFilter.getFromTime() != null) && (auditFilter.getToTime() != null)) {
			statements.add("createdAt BETWEEN ('" + ISO8601DateFormat.format(auditFilter.getFromTime()) + "' , '"
					+ ISO8601DateFormat.format(auditFilter.getToTime()) + "')");
		}
		if ((auditFilter.getFromId() != null) && (auditFilter.getToId() != null)) {
			statements.add("id BETWEEN ('" + auditFilter.getFromId() + "' , '" + auditFilter.getToId() + "')");
		}
		if (statements.isEmpty()) {
			return "";
		}
		return "(" + String.join(" and ", statements) + ")";
	}

	/**
	 * <p>buildFilterStatement.</p>
	 *
	 * Builds the 'valuesKey'/'valuesValue' predicate of the audit 'where' clause. A value carrying a
	 * character that the clause cannot represent is rejected rather than escaped: the 'where' grammar
	 * does accept escape sequences, but the query is read back with QueryHelper.stripQuotes, which only
	 * removes the surrounding quotes. An escaped value would therefore be searched with its backslashes
	 * and silently match nothing.
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param filter a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 * @throws fr.becpg.repo.audit.exception.BeCPGAuditException if the filter syntax, key or value is not supported
	 */
	private String buildFilterStatement(DatabaseAuditPlugin plugin, String filter) {
		String[] splitted = filter.split("=", 2);
		if (splitted.length < 2) {
			throw new BeCPGAuditException("statistics filter '" + filter + "' has wrong syntax");
		}
		String valuesKey = splitted[0].trim();
		String valuesValue = splitted[1].trim();
		if (!AuditPlugin.ID.equals(valuesKey) && !plugin.getKeyMap().containsKey(valuesKey)) {
			throw new BeCPGAuditException("Unknown audit filter key: " + valuesKey);
		}
		if (UNSUPPORTED_FILTER_VALUE_CHARS.matcher(valuesValue).find()) {
			throw new BeCPGAuditException("Audit filter value of key '" + valuesKey + "' contains unsupported characters");
		}
		return "valuesKey='/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + valuesKey + VALUE_SUFFIX
				+ "' and valuesValue='" + valuesValue + "'";
	}

	/**
	 * <p>buildQuery.</p>
	 *
	 * @param whereClause a {@link java.lang.String} object
	 * @return a {@link org.alfresco.rest.framework.resource.parameters.where.Query} object
	 */
	private Query buildQuery(String whereClause) {
		
		if (whereClause != null && !whereClause.isBlank()) {
			try {
				CommonTree whereTree = WhereCompiler.compileWhereClause(whereClause);
				return new QueryImpl(whereTree);
			} catch (RecognitionException e) {
				throw new BeCPGAuditException("Could not compile audit 'where' query : " + e.getMessage());
			}
		}
		
		return null;
	}

	/**
	 * <p>recreateAuditMap.</p>
	 *
	 * @param plugin a {@link fr.becpg.repo.audit.plugin.DatabaseAuditPlugin} object
	 * @param auditValues a {@link java.util.Map} object
	 * @param forDatabase a boolean
	 * @return a {@link java.util.Map} object
	 */
	private Map<String, Serializable> recreateAuditMap(DatabaseAuditPlugin plugin, Map<String, Serializable> auditValues, boolean forDatabase) {
		Map<String, Serializable> auditMap = new HashMap<>();
		for (Entry<String, Serializable> entry : auditValues.entrySet()) {
			if (forDatabase) {
				auditMap.put("/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + entry.getKey() + VALUE_SUFFIX,
						entry.getValue());
			} else {
				auditMap.put(plugin.getAuditApplicationPath() + "/" + entry.getKey(), entry.getValue());
			}
		}
		return auditMap;
	}

	/**
	 * A closed range of audit entry identifiers.
	 */
	private record IdRange(long lowId, long highId) {

		private static final IdRange EMPTY = new IdRange(1, 0);

		private boolean isEmpty() {
			return lowId > highId;
		}
	}

	/**
	 * What a scan gathered, and the identifiers it has not read yet.
	 */
	private record ScanResult(List<AuditEntry> collected, IdRange remaining) {
	}

	private class StatisticsComparator implements Comparator<JSONObject> {
		
		private String comparisonFieldName;
		private Map<String, AuditDataType> statisticsMap;
		private int factor;
		
		public StatisticsComparator(Map<String, AuditDataType> statisticsMap, String comparisonFieldName, boolean ascendingOrder) {
			this.statisticsMap = statisticsMap;
			this.comparisonFieldName = comparisonFieldName;
			this.factor = ascendingOrder ? 1 : -1;
		}
		
		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public int compare(JSONObject o1, JSONObject o2) {
			try {
				Comparable field1 = o1.has(comparisonFieldName) ? (Comparable) o1.get(comparisonFieldName) : null;
				Comparable field2 = o2.has(comparisonFieldName) ? (Comparable) o2.get(comparisonFieldName) : null;
				if (field1 == null && field2 == null) {
					return 0;
				}
				if (field1 != null) {
					if (field2 == null) {
						return factor;
					}
					if (AuditDataType.INTEGER.equals(statisticsMap.get(comparisonFieldName))) {
						Integer int1 = Integer.parseInt(field1.toString());
						Integer int2 = Integer.parseInt(field2.toString());
						return factor * int1.compareTo(int2);
					} else if (AuditDataType.DATE.equals(statisticsMap.get(comparisonFieldName))) {
						Date date1 = field1 instanceof Date date ? date : ISO8601DateFormat.parse(field1.toString());
						Date date2 = field2 instanceof Date date ? date : ISO8601DateFormat.parse(field2.toString());
						return factor * date1.compareTo(date2);
					}
					return factor * field1.compareTo(field2);
				}
				return -factor;
			} catch (Exception e) {
				throw new BeCPGAuditException("Error while comparing fields : " + e.getMessage());
			}
		}
	}
}

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

import org.alfresco.repo.audit.AuditComponent;
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
import org.alfresco.util.ISO8601DateFormat;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.helper.StopWatchSupport;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.ExtraQueryDatabaseAuditPlugin;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditService;

/**
 * <p>DatabaseAuditServiceImpl class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
@Service
public class DatabaseAuditServiceImpl implements DatabaseAuditService {
	
	private static final String BECPG_AUDIT_PATH = "/becpg/audit";

	private static final Log logger = LogFactory.getLog(DatabaseAuditServiceImpl.class);

	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;
	
	@Autowired
	private AuditComponent auditComponent;
	
	/** {@inheritDoc} */
	@Override
	public int recordAuditEntry(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry) {
		return StopWatchSupport.build().logger(logger).run(() -> {
			auditPlugin.beforeRecordAuditEntry(auditValues);
			
			try {
				
				AuditEntry entryToDelete = null;
				
				int id = (int) auditValues.get(AuditPlugin.ID);
				
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
		
		if (plugin instanceof ExtraQueryDatabaseAuditPlugin) {
			AuditQuery extraAuditQuery = ((ExtraQueryDatabaseAuditPlugin) plugin).extraQuery(auditQuery);
			if (extraAuditQuery != null) {
				auditEntries.addAll(internalListAuditEntries(plugin, extraAuditQuery));
			}
		}
		
		List<JSONObject> statistics = new ArrayList<>();
		
		for (AuditEntry auditEntry : auditEntries) {
			
			JSONObject statItem = new JSONObject();
			
			statItem.put(AuditPlugin.ID, auditEntry.getId());
			
			for (String auditKey : plugin.getKeyMap().keySet()) {
				String key = "/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + auditKey + "/value";
				if (auditEntry.getValues().containsKey(key)) {
					statItem.put(auditKey, auditEntry.getValues().get(key));
				}
			}
			
			statistics.add(statItem);
			
		}
		
		if (auditQuery.getSortBy() != null && !auditQuery.getSortBy().isBlank()) {
			Collections.sort(statistics, new StatisticsComparator(plugin.getKeyMap(), auditQuery.getSortBy(), auditQuery.isAscending()));
		}
		
		return statistics;
		
	}

	/** {@inheritDoc} */
	@Override
	public void deleteAuditEntries(DatabaseAuditPlugin plugin, Long fromId, Long toId) {
		auditComponent.deleteAuditEntriesByIdRange(plugin.getAuditApplicationId(), fromId, toId);
	}

	private Collection<AuditEntry> internalListAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
		
		String whereClause = buildWhereClause(plugin, auditFilter);
		
		Query query = buildQuery(whereClause);
		
		Paging paging = Paging.valueOf(Paging.DEFAULT_SKIP_COUNT, auditFilter.getMaxResults());
		
		String[] trueArray = { "true" };
		
		RecognizedParams recognizedParams = new RecognizedParams(Map.of("omitTotalItems", trueArray), paging, null, null, Arrays.asList("values"),
				null, query, List.of(new SortColumn("createdAt", auditFilter.isDbAscending())), false);
		
		Parameters params = Params.valueOf(recognizedParams, plugin.getAuditApplicationId(), null, null);
	
		return audit.listAuditEntries(plugin.getAuditApplicationId(), params).getCollection();
	}

	private String buildWhereClause(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
	
		StringBuilder whereClauseBuilder = new StringBuilder();
		
		List<String> statements = new ArrayList<>();
		
		if (auditFilter.getFilter() != null) {
			
			String[] splitted = auditFilter.getFilter().split("=");
			
			if (splitted.length < 2) {
				throw new BeCPGAuditException("statistics filter '" + auditFilter.getFilter() + "' has wrong syntax");
			}
			
			String valuesKey = splitted[0];
			String valuesValue = splitted[1];
			
			statements.add("valuesKey='" + "/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + valuesKey + "/value' and valuesValue='" + valuesValue + "'");
			
		}
		
		if (auditFilter.getFromTime() != null && auditFilter.getToTime() != null) {
			
			statements.add("createdAt BETWEEN ('" + ISO8601DateFormat.format(auditFilter.getFromTime()) + "' , '" + ISO8601DateFormat.format(auditFilter.getToTime()) + "')");
			
			if (!whereClauseBuilder.toString().isBlank()) {
				whereClauseBuilder.append(" and ");
			}
			
		}
		
		if (!statements.isEmpty()) {
			whereClauseBuilder.append("(");
			
			boolean isFirst = true;
			
			for (String statement : statements) {
				if (!isFirst) {
					whereClauseBuilder.append(" and ");
				}
				
				whereClauseBuilder.append(statement);
				
				isFirst = false;
			}
			
			whereClauseBuilder.append(")");
		}
		
		return whereClauseBuilder.toString();
	}

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

	private Map<String, Serializable> recreateAuditMap(DatabaseAuditPlugin plugin, Map<String, Serializable> auditValues, boolean forDatabase) {
		
		Map<String, Serializable> auditMap = new HashMap<>();
		
		for (Entry<String, Serializable> entry : auditValues.entrySet()) {

			if (forDatabase) {
				auditMap.put("/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + entry.getKey() + "/value", entry.getValue());
			} else {
				auditMap.put(plugin.getAuditApplicationPath() + "/" + entry.getKey(), entry.getValue());
			}
		
		}
		
		return auditMap;
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
						Date date1 = field1 instanceof Date ? (Date) field1 : ISO8601DateFormat.parse(field1.toString());
						Date date2 = field2 instanceof Date ? (Date) field2 : ISO8601DateFormat.parse(field2.toString());
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

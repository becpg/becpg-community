package fr.becpg.repo.audit.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditFilter;
import fr.becpg.repo.audit.plugin.AuditPlugin;
import fr.becpg.repo.audit.service.DatabaseAuditService;

@Service
public class DatabaseAuditServiceImpl implements DatabaseAuditService {
	
	private static final String BECPG_AUDIT_PATH = "/becpg/audit";

	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;
	
	@Autowired
	private AuditComponent auditComponent;

	@Override
	public int recordAuditEntry(AuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry) {
		
		auditPlugin.beforeRecordAuditEntry(auditValues);
		
		try {
			
			AuditEntry entryToDelete = null;
			
			int id = (int) auditValues.get("id");

			if (deleteOldEntry) {
				
				Collection<AuditEntry> entries = listAuditEntries(auditPlugin, 10, "id=" + id);
				
				if (!entries.isEmpty()) {
					entryToDelete = entries.iterator().next();
				}
			}
			
			auditComponent.recordAuditValues(BECPG_AUDIT_PATH, recreateAuditMap(auditPlugin, auditValues));
			
			if (entryToDelete != null) {
				auditComponent.deleteAuditEntries(Arrays.asList(entryToDelete.getId()));
			}
			
			return id;
			
		} finally {
			auditPlugin.afterRecordAuditEntry(auditValues);
		}
		
	}

	@Override
	public List<JSONObject> getAuditStatistics(AuditPlugin plugin, AuditFilter auditFilter) {
		
		Collection<AuditEntry> auditEntries = listAuditEntries(plugin, auditFilter.getMaxResults(), auditFilter.getFilter());
		
		List<JSONObject> statistics = new ArrayList<>();
		
		for (AuditEntry auditEntry : auditEntries) {
			
			JSONObject statItem = new JSONObject();
			
			for (String auditKey : plugin.getStatisticsKeyMap().keySet()) {
				String key = "/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + auditKey + "/value";
				if (auditEntry.getValues().containsKey(key)) {
					statItem.put(auditKey, auditEntry.getValues().get(key));
				}
			}
			
			statistics.add(statItem);
			
		}
		
		if (auditFilter.getSortBy() != null && !auditFilter.getSortBy().isBlank()) {
			Collections.sort(statistics, new StatisticsComparator(plugin.getStatisticsKeyMap(), auditFilter.getSortBy(), auditFilter.isAscendingOrder()));
		}
		
		return statistics;
		
	}

	@Override
	public void deleteAuditStatistics(AuditPlugin plugin, Long fromId, Long toId) {
		auditComponent.deleteAuditEntriesByIdRange(plugin.getAuditApplicationId(), fromId, toId);
	}

	private Collection<AuditEntry> listAuditEntries(AuditPlugin plugin, int maxResults, String filter) {
		
		String whereClause = buildWhereClause(plugin, filter);
		
		Query query = buildQuery(whereClause);
		
		Paging paging = Paging.valueOf(Paging.DEFAULT_SKIP_COUNT, maxResults);
		
		RecognizedParams recognizedParams = new RecognizedParams(null, paging, null, null, Arrays.asList("values"),
				null, query, null, false);
		
		Parameters params = Params.valueOf(recognizedParams, plugin.getAuditApplicationId(), null, null);
	
		return audit.listAuditEntries(plugin.getAuditApplicationId(), params).getCollection();
	}

	private String buildWhereClause(AuditPlugin plugin, String filter) {
	
		if (filter != null) {
			
			String[] splitted = filter.split("=");
			
			if (splitted.length < 2) {
				throw new BeCPGAuditException("statistics filter '" + filter + "' has wrong syntax");
			}
			
			String valuesKey = splitted[0];
			String valuesValue = splitted[1];
			
			return String.format(getWhereClauseFormat(plugin), valuesKey, valuesValue);
			
		}
		
		return null;
	}

	private Query buildQuery(String whereClause) {
		
		if (whereClause != null) {
			try {
				CommonTree whereTree = WhereCompiler.compileWhereClause(whereClause);
				return new QueryImpl(whereTree);
			} catch (RecognitionException e) {
				throw new BeCPGAuditException("Could not compile audit 'where' query : " + e.getMessage());
			}
		}
		
		return null;
	}

	private String getWhereClauseFormat(AuditPlugin plugin) {
		return "(valuesKey='" + "/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/%s/value' and valuesValue='%s')";
	}
	
	private Map<String, Serializable> recreateAuditMap(AuditPlugin plugin, Map<String, Serializable> auditValues) {
		
		Map<String, Serializable> auditMap = new HashMap<>();
		
		for (Entry<String, Serializable> entry : auditValues.entrySet()) {
			auditMap.put(plugin.getAuditApplicationPath() + "/" + entry.getKey(), entry.getValue());
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

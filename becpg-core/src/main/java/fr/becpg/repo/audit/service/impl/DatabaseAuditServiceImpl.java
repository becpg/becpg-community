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
import org.alfresco.repo.audit.model.AuditApplication;
import org.alfresco.repo.audit.model.AuditModelRegistry;
import org.alfresco.repo.domain.audit.AuditDAO;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.rest.api.Audit;
import org.alfresco.rest.api.model.AuditEntry;
import org.alfresco.rest.framework.resource.parameters.Paging;
import org.alfresco.rest.framework.resource.parameters.Parameters;
import org.alfresco.rest.framework.resource.parameters.Params;
import org.alfresco.rest.framework.resource.parameters.Params.RecognizedParams;
import org.alfresco.rest.framework.resource.parameters.where.Query;
import org.alfresco.rest.framework.resource.parameters.where.QueryImpl;
import org.alfresco.rest.framework.resource.parameters.where.WhereCompiler;
import org.alfresco.util.ISO8601DateFormat;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import fr.becpg.repo.audit.exception.BeCPGAuditException;
import fr.becpg.repo.audit.model.AuditDataType;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.plugin.DatabaseAuditPlugin;
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
	
	@Autowired
	private AuditDAO auditDAO;
	
	@Autowired
	private AuditModelRegistry auditModelRegistry;
	
	@Override
	public int recordAuditEntry(DatabaseAuditPlugin auditPlugin, Map<String, Serializable> auditValues, boolean deleteOldEntry) {
		
		auditPlugin.beforeRecordAuditEntry(auditValues);
		
		try {
			
			AuditEntry entryToDelete = null;
			
			int id = (int) auditValues.get("id");

			if (deleteOldEntry) {
				
				AuditQuery auditFilter = AuditQuery.createQuery().filter("id", String.valueOf(id)).maxResults(1);
				
				Collection<AuditEntry> entries = internalListAuditEntries(auditPlugin, auditFilter);
				
				if (!entries.isEmpty()) {
					entryToDelete = entries.iterator().next();
				}
			}
			
			auditComponent.recordAuditValues(BECPG_AUDIT_PATH, recreateAuditMap(auditPlugin, auditValues, false));
			
			if (entryToDelete != null) {
				auditComponent.deleteAuditEntries(Arrays.asList(entryToDelete.getId()));
			}
			
			return id;
			
		} finally {
			auditPlugin.afterRecordAuditEntry(auditValues);
		}
		
	}

	@Override
	public List<JSONObject> listAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
		
		Collection<AuditEntry> auditEntries = internalListAuditEntries(plugin, auditFilter);
		
		List<JSONObject> statistics = new ArrayList<>();
		
		for (AuditEntry auditEntry : auditEntries) {
			
			JSONObject statItem = new JSONObject();
			
			statItem.put("id", auditEntry.getId());
			
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
	public void deleteAuditEntries(DatabaseAuditPlugin plugin, Long fromId, Long toId) {
		auditComponent.deleteAuditEntriesByIdRange(plugin.getAuditApplicationId(), fromId, toId);
	}

	@Override
	public void updateAuditEntry(DatabaseAuditPlugin plugin, Long id, Long time, Map<String, Serializable> values) {
		
		AuditEntry auditEntry = audit.getAuditEntry(plugin.getAuditApplicationId(), id, null);
		
        AuditApplication application = auditModelRegistry.getAuditApplicationByKey(plugin.getAuditApplicationId());
		
		Long applicationId = application.getApplicationId();
		
		deleteAuditEntries(plugin, auditEntry.getId(), auditEntry.getId() + 1);
		
		for (Entry<String, Serializable> entry : values.entrySet()) {
			auditEntry.getValues().put("/" + plugin.getAuditApplicationId() + "/" + plugin.getAuditApplicationPath() + "/" + entry.getKey() + "/value", entry.getValue());
		}
		
		auditDAO.createAuditEntry(applicationId, time, AuthenticationUtil.getFullyAuthenticatedUser(), auditEntry.getValues());
		
	}

	private Collection<AuditEntry> internalListAuditEntries(DatabaseAuditPlugin plugin, AuditQuery auditFilter) {
		
		String whereClause = buildWhereClause(plugin, auditFilter);
		
		Query query = buildQuery(whereClause);
		
		Paging paging = Paging.valueOf(Paging.DEFAULT_SKIP_COUNT, auditFilter.getMaxResults());
		
		RecognizedParams recognizedParams = new RecognizedParams(null, paging, null, null, Arrays.asList("values"),
				null, query, null, false);
		
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

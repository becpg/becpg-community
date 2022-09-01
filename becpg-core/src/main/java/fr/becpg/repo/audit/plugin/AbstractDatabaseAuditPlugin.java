package fr.becpg.repo.audit.plugin;

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

import fr.becpg.repo.audit.exception.BeCPGAuditException;

public abstract class AbstractDatabaseAuditPlugin implements DatabaseAuditPlugin {
	
	private static final String ID = "id";
	private static final String STARTED_AT = "startedAt";
	private static final String COMPLETED_AT = "completedAt";
	private static final String DURATION = "duration";
	
	private static final String BECPG_AUDIT_PATH = "/becpg/audit";
	
	protected static final Map<String, String> KEY_MAP = new HashMap<>();

	static {
		KEY_MAP.put(STARTED_AT, "date");
		KEY_MAP.put(COMPLETED_AT, "date");
		KEY_MAP.put(DURATION, "int");
	}

	private String whereClauseFormat;

	@Autowired
	private AuditComponent auditComponent;
	
	@Autowired
	@Qualifier("auditApi")
	@Lazy
	private Audit audit;
	
	protected abstract String getAuditApplicationId();

	protected abstract String getAuditApplicationPath();
	
	@Override
	public int recordAuditEntry(Map<String, Serializable> auditValues, boolean open) {
		
		Integer id = null;
		
		AuditEntry entryToDelete = null;

		if (open) {
			
			auditValues.put(STARTED_AT, new Date());
			
			id = Objects.hash(auditValues);
			auditValues.put(ID, id);
		} else {
			
			id = (int) auditValues.get(ID);
			Date end = new Date();
			
			auditValues.put(COMPLETED_AT, end);
			
			Date start = (Date) auditValues.get(STARTED_AT);
			
			auditValues.put(DURATION, end.getTime() - start.getTime());

			Collection<AuditEntry> entries = listAuditEntries(10, "id=" + id);
			
			if (!entries.isEmpty()) {
				entryToDelete = entries.iterator().next();
			}
		}
		
		auditComponent.recordAuditValues(BECPG_AUDIT_PATH, recreateAuditMap(auditValues));
		
		if (entryToDelete != null) {
			auditComponent.deleteAuditEntries(Arrays.asList(entryToDelete.getId()));
		}

		return id;
	}
	
	@Override
	public List<JSONObject> buildAuditStatistics(Integer maxResults, String sortBy, String filter) {
		
		Collection<AuditEntry> auditEntries = listAuditEntries(maxResults, filter);
		
		List<JSONObject> statistics = new ArrayList<>();
		
		for (AuditEntry auditEntry : auditEntries) {
			
			JSONObject statItem = new JSONObject();
			
			for (String auditKey : getStatisticsKeyMap().keySet()) {
				String key = "/" + getAuditApplicationId() + "/" + getAuditApplicationPath() + "/" + auditKey + "/value";
				if (auditEntry.getValues().containsKey(key)) {
					statItem.put(auditKey, auditEntry.getValues().get(key));
				}
			}
			
			statistics.add(statItem);
			
		}
		
		if (sortBy != null && !sortBy.isBlank()) {
			Collections.sort(statistics, new StatisticsComparator(sortBy));
		}
		
		return statistics;
		
	}

	private Map<String, Serializable> recreateAuditMap(Map<String, Serializable> auditValues) {
		
		Map<String, Serializable> auditMap = new HashMap<>();
		
		for (Entry<String, Serializable> entry : auditValues.entrySet()) {
			auditMap.put(getAuditApplicationPath() + "/" + entry.getKey(), entry.getValue());
		}
		
		return auditMap;
	}

	private Map<String, String> getStatisticsKeyMap() {
		return KEY_MAP;
	}

	private Collection<AuditEntry> listAuditEntries(int maxResults, String filter) {
		
		String whereClause = buildWhereClause(filter);
		
		Query query = buildQuery(whereClause);
		
		Paging paging = Paging.valueOf(Paging.DEFAULT_SKIP_COUNT, maxResults);
		
		RecognizedParams recognizedParams = new RecognizedParams(null, paging, null, null, Arrays.asList("values"),
				null, query, null, false);
		
		Parameters params = Params.valueOf(recognizedParams, getAuditApplicationId(), null, null);

		return audit.listAuditEntries(getAuditApplicationId(), params).getCollection();
	}
	
	private String buildWhereClause(String filter) {

		if (filter != null) {
			
			String[] splitted = filter.split("=");
			
			if (splitted.length < 2) {
				throw new BeCPGAuditException("statistics filter '" + filter + "' has wrong syntax");
			}
			
			String valuesKey = splitted[0];
			String valuesValue = splitted[1];
			
			return String.format(getWhereClauseFormat(), valuesKey, valuesValue);
			
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
	
	private String getWhereClauseFormat() {
		if (whereClauseFormat == null) {
			whereClauseFormat = "(valuesKey='" + "/" + getAuditApplicationId() + "/" + getAuditApplicationPath() + "/%s/value' and valuesValue='%s')";
		}
		
		return whereClauseFormat;
	}

	private class StatisticsComparator implements Comparator<JSONObject> {
	
		private String comparisonFieldName;
		
		public StatisticsComparator(String comparisonFieldName) {
			this.comparisonFieldName = comparisonFieldName;
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
						return 1;
					}
					
					if ("int".equals(getStatisticsKeyMap().get(comparisonFieldName))) {
						Integer int1 = Integer.parseInt(field1.toString());
						Integer int2 = Integer.parseInt(field2.toString());
						return int1.compareTo(int2);
					}
					
					return field1.compareTo(field2);
				}
				
				return -1;
				
			} catch (Exception e) {
				throw new BeCPGAuditException("Error while comparing fields : " + e.getMessage());
			}
		}
	}

}

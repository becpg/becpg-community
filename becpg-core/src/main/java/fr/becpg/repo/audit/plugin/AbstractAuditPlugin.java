package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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

import fr.becpg.repo.audit.exception.BeCPGAuditException;

public abstract class AbstractAuditPlugin implements AuditPlugin {
	
	protected static final String BECPG_AUDIT_PATH = "/becpg/audit";
	
	private String whereClauseFormat;

	@Autowired
	protected AuditComponent auditComponent;
	
	@Autowired
	@Qualifier("auditApi")
	protected Audit audit;
	
	@Override
	public void recordAuditEntry(Map<String, Serializable> auditValues) {
		auditComponent.recordAuditValues(BECPG_AUDIT_PATH, auditValues);
	}
	
	@Override
	public List<JSONObject> buildAuditStatistics(Integer maxResults, String sortBy, String filter) {
		
		Collection<AuditEntry> auditEntries = listAuditEntries(maxResults, filter);
		
		List<JSONObject> statistics = new ArrayList<>();
		
		for (AuditEntry auditEntry : auditEntries) {
			
			JSONObject statItem = new JSONObject();
			
			for (String auditKey : getStatisticsKeyMap().keySet()) {
				if (auditEntry.getValues().containsKey(getAuditApplicationPath() + "/" + auditKey + "/value")) {
					statItem.put(auditKey, auditEntry.getValues().get(getAuditApplicationPath() + "/" + auditKey + "/value"));
				}
			}
			
			statistics.add(statItem);
			
		}
		
		if (sortBy != null && !sortBy.isBlank()) {
			Collections.sort(statistics, new StatisticsComparator(sortBy));
		}
		
		return statistics;
		
	}
	
	private class StatisticsComparator implements Comparator<JSONObject> {

		private String comparisonFieldName;
		
		public StatisticsComparator(String comparisonFieldName) {
			this.comparisonFieldName = comparisonFieldName;
		}
		
		@Override
		public int compare(JSONObject o1, JSONObject o2) {
			
			try {
				
				String field1 = o1.getString(comparisonFieldName);
				String field2 = o2.getString(comparisonFieldName);
				
				if (field1 == null && field2 == null) {
					return 0;
				}
				
				if (field1 != null) {
					
					if (field2 == null) {
						return ((Comparable<?>) field1).compareTo(null);
					}
					
					if ("int".equals(getStatisticsKeyMap().get(comparisonFieldName))) {
						Integer int1 = Integer.parseInt(field1);
						Integer int2 = Integer.parseInt(field2);
						return int1.compareTo(int2);
					}
					
					return field1.compareTo(field2);
				}
				
				return field2.compareTo(field1);
				
			} catch (IllegalArgumentException e) {
				throw new BeCPGAuditException("Error while comparing fields : " + e.getMessage());
			}
		}
	}
	
	protected Collection<AuditEntry> listAuditEntries(int maxResults, String filter) {
		
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
			whereClauseFormat = "(valuesKey='" + getAuditApplicationPath() + "/%s/value' and valuesValue='%s')";
		}
		
		return whereClauseFormat;
	}

}

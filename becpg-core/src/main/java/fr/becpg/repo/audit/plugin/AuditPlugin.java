package fr.becpg.repo.audit.plugin;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import fr.becpg.repo.audit.AuditType;

public interface AuditPlugin {

	String getAuditApplicationId();
	
	String getAuditApplicationPath();
	
	boolean applyTo(AuditType type);

	Map<String, String> getStatisticsKeyMap();

	void recordAuditEntry(Map<String, Serializable> auditValues);
	
	List<JSONObject> buildAuditStatistics(Integer maxResults, String sortBy, String filter);

}
